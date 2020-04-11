package ie.ayc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Objects;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    static Bitmap image = null;
    String imgurl = "";

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        if( DownloadImageTask.image != null) {
            return DownloadImageTask.image;
        }

        String urldisplay = urls[0];
        Log.v("ayc-download", urldisplay);
        Bitmap mIcon11 = null;
        this.imgurl = urldisplay;
        try {
            URL url = new URL(this.imgurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            connection.disconnect();
            input.close();
            mIcon11 = myBitmap;
            Log.v("ayc-image-loader","loaded");
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
        DownloadImageTask.image = mIcon11;
        return DownloadImageTask.image;
    }

    protected void onPostExecute(Bitmap g) {
        Log.v("ayc-download", "got gravatar");
        bmImage.setImageBitmap(g);
    }
}
