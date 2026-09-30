package otus.gpb.homework.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EditProfileActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var surnameTextView: TextView
    private lateinit var ageTextView: TextView

    private val cameraPermission: String = Manifest.permission.CAMERA

    /**
     * Uri картинки, выбранной в галерее: нужен, чтобы приложить ее к интенту отправки.
     */
    private var photoUri: Uri? = null

    /**
     * Result API: запрос разрешения на камеру.
     * В колбэк приходит только "дали/не дали", поэтому второй и четвертый сценарии
     * различаем через shouldShowRequestPermissionRationale, без своих флагов.
     */
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        when {
            // 1. Разрешение выдано
            isGranted -> showCat()
            // 2. Первый отказ: система еще разрешает показать объяснение -> ничего не делаем
            shouldShowRequestPermissionRationale(cameraPermission) -> Unit
            // 4. Повторный отказ ("больше не спрашивать") -> остаются только настройки
            else -> showSettingsDialog()
        }
    }

    /**
     * Result API: системный пикер медиафайлов.
     * В колбэк приходит null, если пользователь закрыл пикер, ничего не выбрав.
     */
    private val pickPhoto = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            photoUri = it
            populateImage(it)
        }
    }

    /**
     * Result API: запуск своего же экрана за результатом.
     * StartActivityForResult - универсальный контракт: на входе любой Intent,
     * на выходе пара resultCode + data, как в старом onActivityResult.
     */
    private val fillForm = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        // RESULT_CANCELED приходит, если форму закрыли кнопкой "назад" - показывать нечего
        if (result.resultCode == RESULT_OK && data != null) {
            showProfile(FillFormActivity.readProfile(data))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        imageView = findViewById(R.id.imageview_photo)
        imageView.setOnClickListener { showPhotoSourceDialog() }

        nameTextView = findViewById(R.id.textview_name)
        surnameTextView = findViewById(R.id.textview_surname)
        ageTextView = findViewById(R.id.textview_age)

        findViewById<Button>(R.id.button4).setOnClickListener {
            fillForm.launch(FillFormActivity.createIntent(this))
        }

        findViewById<Toolbar>(R.id.toolbar).apply {
            inflateMenu(R.menu.menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.send_item -> {
                        openSenderApp()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    /**
     * Simple dialog: список действий над фото профиля.
     */
    private fun showPhotoSourceDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_photo_title)
            .setItems(R.array.photo_actions) { _, which ->
                when (which) {
                    ACTION_TAKE_PHOTO -> onTakePhotoSelected()
                    ACTION_PICK_PHOTO -> onPickPhotoSelected()
                }
            }
            .show()
    }

    private fun onTakePhotoSelected() {
        when {
            // Разрешение уже есть - запрашивать нечего
            ContextCompat.checkSelfPermission(
                this,
                cameraPermission
            ) == PackageManager.PERMISSION_GRANTED -> showCat()
            // 3. Запрос после отмены: сначала объясняем, зачем нам камера
            shouldShowRequestPermissionRationale(cameraPermission) -> showRationaleDialog()
            // Спрашиваем в первый раз (либо разрешение запрещено навсегда - это увидим в колбэке)
            else -> requestCameraPermission.launch(cameraPermission)
        }
    }

    private fun onPickPhotoSelected() {
        pickPhoto.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    /**
     * Alert dialog с объяснением, зачем приложению камера.
     */
    private fun showRationaleDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_rationale_title)
            .setMessage(R.string.dialog_rationale_message)
            .setPositiveButton(R.string.dialog_rationale_grant) { _, _ ->
                requestCameraPermission.launch(cameraPermission)
            }
            .setNegativeButton(R.string.dialog_rationale_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Разрешение запрещено навсегда: поменять его можно только в настройках приложения.
     */
    private fun showSettingsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_settings_title)
            .setMessage(R.string.dialog_settings_message)
            .setPositiveButton(R.string.dialog_settings_open) { _, _ ->
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", packageName, null)
                    )
                )
            }
            .show()
    }

    private fun showCat() {
        imageView.setImageResource(R.drawable.cat)
    }

    /**
     * Используйте этот метод чтобы отобразить картинку полученную из медиатеки в ImageView
     */
    private fun populateImage(uri: Uri) {
        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
        imageView.setImageBitmap(bitmap)
    }

    private fun showProfile(profile: Profile) {
        nameTextView.text = profile.name
        surnameTextView.text = profile.surname
        ageTextView.text = profile.age
    }

    /**
     * Отправка профиля в другое приложение.
     *
     * Основной путь по заданию - ЯВНЫЙ интент в Telegram: у ACTION_SEND проставлен
     * setPackage, поэтому система не показывает диалог выбора, а отдает данные
     * конкретному приложению.
     *
     * Если Telegram не установлен, явный интент упал бы с ActivityNotFoundException,
     * поэтому откатываемся на НЕЯВНЫЙ интент: тот же ACTION_SEND, но без setPackage
     * и завернутый в createChooser - адресата в этом случае выбирает пользователь
     * из всех приложений, умеющих принять картинку.
     */
    private fun openSenderApp() {
        val shareIntent = createProfileShareIntent(
            profile = Profile(
                name = nameTextView.text.toString(),
                surname = surnameTextView.text.toString(),
                age = ageTextView.text.toString()
            ),
            photoUri = photoUri
        )
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

    private companion object {
        private const val ACTION_TAKE_PHOTO = 0
        private const val ACTION_PICK_PHOTO = 1

        /**
         * Официальный клиент и его сборка из Google Play с другим package.
         */
        private val TELEGRAM_PACKAGES = listOf(
            "org.telegram.messenger",
            "org.telegram.messenger.web"
        )
    }
}
