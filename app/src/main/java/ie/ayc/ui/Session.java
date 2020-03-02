package ie.ayc.ui;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ie.ayc.Login;

public class Session {
    public static final String COOKIES_HEADER = "Set-Cookie";
    private static List<String> session_cookies;

    static {
        session_cookies = new ArrayList<String>();
        try {
            InputStream inputStream = Login.mContext.openFileInput("cookies");
            BufferedReader buf = new BufferedReader(new InputStreamReader(inputStream));
            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();

            while(line != null){
                sb.append(line);
                line = buf.readLine();
            }

            String fileAsString = sb.toString();
            Log.v("ayc-cookie-load-content", fileAsString);
            if(fileAsString.contains(";")) {
                String[] cookies = fileAsString.split("##");
                for(String cookie: cookies) {
                    //session_cookies.add(cookie);
                    Log.v("ayc-cookie-load", "load cookie: " + cookie);
                }
            }
            Log.v("ayc-cookie-load", "Done loading cookies");
        }
        catch(Exception e){
            Log.v("ayc-cookie-load", "failed to load cookies from file");
        }
    }

    public static List<String> getCookies() {
        for(String cookie: session_cookies){
            Log.v("ayc-cookies-get", cookie);
        }
        return session_cookies;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void setCookies(List<String> cookies) {
        for(String cookie: cookies){
            Log.v("ayc-cookies-set", cookie);
        }
        session_cookies = cookies;

        try {
            FileOutputStream outputStream = Login.mContext.openFileOutput("cookies", Context.MODE_PRIVATE);
            outputStream.write(String.join("##", cookies).getBytes());
            outputStream.close();
            Log.v("ayc-cookie-save", "saved cookies to file");
        }
        catch(Exception e){
            Log.v("ayc-cookie-save", "failed ot save cookies to file");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
}
