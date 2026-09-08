package com.smokingtracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.smokingtracker.data.DataStoreManager
import com.smokingtracker.data.ThemePreference
import com.smokingtracker.ui.MainApp
import com.smokingtracker.ui.theme.AppTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : AppCompatActivity() {

    private val dataStoreManager: DataStoreManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var initialThemeLoaded by mutableStateOf(false)
        splashScreen.setKeepOnScreenCondition { !initialThemeLoaded }

        lifecycleScope.launch {
            dataStoreManager.appTheme.first()
            initialThemeLoaded = true
        }

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            lifecycleScope.launch {
                val legacyTag = dataStoreManager.appLanguageTag.first()
                if (legacyTag != null) {
                    if (androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().isEmpty) {
                        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                            androidx.core.os.LocaleListCompat.forLanguageTags(legacyTag)
                        )
                    }
                    dataStoreManager.clearAppLanguageTag()
                }
            }
        }

        enableEdgeToEdge()

        setContent {
            if (!initialThemeLoaded) return@setContent

            val viewModel: MainViewModel = koinViewModel()

            val themePreference by viewModel.themePreference.collectAsStateWithLifecycle()
            val fontPreset by viewModel.fontPreset.collectAsStateWithLifecycle()
            val amoledTheme by viewModel.amoledTheme.collectAsStateWithLifecycle()
            val colorPreset by viewModel.colorPreset.collectAsStateWithLifecycle()
            val containerBorderEnabled by viewModel.containerBorderEnabled.collectAsStateWithLifecycle()
            val containerStyle by viewModel.containerStyle.collectAsStateWithLifecycle()
            val useCustomVariableFont by viewModel.useCustomVariableFont.collectAsStateWithLifecycle()
            val customFontWeight by viewModel.customFontWeight.collectAsStateWithLifecycle()
            val customFontWidth by viewModel.customFontWidth.collectAsStateWithLifecycle()
            val customFontRoundness by viewModel.customFontRoundness.collectAsStateWithLifecycle()
            val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()

            val useDarkTheme = when (themePreference) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
            }

            AppTheme(
                useDarkTheme = useDarkTheme,
                fontPreset = fontPreset,
                amoledThemeEnabled = amoledTheme,
                colorPreset = colorPreset,
                containerBorderEnabled = containerBorderEnabled,
                containerStyle = containerStyle,
                useCustomVariableFont = useCustomVariableFont,
                customFontWeight = customFontWeight,
                customFontWidth = customFontWidth,
                customFontRoundness = customFontRoundness
            ) {
                androidx.compose.runtime.CompositionLocalProvider(
                    com.smokingtracker.ui.theme.LocalVibrationEnabled provides vibrationEnabled
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
