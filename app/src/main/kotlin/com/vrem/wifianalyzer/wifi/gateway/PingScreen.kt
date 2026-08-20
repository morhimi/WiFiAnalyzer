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
package com.vrem.wifianalyzer.wifi.gateway

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vrem.wifianalyzer.R
import java.util.Locale

@Composable
fun PingScreen(
    uiState: PingUiState,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            GatewayInfoCard(gatewayInfo = uiState.gatewayInfo)

            if (uiState.gatewayInfo.isConnected) {
                HeroLatencyCard(uiState = uiState)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    JitterCard(
                        jitterMs = uiState.jitterMs,
                        modifier = Modifier.weight(1f),
                    )
                    PacketLossCard(
                        lossPercent = uiState.packetLossPercent,
                        sent = uiState.packetsSent,
                        received = uiState.packetsReceived,
                        modifier = Modifier.weight(1f),
                    )
                }

                LatencyHistoryCard(history = uiState.history)

                ControlsRow(
                    status = uiState.status,
                    onToggle = onToggle,
                    onReset = onReset,
                )
            } else {
                NoGatewayCard()
            }
        }
    }
}

@Composable
private fun GatewayInfoCard(gatewayInfo: GatewayInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_network_wifi),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gatewayInfo.ssid.ifEmpty { stringResource(R.string.ping_gateway_title) },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text =
                            if (gatewayInfo.isConnected) {
                                "${stringResource(R.string.ping_gateway_title)}: ${gatewayInfo.gatewayIp}"
                            } else {
                                stringResource(R.string.ping_status_idle)
                            },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    )
                }
            }

            if (gatewayInfo.isConnected) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (gatewayInfo.localIp.isNotEmpty()) {
                        Text(
                            text = "${stringResource(R.string.ping_local_ip)}: ${gatewayInfo.localIp}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                    if (gatewayInfo.linkSpeedMbps > 0) {
                        Text(
                            text = "${stringResource(R.string.ping_link_speed)}: ${gatewayInfo.linkSpeedMbps} Mbps",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroLatencyCard(uiState: PingUiState) {
    val qualityColor by animateColorAsState(
        targetValue =
            when (uiState.quality) {
                NetworkQuality.EXCELLENT -> Color(0xFF4CAF50)
                NetworkQuality.GOOD -> Color(0xFF2196F3)
                NetworkQuality.FAIR -> Color(0xFFFF9800)
                NetworkQuality.POOR -> Color(0xFFF44336)
                NetworkQuality.UNREACHABLE -> Color.Gray
            },
        label = "qualityColor",
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.ping_latency),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                QualityBadge(quality = uiState.quality, color = qualityColor)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text =
                    if (uiState.packetsReceived > 0 && uiState.currentRttMs > 0) {
                        String.format(Locale.US, "%.1f", uiState.currentRttMs)
                    } else if (uiState.status == PingStatus.IDLE) {
                        "--"
                    } else {
                        "..."
                    },
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = qualityColor,
            )
            Text(
                text = "ms",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = stringResource(R.string.ping_min),
                    value = "${String.format(Locale.US, "%.1f", uiState.minRttMs)} ms",
                )
                StatItem(
                    label = stringResource(R.string.ping_avg),
                    value = "${String.format(Locale.US, "%.1f", uiState.avgRttMs)} ms",
                )
                StatItem(
                    label = stringResource(R.string.ping_max),
                    value = "${String.format(Locale.US, "%.1f", uiState.maxRttMs)} ms",
                )
            }
        }
    }
}

@Composable
private fun QualityBadge(
    quality: NetworkQuality,
    color: Color,
) {
    val text =
        when (quality) {
            NetworkQuality.EXCELLENT -> stringResource(R.string.ping_quality_excellent)
            NetworkQuality.GOOD -> stringResource(R.string.ping_quality_good)
            NetworkQuality.FAIR -> stringResource(R.string.ping_quality_fair)
            NetworkQuality.POOR -> stringResource(R.string.ping_quality_poor)
            NetworkQuality.UNREACHABLE -> stringResource(R.string.ping_quality_unreachable)
        }

    Box(
        modifier =
            Modifier
                .background(color.copy(alpha = 0.15f), shape = CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun JitterCard(
    jitterMs: Double,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.ping_jitter),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${String.format(Locale.US, "%.2f", jitterMs)} ms",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun PacketLossCard(
    lossPercent: Double,
    sent: Int,
    received: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.ping_packet_loss),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${String.format(Locale.US, "%.1f", lossPercent)}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (lossPercent > 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "$received / $sent ${stringResource(R.string.ping_packets)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun LatencyHistoryCard(history: List<PingSample>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.ping_history_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(16.dp))

            val lineColor = MaterialTheme.colorScheme.primary
            val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

            Canvas(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(140.dp),
            ) {
                val width = size.width
                val height = size.height

                // Draw horizontal reference gridlines
                val maxScale = 100.0
                val gridY20 = height - (20.0 / maxScale * height).toFloat()
                val gridY50 = height - (50.0 / maxScale * height).toFloat()

                drawLine(gridColor, Offset(0f, gridY20), Offset(width, gridY20), strokeWidth = 1.dp.toPx())
                drawLine(gridColor, Offset(0f, gridY50), Offset(width, gridY50), strokeWidth = 1.dp.toPx())

                if (history.size >= 2) {
                    val stepX = width / (history.size - 1)
                    val points =
                        history.mapIndexed { index, sample ->
                            val clampedRtt = sample.rttMs.coerceIn(0.0, maxScale)
                            val x = index * stepX
                            val y = height - (clampedRtt / maxScale * height).toFloat()
                            Offset(x, y)
                        }

                    val path =
                        Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }

                    val fillPath =
                        Path().apply {
                            addPath(path)
                            lineTo(points.last().x, height)
                            lineTo(points.first().x, height)
                            close()
                        }

                    drawPath(
                        path = fillPath,
                        brush =
                            Brush.verticalGradient(
                                colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                                startY = 0f,
                                endY = height,
                            ),
                    )

                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
                    )

                    points.forEach { point ->
                        drawCircle(
                            color = lineColor,
                            radius = 3.5.dp.toPx(),
                            center = point,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlsRow(
    status: PingStatus,
    onToggle: () -> Unit,
    onReset: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onToggle,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                painter =
                    painterResource(
                        if (status == PingStatus.MEASURING) R.drawable.ic_pause else R.drawable.ic_play_arrow,
                    ),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text =
                    if (status == PingStatus.MEASURING) {
                        stringResource(R.string.ping_pause)
                    } else {
                        stringResource(R.string.ping_start)
                    },
            )
        }

        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_reset),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.ping_reset))
        }
    }
}

@Composable
private fun NoGatewayCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_signal_wifi_off),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.ping_no_gateway),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
