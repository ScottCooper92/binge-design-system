package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeExpandableGroupTest {
    @get:Rule
    val rule = createComposeRule()

    private val server = ListItem(icon = Icons.Filled.Dns, label = "Server", detail = "Home")

    @Test
    fun `the header opens the group, which then shows its rows and drops the hint`() {
        var expanded by mutableStateOf(false)
        val asked = mutableListOf<Boolean>()
        rule.setContent {
            BingeExpandableGroup(
                title = null,
                header =
                    bingeExpandableItem(
                        icon = Icons.Filled.Tune,
                        title = "Advanced options",
                        expanded = expanded,
                        onExpandedChange = { open ->
                            asked += open
                            expanded = open
                        },
                        collapsedDetail = "Server, quality and folder",
                    ),
                content = BingeGroupContent.Ready(listOf(server)),
            )
        }
        rule.onNodeWithText("Server, quality and folder").assertExists()
        rule.onNodeWithText("Server").assertDoesNotExist()

        rule.onNodeWithText("Advanced options").performClick()

        assertEquals(listOf(true), asked)
        rule.onNodeWithText("Server").assertExists()
        rule.onNodeWithText("Server, quality and folder").assertDoesNotExist()
    }

    @Test
    fun `a screen reader hears the header's state and can open it`() {
        var expanded by mutableStateOf(false)
        rule.setContent {
            BingeExpandableGroup(
                title = null,
                header = bingeExpandableItem(Icons.Filled.Tune, "Advanced options", expanded, onExpandedChange = { expanded = it }),
                content = BingeGroupContent.Loading,
            )
        }
        val header = rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.Expand))
        header.assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Collapsed"))

        header.performSemanticsAction(SemanticsActions.Expand)

        assertTrue(expanded)
        rule
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.Collapse))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Expanded"))
    }

    @Test
    fun `a failed read offers its retry`() {
        var retried = false
        rule.setContent {
            BingeExpandableGroup(
                title = null,
                header = bingeExpandableItem(Icons.Filled.Tune, "Advanced options", expanded = true, onExpandedChange = {}),
                content = BingeGroupContent.Failed("No options.", "Try again") { retried = true },
            )
        }

        rule.onNodeWithText("Try again").performClick()

        assertTrue(retried)
    }

    @Test
    fun `the rows hang from the header, the last one ending the line`() {
        val rows = listOf(server, server.copy(label = "Profile"), server.copy(label = "Folder")).joinedToHeader()

        assertEquals(
            listOf(ListItemConnector.Continue, ListItemConnector.Continue, ListItemConnector.End),
            rows.map { it.connector },
        )
    }
}
