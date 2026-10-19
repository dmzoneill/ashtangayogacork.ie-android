package ie.ayc;

import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/** Shared task type for the site scraper. */
abstract class ScraperBase extends CookieTask
    implements AsyncResponse, Observable { // xxxxxxxxxxxxx
  /** ScraperBase. */
  protected ScraperBase() {
    // Allows the scraper subclass.
  }
}

/** Loads and caches the studio site data. */
public final class ScraperManager extends ScraperBase {

  /** observers. */
  private static ArrayList<Observer> observers;

  /** instance. */
  private static ScraperManager instance;

  /** profile. */
  private static JSONArray profile;

  /** classes. */
  private static JSONArray classes;

  /** prices. */
  private static JSONArray prices;

  /** bookings. */
  private static JSONArray bookings;

  /** transactions. */
  private static JSONArray transactions;

  /** usedCredit. */
  private static JSONArray usedCredit;

  /** expiringCredit. */
  private static JSONArray expiringCredit;

  static {
    ScraperManager.observers = new ArrayList<>();
  }

  /** callback. */
  private final AsyncResponse callback;

  private ScraperManager() {
    // set context variables if required
    this.callback = this;
    setDelegate(this);
  }

  /**
   * getInstance.
   *
   * @return result
   */
  public static ScraperManager getInstance() {
    if (instance == null) {
      instance = new ScraperManager();
    }
    return instance;
  }

  /**
   * getProfile.
   *
   * @return result
   */
  public static JSONArray getProfile() {
    return profile;
  }

  /**
   * getBookings.
   *
   * @return result
   */
  public static JSONArray getBookings() {
    return bookings;
  }

  /**
   * getExpiringCredit.
   *
   * @return result
   */
  public static JSONArray getExpiringCredit() {
    return expiringCredit;
  }

  /**
   * getUsedCredit.
   *
   * @return result
   */
  public static JSONArray getUsedCredit() {
    return usedCredit;
  }

  /**
   * getTransactions.
   *
   * @return result
   */
  public static JSONArray getTransactions() {
    return transactions;
  }

  /**
   * getPrices.
   *
   * @param filter value
   * @return result
   */
  public static JSONArray getPrices(final int filter) {
    JSONArray filtered = new JSONArray();

    try {
      Log.v("ayc-scraper", " total prices: " + prices.length());

      for (int y = 0; y < prices.length(); y++) {
        JSONObject jo = prices.getJSONObject(y);

        Log.v("ayc-scraper", " prices filter " + y + " / " + prices.length());
        switch (filter) {
          case 0:
            String part7 = " prices filter monthly: ";
            Log.v("ayc-scraper", part7 + jo.get("monthly").toString());
            if (jo.get("monthly").toString().compareToIgnoreCase("1") == 0) {
              filtered.put(jo);
            }
            break;
          case 1:
            Log.v(
                "ayc-scraper",
                " prices filter special: "
                    + jo.get("class_type_restriction").toString()); // xxxxxxx
            String restriction = jo.get("class_type_restriction").toString();
            if (restriction.compareToIgnoreCase("null") != 0) { // xx
              filtered.put(jo);
            }
            break;
          case 2:
          default:
            String part8 = " prices filter standard: ";
            Log.v("ayc-scraper", part8 + jo.get("monthly").toString());
            if (jo.get("monthly").toString().compareToIgnoreCase("0") == 0) {
              filtered.put(jo);
            }
            break;
        }
      }
    } catch (Exception e) {
      Log.v("ayc-scraper", " prices filter error: " + e.getMessage());
    }

    return filtered;
  }

  /**
   * getClasses.
   *
   * @param filterSticky value
   * @return result
   */
  public static JSONArray getClasses(final int filterSticky) {
    JSONArray filtered = new JSONArray();

    try {
      for (int y = 0; y < classes.length(); y++) {
        JSONObject jo = classes.getJSONObject(y);

        String sticky = jo.get("sticky").toString();
        String wanted = String.valueOf(filterSticky);
        if (sticky.compareToIgnoreCase(wanted) == 0) { // x
          filtered.put(jo);
        }
      }
    } catch (Exception e) {
      Log.v("ayc-scraper", " classes filter error: " + e.getStackTrace());
    }

    return filtered;
  }

  /**
   * getClassById.
   *
   * @param id value
   * @return result
   */
  public static JSONObject getClassById(final String id) {
    try {
      for (int y = 0; y < classes.length(); y++) {
        JSONObject jo = classes.getJSONObject(y);

        if (jo.get("class_id").toString().compareToIgnoreCase(id) == 0) {
          return jo;
        }
      }
    } catch (Exception e) {
      Log.v("ayc-scraper-class", " get class by id : " + e.getStackTrace());
      return null;
    }

    return null;
  }

  /** fetchAll. */
  public void fetchAll() {
    String jsonEndpoint = "https://ashtangayoga.ie/json/?a=";

    String[] urls = {
      "get_profile",
      "get_used_credit",
      "get_expiring_credit",
      "get_transactions",
      "get_bookings",
      "get_classes",
      "get_prices"
    };

    for (String action : urls) {
      ScraperManager task = new ScraperManager();
      task.setDelegate(this.callback);
      task.execute(jsonEndpoint + action);
    }
  }

  /** beginMonthly. */
  public void beginMonthly() {
    Log.v("ayc-scraper-update", "begin_monthly: " + observers.size());
    ScraperManager beginMonthlyTask = new ScraperManager();
    beginMonthlyTask.setDelegate(this.callback);
    beginMonthlyTask.execute("https://ashtangayoga.ie/json/?a=begin_monthly");
  }

  /**
   * openDoor.
   *
   * @param id value
   */
  public void openDoor(final String id) {
    Log.v("ayc-scraper-update", "open door: " + observers.size());
    ScraperManager openDoorTask = new ScraperManager();
    openDoorTask.setDelegate(this.callback);
    openDoorTask.execute("https://ashtangayoga.ie/json/?a=open_door&id=" + id);
  }

  /**
   * applyRedeemCode.
   *
   * @param code value
   */
  public void applyRedeemCode(final String code) {
    Log.v("ayc-scraper-update", "book_class_add all: " + observers.size());
    ScraperManager redeemTask = new ScraperManager();
    redeemTask.setDelegate(this.callback);
    String part9 = "https://ashtangayoga.ie/json/?a=redeem_code&code=";
    redeemTask.execute(part9 + code);
  }

  /**
   * bookClassAdd.
   *
   * @param classId value
   */
  public void bookClassAdd(final String classId) {
    Log.v("ayc-scraper-update", "book_class_add all: " + observers.size());
    ScraperManager bookTask = new ScraperManager();
    bookTask.setDelegate(this.callback);
    String str4 = "https://ashtangayoga.ie/json/?a=add_booking&id=actionButton";
    bookTask.execute(str4 + classId);
  }

  /**
   * bookClassRemove.
   *
   * @param classId value
   */
  public void bookClassRemove(final String classId) {
    Log.v("ayc-scraper-update", "book_class_remove all: " + observers.size());
    ScraperManager removeTask = new ScraperManager();
    removeTask.setDelegate(this.callback);
    String cancelBase = "https://ashtangayoga.ie/json/?a=";
    String cancelUrl = cancelBase + "cancel_booking&id=actionButton";
    removeTask.execute(cancelUrl + classId);
  }

  /**
   * getVoucherUrl.
   *
   * @param name value
   * @param transactionId value
   */
  public void getVoucherUrl(final String name, final String transactionId) {
    Log.v("ayc-scraper-update", "make voucher: " + observers.size());
    ScraperManager voucherTask = new ScraperManager();
    voucherTask.setDelegate(this.callback);
    voucherTask.execute(
        "https://ashtangayoga.ie/json/?a=get_voucher&vid="
            + transactionId
            + "&vname="
            + name); // xx
  }

  /**
   * updateUserSettings.
   *
   * @param phone value
   * @param sms value
   * @param mailXXXXXXXXXX value
   */
  public void updateUserSettings(
      final String phone, final String sms, final String mailXXXXXXXXXX) {
    Log.v("ayc-scraper-update", "profile settings: " + observers.size());
    ScraperManager settingsTask = new ScraperManager();
    settingsTask.setDelegate(this.callback);
    settingsTask.execute(
        "https://ashtangayoga.ie/json/?a=update_settings&sphone="
            + phone
            + "&ssms="
            + sms
            + "&smail="
            + mailXXXXXXXXXX);
  }

  /**
   * createIssue.
   *
   * @param title value
   * @param description value
   */
  public void createIssue(final String title, final String description)
      throws UnsupportedEncodingException {
    Log.v("ayc-scraper-update", "profile settings: " + observers.size());
    ScraperManager settingsTask = new ScraperManager();
    settingsTask.setDelegate(this.callback);
    settingsTask.execute(
        "https://ashtangayoga.ie/json/?a=submit_issue&t="
            + URLEncoder.encode(title, "UTF-8")
            + "&d="
            + URLEncoder.encode(description, "UTF-8")
            + "&i=android",
        "UTF-8");
  }

  @Override
  protected String doInBackground(final String... params) {
    final String urlString = params[0];
    final AycCookieManager cookies = AycCookieManager.getInstance();
    Log.v("ayc-scraper", "contacting: " + urlString);
    try {
      final java.net.HttpURLConnection connection =
          SiteHttp.open(urlString, "GET", false, false); // x
      connection.addRequestProperty("Cookie", cookies.getCookieValue());
      SiteHttp.applyPageHeaders(connection, "ayc-android/1.0");
      cookies.addCookies(connection.getHeaderFields().get("Set-Cookie"));
      final String body = SiteHttp.readBody(connection, "ayc-scraper");
      SiteHttp.logChunks("ayc-scraper-resp", body);
      return body;
    } catch (Exception exception) {
      Log.v("ayc-scraper", exception.getMessage());
    }
    return "";
  }

  @Override
  public void processFinish(final String output) {
    Log.v("ayc-scraper", output);
    try {
      Log.v("ayc-scraper-resp", output);

      JSONObject reader = new JSONObject(output);

      Object obj = reader.get("result");
      if (obj instanceof Boolean) {
        this.notifyObservers(UpdateSource.logout);
        return;
      }

      UpdateResponse ur = new UpdateResponse();
      ur.response = reader;

      switch (reader.getString("action")) {
        case "get_bookings":
          bookings = reader.getJSONArray("result");
          this.notifyObservers(UpdateSource.classes);
          break;
        case "get_prices":
          prices = reader.getJSONArray("result");
          this.notifyObservers(UpdateSource.prices);
          break;
        case "get_classes":
          classes = reader.getJSONArray("result");
          this.notifyObservers(UpdateSource.classes);
          break;
        case "get_profile":
          profile = reader.getJSONArray("result");
          this.notifyObservers(UpdateSource.profile);
          break;
        case "get_transactions":
          transactions = reader.getJSONArray("result");
          this.notifyObservers(UpdateSource.profile);
          break;
        case "get_used_credit":
          usedCredit = reader.getJSONArray("result");
          this.notifyObservers(UpdateSource.profile);
          break;
        case "get_expiring_credit":
          expiringCredit = reader.getJSONArray("result");
          this.notifyObservers(UpdateSource.profile);
          break;
        case "open_door":
        case "get_voucher":
          this.notifyObservers(UpdateSource.profile, ur);
          break;
        case "add_booking":
        case "cancel_booking":
        case "redeem_code":
        case "begin_monthly":
          this.notifyObservers(UpdateSource.classes, ur);
          return;
        case "update_settings":
          String jsonEndpoint = "https://ashtangayoga.ie/json/?a=";
          ScraperManager task = new ScraperManager();
          task.setDelegate(this.callback);
          task.execute(jsonEndpoint + "get_profile");
          return;
        default:
          break;
      }
    } catch (Exception e) {
      Log.v("ayc-error", e.getMessage());
    }
  }

  @Override
  public void attach(final Observer obj) {
    if (!ScraperManager.observers.contains(obj)) {
      Log.v("ayc-scraper-attach", obj.toString());
      ScraperManager.observers.add(obj);
    }
  }

  @Override
  public void detach(final Observer obj) {
    ScraperManager.observers.remove(obj);
  }

  @Override
  public void notifyObservers(
      final UpdateSource updateSource, final UpdateResponse urXXXXXXXXXXXX) {
    Log.v("ayc-scraper-update", "update all: " + observers.size());
    for (Observer obj : ScraperManager.observers) {
      Log.v("ayc-scraper-update", obj.toString());
      obj.update(updateSource, urXXXXXXXXXXXX);
    }
  }

  @Override
  public void notifyObservers(final UpdateSource updateSource) {
    for (Observer obj : ScraperManager.observers) {
      obj.update(updateSource);
    }
  }

  @Override
  public void notifyEverySource() {
    for (UpdateSource source : UpdateSource.values()) {
      if (source == UpdateSource.logout) {
        continue;
      }
      this.notifyObservers(source);
    }
  }
}
