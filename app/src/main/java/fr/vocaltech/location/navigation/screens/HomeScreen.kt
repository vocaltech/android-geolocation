package fr.vocaltech.location.navigation.screens

import android.content.Context
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import fr.vocaltech.location.R
import fr.vocaltech.location.WebAppInterface
import fr.vocaltech.location.models.Position
import fr.vocaltech.location.ui.theme.LocationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavHostController,
    context: Context,
    innerPadding: PaddingValues,
    currentPosition: Position?,
    toggleStartLocationService: () -> Unit,
    isLocationServiceStarted: Boolean
) {
    LocationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeContent(
                navController,
                context,
                innerPadding,
                currentPosition,
                toggleStartLocationService,
                isLocationServiceStarted
            )
        }
    }
}

@Composable
fun HomeContent(
    navController: NavHostController,
    context: Context,
    innerPadding: PaddingValues,
    currentPosition: Position?,
    toggleStartLocationService: () -> Unit,
    isLocationServiceStarted: Boolean
) {
    val isPreviewMode = LocalInspectionMode.current

    Column(
        Modifier
            .fillMaxSize()
            .animateContentSize()
            .padding(innerPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
            onClick = toggleStartLocationService
        ) {
            Text(
                text = if (isLocationServiceStarted)
                    "Stop service"
                else
                    "Start service"
            )
        }

        if (! isPreviewMode) {
            AndroidView(
                modifier = Modifier.height(272.dp),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        webViewClient = WebViewClient()
                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                                Log.d(
                                    "HomeScreen.kt", "${message.message()} -- From line " +
                                            "${message.lineNumber()} of ${message.sourceId()}"
                                )
                                return true
                            }
                        }

                        addJavascriptInterface(
                            WebAppInterface(context, currentPosition),
                            "Android"
                        )

                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.setSupportZoom(true)
                    }
                },
                update = { webView ->
                    webView.addJavascriptInterface(
                        WebAppInterface(context, currentPosition),
                        "Android"
                    )
                    //webView.loadUrl("https://lflet.vocality.fr")
                    webView.loadUrl("file:///android_asset/index.html")
                }
            )
        } else {
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(272.dp),
                painter = painterResource(id = R.drawable.leaflet),
                contentDescription = "leaflet.png"
            )
        }

        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .size(width = 320.dp, height = 140.dp)
                .padding(top = 16.dp)
        ) {
            Text(
                modifier = Modifier
                    .padding(top = 16.dp, start = 8.dp)
                    .align(Alignment.CenterHorizontally),
                fontSize = 16.sp,
                text = if (currentPosition != null) {
                    "latitude: ${currentPosition.coordinates?.latitude}\n" +
                            "longitude: ${currentPosition.coordinates?.longitude}\n" +
                            "timestamp: ${currentPosition.timestamp}\n" +
                            "date: ${
                                SimpleDateFormat("yyyy-mm-dd HH:mm:ss", Locale.FRANCE).format(
                                    Date(currentPosition.timestamp)
                                )
                            }"
                } else {
                    "No position available.\nPlease start the service !"
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeContentPreviewNoPosition() {
    LocationTheme {
        HomeContent(
            navController = rememberNavController(),
            context = LocalContext.current,
            innerPadding = PaddingValues(8.dp),
            currentPosition = null,
            toggleStartLocationService = {},
            isLocationServiceStarted = false
        )
    }
}