package com.freshiq.app.ui.screens.scan

import android.content.Context
import com.freshiq.app.data.model.HealthResponseDto
import com.freshiq.app.data.model.PredictionResponseDto
import com.freshiq.app.data.repository.FreshIQRepository
import com.freshiq.app.data.repository.ScanSessionManager
import com.freshiq.app.ui.components.FreshIQErrorType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import android.content.ContextWrapper

class FakeContext : ContextWrapper(null)

class FakeFreshIQRepository : FreshIQRepository {
    var predictProduceCallCount = 0
    var lastFoodType: String? = null

    override suspend fun getHealth(): Result<HealthResponseDto> {
        return Result.success(
            HealthResponseDto(
                status = "healthy",
                version = "1.0.0",
                modelsLoaded = true,
                visionCheckpointVerified = true,
                rulModelLoaded = true,
                featureConfigLoaded = true,
                supportedProduce = listOf("hass_avocado", "mango"),
                timestamp = "2026-09-11T00:00:00Z"
            )
        )
    }

    override suspend fun predictProduce(
        imageFile: File,
        storageCondition: String?,
        foodType: String
    ): Result<PredictionResponseDto> {
        predictProduceCallCount++
        lastFoodType = foodType
        return Result.failure(Exception("Test stub"))
    }
}

class ScanViewModelTest {

    private fun createDummyContext(): Context {
        return FakeContext()
    }

    @Test
    fun testInitialUiState() {
        val fakeRepo = FakeFreshIQRepository()
        val sessionManager = ScanSessionManager()
        val viewModel = ScanViewModel(fakeRepo, sessionManager, createDummyContext())

        val state = viewModel.uiState.value
        assertNull(state.selectedImageUri)
        assertNull(state.selectedFile)
        assertNull(state.fileMetadata)
        assertEquals("ambient", state.storageCondition)
        assertFalse(state.isSubmitting)
        assertNull(state.errorType)
        assertNull(state.customErrorMessage)
        assertEquals(0, fakeRepo.predictProduceCallCount)
    }

    @Test
    fun testStorageConditionChange() {
        val fakeRepo = FakeFreshIQRepository()
        val sessionManager = ScanSessionManager()
        val viewModel = ScanViewModel(fakeRepo, sessionManager, createDummyContext())

        viewModel.setStorageCondition("refrigerated")
        assertEquals("refrigerated", viewModel.uiState.value.storageCondition)

        viewModel.setStorageCondition("elevated")
        assertEquals("elevated", viewModel.uiState.value.storageCondition)
    }

    @Test
    fun testOnCameraPermissionDenied() {
        val fakeRepo = FakeFreshIQRepository()
        val sessionManager = ScanSessionManager()
        val viewModel = ScanViewModel(fakeRepo, sessionManager, createDummyContext())

        viewModel.onCameraPermissionDenied()

        val state = viewModel.uiState.value
        assertEquals(FreshIQErrorType.InvalidFile, state.errorType)
        assertTrue(state.customErrorMessage?.contains("Camera permission is required") == true)
        assertFalse(state.isSubmitting)
        assertEquals(0, fakeRepo.predictProduceCallCount)
    }

    @Test
    fun testOnCameraLaunchError() {
        val fakeRepo = FakeFreshIQRepository()
        val sessionManager = ScanSessionManager()
        val viewModel = ScanViewModel(fakeRepo, sessionManager, createDummyContext())

        viewModel.onCameraLaunchError("ActivityNotFoundException: No camera installed")

        val state = viewModel.uiState.value
        assertEquals(FreshIQErrorType.InvalidFile, state.errorType)
        assertEquals("ActivityNotFoundException: No camera installed", state.customErrorMessage)
        assertFalse(state.isSubmitting)
        assertEquals(0, fakeRepo.predictProduceCallCount)
    }

    @Test
    fun testOnCameraPhotoCancelledDoesNotCrashOrTriggerPrediction() {
        val fakeRepo = FakeFreshIQRepository()
        val sessionManager = ScanSessionManager()
        val viewModel = ScanViewModel(fakeRepo, sessionManager, createDummyContext())

        viewModel.onCameraPhotoCancelled()

        val state = viewModel.uiState.value
        assertNull(state.errorType)
        assertNull(state.customErrorMessage)
        assertFalse(state.isSubmitting)
        assertEquals(0, fakeRepo.predictProduceCallCount)
    }

    @Test
    fun testOnRemoveImageResetsState() {
        val fakeRepo = FakeFreshIQRepository()
        val sessionManager = ScanSessionManager()
        val viewModel = ScanViewModel(fakeRepo, sessionManager, createDummyContext())

        viewModel.onRemoveImage()

        val state = viewModel.uiState.value
        assertNull(state.selectedImageUri)
        assertNull(state.selectedFile)
        assertNull(state.fileMetadata)
        assertNull(state.errorType)
        assertNull(state.customErrorMessage)
    }

    @Test
    fun testInitialProduceTypeIsAvocado() {
        val fakeRepo = FakeFreshIQRepository()
        val sessionManager = ScanSessionManager()
        val viewModel = ScanViewModel(fakeRepo, sessionManager, createDummyContext())

        assertEquals(com.freshiq.app.domain.model.ProduceType.AVOCADO, viewModel.uiState.value.produceType)
    }

    @Test
    fun testSetProduceTypeToMangoDoesNotTriggerNetworkCall() {
        val fakeRepo = FakeFreshIQRepository()
        val sessionManager = ScanSessionManager()
        val viewModel = ScanViewModel(fakeRepo, sessionManager, createDummyContext())

        viewModel.setProduceType(com.freshiq.app.domain.model.ProduceType.MANGO)

        assertEquals(com.freshiq.app.domain.model.ProduceType.MANGO, viewModel.uiState.value.produceType)
        assertEquals(0, fakeRepo.predictProduceCallCount)
        assertNull(fakeRepo.lastFoodType)
    }

    @Test
    fun testSetProduceTypeSwitching() {
        val fakeRepo = FakeFreshIQRepository()
        val sessionManager = ScanSessionManager()
        val viewModel = ScanViewModel(fakeRepo, sessionManager, createDummyContext())

        viewModel.setProduceType(com.freshiq.app.domain.model.ProduceType.MANGO)
        assertEquals(com.freshiq.app.domain.model.ProduceType.MANGO, viewModel.uiState.value.produceType)

        viewModel.setProduceType(com.freshiq.app.domain.model.ProduceType.AVOCADO)
        assertEquals(com.freshiq.app.domain.model.ProduceType.AVOCADO, viewModel.uiState.value.produceType)
        assertEquals(0, fakeRepo.predictProduceCallCount)
    }

    @Test
    fun testSetProduceTypeToBananaDoesNotTriggerNetworkCall() {
        val fakeRepo = FakeFreshIQRepository()
        val sessionManager = ScanSessionManager()
        val viewModel = ScanViewModel(fakeRepo, sessionManager, createDummyContext())

        viewModel.setProduceType(com.freshiq.app.domain.model.ProduceType.BANANA)

        assertEquals(com.freshiq.app.domain.model.ProduceType.BANANA, viewModel.uiState.value.produceType)
        assertEquals(0, fakeRepo.predictProduceCallCount)
        assertNull(fakeRepo.lastFoodType)
    }

    @Test
    fun testSwitchingBetweenAllProduceTypesZeroNetworkCalls() {
        val fakeRepo = FakeFreshIQRepository()
        val sessionManager = ScanSessionManager()
        val viewModel = ScanViewModel(fakeRepo, sessionManager, createDummyContext())

        viewModel.setProduceType(com.freshiq.app.domain.model.ProduceType.AVOCADO)
        viewModel.setProduceType(com.freshiq.app.domain.model.ProduceType.MANGO)
        viewModel.setProduceType(com.freshiq.app.domain.model.ProduceType.BANANA)
        viewModel.setProduceType(com.freshiq.app.domain.model.ProduceType.AVOCADO)

        assertEquals(com.freshiq.app.domain.model.ProduceType.AVOCADO, viewModel.uiState.value.produceType)
        assertEquals(0, fakeRepo.predictProduceCallCount)
        assertNull(fakeRepo.lastFoodType)
    }
}
