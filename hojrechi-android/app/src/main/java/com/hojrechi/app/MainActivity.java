package com.hojrechi.app;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public final class MainActivity extends Activity {
 private static final String HOME = "https://hojrechi.com/";
 private static final int FILE_PICKER = 4219;
 private WebView web;
 private ImageView splash;
 private TextView error;
 private ValueCallback<Uri[]> fileCallback;
 private boolean loadFailed = false;
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  immersive();
  FrameLayout layout = new FrameLayout(this);
  layout.setBackgroundColor(Color.rgb(255, 90, 0));
  web = new SwipeWebView(this);
  web.setOverScrollMode(View.OVER_SCROLL_NEVER);
  web.setBackgroundColor(Color.WHITE);
  WebSettings settings = web.getSettings();
  settings.setJavaScriptEnabled(true);
  settings.setDomStorageEnabled(true);
  settings.setDatabaseEnabled(true);
  settings.setAllowContentAccess(true);
  settings.setAllowFileAccess(false);
  settings.setJavaScriptCanOpenWindowsAutomatically(true);
  settings.setSupportMultipleWindows(false);
  settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
  settings.setUseWideViewPort(true);
  settings.setLoadWithOverviewMode(true);
  settings.setBuiltInZoomControls(false);
  settings.setDisplayZoomControls(false);
  settings.setMediaPlaybackRequiresUserGesture(true);
  CookieManager.getInstance().setAcceptCookie(true);
  CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);
  web.setWebViewClient(new WebViewClient() {
   @Override public boolean shouldOverrideUrlLoading(WebView view, String url) { return external(url); }
   @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) { return external(req.getUrl().toString()); }
   @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
    loadFailed=false;
    if(error!=null)error.setVisibility(View.GONE);
   }
   @Override public void onPageFinished(WebView view, String url) {
    if(splash!=null)splash.setVisibility(View.GONE);
    if(loadFailed && error!=null)error.setVisibility(View.VISIBLE);
    CookieManager.getInstance().flush();
   }
   @Override public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
    if(req.isForMainFrame()) {
     loadFailed=true;
     if(splash!=null)splash.setVisibility(View.GONE);
     if(error!=null)error.setVisibility(View.VISIBLE);
    }
   }
  });
  web.setWebChromeClient(new WebChromeClient() {
   @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
    if(fileCallback!=null)fileCallback.onReceiveValue(null);
    fileCallback=callback;
    try { startActivityForResult(params.createIntent(), FILE_PICKER); return true; }
    catch(Exception ex) { fileCallback.onReceiveValue(null); fileCallback=null; return false; }
   }
  });
  web.setDownloadListener((url,ua,disposition,mime,len)->{
   try {
    if(!url.startsWith("https://"))return;
    String fn=URLUtil.guessFileName(url,disposition,mime);
    DownloadManager.Request request=new DownloadManager.Request(Uri.parse(url));
    request.setTitle(fn);
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fn);
    if(mime!=null)request.setMimeType(mime);
    String cookies=CookieManager.getInstance().getCookie(url);
    if(cookies!=null)request.addRequestHeader("Cookie",cookies);
    if(ua!=null)request.addRequestHeader("User-Agent",ua);
    ((DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);
   } catch(Exception ignored) {}
  });
  layout.addView(web,new FrameLayout.LayoutParams(-1,-1));
  splash=new ImageView(this);
  splash.setBackgroundColor(Color.rgb(255,90,0));
  try(java.io.InputStream in=getAssets().open("splash_portrait.jpg")) {
   splash.setImageBitmap(BitmapFactory.decodeStream(in));
  } catch(Exception ignored) {}
  splash.setScaleType(ImageView.ScaleType.CENTER_CROP);
  layout.addView(splash,new FrameLayout.LayoutParams(-1,-1));
  error=new TextView(this);
  error.setText("ارتباط با حجره‌چی برقرار نشد. برای تلاش مجدد لمس کنید.");
  error.setTextColor(Color.BLACK); error.setBackgroundColor(Color.WHITE);
  error.setGravity(Gravity.CENTER);error.setTextSize(17);
  error.setPadding(30,30,30,30);error.setVisibility(View.GONE);
  error.setOnClickListener(v->{error.setVisibility(View.GONE);web.reload();});
  layout.addView(error,new FrameLayout.LayoutParams(-1,-1));
  setContentView(layout);
  if(Build.VERSION.SDK_INT>=33) getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
   android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::backOrExit);
  if(state==null)web.loadUrl(HOME);
  else if(web.restoreState(state)==null)web.loadUrl(HOME);
 }
 private void immersive() {
  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
  getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|
    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|
    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|
    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
 }
 @Override public void onWindowFocusChanged(boolean focus) {super.onWindowFocusChanged(focus); if(focus)immersive();}
 private boolean external(String link) {
  if(link==null)return false;
  Uri uri=Uri.parse(link);
  String scheme=uri.getScheme();
  if(scheme==null||scheme.equalsIgnoreCase("https")||scheme.equalsIgnoreCase("http"))return false;
  try {
   if(scheme.equalsIgnoreCase("intent")) {
    Intent i=Intent.parseUri(link,Intent.URI_INTENT_SCHEME);
    String fallback=i.getStringExtra("browser_fallback_url");
    i.addCategory(Intent.CATEGORY_BROWSABLE);i.setComponent(null);i.setSelector(null);
    try {startActivity(i);}
    catch(ActivityNotFoundException absent) {if(fallback!=null&&fallback.startsWith("https://"))web.loadUrl(fallback);}
   } else {startActivity(new Intent(Intent.ACTION_VIEW,uri));}
  } catch(Exception ignored){}
  return true;
 }
 @Override public void onBackPressed(){backOrExit();}
 private void backOrExit() {
  if(error!=null && error.getVisibility()==View.VISIBLE){error.setVisibility(View.GONE);web.loadUrl(HOME);}
  else if(web!=null&&web.canGoBack())web.goBack();
  else finish();
 }
 @Override protected void onSaveInstanceState(Bundle out){if(web!=null)web.saveState(out);super.onSaveInstanceState(out);}
 @Override protected void onActivityResult(int req,int result,Intent data){
  super.onActivityResult(req,result,data);
  if(req==FILE_PICKER && fileCallback!=null){fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result,data));fileCallback=null;}
 }
 @Override protected void onDestroy(){
  if(fileCallback!=null){fileCallback.onReceiveValue(null);fileCallback=null;}
  if(web!=null){ViewGroup parent=(ViewGroup)web.getParent();if(parent!=null)parent.removeView(web);web.destroy();web=null;}
  super.onDestroy();
 }
 private final class SwipeWebView extends WebView {
  private float downX,downY;
  SwipeWebView(Context context){super(context);}
  @Override public boolean onTouchEvent(MotionEvent ev){
   if(ev.getActionMasked()==MotionEvent.ACTION_DOWN){downX=ev.getX();downY=ev.getY();}
   else if(ev.getActionMasked()==MotionEvent.ACTION_UP){
    float dx=ev.getX()-downX,dy=ev.getY()-downY;
    float d=getResources().getDisplayMetrics().density;
    if(getScrollY()<=0 && dy>115*d && dy>Math.abs(dx)*1.8f){reload();return true;}
    if((downX<24*d||downX>getWidth()-24*d)&&Math.abs(dx)>130*d&&Math.abs(dx)>Math.abs(dy)*1.8f){backOrExit();return true;}
   }
   return super.onTouchEvent(ev);
  }
 }
}
