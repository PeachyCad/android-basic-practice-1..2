package otus.gpb.homework.activities.compose.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Те же цвета, что в Theme.Activities модуля :app
private val Purple200 = Color(0xFFBB86FC)
private val Purple500 = Color(0xFF6200EE)
private val Teal200 = Color(0xFF03DAC5)

private val LightColors = lightColorScheme(
    primary = Purple500,
    onPrimary = Color.White,
    secondary = Teal200,
    onSecondary = Color.Black
)

private val DarkColors = darkColorScheme(
    primary = Purple200,
    onPrimary = Color.Black,
    secondary = Teal200,
    onSecondary = Color.Black
)

@Composable
fun ActivitiesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}

/**
 * Аналог Toolbar с фоном colorPrimary из View-версии.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}
