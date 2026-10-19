package ie.ayc;

import org.json.JSONException;

/** Async HTTP task that delivers its result to an {@link AsyncResponse}. */
public abstract class CookieTask extends AycAsyncTask<String, String, String> {
  /** delegate. */
  private AsyncResponse delegate;

  /**
   * Sets the listener invoked on the main thread with the response body.
   *
   * @param response listener
   */
  public final void setDelegate(final AsyncResponse response) {
    this.delegate = response;
  }

  @Override
  protected final void onPreExecute() {
    super.onPreExecute();
  }

  @Override
  protected final void onPostExecute(final String result) {
    try {
      this.delegate.processFinish(result);
    } catch (JSONException exception) {
      exception.printStackTrace();
    }
  }
}
