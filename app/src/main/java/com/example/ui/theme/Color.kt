package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Light Studio Stone & Ink Palette
val LightBackground = Color(0xFFF3F2EE)
val LightDisplaySurface = Color(0xFFFFFFFF)
val LightDisplayBorder = Color(0xFFE3E0D8)
val LightInkPrimary = Color(0xFF141821)
val LightInkSecondary = Color(0xFF5D6575)
val LightDigitKey = Color(0xFFFFFFFF)
val LightDigitKeyBorder = Color(0xFFE4E1D9)
val LightFunctionKey = Color(0xFFE6E3DB)
val LightOperatorKey = Color(0xFFDCE3EE)
val LightOperatorText = Color(0xFF1D4E89)
val LightScientificKey = Color(0xFFECEAE3)
val LightClearKey = Color(0xFFFCE7E2)
val LightClearText = Color(0xFFC53916)
val LightEqualsKey = Color(0xFFE8591E)
val LightEqualsText = Color(0xFFFFFFFF)

// Dark Obsidian Precision Palette
val DarkBackground = Color(0xFF0D1017)
val DarkDisplaySurface = Color(0xFF141923)
val DarkDisplayBorder = Color(0xFF232B3A)
val DarkInkPrimary = Color(0xFFF3F5F9)
val DarkInkSecondary = Color(0xFF939DAF)
val DarkDigitKey = Color(0xFF1B212E)
val DarkDigitKeyBorder = Color(0xFF273042)
val DarkFunctionKey = Color(0xFF252D3D)
val DarkOperatorKey = Color(0xFF28344A)
val DarkOperatorText = Color(0xFF8BB8FF)
val DarkScientificKey = Color(0xFF161C27)
val DarkClearKey = Color(0xFF3A2120)
val DarkClearText = Color(0xFFFF8A70)
val DarkEqualsKey = Color(0xFFFF6B2C)
val DarkEqualsText = Color(0xFFFFFFFF)

@Immutable
data class CalculatorColors(
    val displaySurface: Color,
    val displayBorder: Color,
    val digitKey: Color,
    val digitKeyBorder: Color,
    val digitText: Color,
    val functionKey: Color,
    val functionText: Color,
    val operatorKey: Color,
    val operatorText: Color,
    val scientificKey: Color,
    val scientificText: Color,
    val clearKey: Color,
    val clearText: Color,
    val equalsKey: Color,
    val equalsText: Color,
    val accentOrange: Color,
    val subtleText: Color
)

val LightCalculatorColors = CalculatorColors(
    displaySurface = LightDisplaySurface,
    displayBorder = LightDisplayBorder,
    digitKey = LightDigitKey,
    digitKeyBorder = LightDigitKeyBorder,
    digitText = LightInkPrimary,
    functionKey = LightFunctionKey,
    functionText = LightInkPrimary,
    operatorKey = LightOperatorKey,
    operatorText = LightOperatorText,
    scientificKey = LightScientificKey,
    scientificText = LightInkPrimary,
    clearKey = LightClearKey,
    clearText = LightClearText,
    equalsKey = LightEqualsKey,
    equalsText = LightEqualsText,
    accentOrange = LightEqualsKey,
    subtleText = LightInkSecondary
)

val DarkCalculatorColors = CalculatorColors(
    displaySurface = DarkDisplaySurface,
    displayBorder = DarkDisplayBorder,
    digitKey = DarkDigitKey,
    digitKeyBorder = DarkDigitKeyBorder,
    digitText = DarkInkPrimary,
    functionKey = DarkFunctionKey,
    functionText = DarkInkPrimary,
    operatorKey = DarkOperatorKey,
    operatorText = DarkOperatorText,
    scientificKey = DarkScientificKey,
    scientificText = DarkInkPrimary,
    clearKey = DarkClearKey,
    clearText = DarkClearText,
    equalsKey = DarkEqualsKey,
    equalsText = DarkEqualsText,
    accentOrange = DarkEqualsKey,
    subtleText = DarkInkSecondary
)
