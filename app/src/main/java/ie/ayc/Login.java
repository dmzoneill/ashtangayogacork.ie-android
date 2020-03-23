package ie.ayc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;


public class Login extends AppCompatActivity implements AsyncResponse {

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
        stage = 0;
        this.check_logged_in();
    }

    private void check_logged_in() {
        SessionManager task = new SessionManager();
        task.delegate = this.this_async;
        task.execute("https://ashtangayoga.ie/json/?action=check_logged_in");
    }

    public void enableLoginButton() {
        Button clickButton = findViewById(R.id.login_button);

        clickButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LoginManager task = new LoginManager();
                stage = 1;
                task.delegate = this_async;

                EditText usernameField = findViewById(R.id.edittext_username);
                EditText passwordField = findViewById(R.id.edittext_password);

                if (usernameField.getText().length() < 3) {
                    Toast username_too_short = Toast.makeText(getApplicationContext(), "Username too short", Toast.LENGTH_LONG);
                    username_too_short.show();
                    return;
                }

                if (passwordField.getText().length() < 3) {
                    Toast password_too_short = Toast.makeText(getApplicationContext(), "Password too short", Toast.LENGTH_LONG);
                    password_too_short.show();
                    return;
                }

                String postfields = "log=" + usernameField.getText() + "&pwd=" + passwordField.getText() + "&wp-submit=Log+In&redirect_to=https%3A%2F%2Fashtangayoga.ie%2Fprofile%2F&testcookie=1";
                task.execute("https://ashtangayoga.ie/wp-login.php", postfields);
            }
        });
    }

    public void loginProcessed() {
        ScraperManager mgr = ScraperManager.getInstance();
        mgr.fetch_all();

        Intent myIntent = new Intent(Login.this, AycNavigationActivity.class);
        //myIntent.putExtra("key", value); //Optional parameters
        Login.this.startActivity(myIntent);
    }

    @Override
    public void processFinish(String output) {
        Log.v("ayc-delegate", output);
        Log.v("ayc-delegate-stage", String.valueOf(stage));
        try {
            JSONObject reader = new JSONObject(output);
            if (stage == 0) {
                if (reader.getString("result").compareToIgnoreCase("false") == 0) {
                    Log.v("ayc-delegate-button", "enabled");
                    this.enableLoginButton();
                } else {
                    Log.v("ayc-delegate-button", "login success send intent");
                    this.loginProcessed();
                }
            } else if (stage == 1) {
                stage = 2;
                Log.v("ayc-delegate-button", "click, check login");
                this.check_logged_in();
            } else if (stage == 2) {
                if (reader.getString("result").compareToIgnoreCase("false") == 0) {
                    Log.v("ayc-delegate", "login failed confirmed");
                } else {
                    Log.v("ayc-delegate-button", "login success send intent");
                    this.loginProcessed();
                }
            }
        } catch (Exception e) {
            Log.v("ayc-error", e.getStackTrace().toString());
        }
    }
}
