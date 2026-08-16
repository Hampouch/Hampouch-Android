package com.example.hampouch.ui.hamtips

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.hampouch.R
import com.example.hampouch.domain.model.TipCategory
import com.example.hampouch.domain.model.MenuRatingInfo
import com.example.hampouch.domain.model.MenuRatingType
import com.example.hampouch.domain.model.TipPost
import com.example.hampouch.domain.model.TipShareCategory
import com.example.hampouch.ui.dialog.ConfirmActionCard
import com.example.hampouch.core.config.BattleConfig
import com.example.hampouch.domain.model.HamBattleStatus
import com.example.hampouch.ui.hambattle.HamBattleViewModel
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
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPText
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme
import kotlinx.coroutines.launch

fun formatMenuTitle(menuName: String, place: String, price: Int): String {
    val priceText = if (price > 0) "%,d원".format(price) else ""
    return listOf(menuName, place, priceText).filter { it.isNotBlank() }.joinToString(" · ")
}

@Composable
private fun HamTipsWriteScaffold(
    onBackClick: () -> Unit,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = { HamTipsSimpleTopBar(title = stringResource(R.string.hamtips_title), onBackClick = onBackClick) },
        containerColor = HPGray2
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
            ) {
                content()
                if (footer != null) {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
            if (footer != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(HPGray2)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    footer()
                }
            }
        }
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
private fun HamTipsWriteHintSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.hamtips_write_hint_title),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = HPMain
        )
        Spacer(modifier = Modifier.height(8.dp))
        listOf(
            stringResource(R.string.hamtips_write_hint_bullet1),
            stringResource(R.string.hamtips_write_hint_bullet2),
            stringResource(R.string.hamtips_write_hint_bullet3)
        ).forEach { line ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "•", style = MaterialTheme.typography.labelSmall, color = HPText)
                Text(text = line, style = MaterialTheme.typography.labelSmall, color = HPText)
            }
        }
    }
}

@Composable
fun HamTipsWriteTipScreen(
    editingPost: TipPost? = null,
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit,
    viewModel: HamTipsWriteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HamTipsWriteEvent.Submitted -> onSubmitted()
                is HamTipsWriteEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    HamTipsWriteTipContent(
        editingPost = editingPost,
        onBackClick = onBackClick,
        onSubmit = { category, title, content, photoUris, photoKeys ->
            viewModel.submitTip(editingPost, category, title, content, photoUris, photoKeys)
        }
    )
}

@Composable
fun HamTipsWriteTipContent(
    editingPost: TipPost? = null,
    onBackClick: () -> Unit,
    onSubmit: (TipCategory, String, String, List<String>, List<String>) -> Unit
) {
    var category by remember {
        mutableStateOf(TipShareCategory.entries.find { it.category == editingPost?.category } ?: TipShareCategory.SHOPPING)
    }
    var title by remember { mutableStateOf(editingPost?.title.orEmpty()) }
    var content by remember { mutableStateOf(editingPost?.content.orEmpty()) }
    var photoUris by remember { mutableStateOf(editingPost?.imageUris ?: emptyList()) }
    var photoKeys by remember { mutableStateOf(editingPost?.imageKeys ?: emptyList()) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val isSubmitEnabled = title.isNotBlank() && content.isNotBlank()

    HamTipsWriteScaffold(onBackClick = onBackClick) {
        HamTipsWriteHeader(
            overline = stringResource(R.string.hamtips_write_overline),
            heading = stringResource(R.string.hamtips_write_tip_heading),
            subheading = stringResource(R.string.hamtips_write_tip_subheading)
        )
        Spacer(modifier = Modifier.height(15.dp))
        HamTipsFieldCard {
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_section_board))
            Spacer(modifier = Modifier.height(10.dp))
            HamTipsCategoryPickerRow(selected = category, onSelected = { category = it })
            Spacer(modifier = Modifier.height(10.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_section_title))
            Spacer(modifier = Modifier.height(10.dp))
            HamTipsWriteTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = stringResource(R.string.hamtips_write_title_placeholder)
            )
            Spacer(modifier = Modifier.height(15.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_section_content))
            Spacer(modifier = Modifier.height(10.dp))
            HamTipsWriteTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = stringResource(R.string.hamtips_write_content_placeholder),
                minHeight = 160.dp,
                singleLine = false
            )
            Spacer(modifier = Modifier.height(15.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_section_photo))
            Spacer(modifier = Modifier.height(10.dp))
            PhotoAttachGrid(
                photoUris = photoUris,
                onPhotosAdded = { added ->
                    photoUris = (photoUris + added).take(HamTipsMaxPhotoCount)
                    photoKeys = (photoKeys + added.map { "" }).take(HamTipsMaxPhotoCount)
                },
                onPhotoRemoved = { removed ->
                    val index = photoUris.indexOf(removed)
                    photoUris = photoUris - removed
                    if (index != -1) photoKeys = photoKeys.filterIndexed { i, _ -> i != index }
                }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        HamTipsWriteHintSection()
        Spacer(modifier = Modifier.height(25.dp))
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
                onSubmit(category.category, title, content, photoUris, photoKeys)
            }
        )
    }
}

@Composable
fun HamTipsWriteMenuScreen(
    editingPost: TipPost? = null,
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit,
    viewModel: HamTipsWriteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HamTipsWriteEvent.Submitted -> onSubmitted()
                is HamTipsWriteEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    HamTipsWriteMenuContent(
        editingPost = editingPost,
        onBackClick = onBackClick,
        onSubmit = { title, menuName, place, price, rating, comment, photoUris, photoKeys ->
            viewModel.submitMenu(
                editingPost, title, menuName, place, price, rating, comment, photoUris, photoKeys
            )
        }
    )
}

@Composable
fun HamTipsWriteMenuContent(
    editingPost: TipPost? = null,
    onBackClick: () -> Unit,
    onSubmit: (String, String, String, Int, MenuRatingInfo, String, List<String>, List<String>) -> Unit
) {
    var menuName by remember { mutableStateOf(editingPost?.menuName.orEmpty()) }
    var place by remember { mutableStateOf(editingPost?.place.orEmpty()) }
    var price by remember { mutableStateOf(editingPost?.price ?: 0) }
    var taste by remember { mutableStateOf(editingPost?.menuRating?.taste ?: 0) }
    var costEffectiveness by remember { mutableStateOf(editingPost?.menuRating?.costEffectiveness ?: 0) }
    var mood by remember { mutableStateOf(editingPost?.menuRating?.mood ?: 0) }
    var comment by remember { mutableStateOf(editingPost?.content.orEmpty()) }
    var photoUris by remember { mutableStateOf(editingPost?.imageUris ?: emptyList()) }
    var photoKeys by remember { mutableStateOf(editingPost?.imageKeys ?: emptyList()) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val previewTitle = formatMenuTitle(menuName, place, price)
    val isSubmitEnabled = menuName.isNotBlank() && place.isNotBlank() && price > 0

    HamTipsWriteScaffold(onBackClick = onBackClick) {
        HamTipsWriteHeader(
            overline = stringResource(R.string.hamtips_write_overline),
            heading = stringResource(R.string.hamtips_write_menu_heading),
            subheading = stringResource(R.string.hamtips_write_menu_subheading)
        )
        Spacer(modifier = Modifier.height(15.dp))
        HamTipsFieldCard {
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_menu_title_preview_label))
            Spacer(modifier = Modifier.height(5.dp))
            HamTipsTitlePreviewBox(previewText = previewTitle)
            Spacer(modifier = Modifier.height(15.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_menu_name_label))
            Spacer(modifier = Modifier.height(5.dp))
            HamTipsWriteTextField(
                value = menuName,
                onValueChange = { menuName = it },
                placeholder = stringResource(R.string.hamtips_write_menu_name_placeholder)
            )
            Spacer(modifier = Modifier.height(15.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_menu_place_label))
            Spacer(modifier = Modifier.height(5.dp))
            HamTipsWriteTextField(
                value = place,
                onValueChange = { place = it },
                placeholder = stringResource(R.string.hamtips_write_menu_place_placeholder)
            )
            Spacer(modifier = Modifier.height(15.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_menu_price_label))
            Spacer(modifier = Modifier.height(5.dp))
            HamTipsPriceInputField(price = price, onPriceChange = { price = it })
            Spacer(modifier = Modifier.height(15.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_menu_rating_label))
            Spacer(modifier = Modifier.height(5.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(HPWhite)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
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
            Spacer(modifier = Modifier.height(15.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_menu_comment_label))
            Spacer(modifier = Modifier.height(5.dp))
            HamTipsWriteTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = stringResource(R.string.hamtips_write_menu_comment_placeholder),
                minHeight = 120.dp,
                singleLine = false
            )
            Spacer(modifier = Modifier.height(15.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_section_photo))
            Spacer(modifier = Modifier.height(5.dp))
            PhotoAttachGrid(
                photoUris = photoUris,
                onPhotosAdded = { added ->
                    photoUris = (photoUris + added).take(HamTipsMaxPhotoCount)
                    photoKeys = (photoKeys + added.map { "" }).take(HamTipsMaxPhotoCount)
                },
                onPhotoRemoved = { removed ->
                    val index = photoUris.indexOf(removed)
                    photoUris = photoUris - removed
                    if (index != -1) photoKeys = photoKeys.filterIndexed { i, _ -> i != index }
                }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
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
                val rating = MenuRatingInfo(taste = taste, costEffectiveness = costEffectiveness, mood = mood)
                onSubmit(previewTitle, menuName, place, price, rating, comment, photoUris, photoKeys)
            }
        )
    }
}

@Composable
fun HamTipsWriteBattleScreen(
    editingPost: TipPost? = null,
    onBackClick: () -> Unit,
    onSubmitted: () -> Unit,
    initialLink: String = "",
    viewModel: HamTipsWriteViewModel = hiltViewModel(),
    battleViewModel: HamBattleViewModel = hiltViewModel()
) {
    val waitingChallengeLinks: List<String>? =
        if (BattleConfig.USE_SERVER_BATTLE) {
            null
        } else {
            battleViewModel.mockChallengesWith(HamBattleStatus.WAITING).mapNotNull { it.battleCode }
        }

    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HamTipsWriteEvent.Submitted -> onSubmitted()
                is HamTipsWriteEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    HamTipsWriteBattleContent(
        editingPost = editingPost,
        onBackClick = onBackClick,
        initialLink = initialLink,
        waitingChallengeLinks = waitingChallengeLinks,
        onSubmit = { title, content, link ->
            viewModel.submitBattle(editingPost, title, content, link)
        }
    )
}

@Composable
fun HamTipsWriteBattleContent(
    editingPost: TipPost? = null,
    onBackClick: () -> Unit,
    initialLink: String = "",
    waitingChallengeLinks: List<String>? = null,
    onSubmit: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(editingPost?.title.orEmpty()) }
    var content by remember { mutableStateOf(editingPost?.content.orEmpty()) }
    var link by remember { mutableStateOf(editingPost?.battleInfo?.link ?: initialLink) }
    var showLinkNotFoundError by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val isSubmitEnabled = title.isNotBlank() && content.isNotBlank() && link.isNotBlank()

    HamTipsWriteScaffold(
        onBackClick = onBackClick,
        footer = {
            HamTipsSubmitButton(
                text = stringResource(R.string.hamtips_write_submit),
                enabled = isSubmitEnabled,
                onClick = {
                    if (waitingChallengeLinks == null || link.trim() in waitingChallengeLinks) {
                        showLinkNotFoundError = false
                        showConfirmDialog = true
                    } else {
                        showLinkNotFoundError = true
                    }
                }
            )
        }
    ) {
        HamTipsWriteHeader(
            overline = stringResource(R.string.hamtips_write_overline),
            heading = stringResource(R.string.hamtips_write_battle_heading),
            subheading = stringResource(R.string.hamtips_write_battle_subheading)
        )
        Spacer(modifier = Modifier.height(15.dp))
        HamTipsFieldCard {
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_section_title))
            Spacer(modifier = Modifier.height(5.dp))
            HamTipsWriteTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = stringResource(R.string.hamtips_write_battle_title_placeholder)
            )
            Spacer(modifier = Modifier.height(15.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_section_content))
            Spacer(modifier = Modifier.height(5.dp))
            HamTipsWriteTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = stringResource(R.string.hamtips_write_battle_content_placeholder),
                minHeight = 160.dp,
                singleLine = false
            )
            Spacer(modifier = Modifier.height(15.dp))
            HamTipsFieldLabel(stringResource(R.string.hamtips_write_battle_link_label))
            Spacer(modifier = Modifier.height(5.dp))
            HamTipsWriteTextField(
                value = link,
                onValueChange = {
                    link = it
                    showLinkNotFoundError = false
                },
                placeholder = stringResource(R.string.hamtips_write_battle_link_placeholder)
            )
            if (showLinkNotFoundError) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.hamtips_write_battle_link_not_found),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }

    if (showConfirmDialog) {
        HamTipsPostConfirmDialog(
            onCancel = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                onSubmit(title, content, link.trim())
            }
        )
    }
}

@Preview(showBackground = true, name = "1. 꿀팁 공유 - 빈 화면")
@Composable
private fun HamTipsWriteTipScreenEmptyPreview() {
    HampouchTheme {
        HamTipsWriteTipContent(onBackClick = {}, onSubmit = { _, _, _, _, _ -> })
    }
}

@Preview(showBackground = true, name = "2. 꿀팁 공유 - 사진 첨부")
@Composable
private fun HamTipsWriteTipScreenWithPhotosPreview() {
    HampouchTheme {
        HamTipsWriteTipContent(
            editingPost = HamTipsMockData.allPosts().first { it.id == "hamtip_1" }.copy(
                imageUris = listOf("content://preview/sample_1", "content://preview/sample_2")
            ),
            onBackClick = {},
            onSubmit = { _, _, _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "11. 메뉴 추천 - 빈 화면")
@Composable
private fun HamTipsWriteMenuScreenEmptyPreview() {
    HampouchTheme {
        HamTipsWriteMenuContent(onBackClick = {}, onSubmit = { _, _, _, _, _, _, _, _ -> })
    }
}

@Preview(showBackground = true, name = "15. 햄배틀 모집")
@Composable
private fun HamTipsWriteBattleScreenPreview() {
    HampouchTheme {
        HamTipsWriteBattleContent(onBackClick = {}, onSubmit = { _, _, _ -> })
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFB0B0B0, name = "10. 게시글 등록 확인")
@Composable
private fun HamTipsPostConfirmDialogPreview() {
    HampouchTheme {
        HamTipsPostConfirmDialogContent(onCancel = {}, onConfirm = {})
    }
}
