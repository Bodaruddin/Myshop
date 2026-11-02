package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun BarcodeView(
    code: String,
    modifier: Modifier = Modifier,
    showCodeText: Boolean = true,
    lightBackground: Boolean = true
) {
    val effectiveCode = if (code.isBlank()) "890000000000" else code

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (lightBackground) Color.White else Color(0xFF1E293B))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            val barColor = if (lightBackground) Color.Black else Color.White
            val numBars = 45
            val barWidth = size.width / (numBars * 1.5f)
            val seed = effectiveCode.hashCode()

            var x = 0f
            for (i in 0 until numBars) {
                // Generate realistic looking barcode pattern from hash
                val isThick = ((seed shr (i % 31)) and 1) == 1
                val isGuard = i in 0..2 || i in 21..23 || i in (numBars - 3)..<numBars
                val actualWidth = if (isThick) barWidth * 2.2f else barWidth
                val barHeight = if (isGuard) size.height else size.height * 0.85f

                if (i % 2 == 0 || isGuard) {
                    drawRect(
                        color = barColor,
                        topLeft = Offset(x, 0f),
                        size = Size(actualWidth, barHeight)
                    )
                }
                x += actualWidth + (barWidth * 0.5f)
                if (x >= size.width) break
            }
        }

        if (showCodeText) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = effectiveCode,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (lightBackground) Color.Black else Color(0xFFE2E8F0),
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun QrCodePreview(
    content: String,
    modifier: Modifier = Modifier,
    sizeDp: Int = 120
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size((sizeDp - 16).dp)) {
            val grid = 17
            val cellSize = size.width / grid
            val hash = abs(content.hashCode())

            // Draw QR modules
            for (r in 0 until grid) {
                for (c in 0 until grid) {
                    // Position detection corners
                    val isCornerTopLeft = (r in 0..5 && c in 0..5)
                    val isCornerTopRight = (r in 0..5 && c in (grid - 6) until grid)
                    val isCornerBottomLeft = (r in (grid - 6) until grid && c in 0..5)

                    val isBlack = if (isCornerTopLeft || isCornerTopRight || isCornerBottomLeft) {
                        val localR = if (isCornerBottomLeft) r - (grid - 6) else r
                        val localC = if (isCornerTopRight) c - (grid - 6) else c
                        (localR == 0 || localR == 5 || localC == 0 || localC == 5) || (localR in 2..3 && localC in 2..3)
                    } else {
                        ((hash shr ((r * 7 + c) % 31)) and 1) == 1
                    }

                    if (isBlack) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize, cellSize)
                        )
                    }
                }
            }
        }
    }
}
