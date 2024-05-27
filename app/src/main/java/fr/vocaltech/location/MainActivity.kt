package fr.vocaltech.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import fr.vocaltech.location.broadcasts.PositionReceiver
import fr.vocaltech.location.models.Coordinates
import fr.vocaltech.location.models.Position
import fr.vocaltech.location.navigation.BottomNavigation
import fr.vocaltech.location.navigation.Screens
import fr.vocaltech.location.navigation.screens.AnalyticsScreen
import fr.vocaltech.location.navigation.screens.HomeScreen
import fr.vocaltech.location.navigation.screens.ListScreen
import fr.vocaltech.location.navigation.screens.ProfileScreen
import fr.vocaltech.location.ui.theme.LocationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        setContent {
            LocationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocationSetup()
                }
            }
        }
    }
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
@Composable
fun LocationSetup() {
    val context = LocalContext.current

    // init model
    val application = context.applicationContext as LocationApplication
    val model: LocationModel = viewModel(
        factory = LocationModelFactory(
            application
        )
    )

    // broadcast register
    DisposableEffect(context) {
        val positionReceiver = PositionReceiver(model)

        context.registerReceiver(positionReceiver, IntentFilter("PositionService"))

        onDispose {
            context.unregisterReceiver(positionReceiver)
        }
    }

    //
    // --- retrofit - begin ---
    //

    val updatePositionsByUserId: (String) -> Unit = {
        model.positionsByUserId(it)
    }

    val deletePositionsByUserId: (String) -> Unit = {
        model.deletePositionsByUserId(it)
    }

    val positionsByUserId by model.positionsByUserId.observeAsState(emptyList())

    //
    // --- retrofit - end ---
    //

    val currentPosition by model.currentPos.observeAsState()

    val toggleStartLocationService = {
        model.toggleStartLocationService()
    }
    
    val isLocationServiceStarted by model.isLocationServiceStarted.observeAsState(initial = false)

    LocationContent(
        isLocationServiceStarted,
        toggleStartLocationService,
        currentPosition,
        updatePositionsByUserId,
        positionsByUserId,
        deletePositionsByUserId
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationContent(
    isLocationServiceStarted: Boolean,
    toggleStartLocationService: () -> Unit,
    currentPosition: Position?,
    updatePositionsByUserId: (String) -> Unit,
    positionsByUserId: List<Position>,
    deletePositionsByUserId: (String) -> Unit
) {
    // Notification permission
    val postNotificationPermission = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    LaunchedEffect(key1 = true) {
        if (!postNotificationPermission.status.isGranted) {
            postNotificationPermission.launchPermissionRequest()
        }
    }

    // Location permission
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )
    CheckAndRequestLocationPermissions(
        locationPermissionsState,
        isLocationServiceStarted,
        toggleStartLocationService,
        currentPosition,
        updatePositionsByUserId,
        positionsByUserId,
        deletePositionsByUserId
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckAndRequestLocationPermissions(
    locationPermissionsState: MultiplePermissionsState,
    isLocationServiceStarted: Boolean,
    toggleStartLocationService: () -> Unit,
    currentPosition: Position?,
    updatePositionsByUserId: (String) -> Unit,
    positionsByUserId: List<Position>,
    deletePositionsByUserId: (String) -> Unit
) {
    if (locationPermissionsState.allPermissionsGranted) {
        CurrentLocationContent(
            isLocationServiceStarted,
            toggleStartLocationService,
            currentPosition,
            updatePositionsByUserId,
            positionsByUserId,
            deletePositionsByUserId
        )
    } else {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val allPermissionsRevoked =
                locationPermissionsState.permissions.size == locationPermissionsState.revokedPermissions.size

            val textToShow = if (!allPermissionsRevoked) {
                // If not all the permissions are revoked, it's because the user
                // accepted the COARSE permission but not the FINE one
                "Need to grant the FINE location !"
            } else if (locationPermissionsState.shouldShowRationale) {
                // Both location permissions have been denied
                "Please, grant us FINE location"
            } else {
                // First time, the user sees this feature or the user doesn't want to be asked again
                "This feature requires location permission"
            }

            val buttonText = if (!allPermissionsRevoked) {
                "Allow Precise location"
            } else {
                "Request permissions"
            }

            Text(
                text = textToShow,
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(20.0F, TextUnitType.Sp)
            )
            Button(
                onClick = { locationPermissionsState.launchMultiplePermissionRequest() },
                Modifier.padding(top = 16.dp)
            ) {
                Text(buttonText)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentLocationContent(
    isLocationServiceStarted: Boolean,
    toggleStartLocationService: () -> Unit,
    currentPosition: Position?,
    updatePositionsByUserId: (String) -> Unit,
    positionsByUserId: List<Position>,
    deletePositionsByUserId: (String) -> Unit
) {
    val ctx = LocalContext.current
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigation(navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screens.Home.route,
            modifier = Modifier.padding(paddingValues = innerPadding)
        ) {
            composable(Screens.Home.route) {
                HomeScreen(
                    navController,
                    ctx,
                    innerPadding,
                    currentPosition,
                    toggleStartLocationService,
                    isLocationServiceStarted
                )
            }
            composable(Screens.List.route) {
                ListScreen(
                    navController,
                    updatePositionsByUserId,
                    positionsByUserId,
                    deletePositionsByUserId
                )
            }
            composable(Screens.Analytics.route) {
                AnalyticsScreen(navController)
            }
            composable(Screens.Profile.route) {
                ProfileScreen(navController)
            }
        }
    }
}

class WebAppInterface(private val mContext: Context?, private val currentPosition: Position?) {
    companion object {
        var currentZoom = 0
    }

    /** Show a toast from the web page  */
    @JavascriptInterface
    fun showToast(toast: String) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }

    /** Get user id */
    @JavascriptInterface
    fun userId(): String = "random userId"

    /** Get timestamp */
    @JavascriptInterface
    fun curTs(): String = currentPosition?.timestamp.toString()

    /** Get currentPosition */
    @JavascriptInterface
    fun currentPosition(): String {
        var ts = String()

        if (currentPosition != null)
            ts = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE)
            .format(Date(currentPosition.timestamp))

        return StringBuilder()
            .append("{ \"lat\": \"" + currentPosition?.coordinates?.latitude + "\", ")
            .append("\"lng\": \"" + currentPosition?.coordinates?.longitude + "\", ")
            .append("\"ts\": \"$ts\" }")
            .toString()
    }

    /**
     * Set current zoom
     */
    @JavascriptInterface
    fun setCurrentZoom(curZoom: Int) {
        currentZoom = curZoom
    }

    /**
     * Get current zoom
     */
    @JavascriptInterface
    fun currentZoom() = currentZoom
}

@Preview(showBackground = true)
@Composable
fun CurrentLocationContentPreview() {
    LocationTheme {
        CurrentLocationContent(
            isLocationServiceStarted = false,
            toggleStartLocationService = {},
            currentPosition = Position(
                Coordinates(43.599998, 1.43333),
                1707162387654,
                "track_id",
                "user_id"
            ),
            updatePositionsByUserId = {},
            positionsByUserId = emptyList(),
            deletePositionsByUserId = {}
        )
    }
}