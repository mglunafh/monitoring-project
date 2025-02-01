package org.burufi.monitoring.dto

import jakarta.validation.Constraint
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ShoppingCartIdValidator::class])
annotation class ShoppingCartId(
    val message: String = "Invalid shopping cart ID format",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
