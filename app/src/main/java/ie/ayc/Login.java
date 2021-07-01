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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jediburrell.customfab.FloatingActionButton;

import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import com.bumptech.glide.Glide;


public class Login extends AppCompatActivity implements AsyncResponse {

    private Animation scale;
    public static Context mContext;
    static int stage = 0;
    private final AsyncResponse this_async;

    public Login() {
        this.this_async = this;
        mContext = this;
        CookieHandler.setDefault(new CookieManager());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.scale = AnimationUtils.loadAnimation(this, R.anim.buttonclick);

        ImageView loading = findViewById(R.id.loadingView);
        Glide.with(this).load(R.drawable.loadingspinner).into(loading);
        loading.setVisibility(View.VISIBLE);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        LinearLayout login_form = findViewById(R.id.login_form);
        login_form.setVisibility(View.GONE);

        final EditText editname = this.findViewById(R.id.edittext_username);
        editname.setOnFocusChangeListener( new View.OnFocusChangeListener() {
            public void onFocusChange( View v, boolean hasFocus ) {
                if( hasFocus ) {
                    editname.setText( "", TextView.BufferType.EDITABLE );
                }
            }
        });

        TextView rtv = this.findViewById(R.id.register_link);
        rtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://ashtangayoga.ie/wp-login.php?action=register")));
            }
        });

        TextView ltv = this.findViewById(R.id.lost_password_link);
        ltv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://ashtangayoga.ie/wp-login.php?action=lostpassword")));
            }
        });

        TextView ptv = this.findViewById(R.id.privacy_policy_link);
        ptv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://ashtangayoga.ie/privacy-policy")));
            }
        });

        FloatingActionButton fab = findViewById(R.id.floating_action_button);
        fab.setFabSize(FloatingActionButton.FAB_SIZE_MINI);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("ayc", "ayc fab");
                Intent myIntent = new Intent(Login.this.getApplicationContext(), BugReportActivity.class);
                Login.this.startActivity(myIntent);
            }
        });

        stage = 0;
        this.check_logged_in();
    }

    @Override
    protected void onResume() {
        super.onResume();
        stage = 0;
        Log.v("ayc-social", "onresume");
        this.check_logged_in();
    }

    private void check_logged_in() {
        Log.v("ayc-login", "check logged in");
        SessionManager task = new SessionManager();
        task.delegate = this.this_async;
        task.execute("https://ashtangayoga.ie/json/?action=check_logged_in");
    }

    public void enableLoginButton() {
        final Button clickButton = findViewById(R.id.login_button);

        clickButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clickButton.startAnimation(scale);

                LoginManager task = new LoginManager();
                stage = 1;
                task.delegate = this_async;

                EditText usernameField = findViewById(R.id.edittext_username);
                EditText passwordField = findViewById(R.id.edittext_password);

                String uname = usernameField.getText().toString();
                String pword = passwordField.getText().toString();

                if (uname.compareToIgnoreCase("name") == 0) {
                    Common.alert(getBaseContext(), "Please enter your username");
                    return;
                }

                if (uname.length() < 3) {
                    Common.alert(getBaseContext(), "Username too short");
                    return;
                }

                if (pword.length() < 3) {
                    Common.alert(getBaseContext(), "Password too short");
                    return;
                }

                String postfields = "log=" + usernameField.getText() + "&pwd=" + passwordField.getText() + "&wp-submit=Log+In&redirect_to=https%3A%2F%2Fashtangayoga.ie%2Fprofile%2F&testcookie=1";
                task.execute("https://ashtangayoga.ie/wp-login.php", postfields);
                clickButton.setText("Standby");
            }
        });

        final ImageButton facebookButton = findViewById(R.id.login_button_facebook);

        facebookButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                facebookButton.startAnimation(scale);
                Intent myIntent = new Intent(Login.this, SocialLoginManager.class);
                myIntent.putExtra("type","fb");
                Login.this.startActivity(myIntent);
            }
        });

        final ImageButton googleButton = findViewById(R.id.login_button_google);

        googleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                googleButton.startAnimation(scale);
                Intent myIntent = new Intent(Login.this, SocialLoginManager.class);
                myIntent.putExtra("type","google");
                Login.this.startActivity(myIntent);
            }
        });
    }

    public void loginProcessed() {
        ScraperManager mgr = ScraperManager.getInstance();
        mgr.fetch_all();

        Intent myIntent = new Intent(Login.this, AycNavigationActivity.class);
        Login.this.startActivity(myIntent);
    }

    @Override
    public void processFinish(String output) {
        final Button clickButton = findViewById(R.id.login_button);
        Log.v("ayc-delegate", output);
        Log.v("ayc-delegate-stage", String.valueOf(stage));

        ImageView loading = findViewById(R.id.loadingView);
        LinearLayout login_form = findViewById(R.id.login_form);

        try {
            JSONObject reader = new JSONObject(output);
            if (stage == 0) {
                if (reader.getString("result").compareToIgnoreCase("false") == 0) {
                    Log.v("ayc-delegate-button", "enabled");
                    AycCookieManager.getInstance().clearCookies();
                    loading.setVisibility(View.GONE);
                    login_form.setVisibility(View.VISIBLE);
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
                login_form.setVisibility(View.GONE);
                Log.v("ayc-delegate-button", "click, check login");
                this.check_logged_in();
                clickButton.setText("Standby");
            } else if (stage == 2) {
                if (reader.getString("result").compareToIgnoreCase("false") == 0) {
                    Log.v("ayc-delegate", "login failed confirmed");
                    AycCookieManager.getInstance().clearCookies();
                    loading.setVisibility(View.GONE);
                    login_form.setVisibility(View.VISIBLE);
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
