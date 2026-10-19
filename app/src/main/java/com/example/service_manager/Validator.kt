package com.example.service_manager

object Validator {
    fun isNameValid(name: String): Boolean {
        return name.isNotBlank()
    }

    fun isPhoneValid(phone: String): Boolean {
        return phone.isNotBlank()
    }

    fun isEmailValid(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }
}
