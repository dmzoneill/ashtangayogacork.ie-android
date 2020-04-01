package ie.ayc;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class AycNavigationActivity extends AppCompatActivity implements Observer {

    public AycNavigationActivity(){
        ScraperManager.getInstance().attach(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayc_navigation);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_prices, R.id.navigation_profile, R.id.navigation_classes)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    protected void onResume(){
        super.onResume();
        ScraperManagerScheduler.schedule_task(getApplicationContext());
    }

    @Override
    public void update(UpdateSource updatesource) {
        try{
            if(updatesource == UpdateSource.logout) {
                finish();
                return;
            }
        }
        catch(Exception e){
            Log.v("ayc-aycnav", e.getMessage());
        }
    }
}
