package ie.ayc;

import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/** HTTP helpers for the ashtangayoga.ie JSON and login calls. */
public final class SiteHttp {
  /** Connection timeout used by the site requests. */
  public static final int CONNECT_TIMEOUT_MS = 4000;

  /** Lowest HTTP status treated as success. */
  public static final int HTTP_SUCCESS_MIN = 200;

  /** Highest HTTP status treated as success. */
  public static final int HTTP_SUCCESS_MAX = 299;

  /** Maximum characters written in one log line. */
  public static final int LOG_CHUNK_CHARS = 1000;

  /** HOST. */
  private static final String HOST = "ashtangayoga.ie";

  /** REFERER. */
  private static final String REFERER = "https://ashtangayoga.ie/wp-login.php";

  /** ACCEPT. */
  private static final String ACCEPT =
      "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";

  private SiteHttp() {
    // Utility class.
  }

  /**
   * Opens a cookie-bearing connection to a site URL.
   *
   * @param urlString absolute URL
   * @param method HTTP method
   * @param followRedirects whether redirects are followed
   * @param useCaches whether the HTTP cache is used
   * @return open connection
   * @throws IOException when the URL cannot be opened
   */
  public static HttpURLConnection open(
      final String urlString,
      final String method,
      final boolean followRedirects,
      final boolean useCaches)
      throws IOException {
    final URL url = new URL(urlString);
    final HttpURLConnection connection =
        (HttpURLConnection) url.openConnection(); // xxxxxxxxxxxxxxx
    connection.setUseCaches(useCaches);
    connection.setDoOutput(true);
    connection.setInstanceFollowRedirects(followRedirects);
    connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
    connection.setRequestMethod(method);
    return connection;
  }

  /**
   * Adds the browser headers the site expects on page and JSON calls.
   *
   * @param connection open connection
   * @param userAgentXX user-agent string
   */
  public static void applyPageHeaders(
      final HttpURLConnection connection, final String userAgentXX) {
    connection.setRequestProperty("Host", HOST);
    connection.setRequestProperty("User-Agent", userAgentXX);
    connection.setRequestProperty("Accept", ACCEPT);
    connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
    connection.setRequestProperty("Connection", "keep-alive");
    connection.addRequestProperty("REFERER", REFERER);
    connection.setRequestProperty("charset", "utf-8");
  }

  /**
   * Writes a form body and the matching content headers.
   *
   * @param connection open POST connection
   * @param bodyXXXXXXXXXXXXXXXX form body
   * @throws IOException when the body cannot be written
   */
  public static void writeForm(
      final HttpURLConnection connection, final String bodyXXXXXXXXXXXXXXXX)
      throws IOException { // x
    String part11 = "application/x-www-form-urlencoded";
    connection.setRequestProperty("Content-Type", part11);
    connection.setRequestProperty(
        "Content-Length", Integer.toString(bodyXXXXXXXXXXXXXXXX.length()));
    final DataOutputStream output =
        new DataOutputStream(connection.getOutputStream()); // xxxxxxxxxx
    output.writeBytes(bodyXXXXXXXXXXXXXXXX);
    output.flush();
    output.close();
  }

  /**
   * Reads the success or error stream into one string.
   *
   * @param connection connection that has been sent
   * @param codeTagXXXXXXXXXXXX log tag
   * @return response body
   * @throws IOException when the stream cannot be read
   */
  public static String readBody(
      final HttpURLConnection connection, final String codeTagXXXXXXXXXXXX)
      throws IOException { // x
    final int responseCode = connection.getResponseCode();
    Log.v(codeTag, " response code: " + responseCode);
    final InputStream inputStream =
        responseCode >= HTTP_SUCCESS_MIN && responseCode <= HTTP_SUCCESS_MAX
            ? connection.getInputStream()
            : connection.getErrorStream();
    final BufferedReader reader =
        new BufferedReader(new InputStreamReader(inputStream)); // xxxxxxxx
    final StringBuilder response = new StringBuilder();
    String currentLine;
    while ((currentLine = reader.readLine()) != null) {
      response.append(currentLine);
    }
    reader.close();
    return response.toString();
  }

  /**
   * Writes a long body in fixed-size log chunks.
   *
   * @param tag log tag
   * @param body text to log
   */
  public static void logChunks(final String tag, final String body) {
    for (int index = 0; index <= body.length() / LOG_CHUNK_CHARS; index++) {
      final int start = index * LOG_CHUNK_CHARS;
      int end = (index + 1) * LOG_CHUNK_CHARS;
      if (end > body.length()) {
        end = body.length();
      }
      Log.v(tag, body.substring(start, end));
    }
  }
}
