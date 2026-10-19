package ie.ayc;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.net.CookieHandler;
import java.net.CookieManager;
import org.json.JSONObject;

public final class Login extends AppCompatActivity implements AsyncResponse {
  /** Minimum characters required in a login field. */
  private static final int MIN_FIELD_LENGTH = 3;

  /** SITE. */
  private static final String SITE = "https://ashtangayoga.ie/";

  /** LOGIN_PAGE. */
  private static final String LOGIN_PAGE = "wp-login.php?action=";

  /** REGISTER_URL. */
  private static final String REGISTER_URL = SITE + LOGIN_PAGE + "register";

  /** RESET_URL. */
  private static final String RESET_URL = SITE + LOGIN_PAGE + "lostpassword";

  /** PRIVACY_URL. */
  private static final String PRIVACY_URL = SITE + "privacy-policy";

  /** LOGIN_HOST. */
  private static final String LOGIN_HOST = "https%3A%2F%2Fashtangayoga.ie%2F";

  /** LOGIN_REDIRECT. */
  private static final String LOGIN_REDIRECT = LOGIN_HOST + "profile%2F";

  /** LOGIN_SUFFIX. */
  private static final String LOGIN_SUFFIX = "&wp-submit=Log+In&redirect_to=";

  /** appContext. */
  private static Context appContext;

  /** stage. */
  private static int stage = 0;

  /** scale. */
  private Animation scale;

  /** callback. */
  private final AsyncResponse callback;

  /** Login. */
  public Login() {
    this.callback = this;
    appContext = this;
    CookieHandler.setDefault(new CookieManager());
  }

  /**
   * Application context captured when the login screen is created.
   *
   * @return login context
   */
  public static Context appContext() {
    return appContext;
  }

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    this.scale = AnimationUtils.loadAnimation(this, R.anim.buttonclick);

    ImageView loading = findViewById(R.id.loadingView);
    Glide.with(this).load(R.drawable.loadingspinner).into(loading);
    loading.setVisibility(View.VISIBLE);

    try {
      this.getSupportActionBar().hide();
    } catch (NullPointerException e) {
    }

    LinearLayout loginForm = findViewById(R.id.loginForm);
    loginForm.setVisibility(View.GONE);

    final EditText editname = this.findViewById(R.id.edittext_username);
    editname.setOnFocusChangeListener(
        new View.OnFocusChangeListener() {
          public void onFocusChange(final View v, final boolean hasFocus) {
            if (hasFocus) {
              editname.setText("", TextView.BufferType.EDITABLE);
            }
          }
        });

    TextView rtv = this.findViewById(R.id.register_link);
    rtv.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(final View v) {
            Uri register = Uri.parse(REGISTER_URL);
            startActivity(new Intent(Intent.ACTION_VIEW, register));
          }
        });

    TextView ltv = this.findViewById(R.id.lost_password_link);
    ltv.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(final View v) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RESET_URL)));
          }
        });

    TextView ptv = this.findViewById(R.id.privacy_policy_link);
    ptv.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(final View v) {
            Uri privacy = Uri.parse(PRIVACY_URL);
            startActivity(new Intent(Intent.ACTION_VIEW, privacy));
          }
        });

    ActivityChrome.attachBugReportButton(this);

    stage = 0;
    this.checkLoggedIn();
  }

  @Override
  protected void onResume() {
    super.onResume();
    stage = 0;
    Log.v("ayc-social", "onresume");
    this.checkLoggedIn();
  }

  private void checkLoggedIn() {
    Log.v("ayc-login", "check logged in");
    SessionManager task = new SessionManager();
    task.setDelegate(this.callback);
    task.execute("https://ashtangayoga.ie/json/?action=check_logged_in");
  }

  /** enableLoginButton. */
  public void enableLoginButton() {
    final Button clickButton = findViewById(R.id.login_button);

    clickButton.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(final View v) {
            clickButton.startAnimation(scale);

            LoginManager task = new LoginManager();
            stage = 1;
            task.setDelegate(Login.this.callback);

            EditText usernameField = findViewById(R.id.edittext_username);
            EditText passwordField = findViewById(R.id.edittext_password);

            String uname = usernameField.getText().toString();
            String pword = passwordField.getText().toString();

            if (uname.compareToIgnoreCase("name") == 0) {
              Common.alert(getBaseContext(), "Please enter your username");
              return;
            }

            if (uname.length() < MIN_FIELD_LENGTH) {
              Common.alert(getBaseContext(), "Username too short");
              return;
            }

            if (pword.length() < MIN_FIELD_LENGTH) {
              Common.alert(getBaseContext(), "Password too short");
              return;
            }

            String logValue = "log=" + usernameField.getText();
            String pwdValue = "&pwd=" + passwordField.getText();
            String postfields = logValue + pwdValue + LOGIN_SUFFIX;
            postfields += LOGIN_REDIRECT + "&testcookie=1";
            task.execute("https://ashtangayoga.ie/wp-login.php", postfields);
            clickButton.setText("Standby");
          }
        });

    final ImageButton facebookButton = findViewById(R.id.login_button_facebook);

    facebookButton.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(final View v) {
            facebookButton.startAnimation(scale);
            Intent myIntent = new Intent(Login.this, SocialLoginManager.class);
            myIntent.putExtra("type", "fb");
            Login.this.startActivity(myIntent);
          }
        });

    final ImageButton googleButton = findViewById(R.id.login_button_google);

    googleButton.setOnClickListener(
        new View.OnClickListener() {
          public void onClick(final View v) {
            googleButton.startAnimation(scale);
            Intent myIntent = new Intent(Login.this, SocialLoginManager.class);
            myIntent.putExtra("type", "google");
            Login.this.startActivity(myIntent);
          }
        });
  }

  /** loginProcessed. */
  public void loginProcessed() {
    ScraperManager mgr = ScraperManager.getInstance();
    mgr.fetchAll();

    Intent myIntent = new Intent(Login.this, AycNavigationActivity.class);
    Login.this.startActivity(myIntent);
  }

  @Override
  public void processFinish(final String output) {
    final Button clickButton = findViewById(R.id.login_button);
    Log.v("ayc-delegate", output);
    Log.v("ayc-delegate-stage", String.valueOf(stage));

    ImageView loading = findViewById(R.id.loadingView);
    LinearLayout loginForm = findViewById(R.id.loginForm);

    try {
      JSONObject reader = new JSONObject(output);
      if (stage == 0) {
        if (reader.getString("result").compareToIgnoreCase("false") == 0) {
          Log.v("ayc-delegate-button", "enabled");
          AycCookieManager.getInstance().clearCookies();
          loading.setVisibility(View.GONE);
          loginForm.setVisibility(View.VISIBLE);
          this.enableLoginButton();
          clickButton.setText("LOGIN");
        } else {
          Log.v("ayc-delegate-button", "login success send intent");
          this.loginProcessed();
          clickButton.setText("Standby");
        }
      } else if (stage == 1) {
        stage = 2;
        loading.setVisibility(View.VISIBLE);
        loginForm.setVisibility(View.GONE);
        Log.v("ayc-delegate-button", "click, check login");
        this.checkLoggedIn();
        clickButton.setText("Standby");
      } else if (stage == 2) {
        if (reader.getString("result").compareToIgnoreCase("false") == 0) {
          Log.v("ayc-delegate", "login failed confirmed");
          AycCookieManager.getInstance().clearCookies();
          loading.setVisibility(View.GONE);
          loginForm.setVisibility(View.VISIBLE);
          clickButton.setText("LOGIN");
        } else {
          Log.v("ayc-delegate-button", "login success send intent");
          this.loginProcessed();
          clickButton.setText("Standby");
        }
      }
    } catch (Exception e) {
      Log.v("ayc-error", e.getStackTrace().toString());
    }
  }
}
