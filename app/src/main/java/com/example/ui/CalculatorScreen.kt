package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ThemeMode
import com.example.domain.EvalErrorType
import com.example.ui.components.CalcKey
import com.example.ui.components.HistoryBottomSheet
import com.example.ui.components.KeyRole
import com.example.ui.theme.CalculatorTheme
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.SpaceGroteskFamily
import kotlinx.coroutines.launch

@Composable
fun CalculatorScreen(
    uiState: CalculatorUiState,
    onDigit: (Char) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (Char) -> Unit,
    onPercent: () -> Unit,
    onToggleSign: () -> Unit,
    onParenthesis: () -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onSquare: () -> Unit,
    onFactorial: () -> Unit,
    onReciprocal: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onEquals: () -> Unit,
    onToggleAngleMode: () -> Unit,
    onToggleInverse: () -> Unit,
    onToggleScientific: () -> Unit,
    onCycleTheme: () -> Unit,
    onReuseHistoryText: (String) -> Unit,
    onDeleteHistoryItem: (Long) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHistorySheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val copiedMessage = stringResource(R.string.copied_to_clipboard)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            val isWideLayout = maxWidth >= 600.dp || (maxWidth > maxHeight && maxWidth >= 480.dp)

            if (isWideLayout) {
                WideCalculatorLayout(
                    uiState = uiState,
                    onCopyText = { text ->
                        clipboardManager.setText(AnnotatedString(text))
                        coroutineScope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                    },
                    onOpenHistory = { showHistorySheet = true },
                    onToggleAngleMode = onToggleAngleMode,
                    onToggleInverse = onToggleInverse,
                    onCycleTheme = onCycleTheme,
                    onDigit = onDigit,
                    onDecimal = onDecimal,
                    onOperator = onOperator,
                    onPercent = onPercent,
                    onToggleSign = onToggleSign,
                    onParenthesis = onParenthesis,
                    onFunction = onFunction,
                    onConstant = onConstant,
                    onSquare = onSquare,
                    onFactorial = onFactorial,
                    onReciprocal = onReciprocal,
                    onBackspace = onBackspace,
                    onClear = onClear,
                    onEquals = onEquals
                )
            } else {
                CompactCalculatorLayout(
                    uiState = uiState,
                    onCopyText = { text ->
                        clipboardManager.setText(AnnotatedString(text))
                        coroutineScope.launch { snackbarHostState.showSnackbar(copiedMessage) }
                    },
                    onOpenHistory = { showHistorySheet = true },
                    onToggleAngleMode = onToggleAngleMode,
                    onToggleInverse = onToggleInverse,
                    onToggleScientific = onToggleScientific,
                    onCycleTheme = onCycleTheme,
                    onDigit = onDigit,
                    onDecimal = onDecimal,
                    onOperator = onOperator,
                    onPercent = onPercent,
                    onToggleSign = onToggleSign,
                    onParenthesis = onParenthesis,
                    onFunction = onFunction,
                    onConstant = onConstant,
                    onSquare = onSquare,
                    onFactorial = onFactorial,
                    onReciprocal = onReciprocal,
                    onBackspace = onBackspace,
                    onClear = onClear,
                    onEquals = onEquals
                )
            }
        }
    }

    if (showHistorySheet) {
        HistoryBottomSheet(
            history = uiState.history,
            onDismiss = { showHistorySheet = false },
            onSelectText = onReuseHistoryText,
            onDeleteItem = onDeleteHistoryItem,
            onClearAll = onClearHistory
        )
    }
}

@Composable
private fun CompactCalculatorLayout(
    uiState: CalculatorUiState,
    onCopyText: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onToggleAngleMode: () -> Unit,
    onToggleInverse: () -> Unit,
    onToggleScientific: () -> Unit,
    onCycleTheme: () -> Unit,
    onDigit: (Char) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (Char) -> Unit,
    onPercent: () -> Unit,
    onToggleSign: () -> Unit,
    onParenthesis: () -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onSquare: () -> Unit,
    onFactorial: () -> Unit,
    onReciprocal: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onEquals: () -> Unit
) {
    Column(
        modifier = Modifier
            .widthIn(max = 520.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        UtilityTopBar(
            isDegreeMode = uiState.isDegreeMode,
            isScientificExpanded = uiState.isScientificExpanded,
            showScientificToggle = true,
            themeMode = uiState.themeMode,
            historyCount = uiState.history.size,
            onToggleAngleMode = onToggleAngleMode,
            onToggleScientific = onToggleScientific,
            onCycleTheme = onCycleTheme,
            onOpenHistory = onOpenHistory
        )

        CalculatorDisplayCard(
            expression = uiState.expression,
            liveResult = uiState.liveResult,
            errorType = uiState.errorType,
            justEvaluated = uiState.justEvaluated,
            onCopyText = onCopyText,
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (uiState.isScientificExpanded) 0.25f else 0.34f)
        )

        AnimatedVisibility(
            visible = uiState.isScientificExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            ScientificKeypadPanel(
                isInverseMode = uiState.isInverseMode,
                onToggleInverse = onToggleInverse,
                onFunction = onFunction,
                onConstant = onConstant,
                onSquare = onSquare,
                onFactorial = onFactorial,
                onReciprocal = onReciprocal,
                onParenthesis = onParenthesis,
                onOperator = onOperator,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(164.dp)
            )
        }

        StandardKeypadGrid(
            onDigit = onDigit,
            onDecimal = onDecimal,
            onOperator = onOperator,
            onPercent = onPercent,
            onToggleSign = onToggleSign,
            onBackspace = onBackspace,
            onClear = onClear,
            onEquals = onEquals,
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (uiState.isScientificExpanded) 0.56f else 0.66f)
        )
    }
}

@Composable
private fun WideCalculatorLayout(
    uiState: CalculatorUiState,
    onCopyText: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onToggleAngleMode: () -> Unit,
    onToggleInverse: () -> Unit,
    onCycleTheme: () -> Unit,
    onDigit: (Char) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (Char) -> Unit,
    onPercent: () -> Unit,
    onToggleSign: () -> Unit,
    onParenthesis: () -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onSquare: () -> Unit,
    onFactorial: () -> Unit,
    onReciprocal: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onEquals: () -> Unit
) {
    Column(
        modifier = Modifier
            .widthIn(max = 960.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        UtilityTopBar(
            isDegreeMode = uiState.isDegreeMode,
            isScientificExpanded = true,
            showScientificToggle = false,
            themeMode = uiState.themeMode,
            historyCount = uiState.history.size,
            onToggleAngleMode = onToggleAngleMode,
            onToggleScientific = {},
            onCycleTheme = onCycleTheme,
            onOpenHistory = onOpenHistory
        )

        CalculatorDisplayCard(
            expression = uiState.expression,
            liveResult = uiState.liveResult,
            errorType = uiState.errorType,
            justEvaluated = uiState.justEvaluated,
            onCopyText = onCopyText,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.32f)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.68f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ScientificKeypadPanel(
                isInverseMode = uiState.isInverseMode,
                onToggleInverse = onToggleInverse,
                onFunction = onFunction,
                onConstant = onConstant,
                onSquare = onSquare,
                onFactorial = onFactorial,
                onReciprocal = onReciprocal,
                onParenthesis = onParenthesis,
                onOperator = onOperator,
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight()
            )
            StandardKeypadGrid(
                onDigit = onDigit,
                onDecimal = onDecimal,
                onOperator = onOperator,
                onPercent = onPercent,
                onToggleSign = onToggleSign,
                onBackspace = onBackspace,
                onClear = onClear,
                onEquals = onEquals,
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun UtilityTopBar(
    isDegreeMode: Boolean,
    isScientificExpanded: Boolean,
    showScientificToggle: Boolean,
    themeMode: ThemeMode,
    historyCount: Int,
    onToggleAngleMode: () -> Unit,
    onToggleScientific: () -> Unit,
    onCycleTheme: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val calcColors = CalculatorTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onToggleAngleMode,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("angle_mode_button"),
                shape = RoundedCornerShape(12.dp),
                color = calcColors.scientificKey,
                border = BorderStroke(1.dp, calcColors.displayBorder)
            ) {
                Text(
                    text = if (isDegreeMode) stringResource(R.string.angle_deg) else stringResource(R.string.angle_rad),
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge,
                    color = calcColors.accentOrange,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            if (showScientificToggle) {
                Surface(
                    onClick = onToggleScientific,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("scientific_toggle_button"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isScientificExpanded) {
                        calcColors.accentOrange.copy(alpha = 0.16f)
                    } else {
                        calcColors.scientificKey
                    },
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isScientificExpanded) calcColors.accentOrange else calcColors.displayBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Functions,
                            contentDescription = if (isScientificExpanded) {
                                stringResource(R.string.toggle_standard)
                            } else {
                                stringResource(R.string.toggle_scientific)
                            },
                            tint = if (isScientificExpanded) calcColors.accentOrange else calcColors.digitText,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "SCI",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isScientificExpanded) calcColors.accentOrange else calcColors.digitText
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCycleTheme,
                modifier = Modifier.testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = when (themeMode) {
                        ThemeMode.SYSTEM -> Icons.Outlined.BrightnessAuto
                        ThemeMode.DARK -> Icons.Outlined.DarkMode
                        ThemeMode.LIGHT -> Icons.Outlined.LightMode
                    },
                    contentDescription = stringResource(R.string.theme_toggle),
                    tint = calcColors.subtleText
                )
            }

            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier.testTag("history_button")
            ) {
                BadgedBox(
                    badge = {
                        if (historyCount > 0) {
                            Badge(
                                containerColor = calcColors.accentOrange,
                                contentColor = calcColors.equalsText
                            ) {
                                Text(text = if (historyCount > 99) "99+" else historyCount.toString())
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = stringResource(R.string.history_title),
                        tint = calcColors.digitText
                    )
                }
            }
        }
    }
}

@Composable
private fun CalculatorDisplayCard(
    expression: String,
    liveResult: String,
    errorType: EvalErrorType?,
    justEvaluated: Boolean,
    onCopyText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val calcColors = CalculatorTheme.colors
    val scrollState = rememberScrollState()

    LaunchedEffect(expression) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    val errorText = when (errorType) {
        EvalErrorType.DIVIDE_BY_ZERO -> stringResource(R.string.error_divide_by_zero)
        EvalErrorType.DOMAIN_ERROR -> stringResource(R.string.error_domain)
        EvalErrorType.INVALID_EXPRESSION -> stringResource(R.string.error_invalid)
        EvalErrorType.OVERFLOW -> stringResource(R.string.error_overflow)
        null -> null
    }

    val primaryFontSize = when {
        expression.length > 18 -> 28.sp
        expression.length > 12 -> 36.sp
        else -> 48.sp
    }

    Surface(
        modifier = modifier.testTag("display_card"),
        shape = RoundedCornerShape(24.dp),
        color = calcColors.displaySurface,
        border = BorderStroke(1.dp, calcColors.displayBorder),
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            val copyTarget = liveResult.ifEmpty { expression }
            if (copyTarget.isNotEmpty() && errorText == null) {
                IconButton(
                    onClick = { onCopyText(copyTarget) },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(48.dp)
                        .testTag("copy_result_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = stringResource(R.string.copy_result),
                        tint = calcColors.subtleText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = expression.ifEmpty { "0" },
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = if (justEvaluated) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = primaryFontSize,
                        lineHeight = (primaryFontSize.value + 6).sp,
                        color = if (expression.isEmpty()) {
                            calcColors.subtleText.copy(alpha = 0.5f)
                        } else {
                            calcColors.digitText
                        },
                        maxLines = 1,
                        textAlign = TextAlign.End,
                        modifier = Modifier.testTag("display_expression")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                when {
                    errorText != null -> {
                        Text(
                            text = errorText,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.titleMedium,
                            color = calcColors.clearText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("display_error")
                        )
                    }
                    liveResult.isNotEmpty() -> {
                        Text(
                            text = "= $liveResult",
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 24.sp,
                            color = calcColors.accentOrange,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("display_live_result")
                        )
                    }
                    else -> {
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ScientificKeypadPanel(
    isInverseMode: Boolean,
    onToggleInverse: () -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onSquare: () -> Unit,
    onFactorial: () -> Unit,
    onReciprocal: () -> Unit,
    onParenthesis: () -> Unit,
    onOperator: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcKey(
                label = "INV",
                role = KeyRole.SCIENTIFIC,
                isActive = isInverseMode,
                testTag = "key_inv",
                contentDescriptionText = stringResource(R.string.inverse_toggle),
                fontSize = 14.sp,
                cornerRadius = 14.dp,
                onClick = onToggleInverse,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = if (isInverseMode) "sin⁻¹" else "sin",
                role = KeyRole.SCIENTIFIC,
                testTag = "key_sin",
                fontSize = 15.sp,
                cornerRadius = 14.dp,
                onClick = { onFunction(if (isInverseMode) "sin⁻¹" else "sin") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = if (isInverseMode) "cos⁻¹" else "cos",
                role = KeyRole.SCIENTIFIC,
                testTag = "key_cos",
                fontSize = 15.sp,
                cornerRadius = 14.dp,
                onClick = { onFunction(if (isInverseMode) "cos⁻¹" else "cos") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = if (isInverseMode) "tan⁻¹" else "tan",
                role = KeyRole.SCIENTIFIC,
                testTag = "key_tan",
                fontSize = 15.sp,
                cornerRadius = 14.dp,
                onClick = { onFunction(if (isInverseMode) "tan⁻¹" else "tan") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "π",
                role = KeyRole.SCIENTIFIC,
                testTag = "key_pi",
                fontSize = 17.sp,
                cornerRadius = 14.dp,
                onClick = { onConstant("π") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcKey(
                label = if (isInverseMode) "eˣ" else "ln",
                role = KeyRole.SCIENTIFIC,
                testTag = "key_ln",
                fontSize = 15.sp,
                cornerRadius = 14.dp,
                onClick = { onFunction(if (isInverseMode) "e^" else "ln") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = if (isInverseMode) "10ˣ" else "log",
                role = KeyRole.SCIENTIFIC,
                testTag = "key_log",
                fontSize = 15.sp,
                cornerRadius = 14.dp,
                onClick = { onFunction(if (isInverseMode) "10^" else "log") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = if (isInverseMode) "x²" else "√",
                role = KeyRole.SCIENTIFIC,
                testTag = "key_sqrt",
                fontSize = 16.sp,
                cornerRadius = 14.dp,
                onClick = { if (isInverseMode) onSquare() else onFunction("√") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "xʸ",
                role = KeyRole.SCIENTIFIC,
                testTag = "key_power",
                fontSize = 15.sp,
                cornerRadius = 14.dp,
                onClick = { onOperator('^') },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "e",
                role = KeyRole.SCIENTIFIC,
                testTag = "key_e",
                fontSize = 16.sp,
                cornerRadius = 14.dp,
                onClick = { onConstant("e") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcKey(
                label = "( )",
                role = KeyRole.SCIENTIFIC,
                testTag = "key_parens",
                fontSize = 15.sp,
                cornerRadius = 14.dp,
                onClick = onParenthesis,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "x!",
                role = KeyRole.SCIENTIFIC,
                testTag = "key_factorial",
                fontSize = 15.sp,
                cornerRadius = 14.dp,
                onClick = onFactorial,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "1/x",
                role = KeyRole.SCIENTIFIC,
                testTag = "key_reciprocal",
                fontSize = 15.sp,
                cornerRadius = 14.dp,
                onClick = onReciprocal,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "x²",
                role = KeyRole.SCIENTIFIC,
                testTag = "key_square",
                fontSize = 15.sp,
                cornerRadius = 14.dp,
                onClick = onSquare,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "√x",
                role = KeyRole.SCIENTIFIC,
                testTag = "key_sqrt_alt",
                fontSize = 15.sp,
                cornerRadius = 14.dp,
                onClick = { onFunction("√") },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun StandardKeypadGrid(
    onDigit: (Char) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (Char) -> Unit,
    onPercent: () -> Unit,
    onToggleSign: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onEquals: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: AC, Backspace, %, ÷
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcKey(
                label = "AC",
                role = KeyRole.CLEAR,
                testTag = "key_ac",
                fontSize = 20.sp,
                onClick = onClear,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "",
                icon = Icons.AutoMirrored.Rounded.Backspace,
                contentDescriptionText = stringResource(R.string.backspace_desc),
                role = KeyRole.FUNCTION,
                testTag = "key_backspace",
                onClick = onBackspace,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "%",
                role = KeyRole.FUNCTION,
                testTag = "key_percent",
                fontSize = 22.sp,
                onClick = onPercent,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "÷",
                role = KeyRole.OPERATOR,
                testTag = "key_divide",
                fontSize = 26.sp,
                onClick = { onOperator('÷') },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcKey(
                label = "7",
                role = KeyRole.DIGIT,
                testTag = "key_7",
                onClick = { onDigit('7') },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "8",
                role = KeyRole.DIGIT,
                testTag = "key_8",
                onClick = { onDigit('8') },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "9",
                role = KeyRole.DIGIT,
                testTag = "key_9",
                onClick = { onDigit('9') },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "×",
                role = KeyRole.OPERATOR,
                testTag = "key_multiply",
                fontSize = 26.sp,
                onClick = { onOperator('×') },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcKey(
                label = "4",
                role = KeyRole.DIGIT,
                testTag = "key_4",
                onClick = { onDigit('4') },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "5",
                role = KeyRole.DIGIT,
                testTag = "key_5",
                onClick = { onDigit('5') },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "6",
                role = KeyRole.DIGIT,
                testTag = "key_6",
                onClick = { onDigit('6') },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "−",
                role = KeyRole.OPERATOR,
                testTag = "key_minus",
                fontSize = 26.sp,
                onClick = { onOperator('−') },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcKey(
                label = "1",
                role = KeyRole.DIGIT,
                testTag = "key_1",
                onClick = { onDigit('1') },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "2",
                role = KeyRole.DIGIT,
                testTag = "key_2",
                onClick = { onDigit('2') },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "3",
                role = KeyRole.DIGIT,
                testTag = "key_3",
                onClick = { onDigit('3') },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "+",
                role = KeyRole.OPERATOR,
                testTag = "key_plus",
                fontSize = 26.sp,
                onClick = { onOperator('+') },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }

        // Row 5: ±, 0, ., =
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcKey(
                label = "±",
                role = KeyRole.FUNCTION,
                testTag = "key_toggle_sign",
                fontSize = 22.sp,
                onClick = onToggleSign,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "0",
                role = KeyRole.DIGIT,
                testTag = "key_0",
                onClick = { onDigit('0') },
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = ".",
                role = KeyRole.DIGIT,
                testTag = "key_decimal",
                fontSize = 28.sp,
                onClick = onDecimal,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            CalcKey(
                label = "=",
                role = KeyRole.EQUALS,
                testTag = "key_equals",
                fontSize = 28.sp,
                onClick = onEquals,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}
