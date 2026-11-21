@file:OnePerScreen(fullScreen = true)
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.component.BingeMessageDialog
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.preview.WithPreviewImage
import com.binge.designsystem.template.PeopleGridPerson
import com.binge.designsystem.template.PeopleGridScreen
import com.binge.designsystem.template.PeopleGridSection
import com.binge.designsystem.template.PeopleGridState
import com.binge.designsystem.template.PeopleGridTab

private const val PHOTO_URL = "sample://photo"
private const val PHOTO_WIDTH_PX = 320
private const val PHOTO_HEIGHT_PX = 480

private val Names =
    listOf(
        "Amara Okafor",
        "Jonas Lindqvist",
        "Priya Raman",
        "Mateo Ferreira",
        "Sana Haddad",
        "Lena Hoffmann",
        "Tomás Rivera",
        "Ife Adeyemi",
    )
private val Roles = listOf("Lead", "Supporting", "Cameo", null)
private val CrewRoles = listOf("Director", "Screenplay", "Producer", "Cinematographer", "Editor", "Sound designer")
private val Departments = listOf("Directing", "Writing", "Production", "Camera", "Editing", "Sound")

private fun person(
    prefix: String,
    n: Int,
    role: String?,
) = PeopleGridPerson(
    key = "$prefix-$n",
    name = Names[n % Names.size],
    role = role,
    // Every third person has no photo, so a frame shows the placeholder beside real ones.
    photoUrl = if (n % 3 == 2) null else PHOTO_URL,
)

private fun castState(count: Int = 18) =
    PeopleGridState.Ready(
        listOf(PeopleGridSection("cast", title = null, people = List(count) { person("cast", it, Roles[it % Roles.size]) })),
    )

private fun crewState() =
    PeopleGridState.Ready(
        Departments.mapIndexed { d, department ->
            PeopleGridSection(
                key = department,
                title = department,
                people = List(CREW_PER_DEPARTMENT) { person(department, it + d, CrewRoles[d]) },
            )
        },
    )

private const val CREW_PER_DEPARTMENT = 5

@Composable
private fun SamplePeople(
    tabs: List<PeopleGridTab>,
    initialTab: Int = 0,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableIntStateOf(initialTab) }
    var opened by remember { mutableStateOf<String?>(null) }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        WithPreviewImage(PHOTO_WIDTH_PX, PHOTO_HEIGHT_PX) {
            PeopleGridScreen(
                title = "The Dark Knight",
                tabs = tabs,
                selectedTab = selected,
                onTabChange = { selected = it },
                onOpenPerson = { opened = it },
                onRetry = {},
                modifier = modifier,
                onBack = LocalDemoBack.current,
            )
        }
        opened?.let { BingeMessageDialog(title = "Opened", message = it, onDismiss = { opened = null }) }
    }
}

/** Cast and crew as two tabs: the cast on the first, a grid of portraits with a name and a role under each. Tabs, swipe and taps all work. */
@Composable
fun PeopleGridCastCrewSample() =
    SamplePeople(listOf(PeopleGridTab("Cast", castState()), PeopleGridTab("Crew", crewState(), sectionBubble = true)))

/** The crew tab: each department is a heading over its people, and the fast scrollbar's bubble names the department. */
@Composable
fun PeopleGridCrewSample() =
    SamplePeople(listOf(PeopleGridTab("Cast", castState()), PeopleGridTab("Crew", crewState(), sectionBubble = true)), initialTab = 1)

/** The people on their way: plates at the grid's column count, under the same bar and tabs. */
@Composable
fun PeopleGridLoadingSample() =
    SamplePeople(listOf(PeopleGridTab("Cast", PeopleGridState.Loading), PeopleGridTab("Crew", PeopleGridState.Loading)))

/** One tab failed to load while the other is fine: the default failure plate with Try again. */
@Composable
fun PeopleGridFailedSample() =
    SamplePeople(
        listOf(PeopleGridTab("Cast", castState()), PeopleGridTab("Crew", PeopleGridState.Failed(ErrorKind.Network))),
        initialTab = 1,
    )

/** No crew listed: an empty plate in the caller's own words. */
@Composable
fun PeopleGridEmptySample() =
    SamplePeople(
        listOf(
            PeopleGridTab("Cast", castState()),
            PeopleGridTab("Crew", PeopleGridState.Empty(message = "No crew is listed for this title.")),
        ),
        initialTab = 1,
    )

/** A single list of people: with one tab there is no tab row. */
@Composable
fun PeopleGridSingleSample() = SamplePeople(listOf(PeopleGridTab("Cast", castState(count = 9))))
