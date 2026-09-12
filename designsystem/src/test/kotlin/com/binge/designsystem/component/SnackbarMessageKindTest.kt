package com.binge.designsystem.component

import androidx.compose.material3.SnackbarDuration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class SnackbarMessageKindTest {
    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("durationCases")
    fun `each kind maps to its snackbar duration`(kind: SnackbarMessageKind, expected: SnackbarDuration) {
        assertEquals(expected, kind.duration)
    }

    companion object {
        @JvmStatic
        fun durationCases() =
            listOf(
                Arguments.of(SnackbarMessageKind.Confirmation, SnackbarDuration.Short),
                Arguments.of(SnackbarMessageKind.Error, SnackbarDuration.Long),
                Arguments.of(SnackbarMessageKind.InProgress, SnackbarDuration.Indefinite),
            )
    }
}
