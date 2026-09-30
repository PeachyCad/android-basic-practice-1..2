package otus.gpb.homework.activities

import android.content.Intent
import android.net.Uri

private const val MIME_TEXT = "text/plain"
private const val MIME_IMAGE = "image/*"

/**
 * Собирает ACTION_SEND с содержимым профиля: текст полей и выбранная картинка.
 *
 * Интент получается "чистым" - без setPackage, то есть пока неявным. Кому его
 * адресовать, решает вызывающая сторона.
 */
fun createProfileShareIntent(profile: Profile, photoUri: Uri?): Intent =
    Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, profile.asText())
        if (photoUri == null) {
            // Картинку из галереи еще не выбирали (у R.drawable.cat нет Uri) - отдаем только текст
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
