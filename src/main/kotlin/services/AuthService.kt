package com.mod.services

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mailgun.api.v3.MailgunMessagesApi
import com.mailgun.model.message.Message
import com.mod.core.EmailAlreadyExistsException
import com.mod.core.EmailAlreadyVerifiedException
import com.mod.core.EmailIsNotVerifiedException
import com.mod.core.EmailNotExistsException
import com.mod.core.InvalidCredentialsException
import com.mod.core.InvalidVerificationCodeException
import com.mod.core.NoVerificationCodeFoundException
import com.mod.core.VerificationCodeExpiredException
import com.mod.core.generateForgetPasswordVerificationEmailContent
import com.mod.core.generateVerificationCode
import com.mod.core.generateVerificationEmailContent
import com.mod.database.entities.EmailVerificationCodeEntity
import com.mod.database.entities.UserEntity
import com.mod.database.tables.EmailVerificationCodes
import com.mod.database.tables.Users
import io.ktor.server.config.ApplicationConfig
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.Date
import kotlin.time.Duration.Companion.minutes

class AuthService(
    private val mailMessageApi: MailgunMessagesApi,
    private val config: ApplicationConfig
) {
    suspend fun signup(email: String, password: String) {
        val existingUser = newSuspendedTransaction {
            UserEntity.find {
                Users.email eq email
            }.firstOrNull()
        }

        if (existingUser != null) {
            throw EmailAlreadyExistsException(email)
        }

        val code = newSuspendedTransaction {
            val hashedPassword = hashValue(password)
            val createdUser = UserEntity.new {
                this.email = email
                this.password = hashedPassword
                isEmailVerified = false
            }

            val code = generateVerificationCode()
            val hashedCode = hashValue(code.toString())

            EmailVerificationCodeEntity.new {
                user = createdUser
                this.code = hashedCode
                expiresAt = Clock.System.now().plus(5.minutes)
                isUsed = false
            }

            code
        }

        sendVerificationEmail(email, "Verify Your Account", generateVerificationEmailContent(code.toString()))
    }

    suspend fun verifyEmail(email: String, code: String): String {
        val existingUser = newSuspendedTransaction {
            UserEntity.find {
                Users.email eq email
            }.firstOrNull()
        } ?: throw EmailNotExistsException()
        if (existingUser.isEmailVerified) {
            throw EmailAlreadyVerifiedException()
        }

        val verificationCode = newSuspendedTransaction {
            EmailVerificationCodeEntity.find {
                (EmailVerificationCodes.userId eq existingUser.id) and
                        (EmailVerificationCodes.isUsed eq false)
            }.firstOrNull()
        } ?: throw NoVerificationCodeFoundException()

        if (Clock.System.now() > verificationCode.expiresAt) {
            newSuspendedTransaction {
                EmailVerificationCodeEntity.findByIdAndUpdate(verificationCode.id.value) {
                    it.isUsed = true
                }
            }
            throw VerificationCodeExpiredException()
        }

        val verifyResult = BCrypt.verifyer().verify(code.toCharArray(), verificationCode.code)
        if (!verifyResult.verified) {
            throw InvalidVerificationCodeException()
        }

        return newSuspendedTransaction {
            EmailVerificationCodeEntity.findByIdAndUpdate(verificationCode.id.value) {
                it.isUsed = true
            }

            UserEntity.findByIdAndUpdate(existingUser.id.value) {
                it.isEmailVerified = true
            }

            generateToken(existingUser)
        }
    }

    suspend fun login(email: String, password: String): String {
        val existingUser = newSuspendedTransaction {
            UserEntity.find {
                Users.email eq email
            }.firstOrNull()
        } ?: throw InvalidCredentialsException()

        val verifyResult = BCrypt.verifyer().verify(password.toCharArray(), existingUser.password)
        if (!verifyResult.verified) {
            throw InvalidCredentialsException()
        }

        return generateToken(existingUser)
    }

    suspend fun forgetPassword(email: String) {
        val existingUser = newSuspendedTransaction {
            UserEntity.find {
                Users.email eq email
            }.firstOrNull()
        } ?: throw EmailNotExistsException()
        if (!existingUser.isEmailVerified) {
            throw EmailIsNotVerifiedException()
        }

        val code = generateVerificationCode()
        val hashedCode = hashValue(code.toString())

        newSuspendedTransaction {
            EmailVerificationCodeEntity.new {
                user = existingUser
                this.code = hashedCode
                expiresAt = Clock.System.now().plus(5.minutes)
                isUsed = false
            }
        }

        sendVerificationEmail(
            email,
            "Reset Your Password",
            generateForgetPasswordVerificationEmailContent(code.toString())
        )
    }

    suspend fun resetPassword(email: String, code: String, newPassword: String): String {
        val existingUser = newSuspendedTransaction {
            UserEntity.find {
                Users.email eq email
            }.firstOrNull()
        } ?: throw EmailNotExistsException()
        if (!existingUser.isEmailVerified) {
            throw EmailIsNotVerifiedException()
        }

        val verificationCode = newSuspendedTransaction {
            EmailVerificationCodeEntity.find {
                (EmailVerificationCodes.userId eq existingUser.id) and
                        (EmailVerificationCodes.isUsed eq false)
            }.firstOrNull()
        } ?: throw NoVerificationCodeFoundException()

        if (Clock.System.now() > verificationCode.expiresAt) {
            newSuspendedTransaction {
                EmailVerificationCodeEntity.findByIdAndUpdate(verificationCode.id.value) {
                    it.isUsed = true
                }
            }
            throw VerificationCodeExpiredException()
        }

        val verifyResult = BCrypt.verifyer().verify(code.toCharArray(), verificationCode.code)
        if (!verifyResult.verified) {
            throw InvalidVerificationCodeException()
        }

        return newSuspendedTransaction {
            EmailVerificationCodeEntity.findByIdAndUpdate(verificationCode.id.value) {
                it.isUsed = true
            }

            val hashedPassword = hashValue(newPassword)
            UserEntity.findByIdAndUpdate(existingUser.id.value) {
                it.isEmailVerified = true
                it.password = hashedPassword
            }

            generateToken(existingUser)
        }
    }

    private fun hashValue(value: String): String {
        return BCrypt.withDefaults().hashToString(12, value.toCharArray())
    }

    private fun sendVerificationEmail(toEmail: String, title: String, content: String) {
        val message: Message = Message.builder()
            .from(config.property("mailgun.sender").getString())
            .to(toEmail)
            .subject(title)
            .text(content)
            .build()

        mailMessageApi.sendMessage(config.property("mailgun.domain").getString(), message)
    }

    private fun generateToken(user: UserEntity): String {
        val audience = config.property("jwt.audience").getString()
        val domain = config.property("jwt.domain").getString()
        val secret = config.property("jwt.secret").getString()

        return JWT.create()
            .withAudience(audience)
            .withIssuer(domain)
            .withClaim("userId", user.id.value.toString())
            .withClaim("email", user.email)
            .withExpiresAt(Date(System.currentTimeMillis() + 3600000)) // 1 hour
            .sign(Algorithm.HMAC256(secret))
    }
}