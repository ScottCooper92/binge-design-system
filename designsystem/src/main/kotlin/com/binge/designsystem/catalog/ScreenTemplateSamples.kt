@file:OnePerScreen(fullScreen = true)

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.component.BingeFilterChip
import com.binge.designsystem.component.BingeOutlinedButton
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.component.DetailHero
import com.binge.designsystem.component.FilterChipItem
import com.binge.designsystem.component.ListRow
import com.binge.designsystem.component.ListRowSkeletonColumn
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.BingeScreenScaffold
import com.binge.designsystem.template.FilteredListScreen
import com.binge.designsystem.template.FormAction
import com.binge.designsystem.template.FormActionPlacement
import com.binge.designsystem.template.FormScreen
import com.binge.designsystem.template.FormSection
import com.binge.designsystem.template.HeroDetailLazyScreen
import com.binge.designsystem.template.HeroDetailScreen
import com.binge.designsystem.template.HeroDetailStateScreen
import com.binge.designsystem.template.LoadingMessageScreen
import com.binge.designsystem.template.MessageScreen
import com.binge.designsystem.template.ScreenAction
import com.binge.designsystem.template.ScreenBar
import com.binge.designsystem.template.ScreenBody
import com.binge.designsystem.template.StepFlowScreen
import com.binge.designsystem.template.StepHeading
import com.binge.designsystem.template.screenListPadding
import com.binge.designsystem.theme.BingeShapes

/*
 * The screen templates, each at the size it ships: a whole window. Every sample fills its slots with plain
 * rows and copy, so the frame shows the template's own chrome and insets rather than any one screen's content.
 */

private const val SAMPLE_ROWS = 14
private const val SAMPLE_STEPS = 3
private const val SAMPLE_GRID_COLUMNS = 3
private const val SAMPLE_GRID_TILES = 24

/** A collapsing-bar screen over a list: the bar scrims in as rows pass under it. */
@Composable
fun BingeScreenScaffoldSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        BingeScreenScaffold(title = "Episodes", onBack = LocalDemoBack.current) { padding ->
            SampleRows(padding)
        }
    }
}

/** The small bar, which slides away as the list scrolls down and comes back as it scrolls up. */
@Composable
fun BingeScreenScaffoldSmallBarSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        BingeScreenScaffold(title = "Users", onBack = LocalDemoBack.current, bar = ScreenBar.Small) { padding ->
            SampleRows(padding)
        }
    }
}

/** The large bar: a subtitle summarising what the screen shows, and a control at the end of the title row. */
@Composable
fun BingeScreenScaffoldLargeBarSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        BingeScreenScaffold(
            title = "Discover",
            bar = ScreenBar.Large,
            subtitle = "Movies · Popular · 2 filters",
            titleTrailing = {
                BingeFilterChip(
                    label = "Filters",
                    selected = true,
                    onClick = {},
                    count = 2,
                )
            },
        ) { padding ->
            SampleRows(padding)
        }
    }
}

/** A pinned bottom bar: the list's last row comes to rest above it rather than under it. */
@Composable
fun BingeScreenScaffoldBottomBarSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        BingeScreenScaffold(
            title = "Sliders",
            onBack = LocalDemoBack.current,
            bar = ScreenBar.Small,
            bottomBar = {
                Box(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    BingeFilledButton(
                        label = "Add slider",
                        onClick = {},
                        modifier = Modifier
                            .navigationBarsPadding()
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.padding_m)),
                    )
                }
            },
        ) { padding ->
            SampleRows(padding)
        }
    }
}

/** A failure that replaced the screen's content: the reason and two ways out. */
@Composable
fun MessageScreenSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        BingeScreenScaffold(title = "Requests", onBack = LocalDemoBack.current) { padding ->
            ScreenBody(padding) { inner ->
                MessageScreen(
                    headline = "Can't reach the server",
                    body = "Check your connection, then try again.",
                    icon = Icons.Filled.CloudOff,
                    primary = ScreenAction("Try again", onClick = {}, leadingIcon = Icons.Filled.Refresh),
                    secondary = ScreenAction("Settings", onClick = {}),
                    announce = true,
                    modifier = Modifier.padding(inner),
                )
            }
        }
    }
}

/** A message with three ways out, one of them destructive: the stacked actions slot, full width. */
@Composable
fun MessageScreenStackedActionsSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        BingeScreenScaffold(title = "Requests", onBack = LocalDemoBack.current) { padding ->
            ScreenBody(padding) { inner ->
                MessageScreen(
                    headline = "Can't reach the server",
                    body = "Check your connection, then try again.",
                    icon = Icons.Filled.CloudOff,
                    announce = true,
                    modifier = Modifier.padding(inner),
                    actions = {
                        BingeFilledButton(
                            label = "Try again",
                            onClick = {},
                            leadingIcon = Icons.Filled.Refresh,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        BingeOutlinedButton(label = "Edit connection", onClick = {}, modifier = Modifier.fillMaxWidth())
                        BingeOutlinedButton(label = "Disconnect", onClick = {}, destructive = true, modifier = Modifier.fillMaxWidth())
                    },
                )
            }
        }
    }
}

/** An empty list: one sentence under its icon, and nothing to press. */
@Composable
fun MessageScreenEmptySample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        MessageScreen(headline = "Nothing here", body = "Requests you make show up here.", icon = Icons.Filled.Inbox)
    }
}

/** The frame while a screen loads. */
@Composable
fun LoadingMessageScreenSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        LoadingMessageScreen()
    }
}

/** A list split by filters, each page its own list, with a sort action in the bar. */
@Composable
fun FilteredListScreenSample() {
    var selected by remember { mutableIntStateOf(0) }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        FilteredListScreen(
            title = "Requests",
            onBack = LocalDemoBack.current,
            filters = listOf(FilterChipItem("All", 42), FilterChipItem("Pending", 3), FilterChipItem("Approved", 39)),
            selectedFilter = selected,
            onFilterChange = { selected = it },
            actions = { BingeTextButton(label = "Sort", onClick = {}) },
        ) { _, contentPadding ->
            SampleRows(contentPadding)
        }
    }
}

/** A filtered list before its counts arrive: no chips yet, and the bar scrims itself. */
@Composable
fun FilteredListScreenNotReadySample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        FilteredListScreen(
            title = "Requests",
            onBack = LocalDemoBack.current,
            filters = emptyList(),
            selectedFilter = 0,
            onFilterChange = {},
            ready = false,
            notReady = { padding ->
                ListRowSkeletonColumn(contentPadding = padding.screenListPadding())
            },
        ) { _, _ -> }
    }
}

/** A detail page under its hero, with the copy following in the same scroll. */
@Composable
fun HeroDetailScreenSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        HeroDetailScreen(
            title = "A title",
            onBack = LocalDemoBack.current,
            hero = {
                DetailHero(
                    title = "A title",
                    backdropUrl = null,
                    tagline = "The line a poster would carry.",
                    metaText = "2026 · 1h 52m",
                    onBack = LocalDemoBack.current,
                    showChrome = false,
                )
            },
        ) {
            SampleCopy()
        }
    }
}

/** A lazy page under the hero: its rows spaced, capped and centred on a wide window, with a gap under the last. */
@Composable
fun HeroDetailLazyScreenSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        HeroDetailLazyScreen(
            title = "A collection",
            onBack = LocalDemoBack.current,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = dimensionResource(R.dimen.padding_l)),
            hero = {
                DetailHero(
                    title = "A collection",
                    backdropUrl = null,
                    tagline = null,
                    metaText = "6 films",
                    onBack = LocalDemoBack.current,
                    showChrome = false,
                )
            },
        ) {
            items(SAMPLE_ROWS) { index ->
                ListRow(modifier = Modifier.widthIn(max = dimensionResource(R.dimen.content_max_width))) { textModifier ->
                    Text(text = "Part ${index + 1}", style = MaterialTheme.typography.titleMedium, modifier = textModifier)
                }
            }
        }
    }
}

/** A detail page whose load failed: the message under the page's back control, so the way out stays where it was. */
@Composable
fun HeroDetailStateScreenSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        HeroDetailStateScreen(onBack = LocalDemoBack.current) {
            MessageScreen(
                headline = "Can't load this title",
                body = "Check your connection, then try again.",
                icon = Icons.Filled.CloudOff,
                primary = ScreenAction("Try again", onClick = {}, leadingIcon = Icons.Filled.Refresh),
                announce = true,
            )
        }
    }
}

/** The same frame while the page loads. */
@Composable
fun HeroDetailStateScreenLoadingSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        HeroDetailStateScreen(onBack = LocalDemoBack.current) { LoadingMessageScreen() }
    }
}

/** The same page with its one action pinned below the scroll. */
@Composable
fun HeroDetailScreenFooterSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        HeroDetailScreen(
            title = "A title",
            onBack = LocalDemoBack.current,
            footer = {
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHigh)) {
                    BingeFilledButton(
                        label = "Approve",
                        onClick = {},
                        modifier = Modifier
                            .navigationBarsPadding()
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.padding_m)),
                    )
                }
            },
            hero = {
                DetailHero(
                    title = "A title",
                    backdropUrl = null,
                    tagline = null,
                    metaText = "Requested today",
                    onBack = LocalDemoBack.current,
                    showChrome = false,
                )
            },
        ) {
            SampleCopy()
        }
    }
}

/** A short editor that saves from the bar. */
@Composable
fun FormScreenSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        FormScreen(
            title = "General",
            onBack = LocalDemoBack.current,
            primaryAction = FormAction("Save", onClick = {}),
        ) {
            SampleForm()
        }
    }
}

/** A long editor with Cancel and Save pinned below it. */
@Composable
fun FormScreenFooterSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        FormScreen(
            title = "Network",
            onBack = LocalDemoBack.current,
            placement = FormActionPlacement.Footer,
            primaryAction = FormAction("Save", onClick = {}),
            secondaryAction = FormAction("Cancel", onClick = {}),
        ) {
            SampleForm()
        }
    }
}

/** The middle step of a flow: portrait stacks the art, heading and choices; a wide window splits them. */
@Composable
fun StepFlowScreenSample() {
    var step by remember { mutableIntStateOf(1) }
    val leave = LocalDemoBack.current
    val advance = { step = (step + 1).coerceAtMost(SAMPLE_STEPS - 1) }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        StepFlowScreen(
            stepCount = SAMPLE_STEPS,
            currentStep = step,
            onBack = { if (step > 0) step-- else leave() },
            aside = {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(R.dimen.state_icon_container_size))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                )
            },
            heading = {
                StepHeading(kicker = "Privacy", title = "Share usage data?", subtitle = "Counts of which screens are opened. Nothing more.")
            },
            footer = {
                BingeFilledButton(label = "Share", onClick = advance, modifier = Modifier.fillMaxWidth())
                BingeTextButton(label = "Don't share", onClick = advance, modifier = Modifier.fillMaxWidth())
            },
        ) {
            MessageScreen(
                body = "Each point the step makes sits here.",
                icon = Icons.Filled.Shield,
                modifier = Modifier.height(dimensionResource(R.dimen.detail_hero_height)),
            )
        }
    }
}

/** A step still loading what it shows. */
@Composable
fun StepFlowScreenLoadingSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        StepFlowScreen(stepCount = SAMPLE_STEPS, currentStep = 0, loading = true) {}
    }
}

/** A step whose content scrolls itself: a lazy grid takes the height left under the heading, above the commit. */
@Composable
fun StepFlowScreenLazyStepSample() {
    var step by remember { mutableIntStateOf(1) }
    val leave = LocalDemoBack.current
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        StepFlowScreen(
            stepCount = SAMPLE_STEPS,
            currentStep = step,
            onBack = { if (step > 0) step-- else leave() },
            contentScrolls = { false },
            heading = { StepHeading(kicker = "Services", title = "Where do you watch?", subtitle = "Pick as many as you like.") },
            footer = {
                BingeFilledButton(
                    label = "Continue",
                    onClick = { step = (step + 1).coerceAtMost(SAMPLE_STEPS - 1) },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(SAMPLE_GRID_COLUMNS),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(SAMPLE_GRID_TILES) {
                    Box(
                        modifier =
                            Modifier
                                .aspectRatio(1f)
                                .clip(BingeShapes.Medium)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    )
                }
            }
        }
    }
}

@Composable
private fun SampleRows(padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = padding.screenListPadding(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
    ) {
        items(SAMPLE_ROWS) { index ->
            ListRow { textModifier ->
                Text(text = "Row ${index + 1}", style = MaterialTheme.typography.titleMedium, modifier = textModifier)
            }
        }
    }
}

@Composable
private fun SampleCopy() {
    Column(
        modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
    ) {
        repeat(SAMPLE_STEPS) {
            Text(
                text = "The page's own sections follow the hero in one scroll, so the bar fades in as the hero leaves.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SampleForm() {
    FormSection(title = "Server") {
        OutlinedTextField(
            state = rememberTextFieldState("media.example.com"),
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(state = rememberTextFieldState("5055"), label = { Text("Port") }, modifier = Modifier.fillMaxWidth())
    }
    FormSection(title = "Proxy") {
        OutlinedTextField(state = rememberTextFieldState(), label = { Text("Proxy address") }, modifier = Modifier.fillMaxWidth())
    }
}
