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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hampouch.R
import com.example.hampouch.domain.model.ExpenseRecord
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
import com.example.hampouch.ui.theme.HampouchTheme
import java.time.LocalDate

internal fun formatWon(amount: Int): String = "%,d".format(amount)
internal fun formatWon(amount: Long): String = "%,d".format(amount)

private const val MaxEditExpenseAmount = 10_000_000

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

@Preview(showBackground = true, name = "지출 상세 상단바")
@Composable
private fun ExpenseDetailTopBarPreview() {
    HampouchTheme {
        ExpenseDetailTopBar(onBackClick = {})
    }
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

@Preview(showBackground = true, name = "지출 삭제 버튼")
@Composable
private fun ExpenseDeleteIconButtonPreview() {
    HampouchTheme {
        ExpenseDeleteIconButton(onClick = {})
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

@Preview(showBackground = true, name = "카테고리 아이콘 원")
@Composable
private fun CategoryIconCirclePreview() {
    HampouchTheme {
        CategoryIconCircle(categoryId = "cafe", customCategoryName = null)
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

@Preview(showBackground = true, name = "사유 태그 알약")
@Composable
private fun ReasonTagPillPreview() {
    HampouchTheme {
        ReasonTagPill(label = "스트레스")
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

@Preview(showBackground = true, name = "점선 구분선")
@Composable
private fun DashedDividerPreview() {
    HampouchTheme {
        DashedDivider(modifier = Modifier.padding(16.dp))
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

@Preview(showBackground = true, name = "지출 정보 행")
@Composable
private fun ExpenseInfoRowPreview() {
    HampouchTheme {
        ExpenseInfoRow(label = "날짜", value = "2026년 8월 16일")
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

@Preview(showBackground = true, name = "지출 사진 보기 행")
@Composable
private fun ExpensePhotoViewRowPreview() {
    HampouchTheme {
        ExpensePhotoViewRow(photoUris = listOf(MockPhotoUri, MockPhotoUri))
    }
}

private val ChoiceChipMinHeight = 40.dp

sealed interface ChoiceChipIcon {
    data class Vector(val image: ImageVector, val tint: Color = HPText) : ChoiceChipIcon
    data class Resource(val id: Int) : ChoiceChipIcon
}

@Composable
fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ChoiceChipIcon? = null,
    iconSize: Dp = 16.dp,
    iconSpacing: Dp = 4.dp,
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
        when (icon) {
            is ChoiceChipIcon.Resource -> Image(
                painter = painterResource(icon.id),
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
            is ChoiceChipIcon.Vector -> Icon(
                imageVector = icon.image,
                contentDescription = null,
                tint = if (selected) HPWhite else icon.tint,
                modifier = Modifier.size(iconSize)
            )
            null -> Unit
        }
        if (icon != null) {
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

@Preview(showBackground = true, name = "선택 칩")
@Composable
private fun ChoiceChipPreview() {
    HampouchTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChoiceChip(label = "스트레스", selected = true, onClick = {})
            ChoiceChip(label = "보상", selected = false, onClick = {})
        }
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

@Preview(showBackground = true, name = "지출 텍스트 입력")
@Composable
private fun ExpenseTextFieldPreview() {
    HampouchTheme {
        ExpenseTextField(
            value = "",
            onValueChange = {},
            placeholder = "메모를 입력하세요",
            modifier = Modifier.padding(16.dp)
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
            val parsed = digitsOnly.toIntOrNull()
            when {
                digitsOnly.isEmpty() -> onAmountChange(0)
                parsed != null && parsed <= MaxEditExpenseAmount -> onAmountChange(parsed)
                else -> Unit
            }
        },
        placeholder = "0",
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Preview(showBackground = true, name = "금액 입력")
@Composable
private fun AmountTextFieldPreview() {
    HampouchTheme {
        AmountTextField(amount = 4500, onAmountChange = {}, modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun ExpenseFormSectionLabel(label: String, modifier: Modifier = Modifier) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = HPText, modifier = modifier)
}

@Composable
fun ExpenseFormSection(
    label: String,
    modifier: Modifier = Modifier,
    trailingAction: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    ExpenseFormSection(
        label = { ExpenseFormSectionLabel(label) },
        modifier = modifier,
        trailingAction = trailingAction,
        content = content
    )
}

@Composable
fun ExpenseFormSection(
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    trailingAction: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            label()
            Spacer(modifier = Modifier.weight(1f))
            trailingAction()
        }
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Preview(showBackground = true, name = "지출 폼 섹션")
@Composable
private fun ExpenseFormSectionPreview() {
    HampouchTheme {
        ExpenseFormSection(label = "카테고리", modifier = Modifier.padding(16.dp)) {
            Text("카페", style = MaterialTheme.typography.bodyMedium, color = HPBlack)
        }
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
    label: @Composable () -> Unit = {
        ExpenseFormSectionLabel(stringResource(R.string.expensedetail_field_photo))
    },
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
        label = label,
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

@Preview(showBackground = true, name = "지출 사진 편집 섹션")
@Composable
private fun ExpensePhotoEditSectionPreview() {
    HampouchTheme {
        ExpensePhotoEditSection(
            photoUris = listOf(MockPhotoUri, MockPhotoUri),
            onPhotosAdded = {},
            onPhotosRemoved = {},
            onPhotoReplaced = { _, _ -> },
            modifier = Modifier.padding(16.dp)
        )
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

@Preview(showBackground = true, name = "지출 요약 카드")
@Composable
private fun ExpenseSummaryCardPreview() {
    HampouchTheme {
        ExpenseSummaryCard(
            record = ExpenseRecord(
                id = "preview1",
                date = LocalDate.of(2026, 8, 16),
                amount = 4500,
                categoryId = "cafe",
                expenseName = "스타벅스",
                reasonId = "stress"
            ),
            modifier = Modifier.padding(16.dp)
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

@Preview(showBackground = true, name = "지출 날짜 필드")
@Composable
private fun ExpenseDateFieldPreview() {
    HampouchTheme {
        ExpenseDateField(label = "2026년 8월 16일", onClick = {}, modifier = Modifier.padding(16.dp))
    }
}
