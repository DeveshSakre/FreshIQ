package com.freshiq.app.data.model

import com.freshiq.app.domain.model.ProduceType
import com.google.gson.annotations.SerializedName

data class StageProbabilitiesDto(
    // Avocado stage probability keys
    @SerializedName("stage_1") val stage1: Float = 0f,
    @SerializedName("stage_2") val stage2: Float = 0f,
    @SerializedName("stage_3") val stage3: Float = 0f,
    @SerializedName("stage_4") val stage4: Float = 0f,
    @SerializedName("stage_5") val stage5: Float = 0f,

    // Mango stage probability keys
    @SerializedName("Unripe") val unripe: Float? = null,
    @SerializedName("Semiripe") val semiripe: Float? = null,
    @SerializedName("Fully Ripe") val fullyRipe: Float? = null,
    @SerializedName("Overripe") val overripe: Float? = null,
    @SerializedName("Perished") val perished: Float? = null,

    // Banana stage probability keys
    @SerializedName("Semi-ripe") val semiRipeBanana: Float? = null,
    @SerializedName("Ripe") val ripeBanana: Float? = null
) {
    /**
     * Returns ordered list of class name and probability value.
     */
    fun getProbabilitiesList(produceType: ProduceType = ProduceType.AVOCADO): List<Pair<String, Float>> {
        return when (produceType) {
            ProduceType.BANANA -> listOf(
                "Unripe" to (unripe ?: 0f),
                "Semi-ripe" to (semiRipeBanana ?: 0f),
                "Ripe" to (ripeBanana ?: 0f)
            )
            ProduceType.MANGO -> listOf(
                "Unripe" to (unripe ?: 0f),
                "Semiripe" to (semiripe ?: 0f),
                "Fully Ripe" to (fullyRipe ?: 0f),
                "Overripe" to (overripe ?: 0f),
                "Perished" to (perished ?: 0f)
            )
            ProduceType.AVOCADO -> listOf(
                "Stage 1 — Underripe" to stage1,
                "Stage 2 — Breaking" to stage2,
                "Stage 3 — Ripe First Stage" to stage3,
                "Stage 4 — Ripe Second Stage" to stage4,
                "Stage 5 — Overripe" to stage5
            )
        }
    }

    /**
     * Backward-compatible overload for existing callers.
     */
    fun getProbabilitiesList(isMango: Boolean = false): List<Pair<String, Float>> {
        return getProbabilitiesList(if (isMango) ProduceType.MANGO else ProduceType.AVOCADO)
    }
}

data class RipenessAssessmentDto(
    @SerializedName("predicted_ripening_stage") val predictedRipeningStage: Int,
    @SerializedName("stage_label") val stageLabel: String,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("expected_continuous_ripening_stage") val expectedContinuousRipeningStage: Float? = null,
    @SerializedName("probabilities") val probabilities: StageProbabilitiesDto
)

data class ScenarioRulDto(
    @SerializedName("condition") val condition: String,
    @SerializedName("temperature_c") val temperatureC: Float,
    @SerializedName("estimated_rul_days") val estimatedRulDays: Float,
    @SerializedName("is_extrapolated") val isExtrapolated: Boolean,
    @SerializedName("method") val method: String,
    @SerializedName("uncertainty_note") val uncertaintyNote: String?,
    @SerializedName("disclaimer") val disclaimer: String,
    @SerializedName("recommendation") val recommendation: String
)

data class PredictionResponseDto(
    @SerializedName("food_type") val foodType: String = "avocado",
    @SerializedName("item_name") val itemName: String = "Avocado (Hass)",
    @SerializedName("model_id") val modelId: String = "FreshIQ_MobileNetV3_Avocado",
    @SerializedName("ripeness") val ripeness: RipenessAssessmentDto,
    @SerializedName("rul_available") val rulAvailable: Boolean = true,
    @SerializedName("scenarios") val scenarios: Map<String, ScenarioRulDto>? = null,
    @SerializedName("refrigeration_extension_gain_days") val refrigerationExtensionGainDays: Float? = null,
    @SerializedName("actionable_recommendation") val actionableRecommendation: String,
    @SerializedName("legal_disclaimer") val legalDisclaimer: String
)

data class HealthResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("version") val version: String,
    @SerializedName("models_loaded") val modelsLoaded: Boolean,
    @SerializedName("vision_checkpoint_verified") val visionCheckpointVerified: Boolean,
    @SerializedName("rul_model_loaded") val rulModelLoaded: Boolean,
    @SerializedName("feature_config_loaded") val featureConfigLoaded: Boolean,
    @SerializedName("mango_model_loaded") val mangoModelLoaded: Boolean = true,
    @SerializedName("mango_checkpoint_verified") val mangoCheckpointVerified: Boolean = true,
    @SerializedName("banana_model_loaded") val bananaModelLoaded: Boolean = true,
    @SerializedName("banana_checkpoint_verified") val bananaCheckpointVerified: Boolean = true,
    @SerializedName("supported_produce") val supportedProduce: List<String>,
    @SerializedName("timestamp") val timestamp: String
)
