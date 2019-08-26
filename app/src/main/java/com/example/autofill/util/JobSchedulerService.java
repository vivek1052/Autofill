package com.example.autofill.util;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;


public class JobSchedulerService extends JobService {
    private static final String TAG = "JobSchedulerService";

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.d(TAG, "onStartJob: Job Started successfully");
        new Thread(new Runnable() {
            @Override
            public void run() {
                DriveDataModel driveDataModel = new DriveDataModel(getBaseContext());
                driveDataModel.upload(Contract.DATABASE_NAME);
                Log.d(TAG,"File Uploaded");
                jobFinished(jobParameters,false);
            }
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d(TAG, "Job Cancelled");
        return true;
    }
}
