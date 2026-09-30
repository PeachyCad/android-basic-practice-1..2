package otus.gpb.homework.activities.compose

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import otus.gpb.homework.activities.compose.ui.ActivitiesTheme
import otus.gpb.homework.activities.compose.ui.AppTopBar

private const val CAMERA_PERMISSION = Manifest.permission.CAMERA

class EditProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ActivitiesTheme {
                EditProfileRoute()
            }
        }
    }
}

/**
 * Состояние экрана и вся работа с Result API. Сама верстка - в [EditProfileScreen],
 * она ничего не знает про Activity и поэтому рисуется в Preview.
 */
@Composable
private fun EditProfileRoute() {
    val context = LocalContext.current
    val activity = context.findActivity()

    var profile by rememberSaveable { mutableStateOf(Profile()) }
    var photo by rememberSaveable { mutableStateOf<ProfilePhoto>(ProfilePhoto.Placeholder) }
    var dialog by rememberSaveable { mutableStateOf(ProfileDialog.None) }

    // rememberLauncherForActivityResult - Compose-обертка над registerForActivityResult:
    // регистрирует контракт в ActivityResultRegistry и переживает пересоздание Activity.
    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        when {
            // 1. Разрешение выдано
            isGranted -> photo = ProfilePhoto.Cat
            // 2. Первый отказ: система еще разрешает показать объяснение -> ничего не делаем
            activity.shouldShowRequestPermissionRationale(CAMERA_PERMISSION) -> Unit
            // 4. Повторный отказ ("больше не спрашивать") -> остаются только настройки
            else -> dialog = ProfileDialog.Settings
        }
    }

    // В колбэк приходит null, если пользователь закрыл пикер, ничего не выбрав
    val pickPhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { photo = ProfilePhoto.Picked(it) } }

    // null - форму закрыли кнопкой "назад", показывать нечего
    val fillForm = rememberLauncherForActivityResult(
        FillFormActivity.Contract()
    ) { result -> result?.let { profile = it } }

    EditProfileScreen(
        profile = profile,
        photo = photo,
        onPhotoClick = { dialog = ProfileDialog.PhotoSource },
        onEditClick = { fillForm.launch(Unit) },
        onSendClick = {
            context.openSenderApp(profile, (photo as? ProfilePhoto.Picked)?.uri)
        }
    )

    ProfileDialogs(
        dialog = dialog,
        onDismiss = { dialog = ProfileDialog.None },
        onTakePhoto = {
            when {
                // Разрешение уже есть - запрашивать нечего
                context.hasCameraPermission() -> photo = ProfilePhoto.Cat
                // 3. Запрос после отмены: сначала объясняем, зачем нам камера
                activity.shouldShowRequestPermissionRationale(CAMERA_PERMISSION) ->
                    dialog = ProfileDialog.Rationale
                // Спрашиваем в первый раз (либо разрешение запрещено навсегда - это увидим в колбэке)
                else -> requestCameraPermission.launch(CAMERA_PERMISSION)
            }
        },
        onPickPhoto = {
            pickPhoto.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onGrantPermission = { requestCameraPermission.launch(CAMERA_PERMISSION) },
        onOpenSettings = { context.openAppSettings() }
    )
}

@Composable
private fun EditProfileScreen(
    profile: Profile,
    photo: ProfilePhoto,
    onPhotoClick: () -> Unit,
    onEditClick: () -> Unit,
    onSendClick: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.title_profile),
                actions = {
                    IconButton(onClick = onSendClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_baseline_send_24),
                            contentDescription = stringResource(R.string.title_send)
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        Row(
            modifier = Modifier
                .padding(contentPadding)
                .padding(start = 16.dp, top = 16.dp)
        ) {
            ProfilePhotoImage(
                photo = photo,
                onClick = onPhotoClick,
                modifier = Modifier.size(width = 120.dp, height = 160.dp)
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                ProfileField(value = profile.name, hint = stringResource(R.string.hint_name))
                ProfileField(value = profile.surname, hint = stringResource(R.string.hint_surname))
                ProfileField(value = profile.age, hint = stringResource(R.string.hint_age))
                TextButton(
                    onClick = onEditClick,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(stringResource(R.string.button_edit_profile))
                }
            }
        }
    }
}

/**
 * Аналог TextView с android:hint: пока значения нет, показываем подсказку бледным цветом.
 */
@Composable
private fun ProfileField(value: String, hint: String) {
    Text(
        text = value.ifEmpty { hint },
        color = if (value.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else Color.Unspecified,
        fontSize = 18.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED

private fun Context.openAppSettings() {
    startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
    )
}

/**
 * Разрешения и shouldShowRequestPermissionRationale - API Activity, а LocalContext
 * может оказаться оберткой над ней (например, ContextThemeWrapper). Разворачиваем.
 */
private tailrec fun Context.findActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> error("Composable is hosted outside of an Activity")
}

@Preview(showBackground = true)
@Composable
private fun EditProfileScreenPreview() {
    ActivitiesTheme {
        EditProfileScreen(
            profile = Profile(name = "Иван", surname = "Иванов", age = "30"),
            photo = ProfilePhoto.Cat,
            onPhotoClick = {},
            onEditClick = {},
            onSendClick = {}
        )
    }
}
