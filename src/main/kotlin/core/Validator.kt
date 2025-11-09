package com.mod.core

object Validator {
    fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
    }

    fun validatePassword(password: String): Pair<Boolean, String?> {
        val minLength = 8
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }

        return if (password.length < minLength) {
            Pair(false, "Password should be at least $minLength characters long.")
        } else if (!hasUpperCase) {
            Pair(false, "Password should contain at least one uppercase letter.")
        } else if (!hasLowerCase) {
            Pair(false, "Password should contain at least one lowercase letter.")
        } else if (!hasDigit) {
            Pair(false, "Password should contain at least one digit.")
        } else {
            Pair(true, null)
        }
    }
}