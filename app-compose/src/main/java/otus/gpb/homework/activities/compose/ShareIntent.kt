package otus.gpb.homework.activities.compose

import android.content.Context
import android.content.Intent
import android.net.Uri

private const val MIME_TEXT = "text/plain"
private const val MIME_IMAGE = "image/*"

/**
 * Официальный клиент и его сборка из Google Play с другим package.
 */
private val TELEGRAM_PACKAGES = listOf(
    "org.telegram.messenger",
    "org.telegram.messenger.web"
)

/**
 * Отправка профиля в другое приложение.
 *
 * Основной путь по заданию - ЯВНЫЙ интент в Telegram: у ACTION_SEND проставлен
 * setPackage, поэтому система не показывает диалог выбора, а отдает данные
 * конкретному приложению.
 *
 * Если Telegram не установлен, явный интент упал бы с ActivityNotFoundException,
 * поэтому откатываемся на НЕЯВНЫЙ интент через createChooser.
 */
fun Context.openSenderApp(profile: Profile, photoUri: Uri?) {
    val shareIntent = createProfileShareIntent(profile, photoUri)
    val telegramPackage = TELEGRAM_PACKAGES.firstOrNull { candidate ->
        // resolveActivity вернет null, если приложения с таким package на
        // устройстве нет либо оно не умеет обрабатывать наш ACTION_SEND
        Intent(shareIntent).setPackage(candidate).resolveActivity(packageManager) != null
    }

    if (telegramPackage == null) {
        val title = getString(R.string.share_chooser_title)
        startActivity(Intent.createChooser(shareIntent, title))
    } else {
        startActivity(shareIntent.setPackage(telegramPackage))
    }
}

/**
 * Собирает ACTION_SEND с содержимым профиля: текст полей и картинка из галереи.
 */
private fun createProfileShareIntent(profile: Profile, photoUri: Uri?): Intent =
    Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, profile.asText())
        if (photoUri == null) {
            // Картинку из галереи еще не выбирали (у R.drawable.cat нет Uri) - шлем только текст
            type = MIME_TEXT
        } else {
            putExtra(Intent.EXTRA_STREAM, photoUri)
            // Право на чтение Uri выдано нам, получателю его нужно передать явно
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = MIME_IMAGE
        }
    }

private fun Profile.asText(): String = listOf(name, surname, age)
    .filter { it.isNotBlank() }
    .joinToString(separator = "\n")
