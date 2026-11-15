package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "calculator_prefs")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class HistoryItem(
    val id: Long,
    val expression: String,
    val result: String,
    val timestampMillis: Long
)

data class CalculatorPrefs(
    val isDegreeMode: Boolean = true,
    val isScientificExpanded: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val history: List<HistoryItem> = emptyList()
)

class CalculatorPreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_DEGREE = booleanPreferencesKey("is_degree_mode")
        val IS_SCIENTIFIC = booleanPreferencesKey("is_scientific_expanded")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HISTORY_JSON = stringPreferencesKey("history_json")
    }

    val prefsFlow: Flow<CalculatorPrefs> = context.dataStore.data.map { prefs ->
        val themeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(ThemeMode.SYSTEM)
        val historyJson = prefs[Keys.HISTORY_JSON] ?: "[]"
        CalculatorPrefs(
            isDegreeMode = prefs[Keys.IS_DEGREE] ?: true,
            isScientificExpanded = prefs[Keys.IS_SCIENTIFIC] ?: false,
            themeMode = themeMode,
            history = parseHistory(historyJson)
        )
    }

    suspend fun setDegreeMode(isDegree: Boolean) {
        context.dataStore.edit { it[Keys.IS_DEGREE] = isDegree }
    }

    suspend fun setScientificExpanded(expanded: Boolean) {
        context.dataStore.edit { it[Keys.IS_SCIENTIFIC] = expanded }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun addHistoryItem(item: HistoryItem) {
        context.dataStore.edit { prefs ->
            val current = parseHistory(prefs[Keys.HISTORY_JSON] ?: "[]").toMutableList()
            // Avoid duplicating identical consecutive entry
            if (current.firstOrNull()?.let { it.expression == item.expression && it.result == item.result } == true) {
                return@edit
            }
            current.add(0, item)
            val trimmed = current.take(50)
            prefs[Keys.HISTORY_JSON] = serializeHistory(trimmed)
        }
    }

    suspend fun removeHistoryItem(id: Long) {
        context.dataStore.edit { prefs ->
            val current = parseHistory(prefs[Keys.HISTORY_JSON] ?: "[]")
                .filterNot { it.id == id }
            prefs[Keys.HISTORY_JSON] = serializeHistory(current)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { prefs ->
            prefs[Keys.HISTORY_JSON] = "[]"
        }
    }

    private fun parseHistory(json: String): List<HistoryItem> {
        return runCatching {
            val array = JSONArray(json)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        HistoryItem(
                            id = obj.getLong("id"),
                            expression = obj.getString("expression"),
                            result = obj.getString("result"),
                            timestampMillis = obj.optLong("timestamp", 0L)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun serializeHistory(items: List<HistoryItem>): String {
        val array = JSONArray()
        for (item in items) {
            array.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("expression", item.expression)
                    put("result", item.result)
                    put("timestamp", item.timestampMillis)
                }
            )
        }
        return array.toString()
    }
}
