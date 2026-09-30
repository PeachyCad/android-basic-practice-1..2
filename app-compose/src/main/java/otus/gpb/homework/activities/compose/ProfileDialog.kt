package otus.gpb.homework.activities.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * Какой диалог сейчас открыт. В Compose диалог - не объект, который "показывают",
 * а часть UI, которая есть или нет в зависимости от состояния.
 */
internal enum class ProfileDialog { None, PhotoSource, Rationale, Settings }

/**
 * Рисует диалог, соответствующий [dialog]. Закрыть диалог = перевести состояние в None.
 *
 * Любое действие в диалоге сначала закрывает его, поэтому вызывающей стороне
 * не нужно сбрасывать состояние в каждом колбэке. Если действие само откроет
 * следующий диалог (например, Rationale), оно просто перезапишет None.
 */
@Composable
internal fun ProfileDialogs(
    dialog: ProfileDialog,
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    onGrantPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    fun dismissThen(action: () -> Unit): () -> Unit = {
        onDismiss()
        action()
    }

    when (dialog) {
        ProfileDialog.None -> Unit
        ProfileDialog.PhotoSource ->
            PhotoSourceDialog(onDismiss, dismissThen(onTakePhoto), dismissThen(onPickPhoto))
        ProfileDialog.Rationale -> RationaleDialog(onDismiss, dismissThen(onGrantPermission))
        ProfileDialog.Settings -> SettingsDialog(onDismiss, dismissThen(onOpenSettings))
    }
}

/**
 * Simple dialog: список действий над фото профиля, без кнопок подтверждения.
 */
@Composable
private fun PhotoSourceDialog(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickPhoto: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_photo_title)) },
        text = {
            Column {
                DialogItem(stringResource(R.string.dialog_photo_take), onTakePhoto)
                DialogItem(stringResource(R.string.dialog_photo_pick), onPickPhoto)
            }
        },
        // Параметр обязательный, но у simple dialog кнопок нет - выбор делается кликом по пункту
        confirmButton = {}
    )
}

@Composable
private fun DialogItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    )
}

/**
 * Alert dialog с объяснением, зачем приложению камера.
 */
@Composable
private fun RationaleDialog(onDismiss: () -> Unit, onGrant: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_rationale_title)) },
        text = { Text(stringResource(R.string.dialog_rationale_message)) },
        confirmButton = {
            TextButton(onClick = onGrant) {
                Text(stringResource(R.string.dialog_rationale_grant))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_rationale_cancel))
            }
        }
    )
}

/**
 * Разрешение запрещено навсегда: поменять его можно только в настройках приложения.
 */
@Composable
private fun SettingsDialog(onDismiss: () -> Unit, onOpenSettings: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_settings_title)) },
        text = { Text(stringResource(R.string.dialog_settings_message)) },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.dialog_settings_open))
            }
        }
    )
}
