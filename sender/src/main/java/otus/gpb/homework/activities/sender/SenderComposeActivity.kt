package otus.gpb.homework.activities.sender

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

/**
 * Compose-версия экрана sender.
 *
 * Логика Intent'ов намеренно продублирована из [SenderActivity], а не вынесена
 * в общий класс: так видно, что при переходе на Compose она не меняется
 * ни на строку. Отличается только слой UI.
 *
 * Наследуется от ComponentActivity, а не от AppCompatActivity: делегат AppCompat
 * здесь не нужен — тему и внешний вид задаёт MaterialTheme внутри setContent.
 */
class SenderComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SenderScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SenderScreen() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(R.string.compose_screen_title)) })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { context.openRestaurantsInGoogleMaps() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.to_google_maps))
            }
            Button(
                onClick = { context.sendEmail() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.send_email))
            }
            Button(
                onClick = { context.openReceiver() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.open_receiver))
            }
        }
    }
}

// --- Логика Intent'ов: полная копия методов из SenderActivity ---

/** Задача 1: явный Intent в Google Maps. */
private fun Context.openRestaurantsInGoogleMaps() {
    val query = Uri.encode(getString(R.string.maps_query))
    val intent = Intent(Intent.ACTION_VIEW, "geo:0,0?q=$query".toUri()).apply {
        component = ComponentName(MAPS_PACKAGE, MAPS_ACTIVITY)
    }
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, R.string.maps_not_found, Toast.LENGTH_SHORT).show()
    }
}

/** Задача 2: неявный Intent для почтовых клиентов. */
private fun Context.sendEmail() {
    val intent = Intent(Intent.ACTION_SENDTO, MAILTO_SCHEME.toUri()).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_address)))
        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
        putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body))
    }
    try {
        startActivity(Intent.createChooser(intent, getString(R.string.email_chooser_title)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, R.string.email_client_not_found, Toast.LENGTH_SHORT).show()
    }
}

/** Задача 3: неявный Intent с данными о фильме для модуля receiver. */
private fun Context.openReceiver() {
    val payload = Payload(
        title = getString(R.string.payload_title),
        year = getString(R.string.payload_year),
        description = getString(R.string.payload_description)
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = MIME_TEXT_PLAIN
        addCategory(Intent.CATEGORY_DEFAULT)
        putExtra(EXTRA_TITLE, payload.title)
        putExtra(EXTRA_YEAR, payload.year)
        putExtra(EXTRA_DESCRIPTION, payload.description)
    }
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, R.string.receiver_not_found, Toast.LENGTH_SHORT).show()
    }
}

private const val MAPS_PACKAGE = "com.google.android.apps.maps"
private const val MAPS_ACTIVITY = "com.google.android.maps.MapsActivity"
private const val MAILTO_SCHEME = "mailto:"

private const val MIME_TEXT_PLAIN = "text/plain"
private const val EXTRA_TITLE = "title"
private const val EXTRA_YEAR = "year"
private const val EXTRA_DESCRIPTION = "description"

@Preview(showBackground = true)
@Composable
private fun SenderScreenPreview() {
    MaterialTheme {
        SenderScreen()
    }
}
