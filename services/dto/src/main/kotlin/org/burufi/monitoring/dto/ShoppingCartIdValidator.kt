package org.burufi.monitoring.dto

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class ShoppingCartIdValidator : ConstraintValidator<ShoppingCartId, String> {

    private val pattern = "[0-9a-zA-Z-]{1,30}".toRegex()

    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        return value?.let { pattern.matches(it) } == true
    }
}
