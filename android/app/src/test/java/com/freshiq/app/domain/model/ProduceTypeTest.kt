package com.freshiq.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProduceTypeTest {

    @Test
    fun testFromBackendValue() {
        assertEquals(ProduceType.AVOCADO, ProduceType.fromBackendValue("avocado"))
        assertEquals(ProduceType.AVOCADO, ProduceType.fromBackendValue("AVOCADO"))
        assertEquals(ProduceType.AVOCADO, ProduceType.fromBackendValue("hass_avocado"))
        assertEquals(ProduceType.MANGO, ProduceType.fromBackendValue("mango"))
        assertEquals(ProduceType.MANGO, ProduceType.fromBackendValue("MANGO"))
        assertEquals(ProduceType.BANANA, ProduceType.fromBackendValue("banana"))
        assertEquals(ProduceType.BANANA, ProduceType.fromBackendValue("BANANA"))
        assertEquals(ProduceType.AVOCADO, ProduceType.fromBackendValue("unknown"))
        assertEquals(ProduceType.AVOCADO, ProduceType.fromBackendValue(null))
    }

    @Test
    fun testProduceTypeProperties() {
        assertEquals("avocado", ProduceType.AVOCADO.backendValue)
        assertEquals("Avocado", ProduceType.AVOCADO.displayName)
        assertEquals("Persea americana (Hass)", ProduceType.AVOCADO.scientificName)
        assertTrue(ProduceType.AVOCADO.hasRulSupport)

        assertEquals("mango", ProduceType.MANGO.backendValue)
        assertEquals("Mango", ProduceType.MANGO.displayName)
        assertFalse(ProduceType.MANGO.hasRulSupport)

        assertEquals("banana", ProduceType.BANANA.backendValue)
        assertEquals("Banana", ProduceType.BANANA.displayName)
        assertEquals("Musa acuminata (Cavendish)", ProduceType.BANANA.scientificName)
        assertFalse(ProduceType.BANANA.hasRulSupport)
    }

    @Test
    fun testMangoRipenessStageMapping() {
        assertEquals(MangoRipenessStage.UNRIPE, MangoRipenessStage.fromNumber(0))
        assertEquals(MangoRipenessStage.SEMIRIPE, MangoRipenessStage.fromNumber(1))
        assertEquals(MangoRipenessStage.FULLY_RIPE, MangoRipenessStage.fromNumber(2))
        assertEquals(MangoRipenessStage.OVERRIPE, MangoRipenessStage.fromNumber(3))
        assertEquals(MangoRipenessStage.PERISHED, MangoRipenessStage.fromNumber(4))
        // Fallback
        assertEquals(MangoRipenessStage.FULLY_RIPE, MangoRipenessStage.fromNumber(99))
    }

    @Test
    fun testBananaRipenessStageMapping() {
        assertEquals(BananaRipenessStage.UNRIPE, BananaRipenessStage.fromNumber(0))
        assertEquals(BananaRipenessStage.SEMI_RIPE, BananaRipenessStage.fromNumber(1))
        assertEquals(BananaRipenessStage.RIPE, BananaRipenessStage.fromNumber(2))
        // Fallback
        assertEquals(BananaRipenessStage.RIPE, BananaRipenessStage.fromNumber(99))

        assertEquals(BananaRipenessStage.UNRIPE, BananaRipenessStage.fromLabel("unripe"))
        assertEquals(BananaRipenessStage.SEMI_RIPE, BananaRipenessStage.fromLabel("semi-ripe"))
        assertEquals(BananaRipenessStage.SEMI_RIPE, BananaRipenessStage.fromLabel("semiripe"))
        assertEquals(BananaRipenessStage.RIPE, BananaRipenessStage.fromLabel("ripe"))
        assertEquals(BananaRipenessStage.RIPE, BananaRipenessStage.fromLabel("unknown"))
    }
}
