package com.example.hampouch.ui.hamtips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.R
import com.example.hampouch.data.model.MenuRatingInfo
import com.example.hampouch.data.model.MenuRatingType
import com.example.hampouch.data.model.TipPost
import com.example.hampouch.data.model.TipShareCategory
import com.example.hampouch.ui.dialog.ConfirmActionCard
import com.example.hampouch.ui.hamtips.components.HamTipsCategoryPickerRow
import com.example.hampouch.ui.hamtips.components.HamTipsFieldCard
import com.example.hampouch.ui.hamtips.components.HamTipsFieldLabel
import com.example.hampouch.ui.hamtips.components.HamTipsMaxPhotoCount
import com.example.hampouch.ui.hamtips.components.HamTipsSimpleTopBar
import com.example.hampouch.ui.hamtips.components.HamTipsStarRatingRow
import com.example.hampouch.ui.hamtips.components.HamTipsSubmitButton
import com.example.hampouch.ui.hamtips.components.HamTipsTitlePreviewBox
import com.example.hampouch.ui.hamtips.components.HamTipsPriceInputField
import com.example.hampouch.ui.hamtips.components.HamTipsWriteHeader
import com.example.hampouch.ui.hamtips.components.HamTipsWriteTextField
import com.example.hampouch.ui.hamtips.components.PhotoAttachGrid
import com.example.hampouch.ui.theme.HPGray2
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

fun formatMenuTitle(menuName: String, place: String, price: Int): String {
    val priceText = if (price > 0) "%,d원".format(price) else ""
    return listOf(menuName, place, priceText).filter { it.isNotBlank() }.joinToString(" · ")
}

@Composable
private fun HamTipsWriteScaffold(
    onBackClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = { HamTipsSimpleTopBar(title = stringResource(R.string.hamtips_title), onBackClick = onBackClick) },
        containerColor = HPGray2
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            content = content
        )
    }
}

@Composable
private fun HamTipsPostConfirmDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onCancel, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        HamTipsPostConfirmDialogContent(onCancel = onCancel, onConfirm = onConfirm)
    }
}

@Composable
private fun HamTipsPostConfirmDialogContent(onCancel: () -> Unit, onConfirm: () -> Unit) {
    ConfirmActionCard(
        question = stringResource(R.string.hamtips_post_confirm_question),
        confirmLabel = stringResource(R.string.hamtips_write_submit),
        onCancel = onCancel,
        onConfirm = onConfirm
    )
}

@Composable
fun HamTipsWriteTipScreen(
    editingPost: TipPost? = null,
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit
) {
    var category by remember {
        mutableStateOf(TipShareCategory.entries.find { it.category == editingPost?.category } ?: TipShareCategory.SHOPPING)
    }
    var title by remember { mutableStateOf(editingPost?.title.orEmpty()) }
    var content by remember { mutableStateOf(editingPost?.content.orEmpty()) }
    var photoUris by remember { mutableStateOf(editingPost?.imageUris ?: emptyList()) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val isSubmitEnabled = title.isNotBlank() && content.isNotBlank()

    HamTipsWriteScaffold(onBackClick = onBackClick) {
        HamTipsWriteHeader(
            overline = stringResource(R.string.hamtips_write_overline),
            heading = stringResource(R.string.hamtips_write_tip_heading),
            subheading = stringResource(R.string.hamtips_write_tip_subheading)
        )
        Spacer(modifier = Modifier.height(20.dp))
        HamTipsFieldCard {
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_section_board))
            Spacer(modifier = Modifier.height(10.dp))
            HamTipsCategoryPickerRow(selected = category, onSelected = { category = it })
            Spacer(modifier = Modifier.height(20.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_section_title))
            Spacer(modifier = Modifier.height(10.dp))
            HamTipsWriteTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = stringResource(R.string.hamtips_write_title_placeholder)
            )
            Spacer(modifier = Modifier.height(20.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_section_content))
            Spacer(modifier = Modifier.height(10.dp))
            HamTipsWriteTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = stringResource(R.string.hamtips_write_content_placeholder),
                minHeight = 160.dp,
                singleLine = false
            )
            Spacer(modifier = Modifier.height(20.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_section_photo))
            Spacer(modifier = Modifier.height(10.dp))
            PhotoAttachGrid(
                photoUris = photoUris,
                onPhotosAdded = { added -> photoUris = (photoUris + added).take(HamTipsMaxPhotoCount) },
                onPhotoRemoved = { removed -> photoUris = photoUris - removed }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        HamTipsSubmitButton(
            text = stringResource(R.string.hamtips_write_submit),
            enabled = isSubmitEnabled,
            onClick = { showConfirmDialog = true }
        )
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showConfirmDialog) {
        HamTipsPostConfirmDialog(
            onCancel = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                if (editingPost != null) {
                    HamTipsRepository.updateTipPost(editingPost.id, category.category, title, content, photoUris)
                } else {
                    HamTipsRepository.createTipPost(category.category, title, content, photoUris)
                }
                onSubmitted()
            }
        )
    }
}

@Composable
fun HamTipsWriteMenuScreen(
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit
) {
    var menuName by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var price by remember { mutableStateOf(0) }
    var taste by remember { mutableStateOf(0) }
    var costEffectiveness by remember { mutableStateOf(0) }
    var mood by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var photoUris by remember { mutableStateOf(emptyList<String>()) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val previewTitle = formatMenuTitle(menuName, place, price)
    val isSubmitEnabled = menuName.isNotBlank() && place.isNotBlank() && price > 0

    HamTipsWriteScaffold(onBackClick = onBackClick) {
        HamTipsWriteHeader(
            overline = stringResource(R.string.hamtips_write_overline),
            heading = stringResource(R.string.hamtips_write_menu_heading),
            subheading = stringResource(R.string.hamtips_write_menu_subheading)
        )
        Spacer(modifier = Modifier.height(20.dp))
        HamTipsFieldCard {
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_menu_title_preview_label))
            Spacer(modifier = Modifier.height(10.dp))
            HamTipsTitlePreviewBox(previewText = previewTitle)
            Spacer(modifier = Modifier.height(20.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_menu_name_label))
            Spacer(modifier = Modifier.height(10.dp))
            HamTipsWriteTextField(
                value = menuName,
                onValueChange = { menuName = it },
                placeholder = stringResource(R.string.hamtips_write_menu_name_placeholder)
            )
            Spacer(modifier = Modifier.height(20.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_menu_place_label))
            Spacer(modifier = Modifier.height(10.dp))
            HamTipsWriteTextField(
                value = place,
                onValueChange = { place = it },
                placeholder = stringResource(R.string.hamtips_write_menu_place_placeholder)
            )
            Spacer(modifier = Modifier.height(20.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_menu_price_label))
            Spacer(modifier = Modifier.height(10.dp))
            HamTipsPriceInputField(price = price, onPriceChange = { price = it })
            Spacer(modifier = Modifier.height(20.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_menu_rating_label))
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(HPWhite)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MenuRatingType.entries.forEach { type ->
                    val rating = when (type) {
                        MenuRatingType.TASTE -> taste
                        MenuRatingType.COST_EFFECTIVENESS -> costEffectiveness
                        MenuRatingType.MOOD -> mood
                    }
                    HamTipsStarRatingRow(
                        type = type,
                        rating = rating,
                        onRatingChange = { newRating ->
                            when (type) {
                                MenuRatingType.TASTE -> taste = newRating
                                MenuRatingType.COST_EFFECTIVENESS -> costEffectiveness = newRating
                                MenuRatingType.MOOD -> mood = newRating
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_menu_comment_label))
            Spacer(modifier = Modifier.height(10.dp))
            HamTipsWriteTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = stringResource(R.string.hamtips_write_menu_comment_placeholder),
                minHeight = 120.dp,
                singleLine = false
            )
            Spacer(modifier = Modifier.height(20.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_section_photo))
            Spacer(modifier = Modifier.height(10.dp))
            PhotoAttachGrid(
                photoUris = photoUris,
                onPhotosAdded = { added -> photoUris = (photoUris + added).take(HamTipsMaxPhotoCount) },
                onPhotoRemoved = { removed -> photoUris = photoUris - removed }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        HamTipsSubmitButton(
            text = stringResource(R.string.hamtips_write_submit),
            enabled = isSubmitEnabled,
            onClick = { showConfirmDialog = true }
        )
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showConfirmDialog) {
        HamTipsPostConfirmDialog(
            onCancel = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                HamTipsRepository.createMenuPost(
                    title = previewTitle,
                    menuName = menuName,
                    place = place,
                    price = price,
                    rating = MenuRatingInfo(taste = taste, costEffectiveness = costEffectiveness, mood = mood),
                    comment = comment,
                    imageUris = photoUris
                )
                onSubmitted()
            }
        )
    }
}

@Composable
fun HamTipsWriteBattleScreen(
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val isSubmitEnabled = title.isNotBlank() && content.isNotBlank() && link.isNotBlank()

    HamTipsWriteScaffold(onBackClick = onBackClick) {
        HamTipsWriteHeader(
            overline = stringResource(R.string.hamtips_write_overline),
            heading = stringResource(R.string.hamtips_write_battle_heading),
            subheading = stringResource(R.string.hamtips_write_battle_subheading)
        )
        Spacer(modifier = Modifier.height(20.dp))
        HamTipsFieldCard {
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_section_title))
            Spacer(modifier = Modifier.height(10.dp))
            HamTipsWriteTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = stringResource(R.string.hamtips_write_battle_title_placeholder)
            )
            Spacer(modifier = Modifier.height(20.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_section_content))
            Spacer(modifier = Modifier.height(10.dp))
            HamTipsWriteTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = stringResource(R.string.hamtips_write_battle_content_placeholder),
                minHeight = 160.dp,
                singleLine = false
            )
            Spacer(modifier = Modifier.height(20.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_battle_link_label))
            Spacer(modifier = Modifier.height(10.dp))
            HamTipsWriteTextField(
                value = link,
                onValueChange = { link = it },
                placeholder = stringResource(R.string.hamtips_write_battle_link_placeholder)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        HamTipsSubmitButton(
            text = stringResource(R.string.hamtips_write_submit),
            enabled = isSubmitEnabled,
            onClick = { showConfirmDialog = true }
        )
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showConfirmDialog) {
        HamTipsPostConfirmDialog(
            onCancel = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                HamTipsRepository.createBattlePost(title = title, content = content, link = link)
                onSubmitted()
            }
        )
    }
}

@Preview(showBackground = true, name = "1. 꿀팁 공유 - 빈 화면")
@Composable
private fun HamTipsWriteTipScreenEmptyPreview() {
    HampouchTheme {
        HamTipsWriteTipScreen(onBackClick = {}, onSubmitted = {})
    }
}

@Preview(showBackground = true, name = "2. 꿀팁 공유 - 사진 첨부")
@Composable
private fun HamTipsWriteTipScreenWithPhotosPreview() {
    HampouchTheme {
        HamTipsWriteTipScreen(
            editingPost = HamTipsMockData.allPosts().first { it.id == "hamtip_1" }.copy(
                imageUris = listOf("content://preview/sample_1", "content://preview/sample_2")
            ),
            onBackClick = {},
            onSubmitted = {}
        )
    }
}

@Preview(showBackground = true, name = "11. 메뉴 추천 - 빈 화면")
@Composable
private fun HamTipsWriteMenuScreenEmptyPreview() {
    HampouchTheme {
        HamTipsWriteMenuScreen(onBackClick = {}, onSubmitted = {})
    }
}

@Preview(showBackground = true, name = "15. 햄배틀 모집")
@Composable
private fun HamTipsWriteBattleScreenPreview() {
    HampouchTheme {
        HamTipsWriteBattleScreen(onBackClick = {}, onSubmitted = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0, name = "10. 게시글 등록 확인")
@Composable
private fun HamTipsPostConfirmDialogPreview() {
    HampouchTheme {
        HamTipsPostConfirmDialogContent(onCancel = {}, onConfirm = {})
    }
}
