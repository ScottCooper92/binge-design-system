package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme

@Composable
fun GridScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
    ) {
        ExpressiveIconButton(
            onClick = onBack,
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.cd_navigate_back),
            modifier = Modifier.padding(
                start = dimensionResource(R.dimen.padding_s),
                end = dimensionResource(R.dimen.padding_s),
                top = dimensionResource(R.dimen.padding_s),
                bottom = dimensionResource(R.dimen.padding_xs),
            ),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                start = dimensionResource(R.dimen.grid_header_title_padding_h),
                end = dimensionResource(R.dimen.grid_header_title_padding_h),
                bottom = dimensionResource(R.dimen.grid_header_title_padding_bottom),
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewGridScreenHeader() {
    BingeExpressiveTheme {
        GridScreenHeader(title = "Action", onBack = {})
    }
}
