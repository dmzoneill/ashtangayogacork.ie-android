package ie.ayc;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public final class SettingsActivity extends AppCompatActivity {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
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

    ActivityChrome.hideActionBar(this);
    ActivityChrome.attachBugReportButton(this);
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      // Respond to the action bar's Up/Home button
      case android.R.id.home:
        super.onBackPressed();
        return true;
      default:
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  public static final class SettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences preferences() {
      Context context = getContext();
      return PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void onCreatePreferences(
        final Bundle savedInstanceState, final String rootKeyXXXXXXXXX) {
      try {
        JSONArray profile = ScraperManager.getProfile();

        JSONObject obj = profile.getJSONObject(1);
        String phoneValue = obj.getString("phone");
        String emailValue = obj.getString("email_alerts");
        String smsValue = obj.getString("sms_alerts");

        SharedPreferences prefs = preferences();
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("phoneno", phoneValue);
        editor.commit();

        editor.putBoolean("email", emailValue.compareTo("1") == 0);
        editor.commit();

        editor.putBoolean("sms", smsValue.compareTo("1") == 0);
        editor.commit();

        setPreferencesFromResource(R.xml.root_preferences, rootKeyXXXXXXXXX);

        EditTextPreference phonenopref = this.findPreference("phoneno");
        phonenopref.setOnBindEditTextListener(
            new EditTextPreference.OnBindEditTextListener() {
              @Override
              public void onBindEditText(@NonNull final EditText editText) {
                editText.setInputType(InputType.TYPE_CLASS_PHONE);
              }
            });

        prefs.registerOnSharedPreferenceChangeListener(
            new SharedPreferences.OnSharedPreferenceChangeListener() {
              public void onSharedPreferenceChanged(
                  final SharedPreferences prefs, final String key) {
                Log.v("ayc-settings", key);
                Map<String, ?> keys = prefs.getAll();
                Log.v("ayc-settings", String.valueOf(keys.size()));
                for (Map.Entry<String, ?> entry : keys.entrySet()) {
                  String setting = entry.getKey() + ": " + entry.getValue();
                  Log.v("ayc-settings", setting);
                }
                ScraperManager sm = ScraperManager.getInstance();
                sm.updateUserSettings(
                    prefs.getString("phoneno", ""),
                    String.valueOf(prefs.getBoolean("email", false)),
                    String.valueOf(prefs.getBoolean("email", false)));
              }
            });
      } catch (Exception e) {
        Log.v("ayc-settings", e.getMessage());
      }
    }
  }
}
