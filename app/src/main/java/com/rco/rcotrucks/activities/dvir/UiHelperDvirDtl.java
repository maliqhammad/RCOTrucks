package com.rco.rcotrucks.activities.dvir;


import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.rco.rcotrucks.activities.dvir.fragments.DvirReport;
import com.rco.rcotrucks.activities.dvir.fragments.EditPretripFragment;
import com.rco.rcotrucks.activities.dvir.fragments.PreTripReportFragment;
import com.rco.rcotrucks.businesslogic.SyncTask;
import com.rco.rcotrucks.utils.BuildUtils;

import java.lang.ref.WeakReference;

public class UiHelperDvirDtl {
    public static final String TAG = "UiHelperDvirDtl";

    private boolean isDebugMode = BuildUtils.IS_DEBUG;

    private static UiHelperDvirDtl instance;
    private BusHelperDvir rules;

    public UiHelperDvirDtl() {
        rules = BusHelperDvir.instance();
    }

    public static synchronized UiHelperDvirDtl instance() {
        if (instance == null) {
            instance = new UiHelperDvirDtl();
        }

        return instance;
    }

    //region Refresh


    /**
     * Runs background task to refetch the ListView items and redisplay them.  Must be called from UI thread
     * because refreshTask uses serial ".execute()" method.
     */
    public void runRefreshTask(final DvirDtlActivity fragment, final long idRmsRecords,
                               final String objectIdDvirDetail, final String objectTypeDvirDetail) {
        Log.d(TAG, "runRefreshTask() Start.");
        final WeakReference<DvirDtlActivity> activityWeakReference = new WeakReference<>(fragment);

        SyncTask refreshTask = new SyncTask(fragment, new SyncTask.IRefreshTaskMethods() {
            @Override
            public void loadScreen() {
                Log.d(TAG, "runRefreshTask.loadScreen() start.");
                DvirDtlActivity activity = activityWeakReference.get();
                // Todo: maybe need notify if adapter already attached, optimization.
                if (activity != null && !activity.isFinishing()) {
                    Log.d(TAG, "runRefreshTask.loadScreen() calling activity.loadContentView().");
                    activity.loadContentView();
                }
                Log.d(TAG, "runRefreshTask.loadScreen() end.");
            }

            @Override
            public void executeSyncItems() throws Exception {
                rules.loadDvirDtlItems(idRmsRecords, objectIdDvirDetail, objectTypeDvirDetail);
            }
        }, "Refreshing Dvir usage data...");

        Log.d(TAG, "runRefreshTask() starting task, parallel as a design default in case other AsyncTasks are running in background.");
//        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        // We'll run this and the save dvir task on serial executor so they can't conflict.  Could also use
        // a lock/semaphore to be more granular.
        refreshTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

//    public void runRefreshTask(final EditPretripFragment fragment, final long idRmsRecords,
//                               final String objectIdDvirDetail, final String objectTypeDvirDetail) {
//        Log.d(TAG, "runRefreshTask() Start.");
//        final WeakReference<EditPretripFragment> activityWeakReference = new WeakReference<>(fragment);
//
//        SyncTask refreshTask = new SyncTask(fragment, new SyncTask.IRefreshTaskMethods() {
//            @Override
//            public void loadScreen() {
//                Log.d(TAG, "runRefreshTask.loadScreen() start.");
//                DvirDtlActivity activity = activityWeakReference.get();
//                // Todo: maybe need notify if adapter already attached, optimization.
//                if (activity != null && !activity.isFinishing()) {
//                    Log.d(TAG, "runRefreshTask.loadScreen() calling activity.loadContentView().");
//                    activity.loadContentView();
//                }
//                Log.d(TAG, "runRefreshTask.loadScreen() end.");
//            }
//
//            @Override
//            public void executeSyncItems() throws Exception {
//                rules.loadDvirDtlItems(idRmsRecords, objectIdDvirDetail, objectTypeDvirDetail);
//            }
//        }, "Refreshing Dvir usage data...");
//
//        Log.d(TAG, "runRefreshTask() starting task, parallel as a design default in case other AsyncTasks are running in background.");
////        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        // We'll run this and the save dvir task on serial executor so they can't conflict.  Could also use
//        // a lock/semaphore to be more granular.
//        refreshTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
//    }

    public void runRefreshTask(Activity activity, final PreTripReportFragment fragment, final long idRmsRecords,
                               final String objectIdDvirDetail, final String objectTypeDvirDetail) {
        Log.d(TAG, "runRefreshTask() Start.");
        final WeakReference<PreTripReportFragment> activityWeakReference = new WeakReference<>(fragment);

        SyncTask refreshTask = new SyncTask(activity, new SyncTask.IRefreshTaskMethods() {
            @Override
            public void loadScreen() {
                Log.d(TAG, "runRefreshTask.loadScreen() start.");
                PreTripReportFragment preTripReportFragment = activityWeakReference.get();
                if (preTripReportFragment != null && !preTripReportFragment.isDetached()) {
                    Log.d(TAG, "runRefreshTask.loadScreen() calling activity.loadContentView().");
                    preTripReportFragment.loadContentView();
                }
                Log.d(TAG, "runRefreshTask.loadScreen() end.");
            }

            @Override
            public void executeSyncItems() throws Exception {
                rules.loadDvirDtlItems(idRmsRecords, objectIdDvirDetail, objectTypeDvirDetail);
            }
        }, "Refreshing Dvir usage data...");

        Log.d(TAG, "runRefreshTask() starting task, parallel as a design default in case other AsyncTasks are running in background.");
//        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        // We'll run this and the save dvir task on serial executor so they can't conflict.  Could also use
        // a lock/semaphore to be more granular.
        refreshTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }


    public void runRefreshTask(Activity activity, final EditPretripFragment fragment, final long idRmsRecords,
                               final String objectIdDvirDetail, final String objectTypeDvirDetail) {
        Log.d(TAG, "runRefreshTask() Start.");
        final WeakReference<EditPretripFragment> activityWeakReference = new WeakReference<>(fragment);

        SyncTask refreshTask = new SyncTask(activity, new SyncTask.IRefreshTaskMethods() {
            @Override
            public void loadScreen() {
                Log.d(TAG, "runRefreshTask.loadScreen() start.");
                EditPretripFragment editPretripFragment = activityWeakReference.get();
                if (editPretripFragment != null && !editPretripFragment.isDetached()) {
                    Log.d(TAG, "runRefreshTask.loadScreen() calling activity.loadContentView().");
                    editPretripFragment.loadContentView();
                }
                Log.d(TAG, "runRefreshTask.loadScreen() end.");
            }

            @Override
            public void executeSyncItems() throws Exception {
                rules.loadDvirDtlItems(idRmsRecords, objectIdDvirDetail, objectTypeDvirDetail);
            }
        }, "Refreshing Dvir usage data...");

        Log.d(TAG, "runRefreshTask() starting task, parallel as a design default in case other AsyncTasks are running in background.");
//        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        // We'll run this and the save dvir task on serial executor so they can't conflict.  Could also use
        // a lock/semaphore to be more granular.
        refreshTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }


    public void runRefreshTask(Activity activity, final DvirReport fragment, final long idRmsRecords,
                               final String objectIdDvirDetail, final String objectTypeDvirDetail) {
        Log.d(TAG, "runRefreshTask() Start.");
        final WeakReference<DvirReport> activityWeakReference = new WeakReference<>(fragment);

        SyncTask refreshTask = new SyncTask(activity, new SyncTask.IRefreshTaskMethods() {
            @Override
            public void loadScreen() {
                Log.d(TAG, "runRefreshTask.loadScreen() start.");
                DvirReport dvirReport = activityWeakReference.get();
                if (dvirReport != null && !dvirReport.isDetached()) {
                    Log.d(TAG, "runRefreshTask.loadScreen() calling activity.loadContentView().");
                    dvirReport.loadContentView();
                }
                Log.d(TAG, "runRefreshTask.loadScreen() end.");
            }

            @Override
            public void executeSyncItems() throws Exception {
                rules.loadDvirDtlItems(idRmsRecords, objectIdDvirDetail, objectTypeDvirDetail);
            }
        }, "Refreshing Dvir usage data...");

        Log.d(TAG, "runRefreshTask() starting task, parallel as a design default in case other AsyncTasks are running in background.");
//        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        // We'll run this and the save dvir task on serial executor so they can't conflict.  Could also use
        // a lock/semaphore to be more granular.
        refreshTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }


    // endregion Refresh

    // region Up Sync

    // This code probably belongs in another class.

    ;

    public static Object lockSyncUpdateRmsDvirsTask = new Object();
    public static boolean isUpdateRmsDvirsInProgress = false;

    /**
     * Runs background task to query un-upsynced DVIR records from the local database and
     * send them to the Server, then mark the records as sent (if confirmed by returned response).
     */
    public void runSyncUpdateRmsDvirsTask(int maxBatchSize) {
        if (isUpdateRmsDvirsInProgress) return;

        synchronized (lockSyncUpdateRmsDvirsTask) {
            if (isUpdateRmsDvirsInProgress) return;

            isUpdateRmsDvirsInProgress = true;

            SyncUpdateRmsDvirsTask task = new SyncUpdateRmsDvirsTask(SyncUpdateModes.ONE_TIME, maxBatchSize);
            Log.d(TAG, "csv driv: runSyncUpdateRmsDvirsTask() starting task, parallel as a design default in case other AsyncTasks are running in background.");
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public static enum SyncUpdateModes {ONE_TIME, WHILE_PENDING, PERPETUAL}

    public class SyncUpdateRmsDvirsTask extends AsyncTask<String, Integer, Integer> {
        private int syncIntervalSecs = 10;
        private SyncUpdateModes enumMode;
        private int batchSize = -1;

        public SyncUpdateRmsDvirsTask(SyncUpdateModes mode, int batchSize) {
            this.enumMode = mode;
            this.batchSize = batchSize;
        }

        @Override
        protected void onPreExecute() {
            try {
            } catch (Throwable throwable) {
                if (throwable != null)
                    throwable.printStackTrace();
            }
        }

        @Override
        protected Integer doInBackground(String... strings) {
            Log.d(TAG, "csv driv: doInBackground: ");
            int batchSize = 20;
            int maxBatches = 4;
            try {
                do {

                    BusHelperDvir helperDvir = new BusHelperDvir();
//                BusHelperDvir.instance().upsyncDvirGroup(batchSize, maxBatches);
                    helperDvir.upsyncDvirGroup(batchSize, maxBatches);
                    if (enumMode != SyncUpdateModes.PERPETUAL) break;
//                    else if (enumMode == SyncUpdateModes.WHILE_PENDING && isDone) break;

                    Thread.sleep(syncIntervalSecs * 1000);
                } while (true);
            } catch (Throwable throwable) {
                if (throwable != null)
                    throwable.printStackTrace();
            }

            return null;
        }


        //        public boolean upsyncDvirs() throws Exception {
//            List<Map<String, String>> list = rules.getPendingDvirs(maxBatchSize);
//
//            if ((list == null || list.size() == 0) && enumMode != SyncUpdateModes.PERPETUAL)
//                return true;
//
//            // 2.  Send pending records to RMS Server
//            String returnJson = rules.sendPendingDvirs(list);
//
//            // 3.  Mark successfully sent DVIR records as sent based on returned response.
//
//            if (!StringUtils.isNullOrWhitespaces(returnJson))
//                rules.updateRecordsFromUpsyncResponse(returnJson);
//            return false;
//        }
//
        @Override
        protected void onProgressUpdate(Integer... values) {
            try {

            } catch (Throwable throwable) {
                if (throwable != null)
                    throwable.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            try {

            } catch (Throwable throwable) {
                if (throwable != null)
                    throwable.printStackTrace();
            } finally {
                isUpdateRmsDvirsInProgress = false;
            }
        }
    }

    // endregion Up Sync

    // region Support

    // endregion Support


}
