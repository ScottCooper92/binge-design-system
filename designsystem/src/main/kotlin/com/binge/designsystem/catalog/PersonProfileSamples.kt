@file:CatalogGroup("Person profile")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.binge.designsystem.component.PersonFact
import com.binge.designsystem.component.PersonProfileHeader
import com.binge.designsystem.component.PersonProfileSkeleton
import com.binge.designsystem.preview.ScreenshotTheme

/** The top of a person's page with no photo, so the initials stand in: an eyebrow over the name, then labelled facts and one plain line. */
@Composable
fun PersonProfileHeaderSample() {
    ScreenshotTheme {
        PersonProfileHeader(
            name = "Maya Rodrigues",
            photoUrl = null,
            eyebrow = "Acting",
            facts =
                listOf(
                    PersonFact("14 March 1985 (41 years old)", "Born"),
                    PersonFact("Lisbon, Portugal", "Place of birth"),
                    PersonFact("Also known as Maya R."),
                ),
        )
    }
}

/** A long name and a long fact at the header's narrow column: both wrap onto more lines rather than clipping. */
@Composable
fun PersonProfileHeaderLongSample() {
    ScreenshotTheme {
        PersonProfileHeader(
            name = "Maria Fernanda de los Angeles Rodriguez Hernandez",
            photoUrl = null,
            facts = listOf(PersonFact("Santiago de Compostela, Galicia, Spain", "Place of birth")),
        )
    }
}

/** The loading stand-in for the profile header, with three fact lines: the portrait plate and the plates for the name and the facts. */
@Composable
fun PersonProfileSkeletonSample() {
    ScreenshotTheme {
        Column { PersonProfileSkeleton() }
    }
}
