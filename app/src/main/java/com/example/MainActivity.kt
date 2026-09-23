package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.DrBenamRepository
import com.example.ui.components.AppNavDestination
import com.example.ui.components.DrBenamBottomBar
import com.example.ui.components.DrBenamTopBar
import com.example.ui.screens.AppointmentBookingScreen
import com.example.ui.screens.AppointmentsListScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.TreatmentsScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
  private val pendingDestination = MutableStateFlow<String?>(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize the server-authoritative repository.
    DrBenamRepository.init(applicationContext)

    intent?.getStringExtra("NAV_DESTINATION")?.let { target ->
      pendingDestination.value = target
    }

    setContent {
      MyApplicationTheme(darkTheme = false) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          DrBenamApp(
            pendingDestinationFlow = pendingDestination,
            onDestinationHandled = { pendingDestination.value = null }
          )
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    intent.getStringExtra("NAV_DESTINATION")?.let { target ->
      pendingDestination.value = target
    }
  }
}

@Composable
fun DrBenamApp(
  pendingDestinationFlow: MutableStateFlow<String?> = remember { MutableStateFlow(null) },
  onDestinationHandled: () -> Unit = {}
) {
  val context = LocalContext.current
  val isLoggedIn by DrBenamRepository.isLoggedIn.collectAsState()
  val user by DrBenamRepository.userProfile.collectAsState()
  val notifications by DrBenamRepository.notifications.collectAsState()
  val pendingDest by pendingDestinationFlow.collectAsState()
  val unreadNotifs = notifications.count { !it.isRead }

  var currentDestination by remember { mutableStateOf(AppNavDestination.DASHBOARD) }

  // Handle incoming notification navigation
  LaunchedEffect(pendingDest) {
    pendingDest?.let { dest ->
      currentDestination = when (dest) {
        "APPOINTMENTS" -> AppNavDestination.APPOINTMENTS
        "BOOKING" -> AppNavDestination.BOOKING
        "WALLET" -> AppNavDestination.WALLET
        "NOTIFICATIONS" -> AppNavDestination.NOTIFICATIONS
        "SUPPORT" -> AppNavDestination.SUPPORT
        "PROFILE" -> AppNavDestination.PROFILE
        "TREATMENTS" -> AppNavDestination.TREATMENTS
        else -> AppNavDestination.DASHBOARD
      }
      onDestinationHandled()
    }
  }

  // Back button handler
  BackHandler(enabled = isLoggedIn && currentDestination != AppNavDestination.DASHBOARD) {
    currentDestination = AppNavDestination.DASHBOARD
  }

  Box(modifier = Modifier.fillMaxSize()) {
    if (!isLoggedIn) {
      LoginScreen(
        onLoginSuccess = {
          currentDestination = AppNavDestination.DASHBOARD
        }
      )
    } else {
      Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
          DrBenamTopBar(
            currentDestination = currentDestination,
            userName = user.name,
            unreadNotificationsCount = unreadNotifs,
            onNotificationsClick = { currentDestination = AppNavDestination.NOTIFICATIONS },
            onProfileClick = { currentDestination = AppNavDestination.PROFILE }
          )
        },
        bottomBar = {
          DrBenamBottomBar(
            currentDestination = currentDestination,
            onDestinationSelect = { destination ->
              currentDestination = destination
            },
            onLogout = {
              DrBenamRepository.setLoggedIn(false)
            }
          )
        }
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
          Crossfade(targetState = currentDestination, label = "ScreenTransition") { destination ->
            when (destination) {
              AppNavDestination.DASHBOARD -> DashboardScreen(
                onNavigateToBooking = { currentDestination = AppNavDestination.BOOKING },
                onNavigateToAppointments = { currentDestination = AppNavDestination.APPOINTMENTS },
                onNavigateToTreatments = { currentDestination = AppNavDestination.TREATMENTS },
                onNavigateToWallet = { currentDestination = AppNavDestination.WALLET },
                onNavigateToProfile = { currentDestination = AppNavDestination.PROFILE },
                onNavigateToNotifications = { currentDestination = AppNavDestination.NOTIFICATIONS }
              )

              AppNavDestination.APPOINTMENTS -> AppointmentsListScreen(
                onBookNewAppointment = { currentDestination = AppNavDestination.BOOKING }
              )

              AppNavDestination.BOOKING -> AppointmentBookingScreen(
                onNavigateBack = { currentDestination = AppNavDestination.DASHBOARD },
                onBookingSuccess = { currentDestination = AppNavDestination.APPOINTMENTS }
              )

              AppNavDestination.TREATMENTS -> TreatmentsScreen()

              AppNavDestination.WALLET -> WalletScreen()

              AppNavDestination.NOTIFICATIONS -> NotificationsScreen(
                onNavigateToAppointments = { currentDestination = AppNavDestination.APPOINTMENTS }
              )

              AppNavDestination.SUPPORT -> SupportScreen()

              AppNavDestination.PROFILE -> ProfileScreen()
            }
          }
        }
      }
    }
  }
}
