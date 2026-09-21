package com.msfarm.register.engine

import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import android.webkit.*
import kotlinx.coroutines.*
import kotlin.random.Random

object MicrosoftRegisterEngine {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun startFarm(context: Context) {
        scope.launch {
            val tempEmail = "test_${System.currentTimeMillis()}@outlook.com"
            val webView = WebView(context)
            setupWebView(webView)
            webView.loadUrl("https://signup.live.com/")
        }
    }

    private fun setupWebView(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
        }
        webView.addJavascriptInterface(Px2JsBridge(), "AndroidBridge")
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectAntiFingerprint(view)
            }
            
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                val url = request.url.toString()
                if (url.contains("perimeterx.net") || url.contains("px-captcha")) {
                    android.util.Log.d("PX2-Bypass", "Intercepted PX request: $url")
                }
                return super.shouldInterceptRequest(view, request)
            }
        }
    }

    private fun injectAntiFingerprint(view: WebView?) {
        val js = """
        (function() {
            Object.defineProperty(navigator, 'webdriver', {get: () => undefined});
            const originalToDataURL = HTMLCanvasElement.prototype.toDataURL;
            HTMLCanvasElement.prototype.toDataURL = function(type) {
                const context = this.getContext('2d');
                if (context) {
                    const style = context.fillStyle;
                    context.fillStyle = 'rgba(255,255,255,0.01)';
                    context.fillRect(0, 0, 1, 1);
                    context.fillStyle = style;
                }
                return originalToDataURL.apply(this, arguments);
            };
            window.__px_touch_simulated = true;
        })();
        """.trimIndent()
        view?.evaluateJavascript(js, null)
    }

    fun simulateHumanPress(webView: WebView, x: Float, y: Float) {
        val downTime = SystemClock.uptimeMillis()
        val downEvent = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, y, 0)
        webView.dispatchTouchEvent(downEvent)
        
        Thread.sleep(Random.nextLong(150, 350))
        val moveEvent = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, x + 2f, y + 1f, 0)
        webView.dispatchTouchEvent(moveEvent)
        
        val upEvent = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x + 2f, y + 1f, 0)
        webView.dispatchTouchEvent(upEvent)
    }
}

class Px2JsBridge {
    @JavascriptInterface
    fun onCaptchaRequired() {
    }
}
