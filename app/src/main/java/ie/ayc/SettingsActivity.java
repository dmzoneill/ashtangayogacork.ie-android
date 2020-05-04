package ie.ayc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.jediburrell.customfab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}

        FloatingActionButton fab = findViewById(R.id.floating_action_button);
        fab.setFabSize(FloatingActionButton.FAB_SIZE_MINI);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("ayc", "ayc fab");
                Intent myIntent = new Intent(getApplicationContext(), BugReportActivity.class);
                SettingsActivity.this.startActivity(myIntent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            try {
                JSONArray profile = ScraperManager.getProfile();

                JSONObject obj = profile.getJSONObject(1);
                String j_phone = obj.getString("phone");
                String j_email = obj.getString("email_alerts");
                String j_sms = obj.getString("sms_alerts");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
                SharedPreferences.Editor editor = prefs.edit();

                editor.putString("phoneno", j_phone);
                editor.commit();

                editor.putBoolean("email", j_email.compareTo("1") == 0 ? true : false);
                editor.commit();

                editor.putBoolean("sms",  j_sms.compareTo("1") == 0 ? true : false);
                editor.commit();

                setPreferencesFromResource(R.xml.root_preferences, rootKey);

                EditTextPreference phonenopref = this.findPreference("phoneno");
                phonenopref.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                    @Override
                    public void onBindEditText(@NonNull EditText editText) {
                        editText.setInputType(InputType.TYPE_CLASS_PHONE);
                    }
                });

                prefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        Log.v("ayc-settings", key);
                        Map<String, ?> keys = prefs.getAll();
                        Log.v("ayc-settings", String.valueOf(keys.size()));
                        for (Map.Entry<String, ?> entry : keys.entrySet()) {
                            Log.v("ayc-settings", entry.getKey() + ": " + entry.getValue().toString());
                        }
                        ScraperManager sm = ScraperManager.getInstance();
                        sm.update_user_settings(prefs.getString("phoneno", ""),
                                String.valueOf(prefs.getBoolean("email", false)),
                                String.valueOf(prefs.getBoolean("email", false))
                        );
                    }
                });
            }
            catch (Exception e) {
                Log.v("ayc-settings", e.getMessage());
            }
        }
    }
}