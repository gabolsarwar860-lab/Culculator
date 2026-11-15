package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.CalculatorPreferencesRepository
import com.example.ui.CalculatorScreen
import com.example.ui.CalculatorViewModel
import com.example.ui.CalculatorViewModelFactory
import com.example.ui.theme.CalculatorAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = CalculatorPreferencesRepository(applicationContext)

        setContent {
            val viewModel: CalculatorViewModel = viewModel(
                factory = CalculatorViewModelFactory(repository)
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
    }
}
