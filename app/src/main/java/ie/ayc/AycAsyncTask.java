package ie.ayc;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Background work posted back to the main thread.
 *
 * @param <Params> unused type parameter kept for callers
 * @param <Progress> progress value type
 * @param <Result> background result type
 */
public abstract class AycAsyncTask<Params, Progress, Result> {
  /** executor. */
  private final ExecutorService executor;

  /** handler. */
  private Handler handler;

  /** Creates a daemon executor for background work. */
  protected AycAsyncTask() {
    executor =
        Executors.newSingleThreadExecutor(
            runnable -> {
              Thread thread = new Thread(runnable);
              thread.setDaemon(true);
              return thread;
            });
  }

  /**
   * Returns the background executor.
   *
   * @return executor
   */
  public final ExecutorService getExecutor() {
    return executor;
  }

  /**
   * Returns the main-thread handler, creating it on first use.
   *
   * @return handler
   */
  public final Handler getHandler() {
    if (handler == null) {
      synchronized (AycAsyncTask.class) {
        handler = new Handler(Looper.getMainLooper());
      }
    }
    return handler;
  }

  /** Runs on the main thread before background work starts. */
  protected void onPreExecute() {
    // Subclasses override this wherever they need work before execution.
  }

  /**
   * Runs the background work.
   *
   * @param params inputs
   * @return result
   */
  protected abstract Result doInBackground(String... params);

  /**
   * Receives the background result on the main thread.
   *
   * @param result value
   */
  protected abstract void onPostExecute(Result result);

  /**
   * Receives a progress update on the main thread.
   *
   * @param value progress
   */
  protected void onProgressUpdate(final Progress value) {
    // Subclasses override this wherever they need a progress update.
  }

  /**
   * Posts a progress value to the main thread.
   *
   * @param value progress
   */
  public final void publishProgress(final Progress value) {
    getHandler().post(() -> onProgressUpdate(value));
  }

  /** Starts the task with no arguments. */
  public final void execute() {
    execute((String) null);
  }

  /**
   * Starts the task.
   *
   * @param params inputs
   */
  public final void execute(final String... params) {
    getHandler()
        .post(
            () -> {
              onPreExecute();
              executor.execute(
                  () -> {
                    Result result = doInBackground(params);
                    getHandler().post(() -> onPostExecute(result));
                  });
            });
  }

  /** Stops background work. */
  public final void shutDown() {
    if (executor != null) {
      executor.shutdownNow();
    }
  }

  /**
   * Reports whether the executor has stopped.
   *
   * @return true when the executor is absent or stopped
   */
  public final boolean isCancelled() {
    return executor == null || executor.isTerminated() || executor.isShutdown();
  }
}
