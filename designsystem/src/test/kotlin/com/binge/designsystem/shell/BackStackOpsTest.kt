package com.binge.designsystem.shell

import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class BackStackOpsTest {
    @Test
    fun `replaceAll writes the new routes over the stack`() {
        val stack = stackOf("home", "a", "b")
        stack.replaceAll(listOf(Route("home"), Route("c")))
        assertEquals(listOf("home", "c"), stack.names())
    }

    @Test
    fun `replaceAll keeps the instance of an equal route`() {
        val stack = stackOf("home", "a")
        val root = stack.first()
        val second = stack[1]
        stack.replaceAll(listOf(Route("home"), Route("a"), Route("b")))
        assertSame(root, stack[0])
        assertSame(second, stack[1])
        assertEquals(listOf("home", "a", "b"), stack.names())
    }

    @Test
    fun `replaceAll refuses an empty list and leaves the stack alone`() {
        val stack = stackOf("home", "a")
        assertThrows(IllegalArgumentException::class.java) { stack.replaceAll(emptyList<NavKey>()) }
        assertEquals(listOf("home", "a"), stack.names())
    }

    @Test
    fun `popToRoot keeps the root instance and is never empty`() {
        val stack = stackOf("home", "a", "b")
        val root = stack.first()
        stack.popToRoot()
        assertEquals(1, stack.size)
        assertSame(root, stack.first())
    }

    @Test
    fun `popToRoot on a root alone does nothing`() {
        val stack = stackOf("home")
        stack.popToRoot()
        assertEquals(listOf("home"), stack.names())
    }
}
