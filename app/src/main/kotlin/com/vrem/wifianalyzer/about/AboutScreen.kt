/*
 * WiFiAnalyzer
 * Copyright (C) 2015 - 2026 VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.vrem.wifianalyzer.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vrem.wifianalyzer.R
import com.vrem.wifianalyzer.compose.SuccessColor
import com.vrem.wifianalyzer.compose.WiFiAnalyzerTheme

@Composable
fun AboutScreen(
    applicationName: String,
    packageName: String,
    versionInfo: String,
    copyright: String,
    device: String,
    isScanThrottleEnabled: Boolean,
    is5GHzBandSupported: Boolean,
    is6GHzBandSupported: Boolean,
    onWriteReview: () -> Unit,
    onShowLicense: (titleId: Int, resourceId: Int, isSmallFont: Boolean) -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_app),
                    contentDescription = applicationName,
                    modifier = Modifier.size(64.dp),
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = applicationName, fontWeight = FontWeight.Bold)
                    Text(text = packageName)
                    Text(text = versionInfo, fontWeight = FontWeight.Bold)
                    Text(text = stringResource(id = R.string.app_company_name), fontWeight = FontWeight.Bold)
                    Text(text = copyright, fontWeight = FontWeight.Bold)
                }
            }

            Text(text = device, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            WiFiStateItem(
                text = stringResource(id = R.string.wifi_throttling_off),
                errorText = stringResource(id = R.string.wifi_throttling_on),
                success = !isScanThrottleEnabled,
            )
            WiFiStateItem(
                text = stringResource(id = R.string.wifi_band_2ghz),
                success = true,
            )
            WiFiStateItem(
                text = stringResource(id = R.string.wifi_band_5ghz),
                success = is5GHzBandSupported,
            )
            WiFiStateItem(
                text = stringResource(id = R.string.wifi_band_6ghz),
                success = is6GHzBandSupported,
            )

            Spacer(modifier = Modifier.height(16.dp))

            UrlText(text = stringResource(id = R.string.app_url)) { uriHandler.openUri(it) }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.about_license_title),
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            Text(
                text = stringResource(id = R.string.gpl),
                modifier = Modifier.clickableText { onShowLicense(R.string.gpl, R.raw.gpl, true) },
            )
            UrlText(text = stringResource(id = R.string.gpl_url)) { uriHandler.openUri(it) }

            Text(
                text = stringResource(id = R.string.about_description_title),
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(vertical = 16.dp),
            )
            Text(text = stringResource(id = R.string.about_description_text))

            AboutLinkSection(titleRes = R.string.about_documentation, urlRes = R.string.about_documentation_url)
            AboutLinkSection(titleRes = R.string.about_how_to, urlRes = R.string.about_how_to_url)
            AboutLinkSection(titleRes = R.string.about_faq, urlRes = R.string.about_faq_url)
            AboutLinkSection(titleRes = R.string.about_privacy_policy, urlRes = R.string.about_privacy_policy_url)

            Button(
                onClick = { onShowLicense(R.string.about_contributor_title, R.raw.contributors, false) },
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Text(text = stringResource(id = R.string.about_contributor_title))
            }

            Button(
                onClick = onWriteReview,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(text = stringResource(id = R.string.about_write_review))
            }

            Text(
                text = stringResource(id = R.string.about_libraries_title),
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(vertical = 16.dp),
            )

            Text(text = stringResource(id = R.string.about_library_graph))
            Column(modifier = Modifier.padding(start = 16.dp)) {
                UrlText(text = stringResource(id = R.string.about_library_graph_url)) { uriHandler.openUri(it) }
                Text(
                    text = stringResource(id = R.string.al),
                    modifier = Modifier.clickableText { onShowLicense(R.string.al, R.raw.al, true) },
                )
                UrlText(text = stringResource(id = R.string.al_url)) { uriHandler.openUri(it) }
            }

            Text(text = stringResource(id = R.string.about_library_material), modifier = Modifier.padding(top = 8.dp))
            Column(modifier = Modifier.padding(start = 16.dp)) {
                UrlText(text = stringResource(id = R.string.about_library_material_url)) { uriHandler.openUri(it) }
                Text(
                    text = stringResource(id = R.string.al),
                    modifier = Modifier.clickableText { onShowLicense(R.string.al, R.raw.al, true) },
                )
                UrlText(text = stringResource(id = R.string.al_url)) { uriHandler.openUri(it) }
            }
        }
    }
}

@Composable
private fun WiFiStateItem(
    text: String,
    errorText: String = text,
    success: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp),
    ) {
        Icon(
            painter = painterResource(id = if (success) R.drawable.ic_check else R.drawable.ic_close),
            contentDescription = null,
            tint = if (success) SuccessColor else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = if (success) text else errorText, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AboutLinkSection(
    titleRes: Int,
    urlRes: Int,
) {
    val uriHandler = LocalUriHandler.current
    Text(text = stringResource(id = titleRes), modifier = Modifier.padding(top = 16.dp))
    UrlText(text = stringResource(id = urlRes)) { uriHandler.openUri(it) }
}

@Composable
private fun UrlText(
    text: String,
    onClick: (String) -> Unit,
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clickableText { onClick(text) },
    )
}

@Composable
private fun Modifier.clickableText(onClick: () -> Unit): Modifier =
    this.padding(vertical = 2.dp).clickable(onClick = onClick)

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    WiFiAnalyzerTheme(darkTheme = false) {
        AboutScreen(
            applicationName = "WiFiAnalyzer",
            packageName = "com.vrem.wifianalyzer",
            versionInfo = "1.0.0 (100)",
            copyright = "Copyright © 2015 - 2026",
            device = "Samsung - SM-G991B (API 33)",
            isScanThrottleEnabled = false,
            is5GHzBandSupported = true,
            is6GHzBandSupported = true,
            onWriteReview = {},
            onShowLicense = { _, _, _ -> },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenDarkPreview() {
    WiFiAnalyzerTheme(darkTheme = true) {
        AboutScreen(
            applicationName = "WiFiAnalyzer",
            packageName = "com.vrem.wifianalyzer",
            versionInfo = "1.0.0 (100)",
            copyright = "Copyright © 2015 - 2026",
            device = "Samsung - SM-G991B (API 33)",
            isScanThrottleEnabled = false,
            is5GHzBandSupported = true,
            is6GHzBandSupported = true,
            onWriteReview = {},
            onShowLicense = { _, _, _ -> },
        )
    }
}
