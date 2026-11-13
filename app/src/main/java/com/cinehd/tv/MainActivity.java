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
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class MainActivity extends Activity {

    private static final String TARGET_URL = "https://cinehd.vc/watch/tv";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private WebView mWebView;
    private FrameLayout mCustomViewContainer;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private ProgressBar mLoadingSpinner;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable fullscreen immersive mode
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
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        
        // Ensure HTML5 video plays automatically on TV without touch gesture
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Use Desktop Chrome User Agent for best web player compatibility
        settings.setUserAgentString(USER_AGENT);

        // Prevent unwanted popup windows and tabs
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);

        // D-Pad and remote navigation focus
        mWebView.setFocusable(true);
        mWebView.setFocusableInTouchMode(true);
        mWebView.requestFocus();

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost();

                // Stay on cinehd.vc and its media/embed subdomains; block third-party redirects
                if (host != null && (host.contains("cinehd.vc") || host.contains("embed") || host.contains("stream"))) {
                    return false;
                }
                // Return true to prevent rogue ad popups/redirects
                return true;
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
        // TV remote BACK button handling
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 1. If video is currently in fullscreen, exit fullscreen first
            if (mCustomView != null) {
                mWebView.getWebChromeClient();
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

            // 2. If webview has navigation history, go back
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

