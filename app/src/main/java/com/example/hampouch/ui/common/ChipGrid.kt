package com.example.hampouch.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val ChipGridSpacing = 10.dp

val ChipGridDefaultMinColumnWidth = 84.dp

@Composable
fun <T> ChipGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    maxColumns: Int = 3,
    minColumnWidth: Dp = ChipGridDefaultMinColumnWidth,
    chip: @Composable (T) -> Unit
) {
    val scaledMinColumnWidth = minColumnWidth * LocalDensity.current.fontScale.coerceAtLeast(1f)
    BoxWithConstraints(modifier = modifier) {
        val columns = ((maxWidth + ChipGridSpacing) / (scaledMinColumnWidth + ChipGridSpacing))
            .toInt()
            .coerceIn(1, maxColumns)
        Column(verticalArrangement = Arrangement.spacedBy(ChipGridSpacing)) {
            items.chunked(columns).forEach { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(ChipGridSpacing)
                ) {
                    rowItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) { chip(item) }
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
