package com.binge.designsystem

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/** [downloadEtaLabel] shows minutes under an hour, then whole hours rounded to the nearest. */
class DownloadEtaTest {
    @ParameterizedTest
    @CsvSource(
        "0, ",
        "59, ",
        "60, 1",
        "89, 1",
        "90, 2",
        "149, 2",
        "150, 3",
    )
    fun `hours from an hour up, rounded to the nearest`(minutes: Int, hours: Int?) {
        assertEquals(hours, downloadEtaHours(minutes))
    }
}
