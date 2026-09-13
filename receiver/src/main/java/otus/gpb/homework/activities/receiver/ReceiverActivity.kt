package otus.gpb.homework.activities.receiver

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import otus.gpb.homework.activities.receiver.databinding.ActivityReceiverBinding

class ReceiverActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReceiverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showPayload(intent)
    }

    /**
     * Читает три String-extra из пришедшего Intent и раскладывает их по вьюхам.
     *
     * Ключи extras — контракт с модулем sender, имена должны совпадать.
     * getStringExtra() возвращает null, если ключа нет, поэтому orEmpty():
     * Activity может быть открыта и без нужных данных.
     */
    private fun showPayload(intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()

        binding.titleTextView.text = title
        binding.yearTextView.text = intent.getStringExtra(EXTRA_YEAR).orEmpty()
        binding.descriptionTextView.text = intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty()
        binding.posterImageView.setImageDrawable(posterFor(title))
    }

    /**
     * Подбирает постер по названию фильма.
     * Возвращает null, если фильм неизвестен — ImageView останется пустым.
     */
    private fun posterFor(title: String): Drawable? {
        // Явно указана аннотация для большей консистенции контракта
        @DrawableRes val posterRes = when {
            title.equals(TITLE_NICE_GUYS, ignoreCase = true) -> R.drawable.niceguys
            title.equals(TITLE_INTERSTELLAR, ignoreCase = true) -> R.drawable.interstellar
            else -> return null
        }
        // Использован AppCompatResources, потому что проект на AppCompat и это даёт совместимость для векторных ресурсов»
        return AppCompatResources.getDrawable(this, posterRes)
    }

    private companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_YEAR = "year"
        const val EXTRA_DESCRIPTION = "description"

        const val TITLE_NICE_GUYS = "Славные парни"
        const val TITLE_INTERSTELLAR = "Интерстеллар"
    }
}
