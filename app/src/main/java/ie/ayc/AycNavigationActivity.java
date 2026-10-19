package ie.ayc;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;

public final class AycNavigationActivity extends AppCompatActivity {

  /** firebaseAnalytics. */
  private static FirebaseAnalytics firebaseAnalytics;

  /** AycNavigationActivity. */
  public AycNavigationActivity() {
    ScraperManager.getInstance()
        .attach(
            new Observer() {
              /** update. */
              public void update(final UpdateSource source) {
                AycNavigationActivity.this.update(source);
              }

              /** update. */
              public void update(final UpdateSource s, final UpdateResponse r) {
                AycNavigationActivity.this.update(s, r);
              }
            });
  }

  /**
   * Shared analytics client created with the navigation screen.
   *
   * @return analytics client
   */
  public static FirebaseAnalytics analytics() {
    return firebaseAnalytics;
  }

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ayc_navigation);
    BottomNavigationView navView = findViewById(R.id.nav_view);
    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.
    AppBarConfiguration appBarConfiguration =
        new AppBarConfiguration.Builder(
                R.id.navigation_prices,
                R.id.navigation_profile,
                R.id.navigation_classes) // xxxxxxxx
            .build();
    int res5 = R.id.nav_host_fragment;
    NavController navController = Navigation.findNavController(this, res5);
    NavigationUI.setupActionBarWithNavController(
        this, navController, appBarConfiguration); // xxxxxx
    NavigationUI.setupWithNavController(navView, navController);

    ActivityChrome.hideActionBar(this);
    ActivityChrome.attachBugReportButton(this);

    firebaseAnalytics = FirebaseAnalytics.getInstance(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    // ScraperManagerScheduler.scheduleTask(getApplicationContext());
  }

  /**
   * update.
   *
   * @param updatesource value
   */
  public void update(final UpdateSource updatesource) {
    try {
      if (updatesource == UpdateSource.logout) {
        finish();
        return;
      }
    } catch (Exception e) {
      Log.v("ayc-aycnav", e.getMessage());
    }
  }

  /**
   * update.
   *
   * @param updatesource value
   * @param ur value
   */
  public void update(final UpdateSource updatesource, final UpdateResponse ur) {
    Log.v("ayc-classes", "update response");
  }
}
