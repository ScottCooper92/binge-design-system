@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.motion.rememberBingeNavMotion
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for the shared navigation motion: two screens in a fixed stage, a "Push detail" button on the first and a "Pop" button on
 * the second, which run the forward and pop transitions. It rests on the first screen. Under reduce motion, as the screenshot theme sets,
 * the transitions are crossfades.
 */
@Composable
fun BingeNavMotionSample() {
    ScreenshotTheme {
        val motion = rememberBingeNavMotion()
        var onDetail by remember { mutableStateOf(false) }
        Box(Modifier.fillMaxWidth().height(dimensionResource(R.dimen.nav_motion_sample_height))) {
            AnimatedContent(
                targetState = onDetail,
                transitionSpec = { if (targetState) motion.detailForward else motion.detailPop },
                label = "nav-motion-sample",
            ) { detail ->
                Surface(
                    modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.nav_motion_sample_height)),
                    color = if (detail) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Column(Modifier.padding(dimensionResource(R.dimen.padding_m))) {
                        Text(text = if (detail) "Detail screen" else "List screen", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { onDetail = !detail }) { Text(if (detail) "Pop" else "Push detail") }
                    }
                }
            }
        }
    }
}
