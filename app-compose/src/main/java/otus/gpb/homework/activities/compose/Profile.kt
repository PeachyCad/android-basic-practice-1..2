package otus.gpb.homework.activities.compose

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Данные, которые FillFormActivity возвращает на EditProfileActivity.
 *
 * Parcelable нужен дважды: чтобы передать профиль одним extra в результате
 * и чтобы rememberSaveable пережил поворот экрана.
 */
@Parcelize
data class Profile(
    val name: String = "",
    val surname: String = "",
    val age: String = ""
) : Parcelable

/**
 * Что сейчас показывается на месте фото профиля.
 *
 * В View-версии это было состояние самого ImageView. В Compose UI - функция от
 * состояния, поэтому состояние описываем явно.
 */
sealed interface ProfilePhoto : Parcelable {

    /** Ничего не выбрано - иконка-заглушка. */
    @Parcelize
    data object Placeholder : ProfilePhoto

    /** Камеру разрешили - показываем R.drawable.cat. */
    @Parcelize
    data object Cat : ProfilePhoto

    /** Фото из галереи: Uri нужен и для отображения, и для отправки. */
    @Parcelize
    data class Picked(val uri: Uri) : ProfilePhoto
}
