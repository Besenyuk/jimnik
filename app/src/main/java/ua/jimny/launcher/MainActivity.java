package ua.jimny.launcher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class MainActivity extends Activity {
  private WebView web;

  @Override protected void onCreate(Bundle b) {
    super.onCreate(b);
    web = new WebView(this);
    WebSettings s = web.getSettings();
    s.setJavaScriptEnabled(true);
    s.setDomStorageEnabled(true);
    s.setAllowFileAccess(true);
    s.setMediaPlaybackRequiresUserGesture(false);
    web.addJavascriptInterface(new Bridge(), "Android");
    web.loadUrl("file:///android_asset/index.html#kiosk");
    setContentView(web);
    hideBars();
  }

  @Override protected void onResume() { super.onResume(); hideBars(); }

  private void hideBars() {
    getWindow().getDecorView().setSystemUiVisibility(
      View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
      | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
  }

  @Override public void onBackPressed() { web.evaluateJavascript("history.back()", null); }

  class Bridge {
    @JavascriptInterface public boolean launch(String pkg) {
      try {
        Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
        if (i == null) return false;
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        return true;
      } catch (Exception e) { return false; }
    }

    @JavascriptInterface public String apps() {
      PackageManager pm = getPackageManager();
      Intent q = new Intent(Intent.ACTION_MAIN);
      q.addCategory(Intent.CATEGORY_LAUNCHER);
      JSONArray out = new JSONArray();
      for (ResolveInfo r : pm.queryIntentActivities(q, 0)) {
        String p = r.activityInfo.packageName;
        if (p.equals(getPackageName())) continue;
        try {
          JSONObject o = new JSONObject();
          o.put("n", String.valueOf(r.loadLabel(pm)));
          o.put("p", p);
          out.put(o);
        } catch (Exception e) {}
      }
      return out.toString();
    }
  }
}
