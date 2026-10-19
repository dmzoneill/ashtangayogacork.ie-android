package ie.ayc;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import java.util.concurrent.TimeUnit;

public final class ScraperManagerScheduler extends Worker {
  /** How often the background refresh runs. */
  private static final int REFRESH_MINUTES = 15;

  /** pwr. */
  private static PeriodicWorkRequest pwr;

  /**
   * ScraperManagerScheduler.
   *
   * @param context value
   * @param params value
   */
  public ScraperManagerScheduler(
      @NonNull final Context context, @NonNull final WorkerParameters params) {
    super(context, params);
  }

  @Override
  public Result doWork() {
    ScraperManager sm = ScraperManager.getInstance();
    sm.fetchAll();
    return Result.success();
  }

  /**
   * scheduleTask.
   *
   * @param context value
   */
  public static void scheduleTask(final Context context) {
    WorkManager mWorkManager = WorkManager.getInstance(context);

    if (pwr != null) {
      mWorkManager.cancelWorkById(pwr.getId());
    }

    pwr =
        new PeriodicWorkRequest.Builder(
                ScraperManagerScheduler.class,
                REFRESH_MINUTES,
                TimeUnit.MINUTES) // xxxxxxxxxxxxxxxx
            .build();
    mWorkManager.enqueue(pwr);
  }
}
