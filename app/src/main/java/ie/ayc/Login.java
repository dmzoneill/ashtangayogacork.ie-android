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
        this.check_logged_in();
    }

    private void check_logged_in() {
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
                clickButton.setText("Standy");
            }
        });

        ImageButton facebookButton = findViewById(R.id.login_button_facebook);

        facebookButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(Login.this, SocialLoginManager.class);
                myIntent.putExtra("url", "https://www.facebook.com/login.php?skip_api_login=1&api_key=489379651801344&kid_directed_site=0&app_id=489379651801344&signed_next=1&next=https%3A%2F%2Fwww.facebook.com%2Fv3.2%2Fdialog%2Foauth%3Fdisplay%3Dpopup%26response_type%3Dcode%26client_id%3D489379651801344%26redirect_uri%3Dhttps%253A%252F%252Fashtangayoga.ie%252Fwp-login.php%253FloginSocial%253Dfacebook%26state%3Dcb92ff6b7e1a530d849f53313e43a2ae%26scope%3Dpublic_profile%252Cemail%26ret%3Dlogin%26fbapp_pres%3D0%26logger_id%3Dec48732f-9d02-48cc-acd4-2afc5e54d620&cancel_url=https%3A%2F%2Fashtangayoga.ie%2Fwp-login.php%3FloginSocial%3Dfacebook%26error%3Daccess_denied%26error_code%3D200%26error_description%3DPermissions%2Berror%26error_reason%3Duser_denied%26state%3Dcb92ff6b7e1a530d849f53313e43a2ae%23_%3D_&display=popup&locale=en_US&pl_dbl=0"); //Optional parameters
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
