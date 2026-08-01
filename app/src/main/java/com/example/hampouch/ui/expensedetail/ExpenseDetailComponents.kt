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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.hampouch.R
import com.example.hampouch.ui.hamtips.components.rememberImageBitmapFromUri
import com.example.hampouch.ui.home.HomeCategoryCatalog
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray3
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter

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
    customCategoryName
        ?: HomeCategoryCatalog.byId(categoryId)?.let { stringResource(it.labelResId) }
        ?: stringResource(R.string.category_etc)

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
    Canvas(modifier = modifier.fillMaxWidth().height(1.dp)) {
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
        Spacer(modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = HPBlack)
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
                    .clip(RoundedCornerShape(14.dp))
                    .background(HPGray5)
            ) {
                if (painter != null) {
                    Image(
                        painter = painter,
                        contentDescription = stringResource(R.string.cd_expense_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(140.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun <T> ThreeColumnChipGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    chip: @Composable (T) -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) { chip(item) }
                }
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = HPText
) {
    val backgroundColor = if (selected) HPMain else HPWhite
    val contentColor = if (selected) HPWhite else HPBlack
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (selected) HPMain else HPGray4,
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) HPWhite else iconTint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor,
            maxLines = 1
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
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(HPWhite)
            .border(width = 1.dp, color = HPGray4, shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        if (value.isEmpty()) {
            Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = HPGray5)
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
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxItems.coerceAtLeast(1))
    ) { uris -> if (uris.isNotEmpty()) onPhotosPicked(uris.map { it.toString() }) }
    return { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
}

const val ExpensePhotoMaxCount = 5

@Composable
fun ExpensePhotoEditSection(
    photoUris: List<String>,
    onPhotosAdded: (List<String>) -> Unit,
    onPhotosRemoved: (Set<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectMode by remember { mutableStateOf(false) }
    var selectedUris by remember { mutableStateOf(setOf<String>()) }
    val pickerLauncher = rememberExpensePhotoPickerLauncher(
        maxItems = (ExpensePhotoMaxCount - photoUris.size).coerceAtLeast(1),
        onPhotosPicked = onPhotosAdded
    )

    ExpenseFormSection(
        label = stringResource(R.string.expensedetail_field_photo),
        modifier = modifier,
        trailingAction = {
            if (photoUris.isEmpty()) {
                AddPhotoTextButton(onClick = { pickerLauncher() })
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.expensedetail_photo_select_mode),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (selectMode) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectMode) HPMain else HPText,
                        modifier = Modifier.clickable {
                            selectMode = !selectMode
                            if (!selectMode) selectedUris = emptySet()
                        }
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        stringResource(R.string.expensedetail_photo_delete_mode),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selectMode && selectedUris.isNotEmpty()) HPMain else HPGray5,
                        modifier = Modifier.clickable(enabled = selectMode && selectedUris.isNotEmpty()) {
                            onPhotosRemoved(selectedUris)
                            selectedUris = emptySet()
                            selectMode = false
                        }
                    )
                }
            }
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            photoUris.forEach { uri ->
                EditablePhotoTile(
                    uri = uri,
                    selectMode = selectMode,
                    selected = uri in selectedUris,
                    onToggleSelected = {
                        selectedUris = if (uri in selectedUris) selectedUris - uri else selectedUris + uri
                    },
                    onChangeClick = { pickerLauncher() }
                )
            }
            if (photoUris.isNotEmpty() && photoUris.size < ExpensePhotoMaxCount) {
                AddPhotoTile(onClick = { pickerLauncher() })
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
        Icon(Icons.Filled.Add, contentDescription = null, tint = HPMain, modifier = Modifier.size(16.dp))
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
                modifier = Modifier.fillMaxWidth().height(130.dp)
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
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_add_photo), tint = HPText)
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
fun ExpenseSummaryCard(record: com.example.hampouch.data.model.ExpenseRecord, modifier: Modifier = Modifier) {
    val categoryLabel = resolveCategoryLabel(record.categoryId, record.customCategoryName)
    val reasonLabel = resolveReasonLabel(record.reasonId, record.customReason)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(HPWhite)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                record.expenseName ?: categoryLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HPBlack
            )
            Text(categoryLabel, style = MaterialTheme.typography.bodySmall, color = HPText)
        }
        if (reasonLabel != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(HPGray3)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(reasonLabel, style = MaterialTheme.typography.labelMedium, color = HPText)
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(
            stringResource(R.string.expensedetail_amount_won_format, formatWon(record.amount)),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = HPBlack
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
        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = HPText, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = HPBlack)
    }
}
