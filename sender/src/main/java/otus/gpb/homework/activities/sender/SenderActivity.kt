package otus.gpb.homework.activities.sender

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import otus.gpb.homework.activities.sender.databinding.ActivitySenderBinding

class SenderActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySenderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySenderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toGoogleMapsButton.setOnClickListener {
            openRestaurantsInGoogleMaps()
        }
        binding.sendEmailButton.setOnClickListener {
            sendEmail()
        }
        binding.openReceiverButton.setOnClickListener {
            openReceiver()
        }
    }

    /**
     * Кнопка перехода на Compose-версию экрана — пункт меню в верхнем баре.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.sender_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == R.id.action_open_compose) {
            startActivity(Intent(this, SenderComposeActivity::class.java))
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    /**
     * Задача 1: явный Intent в Google Maps с поиском ресторанов рядом.
     *
     * Явным Intent делает установленный [Intent.setComponent] — пакет и класс
     * конкретной Activity. Система не ищет обработчик по intent-filter,
     * а запускает ровно то, что указано.
     */
    private fun openRestaurantsInGoogleMaps() {
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

    /**
     * Задача 2: неявный Intent для почтовых клиентов.
     *
     * ACTION_SENDTO + схема "mailto:" — это контракт, по которому объявляют
     * intent-filter именно почтовые приложения. Компонент не задаётся:
     * система сама подбирает обработчики по фильтрам.
     */
    private fun sendEmail() {
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

    /**
     * Задача 3: неявный Intent с данными о фильме для модуля receiver.
     *
     * Параметры заданы ровно так, как требует README: action = SEND,
     * type = "text/plain", category = DEFAULT. Три extras типа String
     * несут поля [Payload]. Ключи extras — часть контракта между модулями:
     * receiver читает их по тем же именам.
     */
    private fun openReceiver() {
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

    private companion object {
        const val MAPS_PACKAGE = "com.google.android.apps.maps"
        const val MAPS_ACTIVITY = "com.google.android.maps.MapsActivity"
        const val MAILTO_SCHEME = "mailto:"

        const val MIME_TEXT_PLAIN = "text/plain"
        const val EXTRA_TITLE = "title"
        const val EXTRA_YEAR = "year"
        const val EXTRA_DESCRIPTION = "description"
    }
}
