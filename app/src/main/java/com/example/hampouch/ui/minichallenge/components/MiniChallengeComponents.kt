package com.example.hampouch.ui.minichallenge.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.R
import com.example.hampouch.ui.common.NotificationBellIcon
import com.example.hampouch.data.model.MiniChallengeEntry
import com.example.hampouch.data.model.RecommendedMiniChallenge
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPSub
import com.example.hampouch.ui.theme.HPSub1
import com.example.hampouch.ui.theme.HPSub2
import com.example.hampouch.ui.theme.HPSub3
import com.example.hampouch.ui.theme.HPSub4
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun MiniChallengeTopBar(
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.minichallenge_title)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(HPWhite)
            .padding(horizontal = 20.dp)
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = HPBlack
            )
        }
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = HPBlack,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        NotificationBellIcon(onClick = onNotificationClick)
    }
}

private val dateLabelFormatter = DateTimeFormatter.ofPattern("MM.dd")

@Composable
fun MiniChallengeDateRow(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        dates.forEach { date ->
            MiniChallengeDateChip(
                label = date.format(dateLabelFormatter),
                selected = date == selectedDate,
                enabled = !date.isAfter(today),
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@Composable
private fun MiniChallengeDateChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .then(if (selected) Modifier.background(HPMain) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = when {
                selected -> HPWhite
                !enabled -> HPGray5
                else -> HPText
            }
        )
    }
}

@Composable
fun MiniChallengeSummaryCard(
    completedCount: Int,
    totalCount: Int,
    streakDays: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPSub3)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.minichallenge_label),
                style = MaterialTheme.typography.labelLarge,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = HPSub
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.minichallenge_today_progress_title),
                style = MaterialTheme.typography.titleSmall,
                fontSize = 16.sp,
                color = HPBlack
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = completedCount.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = HPBlack
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(R.string.minichallenge_progress_total_format, totalCount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = HPBlack
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        val progress = if (totalCount > 0) completedCount / totalCount.toFloat() else 0f
        StreakRing(streakDays = streakDays, progress = progress)
    }
}

@Composable
private fun StreakRing(streakDays: Int, progress: Float, modifier: Modifier = Modifier) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400),
        label = "mini_challenge_ring_progress"
    )
    Box(
        modifier = modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 20.dp.toPx()
            val arcTopLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            drawArc(
                color = HPWhite,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            if (animatedProgress > 0f) {
                drawArc(
                    color = HPMain,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        Column(
            modifier = Modifier
                .size(73.dp)
                .clip(CircleShape)
                .background(HPSub4),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.minichallenge_streak_label),
                style = MaterialTheme.typography.labelMedium,
                color = HPSub1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = streakDays.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = HPSub1
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = stringResource(R.string.minichallenge_streak_unit),
                    style = MaterialTheme.typography.bodySmall,
                    color = HPSub1
                )
            }
        }
    }
}

private val MiniChallengeDeleteRevealWidth = 72.dp

@Composable
fun MiniChallengeItemRow(
    item: MiniChallengeEntry,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val revealPx = remember(density) { with(density) { -MiniChallengeDeleteRevealWidth.toPx() } }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(20.dp))
                .background(HPWhite),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(
                onClick = {
                    onDelete()
                    scope.launch { offsetX.animateTo(0f) }
                },
                modifier = Modifier.padding(end = 12.dp)
                    .background(color = Color.Red, shape = RoundedCornerShape(10.dp))

            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.minichallenge_delete),
                    tint = HPWhite
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            offsetX.snapTo((offsetX.value + delta).coerceIn(revealPx, 0f))
                        }
                    },
                    onDragStopped = {
                        val target = if (offsetX.value < revealPx / 2f) revealPx else 0f
                        scope.launch { offsetX.animateTo(target, animationSpec = tween(200)) }
                    }
                )
                .clip(RoundedCornerShape(20.dp))
                .background(if (item.isChecked) HPSub2 else HPWhite)
                .clickable(onClick = onToggle)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiniChallengeCheckbox(checked = item.isChecked)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = item.name, style = MaterialTheme.typography.bodyMedium, color = HPBlack)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = item.periodLabel(),
                style = MaterialTheme.typography.bodySmall,
                color = HPText
            )
        }
    }
}

@Composable
private fun MiniChallengeCheckbox(checked: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(34.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                id = if (checked) R.drawable.icon_checked else R.drawable.icon_unchecked
            ),
            contentDescription = if (checked) "Checked" else "Unchecked",
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
fun RecommendedMiniChallengeRow(
    items: List<RecommendedMiniChallenge>,
    onAddClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items = items, key = { it.id }) { item ->
            RecommendedMiniChallengeCard(item = item, onAddClick = { onAddClick(item.id) })
        }
    }
}

@Composable
private fun RecommendedMiniChallengeCard(
    item: RecommendedMiniChallenge,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .padding(15.dp)
    ) {
        Text(
            text = item.periodLabel,
            textAlign = TextAlign.End,
            modifier = modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = HPText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = HPBlack,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.minichallenge_add_button),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = HPText,
            modifier = Modifier.clickable(onClick = onAddClick)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniChallengeCreateTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                stringResource(R.string.minichallenge_title),
                style = MaterialTheme.typography.titleSmall,
                color = HPBlack
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    tint = HPBlack
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = HPWhite)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniChallengeNameField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(
                width = 2.dp,
                color = HPGray5,
                shape = RoundedCornerShape(10.dp)
            ),
        placeholder = {
            Text(
                text = stringResource(R.string.minichallenge_name_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = HPGray5
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = HPWhite,
            unfocusedContainerColor = HPWhite,
            focusedIndicatorColor = Color.Transparent,
            focusedTextColor = HPBlack,
            unfocusedTextColor = HPBlack
        )
    )
}

@Composable
fun MiniChallengeDurationRow(
    options: List<String>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEachIndexed { index, label ->
            MiniChallengeDurationChip(
                label = label,
                selected = index == selectedIndex,
                onClick = { onSelect(index) }
            )
        }
    }
}

@Composable
private fun MiniChallengeDurationChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .border(
                width = 2.dp,
                color = if(selected) HPMain else HPGray5,
                shape = RoundedCornerShape(50)
            )
            .background(if (selected) HPMain else HPWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) HPWhite else HPBlack
        )
    }
}

@Composable
fun MiniChallengeFilterTabRow(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        options.forEachIndexed { index, label ->
            MiniChallengeFilterTabChip(
                label = label,
                selected = index == selectedIndex,
                onClick = { onSelect(index) }
            )
        }
    }
}

@Composable
private fun MiniChallengeFilterTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .then(if (selected) Modifier.background(HPMain) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) HPWhite else HPBlack
        )
    }
}

@Composable
fun RecommendedMiniChallengeListCard(
    item: RecommendedMiniChallenge,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .padding(20.dp)
    ) {
        Text(text = item.periodLabel, style = MaterialTheme.typography.bodySmall, color = HPText)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = HPBlack
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.minichallenge_add_button),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = HPText,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAddClick)
        )
    }
}
