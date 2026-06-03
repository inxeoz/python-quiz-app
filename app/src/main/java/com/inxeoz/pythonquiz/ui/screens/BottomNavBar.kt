package com.inxeoz.pythonquiz.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inxeoz.pythonquiz.ui.theme.LocalQuizColors

enum class BottomNavItem { Home, Flagged, Profile }

@Composable
fun BottomNavBar(
    activeItem: BottomNavItem,
    onHomeClick: () -> Unit,
    onFlaggedClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuizColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 430.dp)
            .padding(top = 12.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavBarItem(
            icon = Icons.Default.Home,
            label = "Home",
            isActive = activeItem == BottomNavItem.Home,
            onClick = onHomeClick
        )
        NavBarItem(
            icon = Icons.Default.Flag,
            label = "Flagged",
            isActive = activeItem == BottomNavItem.Flagged,
            onClick = onFlaggedClick
        )
        NavBarItem(
            icon = Icons.Default.Person,
            label = "Profile",
            isActive = activeItem == BottomNavItem.Profile,
            onClick = onProfileClick
        )
    }
}

@Composable
private fun NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalQuizColors.current
    val tint = if (isActive) colors.accent else colors.textMuted

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(22.dp),
            tint = tint
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = tint
        )
    }
}

@Composable
fun ScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuizColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            shape = CircleShape,
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border),
            onClick = onBack
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.text,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )

    }
}

@Composable
fun ThemeToggle(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalQuizColors.current
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .background(colors.surface, CircleShape)
            .border(1.dp, colors.border, CircleShape)
    ) {
        Icon(
            Icons.Default.LightMode,
            contentDescription = "Toggle theme",
            tint = colors.textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
