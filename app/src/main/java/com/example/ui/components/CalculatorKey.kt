package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalculatorTheme
import com.example.ui.theme.SpaceGroteskFamily

enum class KeyRole {
    DIGIT,
    FUNCTION,
    OPERATOR,
    SCIENTIFIC,
    CLEAR,
    EQUALS
}

@Composable
fun CalcKey(
    label: String,
    role: KeyRole,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    contentDescriptionText: String = label,
    isActive: Boolean = false,
    fontSize: TextUnit = 24.sp,
    cornerRadius: Dp = 20.dp
) {
    val colors = CalculatorTheme.colors
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "key_scale"
    )

    val containerColor: Color = when {
        isActive -> colors.accentOrange.copy(alpha = 0.22f)
        role == KeyRole.DIGIT -> colors.digitKey
        role == KeyRole.FUNCTION -> colors.functionKey
        role == KeyRole.OPERATOR -> colors.operatorKey
        role == KeyRole.SCIENTIFIC -> colors.scientificKey
        role == KeyRole.CLEAR -> colors.clearKey
        role == KeyRole.EQUALS -> colors.equalsKey
        else -> colors.digitKey
    }

    val contentColor: Color = when {
        isActive -> colors.accentOrange
        role == KeyRole.DIGIT -> colors.digitText
        role == KeyRole.FUNCTION -> colors.functionText
        role == KeyRole.OPERATOR -> colors.operatorText
        role == KeyRole.SCIENTIFIC -> colors.scientificText
        role == KeyRole.CLEAR -> colors.clearText
        role == KeyRole.EQUALS -> colors.equalsText
        else -> colors.digitText
    }

    val borderStroke: BorderStroke? = when {
        isActive -> BorderStroke(1.2.dp, colors.accentOrange.copy(alpha = 0.6f))
        role == KeyRole.DIGIT || role == KeyRole.SCIENTIFIC ->
            BorderStroke(1.dp, colors.digitKeyBorder)
        else -> null
    }

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier
            .minimumInteractiveComponentSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .testTag(testTag)
            .semantics { contentDescription = contentDescriptionText },
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        contentColor = contentColor,
        border = borderStroke,
        tonalElevation = if (role == KeyRole.DIGIT) 1.dp else 0.dp,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescriptionText,
                    tint = contentColor
                )
            } else {
                Text(
                    text = label,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = when (role) {
                        KeyRole.EQUALS, KeyRole.OPERATOR, KeyRole.CLEAR -> FontWeight.SemiBold
                        KeyRole.SCIENTIFIC -> FontWeight.Medium
                        else -> FontWeight.Medium
                    },
                    fontSize = fontSize,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}
