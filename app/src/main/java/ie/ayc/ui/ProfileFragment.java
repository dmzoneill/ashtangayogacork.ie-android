package ie.ayc.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import ie.ayc.DownloadImageTask;
import ie.ayc.Observer;
import ie.ayc.R;
import ie.ayc.ScraperManager;
import ie.ayc.UpdateSource;

public class ProfileFragment extends Fragment implements Observer {

    private View root;

    public ProfileFragment(){
        ScraperManager sm = ScraperManager.getInstance();
        sm.attach(this);
        sm.object_notify(UpdateSource.profile);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.root = inflater.inflate(R.layout.fragment_profile, container, false);

        ScraperManager sm = ScraperManager.getInstance();
        sm.attach(this);
        sm.object_notify(UpdateSource.profile);

        return root;
    }

    private void upateImageView() {
        ImageView profileview = root.findViewById(R.id.person);
        DownloadImageTask dit = new DownloadImageTask(profileview);
        try {
            JSONArray profile = ScraperManager.getProfile();
            JSONObject obj = profile.getJSONObject(0);
            JSONObject ud = obj.getJSONObject("data");
            String img = ud.getString("user_avatar_url");
            Log.v("ayc-profile",img);
            dit.execute(img);
        }
        catch(Exception e) {
            Log.v("ayc-profile",e.getMessage());
        }
    }

    @Override
    public void update(UpdateSource updatesource) {
        if(updatesource != UpdateSource.profile) return;
        try {
            this.upateImageView();
        }
        catch (Exception e){
            Log.v("ayc-classes-update", e.getMessage());
        }
    }
}
