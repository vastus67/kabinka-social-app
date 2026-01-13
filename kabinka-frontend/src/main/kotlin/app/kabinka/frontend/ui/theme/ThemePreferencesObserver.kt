package app.kabinka.frontend.ui.theme

import android.content.Context
import android.content.SharedPreferences
import app.kabinka.social.MastodonApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ThemePreferences(
    val themeMode: Int = 0,  // 0=AUTO, 1=LIGHT, 2=DARK
    val useDynamicColors: Boolean = false
)

object ThemePreferencesObserver : SharedPreferences.OnSharedPreferenceChangeListener {
    
    private val _preferences = MutableStateFlow(ThemePreferences())
    val preferences: StateFlow<ThemePreferences> = _preferences.asStateFlow()
    
    private var sharedPreferences: SharedPreferences? = null
    
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("global", Context.MODE_PRIVATE)
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        
        // Load initial values
        loadPreferences()
    }
    
    private fun loadPreferences() {
        val prefs = sharedPreferences ?: return
        _preferences.value = ThemePreferences(
            themeMode = prefs.getInt("theme", 0),
            useDynamicColors = prefs.getBoolean("useDynamicColors", false)
        )
    }
    
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "theme", "useDynamicColors" -> loadPreferences()
        }
    }
    
    fun cleanup() {
        sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }
}
