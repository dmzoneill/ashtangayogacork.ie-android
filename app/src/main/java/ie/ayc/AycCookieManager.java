package ie.ayc;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

public final class AycCookieManager {
  /** COOKIES_FILE. */
  private static final String COOKIES_FILE = "cookies";

  /** COOKIES_DELIMETER. */
  private static final String COOKIES_DELIMETER = "\n";

  /** instance. */
  private static AycCookieManager instance;

  /** mCookieManager. */
  private CookieManager mCookieManager = null;

  private AycCookieManager() {
    this.mCookieManager = new CookieManager();
    this.mCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    CookieHandler.setDefault(mCookieManager);
    this.loadCookies();
  }

  /**
   * getInstance.
   *
   * @return result
   */
  public static AycCookieManager getInstance() {
    if (instance == null) {
      instance = new AycCookieManager();
    }
    return instance;
  }

  /**
   * encodeValue.
   *
   * @param value value
   * @return result
   */
  public static String encodeValue(final String value) {
    try {
      return URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException(ex.getCause());
    }
  }

  private static String implode(final List<HttpCookie> list) {
    Log.v("ayc-cookie-save", "imploding: " + list.size() + " cookies");

    if (list.size() == 0) {
      return "";
    }
    if (list.size() == 1) {
      return list.get(0).getName() + "=" + list.get(0).getValue();
    }

    String imploded = "";
    int y = 0;

    while (y < list.size() - 1) {
      HttpCookie cookie = list.get(y);
      String name = cookie.getName();
      imploded += name + "=" + cookie.getValue() + COOKIES_DELIMETER;
      y++;
    }
    imploded += list.get(y).getName() + "=" + list.get(y).getValue();

    return imploded;
  }

  /**
   * getCookies.
   *
   * @return result
   */
  public List<HttpCookie> getCookies() {
    if (this.mCookieManager == null) {
      return null;
    } else {
      return this.mCookieManager.getCookieStore().getCookies();
    }
  }

  /**
   * addCookies.
   *
   * @param list value
   */
  public void addCookies(final List<String> list) {
    try {
      if (list == null) {
        Log.v("ayc-cookie-add", "empty list");
        return;
      }
      if (list.size() == 0) {
        Log.v("ayc-cookie-add", "empty list");
        return;
      }
      for (String cookie : list) {
        String[] allparts = cookie.split(";");
        String[] cookieParts = allparts[0].split("=");
        if (cookieParts.length > 1) {
          HttpCookie thecookie = new HttpCookie(cookieParts[0], cookieParts[1]);
          String part1 = "https://ashtangayoga.ie";
          this.mCookieManager.getCookieStore().add(new URI(part1), thecookie);
          Log.v("ayc-cookie-update", "updated cookie store with: " + cookie);
        } else {
          Log.v("ayc-cookie-single", cookie);
        }
        this.saveCookies();
      }
    } catch (Exception e) {
      Log.v("ayc-cookie-add", "failed to update cookie store");
      Log.v("ayc-cookie-add", e.getMessage());
      for (int i = 0; i < e.getStackTrace().length; i++) {
        StackTraceElement x = e.getStackTrace()[i];
        Log.v(
            "ayc-cookie-add",
            x.getFileName()
                + ": "
                + x.getMethodName()
                + ": "
                + x.getLineNumber()
                + ": "
                + x.toString());
      }
    }
  }

  /** clearCookies. */
  public void clearCookies() {
    if (this.mCookieManager != null) {
      this.mCookieManager.getCookieStore().removeAll();
      Log.v("ayc-cookie-clear", "clear cookies");
      this.saveCookies();
    }
  }

  /** saveCookies. */
  public void saveCookies() {
    try {
      FileOutputStream outputStream =
          Login.appContext().openFileOutput(COOKIES_FILE, Context.MODE_PRIVATE);
      String imploded = implode(getCookies());
      Log.v("ayc-cookie-save", "imploded: " + imploded);
      outputStream.write(imploded.getBytes());
      outputStream.close();
      Log.v("ayc-cookie-save", "saved cookies to file");
    } catch (Exception e) {
      Log.v("ayc-cookie-save", "failed to save cookies to file");
    }
  }

  /** loadCookies. */
  public void loadCookies() {
    try {
      InputStream inputStream = Login.appContext().openFileInput(COOKIES_FILE);
      InputStreamReader reader = new InputStreamReader(inputStream);
      BufferedReader buf = new BufferedReader(reader);
      String line = buf.readLine();

      while (line != null) {
        // sb.append(line);
        String[] cookieParts = line.split("=");
        this.mCookieManager
            .getCookieStore()
            .add(
                new URI("https://ashtangayoga.ie"),
                new HttpCookie(cookieParts[0], cookieParts[1])); // x
        Log.v("ayc-cookie-load", "load cookie: " + line);
        line = buf.readLine();
      }

      Log.v("ayc-cookie-load", "Done loading cookies");
    } catch (Exception e) {
      Log.v("ayc-cookie-load", "failed to load cookies from file");
    }
  }

  /**
   * isCookieManagerEmpty.
   *
   * @return result
   */
  public boolean isCookieManagerEmpty() {
    if (this.mCookieManager == null) {
      return true;
    } else {
      return this.mCookieManager.getCookieStore().getCookies().isEmpty();
    }
  }

  /**
   * getCookieValue.
   *
   * @return result
   */
  public String getCookieValue() {
    String cookieValue = "";

    if (!isCookieManagerEmpty()) {
      for (HttpCookie eachCookie : getCookies()) {
        String pair = eachCookie.getName() + "=" + eachCookie.getValue();
        Log.v("ayc-cookie-get", pair);
        cookieValue = cookieValue + pair + "; ";
      }
    }
    return cookieValue;
  }
}
