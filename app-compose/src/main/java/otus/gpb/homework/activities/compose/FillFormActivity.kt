package otus.gpb.homework.activities.compose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import otus.gpb.homework.activities.compose.ui.ActivitiesTheme
import otus.gpb.homework.activities.compose.ui.AppTopBar

/**
 * Экран заполнения профиля. Запускается за результатом через [Contract],
 * поэтому ключ extras остается деталью реализации экрана.
 */
class FillFormActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ActivitiesTheme {
                FillFormScreen(
                    onApply = { profile ->
                        // Отдаем результат запустившей Activity и закрываемся.
                        setResult(RESULT_OK, Intent().putExtra(EXTRA_PROFILE, profile))
                        finish()
                    }
                )
            }
        }
    }

    /**
     * Типизированный контракт Result API: на входе ничего, на выходе профиль
     * или null, если форму закрыли кнопкой "назад".
     *
     * Та же пара createIntent/readProfile из View-версии, только упакованная в
     * контракт - вызывающей стороне не нужно разбирать resultCode и Intent самой.
     */
    class Contract : ActivityResultContract<Unit, Profile?>() {

        override fun createIntent(context: Context, input: Unit): Intent =
            Intent(context, FillFormActivity::class.java)

        override fun parseResult(resultCode: Int, intent: Intent?): Profile? =
            intent
                ?.takeIf { resultCode == RESULT_OK }
                ?.let { IntentCompat.getParcelableExtra(it, EXTRA_PROFILE, Profile::class.java) }
    }

    private companion object {
        private const val EXTRA_PROFILE = "extra_profile"
    }
}

@Composable
private fun FillFormScreen(onApply: (Profile) -> Unit) {
    // rememberSaveable, а не remember: введенный текст должен пережить поворот экрана.
    // EditText делал это сам, в Compose состояние полей - наша забота.
    var name by rememberSaveable { mutableStateOf("") }
    var surname by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = { AppTopBar(title = stringResource(R.string.title_fill_form)) }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FormField(
                value = name,
                onValueChange = { name = it },
                label = R.string.hint_name,
                keyboardOptions = nameKeyboardOptions
            )
            FormField(
                value = surname,
                onValueChange = { surname = it },
                label = R.string.hint_surname,
                keyboardOptions = nameKeyboardOptions
            )
            FormField(
                value = age,
                // Цифровая клавиатура не мешает вставить текст из буфера - отсекаем явно
                onValueChange = { age = it.filter(Char::isDigit) },
                label = R.string.hint_age,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            )
            Button(
                onClick = { onApply(Profile(name = name, surname = surname, age = age)) },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            ) {
                Text(stringResource(R.string.button_apply))
            }
        }
    }
}

private val nameKeyboardOptions = KeyboardOptions(
    capitalization = KeyboardCapitalization.Words,
    imeAction = ImeAction.Next
)

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes label: Int,
    keyboardOptions: KeyboardOptions
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
private fun FillFormScreenPreview() {
    ActivitiesTheme {
        FillFormScreen(onApply = {})
    }
}
