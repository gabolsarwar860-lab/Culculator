package com.example

import com.example.domain.EvalErrorType
import com.example.domain.EvalResult
import com.example.domain.ExpressionEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpressionEvaluatorTest {

    @Test
    fun `basic arithmetic respects operator precedence`() {
        val result = ExpressionEvaluator.evaluate("2 + 3 × 4 − 6 ÷ 2")
        assertTrue(result is EvalResult.Success)
        assertEquals(11.0, (result as EvalResult.Success).value, 1e-9)
        assertEquals("11", result.formatted)
    }

    @Test
    fun `floating point addition avoids binary noise`() {
        val result = ExpressionEvaluator.evaluate("0.1 + 0.2")
        assertTrue(result is EvalResult.Success)
        assertEquals("0.3", (result as EvalResult.Success).formatted)
    }

    @Test
    fun `calculator percentage semantics on addition and subtraction`() {
        val addPct = ExpressionEvaluator.evaluate("200 + 10%") as EvalResult.Success
        assertEquals(220.0, addPct.value, 1e-9)

        val subPct = ExpressionEvaluator.evaluate("200 − 15%") as EvalResult.Success
        assertEquals(170.0, subPct.value, 1e-9)

        val standalonePct = ExpressionEvaluator.evaluate("50%") as EvalResult.Success
        assertEquals(0.5, standalonePct.value, 1e-9)
    }

    @Test
    fun `implicit multiplication and auto-closing parentheses work`() {
        val res1 = ExpressionEvaluator.evaluate("2(3 + 4") as EvalResult.Success
        assertEquals(14.0, res1.value, 1e-9)

        val res2 = ExpressionEvaluator.evaluate("(2+3)(4+1)") as EvalResult.Success
        assertEquals(25.0, res2.value, 1e-9)
    }

    @Test
    fun `trigonometry respects degree and radian modes`() {
        val sinDeg = ExpressionEvaluator.evaluate("sin(30)", isDegreeMode = true) as EvalResult.Success
        assertEquals(0.5, sinDeg.value, 1e-9)

        val cosDeg = ExpressionEvaluator.evaluate("cos(90)", isDegreeMode = true) as EvalResult.Success
        assertEquals(0.0, cosDeg.value, 1e-9)

        val sinRad = ExpressionEvaluator.evaluate("sin(π ÷ 2)", isDegreeMode = false) as EvalResult.Success
        assertEquals(1.0, sinRad.value, 1e-9)
    }

    @Test
    fun `scientific functions and factorial evaluate accurately`() {
        val fact = ExpressionEvaluator.evaluate("5!") as EvalResult.Success
        assertEquals(120.0, fact.value, 1e-9)

        val logRes = ExpressionEvaluator.evaluate("log(1000)") as EvalResult.Success
        assertEquals(3.0, logRes.value, 1e-9)

        val sqrtRes = ExpressionEvaluator.evaluate("√(144)") as EvalResult.Success
        assertEquals(12.0, sqrtRes.value, 1e-9)
    }

    @Test
    fun `division by zero and domain errors are reported cleanly`() {
        val divZero = ExpressionEvaluator.evaluate("10 ÷ 0")
        assertEquals(EvalResult.Error(EvalErrorType.DIVIDE_BY_ZERO), divZero)

        val negSqrt = ExpressionEvaluator.evaluate("√(−9)")
        assertEquals(EvalResult.Error(EvalErrorType.DOMAIN_ERROR), negSqrt)

        val tan90 = ExpressionEvaluator.evaluate("tan(90)", isDegreeMode = true)
        assertEquals(EvalResult.Error(EvalErrorType.DOMAIN_ERROR), tan90)
    }
}
