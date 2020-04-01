package ie.ayc;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

public class ScraperManagerScheduler extends Worker {
    private static PeriodicWorkRequest pwr;

    public ScraperManagerScheduler(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        ScraperManager sm = ScraperManager.getInstance();
        sm.fetch_all();
        return Result.success();
    }

    public static void schedule_task(Context context) {
        WorkManager mWorkManager = WorkManager.getInstance(context);

        if (pwr != null) {
            mWorkManager.cancelWorkById(pwr.getId());
        }

        pwr = new PeriodicWorkRequest.Builder(ScraperManagerScheduler.class, 15, TimeUnit.MINUTES).build();
        mWorkManager.enqueue(pwr);
    }
}