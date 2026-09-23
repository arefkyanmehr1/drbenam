package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DrBenamPrimary
import com.example.ui.theme.DrBenamPrimaryDark
import com.example.ui.theme.DrBenamPrimarySoft

enum class AppNavDestination {
  DASHBOARD,
  APPOINTMENTS,
  BOOKING,
  TREATMENTS,
  WALLET,
  NOTIFICATIONS,
  SUPPORT,
  PROFILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrBenamTopBar(
  currentDestination: AppNavDestination,
  userName: String,
  unreadNotificationsCount: Int,
  onNotificationsClick: () -> Unit,
  onProfileClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  TopAppBar(
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(DrBenamPrimarySoft),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = DrBenamPrimaryDark,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "مطب دکتر ابراهیم بنام",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }
      }
    },
    actions = {
      // Notifications Button with badge
      IconButton(onClick = onNotificationsClick) {
        BadgedBox(
          badge = {
            if (unreadNotificationsCount > 0) {
              Badge(
                containerColor = Color(0xFFEF4444)
              ) {
                Text(unreadNotificationsCount.toString(), color = Color.White, fontSize = 9.sp)
              }
            }
          }
        ) {
          Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "اعلان‌ها",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      // Profile avatar button
      IconButton(onClick = onProfileClick) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(DrBenamPrimary),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = userName.take(1).ifEmpty { "ک" },
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
        }
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    modifier = modifier
  )
}

@Composable
fun DrBenamBottomBar(
  currentDestination: AppNavDestination,
  onDestinationSelect: (AppNavDestination) -> Unit,
  onLogout: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showMoreMenu by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    NavigationBar(
      modifier = Modifier.fillMaxWidth(),
      containerColor = MaterialTheme.colorScheme.surface
    ) {
      NavigationBarItem(
        selected = currentDestination == AppNavDestination.DASHBOARD,
        onClick = { onDestinationSelect(AppNavDestination.DASHBOARD) },
        icon = { Icon(Icons.Default.Dashboard, contentDescription = "پیشخوان") },
        label = { Text("پیشخوان", fontSize = 10.sp) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = DrBenamPrimaryDark,
          selectedTextColor = DrBenamPrimaryDark,
          indicatorColor = DrBenamPrimarySoft
        )
      )

      NavigationBarItem(
        selected = currentDestination == AppNavDestination.APPOINTMENTS,
        onClick = { onDestinationSelect(AppNavDestination.APPOINTMENTS) },
        icon = { Icon(Icons.Default.EventNote, contentDescription = "نوبت‌های من") },
        label = { Text("نوبت‌های من", fontSize = 10.sp) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = DrBenamPrimaryDark,
          selectedTextColor = DrBenamPrimaryDark,
          indicatorColor = DrBenamPrimarySoft
        )
      )

      NavigationBarItem(
        selected = currentDestination == AppNavDestination.BOOKING,
        onClick = { onDestinationSelect(AppNavDestination.BOOKING) },
        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "نوبت جدید") },
        label = { Text("نوبت جدید", fontSize = 10.sp) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = DrBenamPrimaryDark,
          selectedTextColor = DrBenamPrimaryDark,
          indicatorColor = DrBenamPrimarySoft
        )
      )

      NavigationBarItem(
        selected = currentDestination == AppNavDestination.TREATMENTS,
        onClick = { onDestinationSelect(AppNavDestination.TREATMENTS) },
        icon = { Icon(Icons.Default.Description, contentDescription = "سوابق") },
        label = { Text("سوابق درمان", fontSize = 10.sp) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = DrBenamPrimaryDark,
          selectedTextColor = DrBenamPrimaryDark,
          indicatorColor = DrBenamPrimarySoft
        )
      )

      val isMoreActive = currentDestination in listOf(
        AppNavDestination.WALLET,
        AppNavDestination.NOTIFICATIONS,
        AppNavDestination.SUPPORT,
        AppNavDestination.PROFILE
      )

      NavigationBarItem(
        selected = isMoreActive,
        onClick = { showMoreMenu = true },
        icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "بیشتر") },
        label = { Text("بیشتر", fontSize = 10.sp) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = DrBenamPrimaryDark,
          selectedTextColor = DrBenamPrimaryDark,
          indicatorColor = DrBenamPrimarySoft
        )
      )
    }

    // Popup for "More" destinations matching mobile sidebar.php
    DropdownMenu(
      expanded = showMoreMenu,
      onDismissRequest = { showMoreMenu = false }
    ) {
      DropdownMenuItem(
        text = { Text("کیف پول من", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
        onClick = {
          showMoreMenu = false
          onDestinationSelect(AppNavDestination.WALLET)
        },
        leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = DrBenamPrimary) }
      )
      DropdownMenuItem(
        text = { Text("اعلان‌ها", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
        onClick = {
          showMoreMenu = false
          onDestinationSelect(AppNavDestination.NOTIFICATIONS)
        },
        leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null, tint = DrBenamPrimary) }
      )
      DropdownMenuItem(
        text = { Text("مرکز پشتیبانی", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
        onClick = {
          showMoreMenu = false
          onDestinationSelect(AppNavDestination.SUPPORT)
        },
        leadingIcon = { Icon(Icons.Default.SupportAgent, contentDescription = null, tint = DrBenamPrimary) }
      )
      DropdownMenuItem(
        text = { Text("پروفایل من", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
        onClick = {
          showMoreMenu = false
          onDestinationSelect(AppNavDestination.PROFILE)
        },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = DrBenamPrimary) }
      )
      DropdownMenuItem(
        text = { Text("خروج از حساب", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)) },
        onClick = {
          showMoreMenu = false
          onLogout()
        }
      )
    }
  }
}
