package com.rco.rcotrucks.activities.fuelreceipts.utils;


import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.rco.rcotrucks.activities.fuelreceipts.activities.CreateTollReceipt;
import com.rco.rcotrucks.activities.fuelreceipts.activities.FuelReceiptDtlActivity;
import com.rco.rcotrucks.activities.fuelreceipts.activities.TollReceiptDetailActivity;
import com.rco.rcotrucks.activities.fuelreceipts.fragments.ReceiptDetailFragment;
import com.rco.rcotrucks.activities.fuelreceipts.fragments.TollReceiptDetailFragment;
import com.rco.rcotrucks.businesslogic.SyncTask;
import com.rco.rcotrucks.businesslogic.SyncTaskForFragment;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecordRulesHelper;
import com.rco.rcotrucks.utils.BuildUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UiHelperFuelReceiptDtl {
    public static final String TAG = "UiHelperFuelReceiptDtl";

    private boolean isDebugMode = BuildUtils.IS_DEBUG;

    private static UiHelperFuelReceiptDtl instance;
    private BusHelperFuelReceipts rules;

    public UiHelperFuelReceiptDtl() {
        rules = BusHelperFuelReceipts.instance();
    }

    public static synchronized UiHelperFuelReceiptDtl instance() {
        if (instance == null) {
            instance = new UiHelperFuelReceiptDtl();
        }

        return instance;
    }

    //region Refresh


    /**
     * Runs background task to refetch the ListView items and redisplay them.  Must be called from UI thread
     * because refreshTask uses serial ".execute()" method.
     *
     * @param activity
     */
    public void runRefreshTask(final FuelReceiptDtlActivity activity, final long idRmsRecords,
                               final String objectIdFuelReceiptDetail, final String objectTypeFuelReceiptDetail) {
        Log.d(TAG, "runRefreshTask() Start.");
        final WeakReference<FuelReceiptDtlActivity> activityWeakReference = new WeakReference<>(activity);

        SyncTask refreshTask = new SyncTask(activity, new SyncTask.IRefreshTaskMethods() {
            @Override
            public void loadScreen() {
                Log.d(TAG, "runRefreshTask.loadScreen() start.");
                FuelReceiptDtlActivity activity = activityWeakReference.get();
                // Todo: maybe need notify if adapter already attached, optimization.
                if (activity != null && !activity.isFinishing()) {
                    Log.d(TAG, "runRefreshTask.loadScreen() calling activity.loadContentView().");
                    activity.loadContentView();
                }
                Log.d(TAG, "runRefreshTask.loadScreen() end.");
            }

            @Override
            public void executeSyncItems() throws Exception {
                rules.loadFuelReceiptDtlItems(activity, idRmsRecords, objectIdFuelReceiptDetail, objectTypeFuelReceiptDetail);
            }
        }, "Refreshing FuelReceipt usage data...");

        Log.d(TAG, "runRefreshTask() starting task, serial in case other conflicting AsyncTasks are running in background.");
//        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        // We'll run this and the save fuel receipt task on serial executor so they can't conflict.  Could also use
        // a lock/semaphore to be more granular.
        ExecutorService executor = Executors.newSingleThreadExecutor(); // ******* Todo: experimental.
//        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.d(TAG, "runRefreshTask() starting task, ***** Experimental - using dedicated executor.");

        refreshTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public void runRefreshTask(final TollReceiptDetailActivity activity, final long idRmsRecords,
                               final String objectIdFuelReceiptDetail, final String objectTypeFuelReceiptDetail) {
        Log.d(TAG, "runRefreshTask() Start.");
        final WeakReference<TollReceiptDetailActivity> activityWeakReference = new WeakReference<>(activity);

        SyncTask refreshTask = new SyncTask(activity, new SyncTask.IRefreshTaskMethods() {
            @Override
            public void loadScreen() {
                Log.d(TAG, "runRefreshTask.loadScreen() start.");
                TollReceiptDetailActivity activity = activityWeakReference.get();
                // Todo: maybe need notify if adapter already attached, optimization.
                if (activity != null && !activity.isFinishing()) {
                    Log.d(TAG, "runRefreshTask.loadScreen() calling activity.loadContentView().");
                    activity.loadContentView();
                }
                Log.d(TAG, "runRefreshTask.loadScreen() end.");
            }

            @Override
            public void executeSyncItems() throws Exception {
                rules.loadFuelReceiptDtlItems(activity, idRmsRecords, objectIdFuelReceiptDetail, objectTypeFuelReceiptDetail);
            }
        }, "Refreshing FuelReceipt usage data...");

        Log.d(TAG, "runRefreshTask() starting task, serial in case other conflicting AsyncTasks are running in background.");
//        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        // We'll run this and the save fuel receipt task on serial executor so they can't conflict.  Could also use
        // a lock/semaphore to be more granular.
        ExecutorService executor = Executors.newSingleThreadExecutor(); // ******* Todo: experimental.
//        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.d(TAG, "runRefreshTask() starting task, ***** Experimental - using dedicated executor.");

        refreshTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public void runRefreshTask(final CreateTollReceipt activity, final long idRmsRecords,
                               final String objectIdFuelReceiptDetail, final String objectTypeFuelReceiptDetail) {
        Log.d(TAG, "CreateTollReceipt: runRefreshTask() Start.");
        final WeakReference<CreateTollReceipt> activityWeakReference = new WeakReference<>(activity);

        SyncTask refreshTask = new SyncTask(activity, new SyncTask.IRefreshTaskMethods() {
            @Override
            public void loadScreen() {
                Log.d(TAG, "CreateTollReceipt: runRefreshTask.loadScreen() start.");
                CreateTollReceipt activity = activityWeakReference.get();
                // Todo: maybe need notify if adapter already attached, optimization.
                if (activity != null && !activity.isFinishing()) {
                    Log.d(TAG, "CreateTollReceipt: runRefreshTask.loadScreen() calling activity.loadContentView().");
//                    activity.loadContentView();
                }
                Log.d(TAG, "CreateTollReceipt: runRefreshTask.loadScreen() end.");
            }

            @Override
            public void executeSyncItems() throws Exception {
                Log.d(TAG, "CreateTollReceipt: executeSyncItems: loadFuelReceiptDtlItems: ");
                rules.loadFuelReceiptDtlItems(activity, idRmsRecords, objectIdFuelReceiptDetail, objectTypeFuelReceiptDetail);
            }
        }, "Refreshing FuelReceipt usage data...");

        Log.d(TAG, "CreateTollReceipt: runRefreshTask() starting task, serial in case other conflicting AsyncTasks are running in background.");
//        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        // We'll run this and the save fuel receipt task on serial executor so they can't conflict.  Could also use
        // a lock/semaphore to be more granular.
        ExecutorService executor = Executors.newSingleThreadExecutor(); // ******* Todo: experimental.
//        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.d(TAG, "CreateTollReceipt: runRefreshTask() starting task, ***** Experimental - using dedicated executor.");

        refreshTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }



    public void runRefreshTask(final Activity activity, ReceiptDetailFragment fragment, final long idRmsRecords,
                               final String objectIdFuelReceiptDetail, final String objectTypeFuelReceiptDetail) {
        Log.d(TAG, "runRefreshTask() Start.");
        final WeakReference<ReceiptDetailFragment> activityWeakReference = new WeakReference<>(fragment);

//        SyncTask refreshTask = new SyncTask(activity, new SyncTask.IRefreshTaskMethods() {
        SyncTaskForFragment refreshTask = new SyncTaskForFragment(activity, new SyncTask.IRefreshTaskMethods() {
            @Override
            public void loadScreen() {
                Log.d(TAG, "runRefreshTask.loadScreen() start.");
                ReceiptDetailFragment fragment = activityWeakReference.get();
                // Todo: maybe need notify if adapter already attached, optimization.
//                if (fragment != null && !fragment.isDetached()) {
                    Log.d(TAG, "fragment: "+fragment);

                    fragment.loadContentView();
//                }
                Log.d(TAG, "runRefreshTask.loadScreen() end.");
            }

            @Override
            public void executeSyncItems() throws Exception {
                rules.loadFuelReceiptDtlItems(activity, idRmsRecords, objectIdFuelReceiptDetail, objectTypeFuelReceiptDetail);
            }
        }, "Refreshing FuelReceipt usage data...");

        Log.d(TAG, "runRefreshTask() starting task, serial in case other conflicting AsyncTasks are running in background.");
//        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        // We'll run this and the save fuel receipt task on serial executor so they can't conflict.  Could also use
        // a lock/semaphore to be more granular.
        ExecutorService executor = Executors.newSingleThreadExecutor(); // ******* Todo: experimental.
//        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.d(TAG, "runRefreshTask() starting task, ***** Experimental - using dedicated executor.");

        refreshTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }


    public void runRefreshTask(final Activity activity, TollReceiptDetailFragment fragment, final long idRmsRecords,
                               final String objectIdFuelReceiptDetail, final String objectTypeFuelReceiptDetail) {
        Log.d(TAG, "runRefreshTask() Start.");
        final WeakReference<TollReceiptDetailFragment> activityWeakReference = new WeakReference<>(fragment);

//        SyncTask refreshTask = new SyncTask(activity, new SyncTask.IRefreshTaskMethods() {
        SyncTaskForFragment refreshTask = new SyncTaskForFragment(activity, new SyncTask.IRefreshTaskMethods() {
            @Override
            public void loadScreen() {
                Log.d(TAG, "runRefreshTask.loadScreen() start.");
                TollReceiptDetailFragment fragment = activityWeakReference.get();
                // Todo: maybe need notify if adapter already attached, optimization.
//                if (fragment != null && !fragment.isDetached()) {
                Log.d(TAG, "fragment: "+fragment);

                fragment.loadContentView();
//                }
                Log.d(TAG, "runRefreshTask.loadScreen() end.");
            }

            @Override
            public void executeSyncItems() throws Exception {
                rules.loadFuelReceiptDtlItems(activity, idRmsRecords, objectIdFuelReceiptDetail, objectTypeFuelReceiptDetail);
            }
        }, "Refreshing FuelReceipt usage data...");

        Log.d(TAG, "runRefreshTask() starting task, serial in case other conflicting AsyncTasks are running in background.");
//        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        // We'll run this and the save fuel receipt task on serial executor so they can't conflict.  Could also use
        // a lock/semaphore to be more granular.
        ExecutorService executor = Executors.newSingleThreadExecutor(); // ******* Todo: experimental.
//        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.d(TAG, "runRefreshTask() starting task, ***** Experimental - using dedicated executor.");

        refreshTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }


    public static Object lockSyncUpdateRmsFuelReceiptsTask = new Object();
    public static boolean isUpdateRmsFuelReceiptsInProgress = false;

    /**
     * Runs background task to query un-upsynced FuelReceipt records from the local database and
     * send them to the Server, then mark the records as sent (if confirmed by returned response).
     */
    public void runSyncUpdateRmsFuelReceiptsTask(int maxBatchSize) {
        if (isUpdateRmsFuelReceiptsInProgress) return;

        synchronized (lockSyncUpdateRmsFuelReceiptsTask) {
            if (isUpdateRmsFuelReceiptsInProgress) return;

            isUpdateRmsFuelReceiptsInProgress = true;

            SyncUpdateRmsFuelReceiptsTask task = new SyncUpdateRmsFuelReceiptsTask(SyncUpdateModes.ONE_TIME, maxBatchSize);
            Log.d(TAG, "runSyncUpdateRmsFuelReceiptsTask() starting task, parallel as a design default in case other AsyncTasks are running in background.");
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public static enum SyncUpdateModes {ONE_TIME, WHILE_PENDING, PERPETUAL}

    public class SyncUpdateRmsFuelReceiptsTask extends AsyncTask<String, Integer, Integer> {
        private int syncIntervalSecs = 10;
        private SyncUpdateModes enumMode;
        private int batchSize = -1;

        public SyncUpdateRmsFuelReceiptsTask(SyncUpdateModes mode, int batchSize) {
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
            int batchSize = 20;
            int maxBatches = 4;
            try {
                do {

                    BusHelperFuelReceipts helper = new BusHelperFuelReceipts(RecordRulesHelper.getDb());
//                BusHelperFuelReceipts.instance().upsyncFuelReceiptGroup(batchSize, maxBatches);
                    helper.upsyncFuelReceiptGroup(batchSize, maxBatches);
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


        //        public boolean upsyncFuelReceipts() throws Exception {
//            List<Map<String, String>> list = rules.getPendingFuelReceipts(maxBatchSize);
//
//            if ((list == null || list.size() == 0) && enumMode != SyncUpdateModes.PERPETUAL)
//                return true;
//
//            // 2.  Send pending records to RMS Server
//            String returnJson = rules.sendPendingFuelReceipts(list);
//
//            // 3.  Mark successfully sent FuelReceipt records as sent based on returned response.
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
                isUpdateRmsFuelReceiptsInProgress = false;
            }
        }
    }

    // endregion Up Sync

    // region Support

    // endregion Support


}
