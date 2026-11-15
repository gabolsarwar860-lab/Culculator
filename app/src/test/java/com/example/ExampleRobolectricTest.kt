package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.CalculatorPreferencesRepository
import com.example.ui.CalculatorScreen
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.CalculatorAppTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Calculator", appName)
    }

    @Test
    fun `basic calculation and clear flow works in UI`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = CalculatorPreferencesRepository(context)
        val viewModel = CalculatorViewModel(repository)

        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsState()
            CalculatorAppTheme(themeMode = uiState.themeMode) {
                CalculatorScreen(
                    uiState = uiState,
                    onDigit = viewModel::onDigit,
                    onDecimal = viewModel::onDecimal,
                    onOperator = viewModel::onOperator,
                    onPercent = viewModel::onPercent,
                    onToggleSign = viewModel::onToggleSign,
                    onParenthesis = viewModel::onSmartParenthesis,
                    onFunction = viewModel::onFunction,
                    onConstant = viewModel::onConstant,
                    onSquare = viewModel::onSquare,
                    onFactorial = viewModel::onFactorial,
                    onReciprocal = viewModel::onReciprocal,
                    onBackspace = viewModel::onBackspace,
                    onClear = viewModel::onClear,
                    onEquals = viewModel::onEquals,
                    onToggleAngleMode = viewModel::onToggleAngleMode,
                    onToggleInverse = viewModel::onToggleInverse,
                    onToggleScientific = viewModel::onToggleScientific,
                    onCycleTheme = viewModel::onCycleTheme,
                    onReuseHistoryText = viewModel::onReuseHistoryText,
                    onDeleteHistoryItem = viewModel::onDeleteHistoryItem,
                    onClearHistory = viewModel::onClearHistory
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("key_7").performClick()
        composeTestRule.onNodeWithTag("key_multiply").performClick()
        composeTestRule.onNodeWithTag("key_8").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("display_expression").assertTextEquals("7×8")
        composeTestRule.onNodeWithTag("display_live_result").assertTextEquals("= 56")

        composeTestRule.onNodeWithTag("key_equals").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("display_expression").assertTextEquals("56")

        composeTestRule.onNodeWithTag("key_ac").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("display_expression").assertTextEquals("0")

        composeTestRule.onNodeWithTag("scientific_toggle_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("key_sin").assertIsDisplayed()
    }
}
