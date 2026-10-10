package com.navira.app.ui.screens.map

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.navira.app.BuildConfig
import com.navira.app.data.location.LatLngPoint
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

class NaviraWebBridge(
    private val onMapLoaded: () -> Unit,
    private val onPan: () -> Unit
) {
    @JavascriptInterface
    fun onMapReady() {
        onMapLoaded()
    }

    @JavascriptInterface
    fun onUserPan() {
        onPan()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LeafletMapView(
    modifier: Modifier = Modifier,
    currentLat: Double?,
    currentLng: Double?,
    currentAccuracy: Float,
    currentBearing: Float,
    startLat: Double?,
    startLng: Double?,
    mapStyle: String, // "real_life" or "clean_white"
    recenterTrigger: Long = 0L,
    highlightPoints: List<LatLngPoint>? = null,
    onUserPanned: () -> Unit = {}
) {
    val context = LocalContext.current
    val assetLoader = remember {
        WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .build()
    }

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // Security Hardening
            settings.apply {
                javaScriptEnabled = true
                allowFileAccess = false
                allowContentAccess = false
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
            }

            if (BuildConfig.DEBUG) {
                WebView.setWebContentsDebuggingEnabled(true)
            }

            addJavascriptInterface(
                NaviraWebBridge(
                    onMapLoaded = {
                        Timber.d("Leaflet Map is ready")
                    },
                    onPan = {
                        onUserPanned()
                    }
                ),
                "NaviraBridge"
            )

            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? {
                    return assetLoader.shouldInterceptRequest(request.url)
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    // Block all navigation outside appassets
                    val host = request.url.host
                    return host != "appassets.androidplatform.net"
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    val initLat = currentLat ?: -6.2088
                    val initLng = currentLng ?: 106.8456
                    view?.evaluateJavascript(
                        "initNaviraMap($initLat, $initLng, '$mapStyle');",
                        null
                    )
                }
            }

            loadUrl("https://appassets.androidplatform.net/assets/map/map.html")
        }
    }

    // Update Start Location
    LaunchedEffect(startLat, startLng) {
        if (startLat != null && startLng != null) {
            webView.evaluateJavascript(
                "setStartLocation($startLat, $startLng);",
                null
            )
        }
    }

    // Update Current Location
    LaunchedEffect(currentLat, currentLng, currentAccuracy, currentBearing) {
        if (currentLat != null && currentLng != null) {
            webView.evaluateJavascript(
                "updateLocation($currentLat, $currentLng, $currentAccuracy, $currentBearing);",
                null
            )
        }
    }

    // Switch Map Style
    LaunchedEffect(mapStyle) {
        webView.evaluateJavascript(
            "setMapStyle('$mapStyle');",
            null
        )
    }

    // Recenter map
    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger > 0L) {
            webView.evaluateJavascript("recenterMap();", null)
        }
    }

    // Highlight route for Pulang ke Start
    LaunchedEffect(highlightPoints) {
        if (highlightPoints != null && highlightPoints.isNotEmpty()) {
            val jsonArray = JSONArray()
            for (p in highlightPoints) {
                val obj = JSONObject()
                obj.put("lat", p.latitude)
                obj.put("lng", p.longitude)
                jsonArray.put(obj)
            }
            webView.evaluateJavascript(
                "highlightRoute('${jsonArray}');",
                null
            )
        }
    }

    DisposableEffect(Unit) {
        webView.onResume()
        onDispose {
            webView.onPause()
            webView.destroy()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}
