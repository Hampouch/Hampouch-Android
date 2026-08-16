package com.example.hampouch.ui.hamtips.components

import android.graphics.drawable.BitmapDrawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.imageLoader
import coil.request.ImageRequest
import com.example.hampouch.R
import com.example.hampouch.ui.theme.HPBlack
import com.example.hampouch.ui.theme.HPGray4
import com.example.hampouch.ui.theme.HPGray5
import com.example.hampouch.ui.theme.HPMain
import com.example.hampouch.ui.theme.HPWhite
import com.example.hampouch.ui.theme.HampouchTheme

const val HamTipsMaxPhotoCount = 5

@Composable
fun rememberImageBitmapFromUri(uriString: String): ImageBitmap? {
    val context = LocalContext.current
    var bitmap by remember(uriString) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(uriString) {
        bitmap = try {
            val request = ImageRequest.Builder(context)
                .data(uriString)
                .allowHardware(false)
                .build()
            val result = context.imageLoader.execute(request)
            (result.drawable as? BitmapDrawable)?.bitmap?.asImageBitmap()
        } catch (error: Exception) {
            null
        }
    }
    return bitmap
}

@Composable
private fun rememberPhotoPickerLauncher(maxItems: Int, onPhotosPicked: (List<String>) -> Unit): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxItems.coerceAtLeast(2))
    ) { uris -> if (uris.isNotEmpty()) onPhotosPicked(uris.map { it.toString() }) }
    return { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
}

private fun Modifier.dashedBorder(color: Color, cornerRadius: Dp): Modifier = this.drawWithContent {
    drawContent()
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f))
    )
}

@Composable
private fun AttachedPhotoTile(uriString: String, onRemove: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(96.dp)) {
        val bitmap = rememberImageBitmapFromUri(uriString)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(HPGray5)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-6).dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(HPMain)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.hamtips_cd_remove_photo),
                tint = HPWhite,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun AddPhotoTile(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(96.dp)
            .dashedBorder(color = HPMain, cornerRadius = 16.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(R.string.hamtips_cd_add_photo),
            tint = HPMain,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun PhotoAttachGrid(
    photoUris: List<String>,
    onPhotosAdded: (List<String>) -> Unit,
    onPhotoRemoved: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxCount: Int = HamTipsMaxPhotoCount
) {
    val launcher = rememberPhotoPickerLauncher(
        maxItems = (maxCount - photoUris.size).coerceAtLeast(1),
        onPhotosPicked = onPhotosAdded
    )
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        photoUris.forEach { uriString ->
            AttachedPhotoTile(uriString = uriString, onRemove = { onPhotoRemoved(uriString) })
        }
        if (photoUris.size < maxCount) {
            AddPhotoTile(onClick = { launcher() })
        }
    }
}

@Preview(showBackground = true, name = "사진 첨부 그리드")
@Composable
private fun PhotoAttachGridPreview() {
    HampouchTheme {
        PhotoAttachGrid(
            photoUris = listOf("content://sample/photo1.jpg", "content://sample/photo2.jpg"),
            onPhotosAdded = {},
            onPhotoRemoved = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun PhotoFullScreenViewer(uriString: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HPBlack)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = rememberImageBitmapFromUri(uriString)
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = stringResource(R.string.hamtips_cd_photo_fullscreen),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "사진 전체화면 보기")
@Composable
private fun PhotoFullScreenViewerPreview() {
    HampouchTheme {
        PhotoFullScreenViewer(uriString = "content://sample/photo1.jpg", onDismiss = {})
    }
}

@Composable
fun HamTipsPhotoCarousel(photoUris: List<String>, modifier: Modifier = Modifier) {
    if (photoUris.isEmpty()) return
    var fullScreenUri by remember { mutableStateOf<String?>(null) }
    val pagerState = rememberPagerState(pageCount = { photoUris.size })

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) { page ->
            val uriString = photoUris[page]
            val bitmap = rememberImageBitmapFromUri(uriString)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(HPGray5)
                    .clickable { fullScreenUri = uriString }
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        if (photoUris.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(photoUris.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (index == pagerState.currentPage) HPMain else HPGray4)
                    )
                }
            }
        }
    }

    fullScreenUri?.let { uri ->
        PhotoFullScreenViewer(uriString = uri, onDismiss = { fullScreenUri = null })
    }
}

@Preview(showBackground = true, name = "한팁 사진 캐러셀")
@Composable
private fun HamTipsPhotoCarouselPreview() {
    HampouchTheme {
        HamTipsPhotoCarousel(
            photoUris = listOf("content://sample/photo1.jpg", "content://sample/photo2.jpg"),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun HamTipsFeedThumbnail(uriString: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(HPGray5)
    ) {
        val bitmap = uriString?.let { rememberImageBitmapFromUri(it) }
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true, name = "한팁 피드 썸네일")
@Composable
private fun HamTipsFeedThumbnailPreview() {
    HampouchTheme {
        HamTipsFeedThumbnail(
            uriString = "content://sample/photo1.jpg",
            modifier = Modifier.size(96.dp)
        )
    }
}
