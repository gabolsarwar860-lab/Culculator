package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CalculatorPreferencesRepository
import com.example.data.HistoryItem
import com.example.data.ThemeMode
import com.example.domain.EvalErrorType
import com.example.domain.EvalResult
import com.example.domain.ExpressionEvaluator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val expression: String = "",
    val liveResult: String = "",
    val errorType: EvalErrorType? = null,
    val justEvaluated: Boolean = false,
    val isDegreeMode: Boolean = true,
    val isInverseMode: Boolean = false,
    val isScientificExpanded: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val history: List<HistoryItem> = emptyList()
)

class CalculatorViewModel(
    private val repository: CalculatorPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val binaryOperators = setOf('+', '−', '×', '÷', '^')

    init {
        viewModelScope.launch {
            repository.prefsFlow.collect { prefs ->
                _uiState.update { current ->
                    val updated = current.copy(
                        isDegreeMode = prefs.isDegreeMode,
                        isScientificExpanded = prefs.isScientificExpanded,
                        themeMode = prefs.themeMode,
                        history = prefs.history
                    )
                    recomputeLive(updated)
                }
            }
        }
    }

    fun onDigit(digit: Char) {
        _uiState.update { state ->
            val base = if (state.justEvaluated) "" else state.expression
            // Prevent redundant leading zero like "00" when at start of number
            val lastNumber = extractTrailingNumber(base)
            val nextExpr = if (lastNumber == "0" && digit == '0') {
                base
            } else if (lastNumber == "0" && digit != '0') {
                base.dropLast(1) + digit
            } else {
                base + digit
            }
            recomputeLive(
                state.copy(
                    expression = nextExpr,
                    justEvaluated = false,
                    errorType = null
                )
            )
        }
    }

    fun onDecimal() {
        _uiState.update { state ->
            val base = if (state.justEvaluated) "" else state.expression
            val lastNumber = extractTrailingNumber(base)
            if (lastNumber.contains('.')) {
                return@update state
            }
            val nextExpr = if (lastNumber.isEmpty()) {
                val needsZero = base.isEmpty() || base.last() in binaryOperators || base.last() == '('
                if (needsZero) "${base}0." else "${base}×0."
            } else {
                "$base."
            }
            recomputeLive(
                state.copy(
                    expression = nextExpr,
                    justEvaluated = false,
                    errorType = null
                )
            )
        }
    }

    fun onOperator(op: Char) {
        _uiState.update { state ->
            val current = state.expression
            if (current.isEmpty()) {
                return@update if (op == '−') {
                    recomputeLive(state.copy(expression = "−", justEvaluated = false, errorType = null))
                } else {
                    state
                }
            }

            val last = current.last()
            val nextExpr = when {
                last == '(' -> {
                    if (op == '−') "$current−" else current
                }
                last in binaryOperators -> {
                    if (op == '−' && last in setOf('×', '÷', '^')) {
                        "$current−"
                    } else if (current.length >= 2 && current[current.length - 2] in setOf('×', '÷', '^', '(') && last == '−') {
                        current
                    } else {
                        current.dropLast(1) + op
                    }
                }
                else -> "$current$op"
            }

            recomputeLive(
                state.copy(
                    expression = nextExpr,
                    justEvaluated = false,
                    errorType = null
                )
            )
        }
    }

    fun onPercent() {
        _uiState.update { state ->
            val current = state.expression
            if (current.isEmpty()) return@update state
            val last = current.last()
            if (last.isDigit() || last == ')' || last == 'π' || last == 'e' || last == '!') {
                recomputeLive(
                    state.copy(
                        expression = "$current%",
                        justEvaluated = false,
                        errorType = null
                    )
                )
            } else {
                state
            }
        }
    }

    fun onToggleSign() {
        _uiState.update { state ->
            val current = state.expression
            if (current.isEmpty()) {
                return@update recomputeLive(state.copy(expression = "−", justEvaluated = false, errorType = null))
            }
            if (current == "−") {
                return@update recomputeLive(state.copy(expression = "", justEvaluated = false, errorType = null))
            }

            // Toggle leading sign of the trailing number or parenthesized expression
            val trailingStart = findTrailingNumberStart(current)
            val nextExpr = if (trailingStart >= 0) {
                if (trailingStart > 0 && current[trailingStart - 1] == '−') {
                    val beforeMinus = trailingStart - 1
                    val isUnaryMinus = beforeMinus == 0 || current[beforeMinus - 1] in (binaryOperators + '(')
                    if (isUnaryMinus) {
                        current.removeRange(beforeMinus, trailingStart)
                    } else {
                        current.substring(0, trailingStart) + "(−" + current.substring(trailingStart)
                    }
                } else if (trailingStart == 0) {
                    "−$current"
                } else {
                    val prev = current[trailingStart - 1]
                    if (prev in setOf('×', '÷', '^', '(')) {
                        current.substring(0, trailingStart) + "−" + current.substring(trailingStart)
                    } else {
                        current.substring(0, trailingStart) + "(−" + current.substring(trailingStart)
                    }
                }
            } else {
                "$current(−"
            }

            recomputeLive(
                state.copy(
                    expression = nextExpr,
                    justEvaluated = false,
                    errorType = null
                )
            )
        }
    }

    fun onSmartParenthesis() {
        _uiState.update { state ->
            val current = if (state.justEvaluated) "" else state.expression
            val openCount = current.count { it == '(' }
            val closeCount = current.count { it == ')' }
            val last = current.lastOrNull()
            val insertChar = if (openCount > closeCount &&
                last != null &&
                (last.isDigit() || last == ')' || last == 'π' || last == 'e' || last == '%' || last == '!')
            ) {
                ")"
            } else {
                "("
            }
            recomputeLive(
                state.copy(
                    expression = current + insertChar,
                    justEvaluated = false,
                    errorType = null
                )
            )
        }
    }

    fun onFunction(functionToken: String) {
        _uiState.update { state ->
            val current = if (state.justEvaluated) "" else state.expression
            val nextExpr = "$current$functionToken("
            recomputeLive(
                state.copy(
                    expression = nextExpr,
                    justEvaluated = false,
                    errorType = null
                )
            )
        }
    }

    fun onConstant(constant: String) {
        _uiState.update { state ->
            val current = if (state.justEvaluated) "" else state.expression
            recomputeLive(
                state.copy(
                    expression = "$current$constant",
                    justEvaluated = false,
                    errorType = null
                )
            )
        }
    }

    fun onSquare() {
        _uiState.update { state ->
            val current = state.expression
            if (current.isEmpty()) return@update state
            val last = current.last()
            if (last.isDigit() || last == ')' || last == 'π' || last == 'e') {
                recomputeLive(
                    state.copy(
                        expression = "$current^2",
                        justEvaluated = false,
                        errorType = null
                    )
                )
            } else {
                state
            }
        }
    }

    fun onFactorial() {
        _uiState.update { state ->
            val current = state.expression
            if (current.isEmpty()) return@update state
            val last = current.last()
            if (last.isDigit() || last == ')') {
                recomputeLive(
                    state.copy(
                        expression = "$current!",
                        justEvaluated = false,
                        errorType = null
                    )
                )
            } else {
                state
            }
        }
    }

    fun onReciprocal() {
        _uiState.update { state ->
            val current = state.expression
            if (current.isEmpty()) return@update state
            val nextExpr = "1÷($current)"
            recomputeLive(
                state.copy(
                    expression = nextExpr,
                    justEvaluated = false,
                    errorType = null
                )
            )
        }
    }

    fun onBackspace() {
        _uiState.update { state ->
            val current = state.expression
            if (current.isEmpty()) return@update state

            // Remove whole function name token when deleting trailing "sin(" etc.
            val knownTokens = listOf("sin⁻¹(", "cos⁻¹(", "tan⁻¹(", "10^(", "e^(", "sin(", "cos(", "tan(", "log(", "ln(", "√(")
            val matchedToken = knownTokens.firstOrNull { current.endsWith(it) }
            val nextExpr = if (matchedToken != null) {
                current.dropLast(matchedToken.length)
            } else {
                current.dropLast(1)
            }

            recomputeLive(
                state.copy(
                    expression = nextExpr,
                    justEvaluated = false,
                    errorType = null
                )
            )
        }
    }

    fun onClear() {
        _uiState.update { state ->
            state.copy(
                expression = "",
                liveResult = "",
                errorType = null,
                justEvaluated = false
            )
        }
    }

    fun onEquals() {
        val currentState = _uiState.value
        val expr = currentState.expression.trim()
        if (expr.isEmpty()) return

        when (val result = ExpressionEvaluator.evaluate(expr, currentState.isDegreeMode)) {
            is EvalResult.Success -> {
                val rawResult = ExpressionEvaluator.formatRawNumber(result.value)
                _uiState.update {
                    it.copy(
                        expression = rawResult,
                        liveResult = "",
                        errorType = null,
                        justEvaluated = true
                    )
                }
                val item = HistoryItem(
                    id = System.nanoTime(),
                    expression = expr,
                    result = result.formatted,
                    timestampMillis = System.currentTimeMillis()
                )
                viewModelScope.launch {
                    repository.addHistoryItem(item)
                }
            }
            is EvalResult.Error -> {
                _uiState.update {
                    it.copy(
                        errorType = result.errorType,
                        liveResult = ""
                    )
                }
            }
            EvalResult.Empty -> Unit
        }
    }

    fun onToggleAngleMode() {
        val next = !_uiState.value.isDegreeMode
        _uiState.update { recomputeLive(it.copy(isDegreeMode = next)) }
        viewModelScope.launch { repository.setDegreeMode(next) }
    }

    fun onToggleInverse() {
        _uiState.update { it.copy(isInverseMode = !it.isInverseMode) }
    }

    fun onToggleScientific() {
        val next = !_uiState.value.isScientificExpanded
        _uiState.update { it.copy(isScientificExpanded = next) }
        viewModelScope.launch { repository.setScientificExpanded(next) }
    }

    fun onCycleTheme() {
        val next = when (_uiState.value.themeMode) {
            ThemeMode.SYSTEM -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.SYSTEM
        }
        _uiState.update { it.copy(themeMode = next) }
        viewModelScope.launch { repository.setThemeMode(next) }
    }

    fun onReuseHistoryText(text: String) {
        val cleaned = text.replace(",", "")
        _uiState.update { state ->
            recomputeLive(
                state.copy(
                    expression = cleaned,
                    justEvaluated = false,
                    errorType = null
                )
            )
        }
    }

    fun onDeleteHistoryItem(id: Long) {
        viewModelScope.launch { repository.removeHistoryItem(id) }
    }

    fun onClearHistory() {
        viewModelScope.launch { repository.clearHistory() }
    }

    private fun recomputeLive(state: CalculatorUiState): CalculatorUiState {
        if (state.justEvaluated || !ExpressionEvaluator.canEvaluateLive(state.expression)) {
            return state.copy(liveResult = "")
        }
        return when (val eval = ExpressionEvaluator.evaluate(state.expression, state.isDegreeMode)) {
            is EvalResult.Success -> {
                val formattedRaw = ExpressionEvaluator.formatRawNumber(eval.value)
                // Don't echo identical simple number as live preview
                val normalizedExpr = state.expression.replace(",", "").replace("−", "-")
                if (normalizedExpr == formattedRaw) {
                    state.copy(liveResult = "")
                } else {
                    state.copy(liveResult = eval.formatted)
                }
            }
            else -> state.copy(liveResult = "")
        }
    }

    private fun extractTrailingNumber(expr: String): String {
        val start = findTrailingNumberStart(expr)
        return if (start < 0) "" else expr.substring(start)
    }

    private fun findTrailingNumberStart(expr: String): Int {
        if (expr.isEmpty()) return -1
        var idx = expr.length - 1
        var seenChar = false
        while (idx >= 0) {
            val c = expr[idx]
            if (c.isDigit() || c == '.') {
                seenChar = true
                idx--
            } else {
                break
            }
        }
        return if (seenChar) idx + 1 else -1
    }
}

class CalculatorViewModelFactory(
    private val repository: CalculatorPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            return CalculatorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
