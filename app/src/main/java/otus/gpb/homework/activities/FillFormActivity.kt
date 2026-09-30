package otus.gpb.homework.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

/**
 * Экран заполнения профиля. Запускается за результатом, поэтому наружу отдает
 * только фабрики Intent'ов - ключи extras остаются деталью реализации экрана.
 */
class FillFormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_form)

        val nameInput = findViewById<EditText>(R.id.edittext_name)
        val surnameInput = findViewById<EditText>(R.id.edittext_surname)
        val ageInput = findViewById<EditText>(R.id.edittext_age)

        findViewById<Button>(R.id.button_apply).setOnClickListener {
            val profile = Profile(
                name = nameInput.text.toString(),
                surname = surnameInput.text.toString(),
                age = ageInput.text.toString()
            )
            // Отдаем результат запустившей Activity и закрываемся.
            setResult(RESULT_OK, createResult(profile))
            finish()
        }
    }

    companion object {
        private const val EXTRA_NAME = "extra_name"
        private const val EXTRA_SURNAME = "extra_surname"
        private const val EXTRA_AGE = "extra_age"

        fun createIntent(context: Context): Intent =
            Intent(context, FillFormActivity::class.java)

        /**
         * Читает то, что форма положила в результат.
         */
        fun readProfile(data: Intent): Profile = Profile(
            name = data.getStringExtra(EXTRA_NAME).orEmpty(),
            surname = data.getStringExtra(EXTRA_SURNAME).orEmpty(),
            age = data.getStringExtra(EXTRA_AGE).orEmpty()
        )

        private fun createResult(profile: Profile): Intent = Intent()
            .putExtra(EXTRA_NAME, profile.name)
            .putExtra(EXTRA_SURNAME, profile.surname)
            .putExtra(EXTRA_AGE, profile.age)
    }
}
