package com.example.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

sealed class EvalResult {
    data class Success(val value: Double, val formatted: String) : EvalResult()
    data class Error(val errorType: EvalErrorType) : EvalResult()
    data object Empty : EvalResult()
}

enum class EvalErrorType {
    DIVIDE_BY_ZERO,
    DOMAIN_ERROR,
    INVALID_EXPRESSION,
    OVERFLOW
}

object ExpressionEvaluator {

    private class EvalException(val errorType: EvalErrorType) : RuntimeException()

    private val usSymbols = DecimalFormatSymbols(Locale.US)
    private val groupingFormat = DecimalFormat("#,##0.############", usSymbols)
    private val scientificFormat = DecimalFormat("0.######E0", usSymbols)

    fun evaluate(expression: String, isDegreeMode: Boolean = true): EvalResult {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return EvalResult.Empty

        val normalized = autoCloseParentheses(normalizeSymbols(trimmed))
        return try {
            val tokens = tokenize(normalized)
            if (tokens.isEmpty()) return EvalResult.Empty
            val parser = Parser(tokens, isDegreeMode)
            val rawValue = parser.parse()
            if (rawValue.isNaN()) {
                EvalResult.Error(EvalErrorType.DOMAIN_ERROR)
            } else if (rawValue.isInfinite()) {
                EvalResult.Error(EvalErrorType.OVERFLOW)
            } else {
                val snapped = snapPrecision(rawValue)
                EvalResult.Success(snapped, formatNumber(snapped))
            }
        } catch (e: EvalException) {
            EvalResult.Error(e.errorType)
        } catch (_: Exception) {
            EvalResult.Error(EvalErrorType.INVALID_EXPRESSION)
        }
    }

    fun canEvaluateLive(expression: String): Boolean {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return false
        val last = trimmed.last()
        if (last in listOf('+', '-', '−', '*', '×', '/', '÷', '^', '(')) return false
        return trimmed.any { it.isDigit() || it == 'π' || it == 'e' }
    }

    fun formatNumber(value: Double): String {
        if (value == 0.0) return "0"
        val clean = if (value == -0.0) 0.0 else value
        val absVal = abs(clean)
        if (absVal >= 1e12 || (absVal > 0.0 && absVal < 1e-8)) {
            return scientificFormat.format(clean).replace("E", "E")
        }
        return groupingFormat.format(clean)
    }

    fun formatRawNumber(value: Double): String {
        if (value == 0.0) return "0"
        val clean = if (value == -0.0) 0.0 else value
        val absVal = abs(clean)
        if (absVal >= 1e12 || (absVal > 0.0 && absVal < 1e-8)) {
            return scientificFormat.format(clean)
        }
        val bd = BigDecimal.valueOf(clean).stripTrailingZeros()
        return bd.toPlainString()
    }

    private fun snapPrecision(value: Double): Double {
        if (abs(value) < 1e-13) return 0.0
        val bd = BigDecimal(value, MathContext(12, RoundingMode.HALF_UP))
        val snapped = bd.toDouble()
        return if (abs(snapped) < 1e-13) 0.0 else snapped
    }

    private fun normalizeSymbols(expr: String): String {
        return expr
            .replace(",", "")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("√", "sqrt")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
    }

    private fun autoCloseParentheses(expr: String): String {
        var openCount = 0
        for (ch in expr) {
            if (ch == '(') openCount++
            else if (ch == ')' && openCount > 0) openCount--
        }
        return if (openCount > 0) expr + ")".repeat(openCount) else expr
    }

    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Constant(val value: Double) : Token()
        data class Operator(val op: Char) : Token()
        data class Function(val name: String) : Token()
        data object LParen : Token()
        data object RParen : Token()
        data object Percent : Token()
        data object Factorial : Token()
    }

    private fun tokenize(input: String): List<Token> {
        val raw = mutableListOf<Token>()
        var i = 0
        while (i < input.length) {
            val ch = input[i]
            when {
                ch.isWhitespace() -> i++
                ch.isDigit() || ch == '.' -> {
                    val start = i
                    var hasDot = (ch == '.')
                    i++
                    while (i < input.length) {
                        val c = input[i]
                        if (c.isDigit()) {
                            i++
                        } else if (c == '.' && !hasDot) {
                            hasDot = true
                            i++
                        } else if ((c == 'E' || c == 'e') && i + 1 < input.length &&
                            (input[i + 1].isDigit() || input[i + 1] == '+' || input[i + 1] == '-')
                        ) {
                            // Only treat 'e' as scientific exponent if followed by +/- or digit when part of number
                            val next = input[i + 1]
                            if (c == 'E' || next == '+' || next == '-') {
                                i++
                                if (input[i] == '+' || input[i] == '-') i++
                                while (i < input.length && input[i].isDigit()) i++
                            } else {
                                break
                            }
                        } else {
                            break
                        }
                    }
                    val numStr = input.substring(start, i)
                    val value = numStr.toDoubleOrNull()
                        ?: throw EvalException(EvalErrorType.INVALID_EXPRESSION)
                    raw.add(Token.Number(value))
                }
                ch == 'π' -> {
                    raw.add(Token.Constant(Math.PI))
                    i++
                }
                ch == 'e' -> {
                    raw.add(Token.Constant(Math.E))
                    i++
                }
                ch == '(' -> {
                    raw.add(Token.LParen)
                    i++
                }
                ch == ')' -> {
                    raw.add(Token.RParen)
                    i++
                }
                ch == '%' -> {
                    raw.add(Token.Percent)
                    i++
                }
                ch == '!' -> {
                    raw.add(Token.Factorial)
                    i++
                }
                ch in listOf('+', '-', '*', '/', '^') -> {
                    raw.add(Token.Operator(ch))
                    i++
                }
                ch.isLetter() -> {
                    val start = i
                    while (i < input.length && input[i].isLetter()) i++
                    val name = input.substring(start, i).lowercase(Locale.US)
                    if (name == "pi") {
                        raw.add(Token.Constant(Math.PI))
                    } else if (name in setOf("sin", "cos", "tan", "asin", "acos", "atan", "ln", "log", "sqrt", "exp", "tenpow")) {
                        raw.add(Token.Function(name))
                    } else {
                        throw EvalException(EvalErrorType.INVALID_EXPRESSION)
                    }
                }
                else -> throw EvalException(EvalErrorType.INVALID_EXPRESSION)
            }
        }

        // Insert implicit multiplication tokens where mathematically implied
        val result = mutableListOf<Token>()
        for (idx in raw.indices) {
            val curr = raw[idx]
            if (idx > 0) {
                val prev = raw[idx - 1]
                val prevIsValue = prev is Token.Number || prev is Token.Constant ||
                    prev is Token.RParen || prev is Token.Percent || prev is Token.Factorial
                val currStartsValue = curr is Token.Number || curr is Token.Constant ||
                    curr is Token.LParen || curr is Token.Function
                if (prevIsValue && currStartsValue) {
                    result.add(Token.Operator('*'))
                }
            }
            result.add(curr)
        }
        return result
    }

    private data class TermValue(
        val value: Double,
        val isSinglePercent: Boolean = false,
        val rawPercentOperand: Double = 0.0
    )

    private class Parser(
        private val tokens: List<Token>,
        private val isDegreeMode: Boolean
    ) {
        private var pos = 0

        private fun peek(): Token? = tokens.getOrNull(pos)
        private fun consume(): Token = tokens[pos++]

        fun parse(): Double {
            val result = parseExpression()
            if (pos < tokens.size) {
                throw EvalException(EvalErrorType.INVALID_EXPRESSION)
            }
            return result
        }

        private fun parseExpression(): Double {
            var left = parseTerm().value
            while (true) {
                val token = peek()
                if (token is Token.Operator && (token.op == '+' || token.op == '-')) {
                    consume()
                    val right = parseTerm()
                    val rhsValue = if (right.isSinglePercent) {
                        left * (right.rawPercentOperand / 100.0)
                    } else {
                        right.value
                    }
                    left = if (token.op == '+') left + rhsValue else left - rhsValue
                } else {
                    break
                }
            }
            return left
        }

        private fun parseTerm(): TermValue {
            val first = parseFactorWithPercentMeta()
            var acc = first.first
            var singlePercent = first.second
            var rawPercent = first.third

            while (true) {
                val token = peek()
                if (token is Token.Operator && (token.op == '*' || token.op == '/')) {
                    consume()
                    singlePercent = false
                    val right = parseFactor()
                    if (token.op == '/') {
                        if (abs(right) == 0.0) throw EvalException(EvalErrorType.DIVIDE_BY_ZERO)
                        acc /= right
                    } else {
                        acc *= right
                    }
                } else {
                    break
                }
            }
            return TermValue(acc, singlePercent, rawPercent)
        }

        private fun parseFactor(): Double = parseFactorWithPercentMeta().first

        // Returns (evaluatedValue, wasSinglePercentPostfix, rawOperandBeforePercent)
        private fun parseFactorWithPercentMeta(): Triple<Double, Boolean, Double> {
            val baseResult = parseUnary()
            if (peek() is Token.Operator && (peek() as Token.Operator).op == '^') {
                consume()
                val exponent = parseFactor()
                val res = baseResult.first.pow(exponent)
                if (res.isNaN()) throw EvalException(EvalErrorType.DOMAIN_ERROR)
                if (res.isInfinite()) throw EvalException(EvalErrorType.OVERFLOW)
                return Triple(res, false, 0.0)
            }
            return baseResult
        }

        private fun parseUnary(): Triple<Double, Boolean, Double> {
            val token = peek()
            if (token is Token.Operator && (token.op == '+' || token.op == '-')) {
                consume()
                val inner = parseUnary()
                val signed = if (token.op == '-') -inner.first else inner.first
                return Triple(signed, inner.second, if (token.op == '-') -inner.third else inner.third)
            }
            return parsePostfix()
        }

        private fun parsePostfix(): Triple<Double, Boolean, Double> {
            var value = parsePrimary()
            var isPercent = false
            var rawOperand = 0.0
            while (true) {
                when (peek()) {
                    is Token.Percent -> {
                        consume()
                        if (!isPercent) {
                            rawOperand = value
                            isPercent = true
                        } else {
                            // chained percent like 50%% -> treat as plain division by 100
                            isPercent = false
                        }
                        value /= 100.0
                    }
                    is Token.Factorial -> {
                        consume()
                        isPercent = false
                        value = factorial(value)
                    }
                    else -> break
                }
            }
            return Triple(value, isPercent, rawOperand)
        }

        private fun parsePrimary(): Double {
            val token = peek() ?: throw EvalException(EvalErrorType.INVALID_EXPRESSION)
            return when (token) {
                is Token.Number -> {
                    consume()
                    token.value
                }
                is Token.Constant -> {
                    consume()
                    token.value
                }
                is Token.LParen -> {
                    consume()
                    val inner = parseExpression()
                    if (peek() !is Token.RParen) {
                        throw EvalException(EvalErrorType.INVALID_EXPRESSION)
                    }
                    consume()
                    inner
                }
                is Token.Function -> {
                    consume()
                    val arg = if (peek() is Token.LParen) {
                        consume()
                        val v = parseExpression()
                        if (peek() !is Token.RParen) {
                            throw EvalException(EvalErrorType.INVALID_EXPRESSION)
                        }
                        consume()
                        v
                    } else {
                        parseUnary().first
                    }
                    applyFunction(token.name, arg)
                }
                else -> throw EvalException(EvalErrorType.INVALID_EXPRESSION)
            }
        }

        private fun applyFunction(name: String, arg: Double): Double {
            return when (name) {
                "sin" -> {
                    val rad = if (isDegreeMode) Math.toRadians(arg) else arg
                    sin(rad)
                }
                "cos" -> {
                    val rad = if (isDegreeMode) Math.toRadians(arg) else arg
                    cos(rad)
                }
                "tan" -> {
                    val rad = if (isDegreeMode) Math.toRadians(arg) else arg
                    if (abs(cos(rad)) < 1e-12) throw EvalException(EvalErrorType.DOMAIN_ERROR)
                    tan(rad)
                }
                "asin" -> {
                    if (arg < -1.0 || arg > 1.0) throw EvalException(EvalErrorType.DOMAIN_ERROR)
                    val res = asin(arg)
                    if (isDegreeMode) Math.toDegrees(res) else res
                }
                "acos" -> {
                    if (arg < -1.0 || arg > 1.0) throw EvalException(EvalErrorType.DOMAIN_ERROR)
                    val res = acos(arg)
                    if (isDegreeMode) Math.toDegrees(res) else res
                }
                "atan" -> {
                    val res = atan(arg)
                    if (isDegreeMode) Math.toDegrees(res) else res
                }
                "ln" -> {
                    if (arg <= 0.0) throw EvalException(EvalErrorType.DOMAIN_ERROR)
                    ln(arg)
                }
                "log" -> {
                    if (arg <= 0.0) throw EvalException(EvalErrorType.DOMAIN_ERROR)
                    log10(arg)
                }
                "sqrt" -> {
                    if (arg < 0.0) throw EvalException(EvalErrorType.DOMAIN_ERROR)
                    sqrt(arg)
                }
                "exp" -> {
                    val res = exp(arg)
                    if (res.isInfinite()) throw EvalException(EvalErrorType.OVERFLOW)
                    res
                }
                "tenpow" -> {
                    val res = 10.0.pow(arg)
                    if (res.isInfinite()) throw EvalException(EvalErrorType.OVERFLOW)
                    res
                }
                else -> throw EvalException(EvalErrorType.INVALID_EXPRESSION)
            }
        }

        private fun factorial(n: Double): Double {
            if (n < 0.0 || n != kotlin.math.floor(n)) {
                throw EvalException(EvalErrorType.DOMAIN_ERROR)
            }
            if (n > 170.0) {
                throw EvalException(EvalErrorType.OVERFLOW)
            }
            var res = 1.0
            for (k in 2..n.toInt()) {
                res *= k
            }
            return res
        }
    }
}
