package ie.ayc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jediburrell.customfab.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import ie.ayc.ui.PricesFragment;

public class AycNavigationActivity extends AppCompatActivity implements Observer {

    public static FirebaseAnalytics mFirebaseAnalytics;
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
                Intent myIntent = new Intent(AycNavigationActivity.this.getApplicationContext(), BugReportActivity.class);
                AycNavigationActivity.this.startActivity(myIntent);
            }
        });

        AycNavigationActivity.mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        //ScraperManagerScheduler.schedule_task(getApplicationContext());
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

    @Override
    public void update(UpdateSource updatesource, UpdateResponse ur) {
        Log.v("ayc-classes", "update response");
    }
}
