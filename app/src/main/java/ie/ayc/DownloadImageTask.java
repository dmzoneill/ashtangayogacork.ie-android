package ie.ayc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Objects;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    String blank = "";
    String imgurl = "";

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
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

            byte[] b = new byte[10];
            input.read(b, 0, 10);
            blank = String.valueOf(Arrays.hashCode(b));
            mIcon11 = myBitmap;
            Log.v("ayc-image-loader","loaded");
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap g) {
        Log.v("ayc-download", "here");
        if (blank.compareTo("1917157348") == 0) {
            return;
        }

        bmImage.setImageBitmap(g);
        //bmImage.setImageURI(Uri.parse(this.imgurl));
        Log.v("ayc-download", "there");
    }
}
