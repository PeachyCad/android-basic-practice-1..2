package otus.gpb.homework.activities.compose

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Аналог ImageView с id=imageview_photo: 120x160, centerCrop, кликабельный.
 */
@Composable
fun ProfilePhotoImage(
    photo: ProfilePhoto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val placeholder = painterResource(R.drawable.ic_baseline_add_photo_alternate_24)
    val painter: Painter = when (photo) {
        ProfilePhoto.Placeholder -> placeholder
        ProfilePhoto.Cat -> painterResource(R.drawable.cat)
        is ProfilePhoto.Picked -> {
            val bitmap by rememberPopulatedImage(photo.uri)
            // Пока картинка декодируется (или если Uri уже не читается) - заглушка
            bitmap?.let(::BitmapPainter) ?: placeholder
        }
    }

    Box(modifier = modifier.clickable(onClick = onClick)) {
        Image(
            painter = painter,
            contentDescription = stringResource(R.string.content_description_photo),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Compose-аналог populateImage: декодирует картинку из медиатеки.
 *
 * В View-версии декодирование шло прямо на главном потоке. Здесь produceState
 * запускает корутину, привязанную к композиции: декодируем на IO, а при смене
 * [uri] прошлая загрузка отменяется автоматически.
 */
@Composable
private fun rememberPopulatedImage(uri: Uri): State<ImageBitmap?> {
    val contentResolver = LocalContext.current.contentResolver
    return produceState<ImageBitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            // После смерти процесса временный доступ к Uri из пикера может пропасть
            runCatching {
                contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
            }.getOrNull()?.asImageBitmap()
        }
    }
}
