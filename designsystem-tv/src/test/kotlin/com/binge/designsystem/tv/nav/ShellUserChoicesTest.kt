package com.binge.designsystem.tv.nav

import com.binge.designsystem.tv.focus.TvShellArrival
import com.binge.designsystem.tv.focus.TvUserMoves
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Back from the content to the bar is the user choosing the bar, as a directional key is, so an arrival still waiting yields to it. */
class ShellUserChoicesTest {
    private val traces = mutableListOf<String>()
    private val arrival = TvShellArrival(onTrace = { traces += it })

    @Test
    fun `a user choice clears an arrival that is still pending`() {
        arrival.arm()

        ShellUserChoices(TvUserMoves(), arrival).noteChoice()

        assertEquals(listOf("arm", "clear while an arrival is pending"), traces)
    }

    @Test
    fun `going home is a choice and arms nothing for the switch it causes`() {
        val choices = ShellUserChoices(TvUserMoves(), arrival)
        var wentHome = 0

        choices.goHome { wentHome++ }
        arrival.armOnChange("movies")
        arrival.armOnChange("tv")

        assertEquals(1, wentHome)
        assertEquals(listOf("clear with nothing pending", "quiet switch armed nothing"), traces)
    }
}
