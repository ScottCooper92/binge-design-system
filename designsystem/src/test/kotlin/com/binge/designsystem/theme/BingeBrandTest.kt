package com.binge.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BingeBrandTest {
    private val indigo = Color(0xFF5C6BC0)
    private val indigoLight = Color(0xFFC5CAE9)
    private val ink = Color(0xFF0D1340)

    @Test
    fun bingesOwnBrandBuilds() {
        BingeBrand.Binge
    }

    @Test
    fun aBrandThatSetsItsFixedRolesBuilds() {
        BingeBrand(light = branded(LightColorScheme.copy(primary = indigo)), dark = branded(DarkColorScheme.copy(primary = indigo)))
    }

    @Test
    fun aBrandThatChangesPrimaryButKeepsBingesFixedRolesIsRejected() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            BingeBrand(light = branded(LightColorScheme.copy(primary = indigo)), dark = DarkColorScheme.copy(primary = indigo))
        }
        assertTrue(error.message.orEmpty().contains("dark scheme"))
    }

    @Test
    fun keepingOneFixedRoleIsEnoughToBeRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            BingeBrand(
                light = branded(LightColorScheme.copy(primary = indigo)).copy(onPrimaryFixed = LightColorScheme.onPrimaryFixed),
                dark = branded(DarkColorScheme.copy(primary = indigo)),
            )
        }
    }

    private fun branded(scheme: ColorScheme) =
        scheme.copy(primaryFixed = indigoLight, primaryFixedDim = indigo, onPrimaryFixed = ink, onPrimaryFixedVariant = ink)
}
