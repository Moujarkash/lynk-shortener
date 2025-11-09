package com.mod.core

import java.util.*

fun generateVerificationCode(): Int {
    val random = Random(System.currentTimeMillis())
    return random.nextInt(10000, 100000)
}

fun generateRandomString(length: Int): String {
    val random = Random(System.currentTimeMillis())
    val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}

fun generateVerificationEmailContent(code: String): String = """
Verify Your Account on Lynk Shortener Platform!
                    
Welcome to Lynk Shortener! To complete your registration, please enter the following verification code:

Verification Code: $code

Please enter this code in the verification field on our platform to verify your email address.

This code will expire in 2 minutes.

If you didn't sign up for Lynk Shortener, please ignore this email.

Thanks,

Lynk Shortener Team
    
"""

fun generateForgetPasswordVerificationEmailContent(code: String): String = """
We received a request to reset the password for your account. To complete the process, please use the verification code below:

Verification Code: $code

This code will expire in 2 minutes.

If you did not request a password reset, please ignore this email. Someone may have entered your email address by mistake.

Thanks,

Lynk Shortener Team
    
"""

fun generateUpdateEmailVerificationEmailContent(code: String): String = """
We received a request to update the email for your account. To complete the process, please use the verification code below:

Verification Code: $code

This code will expire in 2 minutes.

If you did not request an email update, please ignore this email. Someone may have entered your email address by mistake.

Thanks,

Lynk Shortener Team
    
"""
