package com.example.hampouch.ui.expensedetail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.R
import com.example.hampouch.ui.common.ReasonTagAndAmountColumn
import com.example.hampouch.ui.hamtips.components.rememberImageBitmapFromUri
import com.example.hampouch.ui.home.HomeCategoryCatalog
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray3
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite

internal fun formatWon(amount: Int): String = "%,d".format(amount)

private val MockDrawableRes = mapOf(
    "img_hamster_chubby" to R.drawable.img_hamster_chubby
)

@Composable
private fun rememberExpensePhotoPainter(uriString: String): Painter? {
    if (uriString.startsWith("drawable://")) {
        val resId = MockDrawableRes[uriString.removePrefix("drawable://")] ?: return null
        return painterResource(resId)
    }
    val bitmap: ImageBitmap? = rememberImageBitmapFromUri(uriString)
    return bitmap?.let { BitmapPainter(it) }
}

@Composable
fun resolveCategoryIcon(categoryId: String?): ImageVector =
    HomeCategoryCatalog.byId(categoryId)?.icon ?: HomeCategoryCatalog.defaultIcon

@Composable
fun resolveCategoryColor(categoryId: String?): Color =
    HomeCategoryCatalog.byId(categoryId)?.accentColor ?: HomeCategoryCatalog.defaultColor

@Composable
fun resolveCategoryLabel(categoryId: String?, customCategoryName: String?): String =
    resolveCategoryLabelOrNull(categoryId, customCategoryName)
        ?: stringResource(R.string.category_etc)

/**
 * 카테고리를 고르지 않았으면 null. "기타"는 사용자가 명시적으로 고른 경우에만 나온다.
 * 라벨을 생략할 수 있는 화면(홈 카드, 저장 확인)에서 쓴다.
 */
@Composable
fun resolveCategoryLabelOrNull(categoryId: String?, customCategoryName: String?): String? =
    customCategoryName ?: HomeCategoryCatalog.byId(categoryId)?.let { stringResource(it.labelResId) }

@Composable
fun resolveReasonLabel(reasonId: String?, customReason: String?): String? =
    customReason ?: ExpenseReasonCatalog.byId(reasonId)?.let { stringResource(it.labelResId) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingAction: @Composable () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                stringResource(R.string.expensedetail_title),
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
        actions = { trailingAction() },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = HPWhite)
    )
}

@Composable
fun ExpenseDeleteIconButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            Icons.Filled.DeleteOutline,
            contentDescription = stringResource(R.string.cd_delete_expense),
            tint = HPBlack
        )
    }
}

@Composable
fun CategoryIconCircle(
    categoryId: String?,
    customCategoryName: String?,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(HPGray3),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = resolveCategoryIcon(categoryId),
            contentDescription = null,
            tint = resolveCategoryColor(categoryId),
            modifier = Modifier.size(size * 0.4f)
        )
    }
}

@Composable
fun ReasonTagPill(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(HPMain)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = HPWhite)
    }
}

@Composable
fun DashedDivider(modifier: Modifier = Modifier, color: Color = HPGray4) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(color, Offset.Zero, Offset(size.width, 0f), pathEffect = dash)
    }
}

@Composable
fun ExpenseInfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = HPText)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = HPBlack,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ExpensePhotoViewRow(photoUris: List<String>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        photoUris.forEach { uri ->
            val painter = rememberExpensePhotoPainter(uri)
            Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 140.dp)
                    .background(HPGray5)
            ) {
                if (painter != null) {
                    Image(
                        painter = painter,
                        contentDescription = stringResource(R.string.cd_expense_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            }
        }
    }
}

private val ChoiceChipMinHeight = 40.dp

@Composable
fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    iconTint: Color = HPText,
    iconSize: Dp = 16.dp,
    iconSpacing: Dp = 4.dp,
    /** 기본값은 제한 없음. 사용자가 입력한 문자열을 라벨로 쓰는 칩에서만 줄 수를 제한한다. */
    maxLines: Int = Int.MAX_VALUE
) {
    val backgroundColor = if (selected) HPMain else HPWhite
    val contentColor = if (selected) HPWhite else HPBlack
    Row(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .heightIn(min = ChoiceChipMinHeight)
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (selected) HPMain else HPGray4,
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconRes != null) {
            Image(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(iconSpacing))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) HPWhite else iconTint,
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(iconSpacing))
        }
        Text(
            label,
            modifier = Modifier.weight(1f, fill = false),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ExpenseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    showBorder: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(HPWhite)
            .then(
                if (showBorder) {
                    Modifier.border(
                        width = 1.dp,
                        color = HPGray4,
                        shape = RoundedCornerShape(10.dp)
                    )
                } else {
                    Modifier
                }
            )
            .padding(contentPadding)
    ) {
        if (value.isEmpty()) {
            Text(
                placeholder,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyMedium,
                color = HPGray5
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = keyboardOptions,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = HPBlack),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun AmountTextField(
    amount: Int,
    onAmountChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ExpenseTextField(
        value = if (amount == 0) "" else formatWon(amount),
        onValueChange = { raw ->
            val digitsOnly = raw.filter { it.isDigit() }
            onAmountChange(digitsOnly.toIntOrNull() ?: 0)
        },
        placeholder = "0",
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
fun ExpenseFormSection(
    label: String,
    modifier: Modifier = Modifier,
    trailingAction: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = HPText)
            Spacer(modifier = Modifier.weight(1f))
            trailingAction()
        }
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun rememberExpensePhotoPickerLauncher(
    maxItems: Int,
    onPhotosPicked: (List<String>) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxItems.coerceAtLeast(2))
    ) { uris -> if (uris.isNotEmpty()) onPhotosPicked(uris.map { it.toString() }) }
    return { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
}

@Composable
private fun rememberExpenseSinglePhotoPickerLauncher(
    onPhotoPicked: (String) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { onPhotoPicked(it.toString()) } }
    return { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
}

const val ExpensePhotoMaxCount = 5

@Composable
fun ExpensePhotoEditSection(
    photoUris: List<String>,
    onPhotosAdded: (List<String>) -> Unit,
    onPhotosRemoved: (Set<Int>) -> Unit,
    onPhotoReplaced: (index: Int, newUri: String) -> Unit,
    modifier: Modifier = Modifier,
    maxCount: Int = ExpensePhotoMaxCount
) {
    var selectMode by remember { mutableStateOf(false) }
    var selectedIndices by remember { mutableStateOf(setOf<Int>()) }
    var changeTargetIndex by remember { mutableStateOf(-1) }
    val addLauncher = rememberExpensePhotoPickerLauncher(
        maxItems = (maxCount - photoUris.size).coerceAtLeast(1),
        onPhotosPicked = onPhotosAdded
    )
    val changeLauncher = rememberExpenseSinglePhotoPickerLauncher(
        onPhotoPicked = { newUri ->
            if (changeTargetIndex in photoUris.indices) {
                onPhotoReplaced(changeTargetIndex, newUri)
            }
        }
    )

    ExpenseFormSection(
        label = stringResource(R.string.expensedetail_field_photo),
        modifier = modifier,
        trailingAction = {
            if (photoUris.isEmpty()) {
                AddPhotoTextButton(onClick = { addLauncher() })
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.expensedetail_photo_select_mode),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (selectMode) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectMode) HPMain else HPText,
                        modifier = Modifier.clickable {
                            selectMode = !selectMode
                            if (!selectMode) selectedIndices = emptySet()
                        }
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        stringResource(R.string.expensedetail_photo_delete_mode),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectMode && selectedIndices.isNotEmpty()) HPMain else HPGray5,
                        modifier = Modifier.clickable(enabled = selectMode && selectedIndices.isNotEmpty()) {
                            onPhotosRemoved(selectedIndices)
                            selectedIndices = emptySet()
                            selectMode = false
                        }
                    )
                }
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            photoUris.forEachIndexed { index, uri ->
                EditablePhotoTile(
                    uri = uri,
                    selectMode = selectMode,
                    selected = index in selectedIndices,
                    onToggleSelected = {
                        selectedIndices =
                            if (index in selectedIndices) selectedIndices - index else selectedIndices + index
                    },
                    onChangeClick = {
                        changeTargetIndex = index
                        changeLauncher()
                    }
                )
            }
            if (photoUris.isNotEmpty() && photoUris.size < maxCount) {
                AddPhotoTile(onClick = { addLauncher() })
            }
        }
    }
}

@Composable
private fun AddPhotoTextButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = null,
            tint = HPMain,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            stringResource(R.string.expensedetail_photo_add),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = HPMain
        )
    }
}

@Composable
private fun EditablePhotoTile(
    uri: String,
    selectMode: Boolean,
    selected: Boolean,
    onToggleSelected: () -> Unit,
    onChangeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val painter = rememberExpensePhotoPainter(uri)
    Box(
        modifier = modifier
            .size(width = 140.dp, height = 130.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(HPGray5)
            .clickable { if (selectMode) onToggleSelected() else onChangeClick() }
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = stringResource(R.string.cd_expense_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )
        }
        if (!selectMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(50))
                    .background(HPBlack.copy(alpha = 0.45f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    stringResource(R.string.expensedetail_photo_change),
                    style = MaterialTheme.typography.labelMedium,
                    color = HPWhite
                )
            }
        } else {
            Image(
                painter = painterResource(if (selected) R.drawable.icon_checked else R.drawable.icon_unchecked),
                contentDescription = if (selected) {
                    stringResource(R.string.cd_remove_photo)
                } else null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .size(22.dp)
            )
        }
    }
}

@Composable
private fun AddPhotoTile(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 100.dp, height = 130.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = HPGray4,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Add,
                contentDescription = stringResource(R.string.cd_add_photo),
                tint = HPText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.expensedetail_photo_add),
                style = MaterialTheme.typography.labelMedium,
                color = HPText
            )
        }
    }
}

@Composable
fun ExpenseSummaryCard(
    record: com.example.hampouch.domain.model.ExpenseRecord,
    modifier: Modifier = Modifier
) {
    val categoryLabel = resolveCategoryLabelOrNull(record.categoryId, record.customCategoryName)
    val reasonLabel = resolveReasonLabel(record.reasonId, record.customReason)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(resolveCategoryColor(record.categoryId).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = resolveCategoryIcon(record.categoryId),
                    contentDescription = null,
                    tint = resolveCategoryColor(record.categoryId),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                if (record.expenseName != null) {
                    Text(
                        record.expenseName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = HPBlack,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (categoryLabel != null) {
                    Text(
                        categoryLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = HPText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        ReasonTagAndAmountColumn(
            reasonTag = reasonLabel,
            amountText = stringResource(R.string.expensedetail_amount_won_format, formatWon(record.amount))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDateField(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(HPWhite)
            .border(width = 1.dp, color = HPGray4, shape = RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.CalendarMonth,
            contentDescription = null,
            tint = HPText,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = HPBlack)
    }
}
