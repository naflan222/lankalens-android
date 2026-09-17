package lk.lankalens.app;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SafeBrowsingResponse;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String HOME_URL = "https://lankalens.lk/";
    private static final int FILE_CHOOSER_REQUEST = 4107;
    private static final int MAX_LISTING_IMAGES = 3;
    private static final int MAX_UPLOAD_BYTES = 950 * 1024;
    private static final int MAX_IMAGE_EDGE = 1600;

    private WebView webView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progress;
    private View errorPanel;
    private TextView errorText;
    private ValueCallback<Uri[]> fileCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ensureWebViewCacheDirs();
        super.onCreate(savedInstanceState);
        if (isEmulatorEnvironment() && getWindow() != null) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progress = findViewById(R.id.progress);
        errorPanel = findViewById(R.id.errorPanel);
        errorText = findViewById(R.id.errorText);
        Button retryButton = findViewById(R.id.retryButton);

        configureWebView();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        swipeRefresh.setColorSchemeResources(R.color.lankalens_green, R.color.lankalens_gold);
        swipeRefresh.setOnRefreshListener(() -> {
            if (webView != null) {
                webView.reload();
            } else {
                swipeRefresh.setRefreshing(false);
            }
        });
        retryButton.setOnClickListener(v -> {
            errorPanel.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            if (webView != null) {
                String currentUrl = webView.getUrl();
                if (currentUrl == null || currentUrl.trim().isEmpty()) {
                    webView.loadUrl(resolveInitialUrl(getIntent()));
                } else {
                    webView.reload();
                }
            }
        });

        String initialUrl = resolveInitialUrl(getIntent());
        if (savedInstanceState == null) {
            webView.loadUrl(initialUrl);
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    private void ensureWebViewCacheDirs() {
        try {
            File cacheDir = getCacheDir();
            if (cacheDir != null) {
                // If corrupted fake index or incomplete index-dir was left, delete the HTTP Cache dir so Chromium reinitializes cleanly
                File httpCacheDir = new File(cacheDir, "WebView/Default/HTTP Cache");
                if (httpCacheDir.exists()) {
                    File indexDir = new File(httpCacheDir, "index-dir");
                    File realIndex = new File(indexDir, "the-real-index");
                    if (indexDir.exists() && (!indexDir.isDirectory() || !realIndex.exists())) {
                        deleteRecursive(httpCacheDir);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory != null && fileOrDirectory.exists()) {
            if (fileOrDirectory.isDirectory()) {
                File[] children = fileOrDirectory.listFiles();
                if (children != null) {
                    for (File child : children) {
                        deleteRecursive(child);
                    }
                }
            }
            fileOrDirectory.delete();
        }
    }

    private boolean isEmulatorEnvironment() {
        String fp = Build.FINGERPRINT != null ? Build.FINGERPRINT.toLowerCase() : "";
        String model = Build.MODEL != null ? Build.MODEL.toLowerCase() : "";
        String board = Build.BOARD != null ? Build.BOARD.toLowerCase() : "";
        String brand = Build.BRAND != null ? Build.BRAND.toLowerCase() : "";
        String device = Build.DEVICE != null ? Build.DEVICE.toLowerCase() : "";
        String product = Build.PRODUCT != null ? Build.PRODUCT.toLowerCase() : "";
        String hardware = Build.HARDWARE != null ? Build.HARDWARE.toLowerCase() : "";

        return fp.startsWith("generic")
                || fp.startsWith("unknown")
                || fp.contains("emulator")
                || fp.contains("vbox")
                || fp.contains("test-keys")
                || model.contains("google_sdk")
                || model.contains("emulator")
                || model.contains("android sdk")
                || board.contains("goldfish")
                || board.contains("ranchu")
                || hardware.contains("goldfish")
                || hardware.contains("ranchu")
                || hardware.contains("vbox86")
                || hardware.contains("cutf")
                || hardware.contains("cuttlefish")
                || product.contains("sdk")
                || product.contains("google_sdk")
                || product.contains("vbox86")
                || product.contains("cuttlefish")
                || product.contains("emulator")
                || (brand.startsWith("generic") && device.startsWith("generic"));
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setUserAgentString(settings.getUserAgentString() + " LankaLensAndroid/1.1");

        // Performance optimizations
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(false);
        }
        // Enable hardware acceleration for smooth 60/120fps scrolling on devices;
        // only fall back to software rendering if running in virtualized emulator without DRM
        if (isEmulatorEnvironment()) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> uploadMsg,
                                             FileChooserParams fileChooserParams) {
                if (fileCallback != null) {
                    fileCallback.onReceiveValue(null);
                }
                fileCallback = uploadMsg;

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                String type = "*/*";
                String[] accepts = fileChooserParams == null ? null : fileChooserParams.getAcceptTypes();
                if (accepts != null) {
                    for (String accept : accepts) {
                        if (accept != null && !accept.trim().isEmpty()) {
                            type = accept.trim();
                            break;
                        }
                    }
                }
                intent.setType(type);
                boolean multiple = fileChooserParams != null
                        && fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE;
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, multiple);

                try {
                    startActivityForResult(Intent.createChooser(intent, "Choose photos"), FILE_CHOOSER_REQUEST);
                    return true;
                } catch (ActivityNotFoundException e) {
                    fileCallback = null;
                    Toast.makeText(MainActivity.this, "No photo picker is available on this device.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progress.setVisibility(View.VISIBLE);
                errorPanel.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progress.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                injectAndroidOnlyUiFixes(view);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleNavigation(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleNavigation(Uri.parse(url));
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request != null && request.isForMainFrame()) {
                    String description = error != null && error.getDescription() != null
                            ? error.getDescription().toString()
                            : "Could not connect to LankaLens.";
                    showError(description);
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                if (request != null && request.isForMainFrame()) {
                    int statusCode = errorResponse != null ? errorResponse.getStatusCode() : -1;
                    if (statusCode >= 400) {
                        showError("Server error (" + statusCode + "). Please tap Retry to reload.");
                    }
                }
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                recreateWebView();
                showError("Web view rendering process was restarted. Tap Try again to reload.");
                return true;
            }

            @Override
            public void onSafeBrowsingHit(WebView view, WebResourceRequest request,
                                          int threatType, SafeBrowsingResponse callback) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    callback.backToSafety(true);
                    Toast.makeText(MainActivity.this, "This link was blocked for your safety.", Toast.LENGTH_LONG).show();
                } else {
                    super.onSafeBrowsingHit(view, request, threatType, callback);
                }
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (Exception e) {
                Toast.makeText(this, "Unable to open this download.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void injectAndroidOnlyUiFixes(WebView view) {
        String js = "(function(){" +
                "var id='ll-android-ui-fixes';" +
                "var s=document.getElementById(id);" +
                "if(!s){" +
                "s=document.createElement('style');s.id=id;" +
                "(document.head||document.documentElement).appendChild(s);" +
                "}" +
                "s.textContent='" +
                ".app-tabbar .sell-tab{top:-11px!important;}" +
                ".app-tabbar .sell-fab{width:48px!important;height:48px!important;font-size:22px!important;box-shadow:0 5px 12px rgba(240,165,0,.30)!important;}" +
                ".app-tabbar .sell-fab .ionicon{width:22px!important;height:22px!important;font-size:22px!important;}" +
                ".app-tabbar .sell-tab span{font-size:10px!important;margin-top:1px!important;}" +
                ".breadcrumbs,nav[aria-label=\"Breadcrumb\"],#detail-root .breadcrumbs{display:none!important;visibility:hidden!important;height:0!important;padding:0!important;margin:0!important;overflow:hidden!important;}" +
                "';" +
                "function cleanProductDirectory(){" +
                "try{" +
                "var crumbs=document.querySelectorAll('.breadcrumbs,nav[aria-label=\"Breadcrumb\"]');" +
                "for(var i=0;i<crumbs.length;i++){" +
                "crumbs[i].style.setProperty('display','none','important');" +
                "if(crumbs[i].parentNode&&crumbs[i].closest&&crumbs[i].closest('#detail-root')){" +
                "crumbs[i].parentNode.removeChild(crumbs[i]);" +
                "}" +
                "}" +
                "if(location.hash&&(location.hash.indexOf('#/ads/')===0||location.hash.indexOf('#/listing/')===0)){" +
                "window.scrollTo(0,0);" +
                "var p=document.getElementById('page');if(p)p.scrollTop=0;" +
                "}" +
                "}catch(e){}" +
                "}" +
                "cleanProductDirectory();" +
                "if(!window.__llDetailWatcher){" +
                "window.__llDetailWatcher=true;" +
                "window.addEventListener('hashchange',function(){" +
                "cleanProductDirectory();" +
                "setTimeout(cleanProductDirectory,100);" +
                "});" +
                "}" +
                "})();";
        view.evaluateJavascript(js, null);
    }

    private boolean handleNavigation(Uri uri) {
        if (uri == null) return false;
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);

        if (("https".equals(scheme) || "http".equals(scheme)) &&
                ("lankalens.lk".equals(host) || "www.lankalens.lk".equals(host))) {
            return false;
        }

        if ("tel".equals(scheme) || "mailto".equals(scheme) || "sms".equals(scheme) ||
                "whatsapp".equals(scheme) || "intent".equals(scheme) ||
                "https".equals(scheme) || "http".equals(scheme)) {
            try {
                Intent external;
                if ("intent".equals(scheme)) {
                    external = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME);
                } else {
                    external = new Intent(Intent.ACTION_VIEW, uri);
                }
                startActivity(external);
            } catch (Exception e) {
                Toast.makeText(this, "No app is available to open this link.", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    private void recreateWebView() {
        try {
            if (webView != null) {
                swipeRefresh.removeView(webView);
                webView.destroy();
                webView = null;
            }
            webView = new WebView(this);
            webView.setLayoutParams(new androidx.swiperefreshlayout.widget.SwipeRefreshLayout.LayoutParams(
                    androidx.swiperefreshlayout.widget.SwipeRefreshLayout.LayoutParams.MATCH_PARENT,
                    androidx.swiperefreshlayout.widget.SwipeRefreshLayout.LayoutParams.MATCH_PARENT
            ));
            webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            swipeRefresh.addView(webView);
            configureWebView();
        } catch (Exception ignored) {
        }
    }

    private void showError(String message) {
        progress.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        errorText.setText(message);
        errorPanel.setVisibility(View.VISIBLE);
    }

    private String resolveInitialUrl(Intent intent) {
        Uri data = intent == null ? null : intent.getData();
        if (data == null) return HOME_URL;
        String host = data.getHost();
        if (host != null && (host.equalsIgnoreCase("lankalens.lk") || host.equalsIgnoreCase("www.lankalens.lk"))) {
            return data.toString();
        }
        return HOME_URL;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String url = resolveInitialUrl(intent);
        webView.loadUrl(url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
            webView.resumeTimers();
        }
    }

    @Override
    protected void onPause() {
        if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
        if (webView != null) {
            webView.onPause();
            webView.pauseTimers();
        }
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) {
            webView.saveState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != FILE_CHOOSER_REQUEST || fileCallback == null) return;

        ValueCallback<Uri[]> callback = fileCallback;
        fileCallback = null;

        if (resultCode != RESULT_OK || data == null) {
            callback.onReceiveValue(null);
            return;
        }

        List<Uri> selected = collectSelectedUris(data);
        if (selected.isEmpty()) {
            callback.onReceiveValue(null);
            return;
        }

        Toast.makeText(this, "Preparing photo" + (selected.size() > 1 ? "s" : "") + "…", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            ArrayList<Uri> prepared = new ArrayList<>();
            int index = 0;
            for (Uri source : selected) {
                if (prepared.size() >= MAX_LISTING_IMAGES) break;
                Uri out = prepareImageForUpload(source, index++);
                if (out != null) prepared.add(out);
            }
            runOnUiThread(() -> {
                if (prepared.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Could not prepare that photo. Try another image.", Toast.LENGTH_LONG).show();
                    callback.onReceiveValue(null);
                } else {
                    callback.onReceiveValue(prepared.toArray(new Uri[0]));
                }
            });
        }).start();
    }

    private List<Uri> collectSelectedUris(Intent data) {
        ArrayList<Uri> result = new ArrayList<>();
        ClipData clip = data.getClipData();
        if (clip != null) {
            for (int i = 0; i < clip.getItemCount() && result.size() < MAX_LISTING_IMAGES; i++) {
                Uri uri = clip.getItemAt(i).getUri();
                if (uri != null) result.add(uri);
            }
        } else if (data.getData() != null) {
            result.add(data.getData());
        }
        return result;
    }

    private Uri prepareImageForUpload(Uri source, int index) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream in = getContentResolver().openInputStream(source)) {
                BitmapFactory.decodeStream(in, null, bounds);
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null;

            int sample = 1;
            int largest = Math.max(bounds.outWidth, bounds.outHeight);
            while (largest / sample > 2200) sample *= 2;

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap;
            try (InputStream in = getContentResolver().openInputStream(source)) {
                bitmap = BitmapFactory.decodeStream(in, null, opts);
            }
            if (bitmap == null) return null;

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int edge = Math.max(width, height);
            if (edge > MAX_IMAGE_EDGE) {
                float ratio = MAX_IMAGE_EDGE / (float) edge;
                int targetW = Math.max(1, Math.round(width * ratio));
                int targetH = Math.max(1, Math.round(height * ratio));
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, targetW, targetH, true);
                if (scaled != bitmap) bitmap.recycle();
                bitmap = scaled;
            }

            byte[] encoded = encodeUnderLimit(bitmap);
            bitmap.recycle();
            if (encoded == null || encoded.length == 0) return null;

            File dir = new File(getCacheDir(), "upload-cache");
            if (!dir.exists() && !dir.mkdirs()) return null;
            File out = new File(dir, "lankalens-photo-" + System.currentTimeMillis() + "-" + index + ".jpg");
            try (FileOutputStream stream = new FileOutputStream(out)) {
                stream.write(encoded);
                stream.flush();
            }
            return FileProvider.getUriForFile(this, getPackageName() + ".files", out);
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] encodeUnderLimit(Bitmap bitmap) {
        int quality = 84;
        byte[] bytes = null;
        while (quality >= 46) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) return null;
            bytes = out.toByteArray();
            if (bytes.length <= MAX_UPLOAD_BYTES) return bytes;
            quality -= 8;
        }
        return bytes != null && bytes.length <= MAX_UPLOAD_BYTES ? bytes : null;
    }

    @Override
    protected void onDestroy() {
        if (fileCallback != null) {
            fileCallback.onReceiveValue(null);
            fileCallback = null;
        }
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
        }
        super.onDestroy();
    }
}
