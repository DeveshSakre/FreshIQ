package com.freshiq.app.domain.model

/**
 * Supported produce types in FreshIQ.
 *
 * Current production capabilities:
 * - AVOCADO: Stage classification + Remaining Useful Life (RUL) + What-If storage scenarios.
 * - MANGO: Dedicated 5-class stage classification ONLY (NO RUL, NO storage simulation).
 * - BANANA: Dedicated 3-class stage classification ONLY (NO RUL, NO storage simulation).
 */
enum class ProduceType(
    val displayName: String,
    val scientificName: String,
    val backendValue: String,
    val hasRulSupport: Boolean
) {
    AVOCADO(
        displayName = "Avocado",
        scientificName = "Persea americana (Hass)",
        backendValue = "avocado",
        hasRulSupport = true
    ),
    MANGO(
        displayName = "Mango",
        scientificName = "Mangifera indica (White Chaunsa Late)",
        backendValue = "mango",
        hasRulSupport = false
    ),
    BANANA(
        displayName = "Banana",
        scientificName = "Musa acuminata (Cavendish)",
        backendValue = "banana",
        hasRulSupport = false
    );

    companion object {
        fun fromBackendValue(value: String?): ProduceType {
            return when (value?.lowercase()?.trim()) {
                "mango" -> MANGO
                "banana" -> BANANA
                else -> AVOCADO
            }
        }
    }
}
