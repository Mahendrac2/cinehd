package com.cinehd.tv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class MainActivity extends Activity {

    // Main website entry point (watch/tv returns 404/403)
    private static final String TARGET_URL = "https://cinehd.vc";

    private WebView mWebView;
    private FrameLayout mCustomViewContainer;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private ProgressBar mLoadingSpinner;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen immersive layout
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mWebView = findViewById(R.id.tv_webview);
        mCustomViewContainer = findViewById(R.id.custom_view_container);
        mLoadingSpinner = findViewById(R.id.loading_spinner);

        setupWebView();
        mWebView.loadUrl(TARGET_URL);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        // Enable cookies and third-party cookies (Required for Cloudflare verification)
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(mWebView, true);

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Ensure video plays automatically without touch gestures
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Mask WebView identifier '; wv' so Cloudflare Turnstile doesn't block it
        String defaultUa = settings.getUserAgentString();
        if (defaultUa != null) {
            String cleanUa = defaultUa.replace("; wv", "");
            settings.setUserAgentString(cleanUa);
        }

        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        mWebView.setFocusable(true);
        mWebView.setFocusableInTouchMode(true);
        mWebView.requestFocus();

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String urlStr = uri.toString();

                // Allow cinehd and all necessary media/auth/cdn domains
                if (urlStr.contains("cinehd") || urlStr.contains("cloudflare") || urlStr.contains("embed") || urlStr.contains("stream") || urlStr.contains("tmdb")) {
                    return false;
                }
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (mLoadingSpinner != null) {
                    mLoadingSpinner.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (mLoadingSpinner != null) {
                    mLoadingSpinner.setVisibility(View.GONE);
                }
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (mCustomView != null) {
                    onHideCustomView();
                    return;
                }

                mCustomView = view;
                mCustomViewCallback = callback;

                mWebView.setVisibility(View.GONE);
                mCustomViewContainer.addView(mCustomView);
                mCustomViewContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onHideCustomView() {
                if (mCustomView == null) {
                    return;
                }

                mCustomViewContainer.removeView(mCustomView);
                mCustomViewContainer.setVisibility(View.GONE);
                mCustomView = null;

                if (mCustomViewCallback != null) {
                    mCustomViewCallback.onCustomViewHidden();
                }

                mWebView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 90 && mLoadingSpinner != null) {
                    mLoadingSpinner.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mCustomView != null) {
                if (mCustomViewContainer.getVisibility() == View.VISIBLE) {
                    mCustomViewContainer.removeView(mCustomView);
                    mCustomViewContainer.setVisibility(View.GONE);
                    mCustomView = null;
                    if (mCustomViewCallback != null) {
                        mCustomViewCallback.onCustomViewHidden();
                    }
                    mWebView.setVisibility(View.VISIBLE);
                    return true;
                }
            }

            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWebView != null) {
            mWebView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
        }
        super.onDestroy();
    }
}
