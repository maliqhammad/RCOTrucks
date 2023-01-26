package com.rco.rcotrucks.activities.fuelreceipts.utils;


import android.os.AsyncTask;
import android.util.Log;

import com.rco.rcotrucks.activities.fuelreceipts.fragments.FuelReceiptListFragment;
import com.rco.rcotrucks.activities.fuelreceipts.fragments.ReceiptFragment;
import com.rco.rcotrucks.businesslogic.SyncTask;
import com.rco.rcotrucks.utils.BuildUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UiHelperFuelReceiptList {
    public static final String TAG = UiHelperFuelReceiptList.class.getSimpleName();

    private boolean isDebugMode = BuildUtils.IS_DEBUG;
    private BusHelperFuelReceipts rules = BusHelperFuelReceipts.instance();
    private static UiHelperFuelReceiptList instance;

    public static synchronized UiHelperFuelReceiptList instance() {
        if (instance == null) {
            instance = new UiHelperFuelReceiptList();
        }

        return instance;
    }

    //region Refresh

    /**
     * Runs background task to refetch the ListView items and redisplay them.  Must be called from UI thread
     * if refreshTask uses serial ".execute()" method.
     *
     * @param fuelReceiptListFragment
     */
    public void runRefreshTask(final FuelReceiptListFragment fuelReceiptListFragment, String startDate, String endDate) {
        Log.d(TAG, "runRefreshTask: runRefreshTask() Start.");
        final WeakReference<FuelReceiptListFragment> activityWeakReference = new WeakReference<>(fuelReceiptListFragment);

        SyncTask refreshTask = new SyncTask(fuelReceiptListFragment.getActivity(), new SyncTask.IRefreshTaskMethods() {
            @Override
            public void loadScreen() {
                Log.d(TAG, "runRefreshTask: loadScreen() Start.");
                FuelReceiptListFragment fragment = activityWeakReference.get();
                // Todo: maybe need notify if adapter already attached, optimization.
                if (fragment != null && !fragment.getActivity().isFinishing()) {
                    Log.d(TAG, "runRefreshTask: Case: fragment != null && !fragment.getActivity().isFinishing() .");
                    fragment.loadContentView();
                }
                Log.d(TAG, "runRefreshTask: loadScreen() End.");
            }

            @Override
            public void executeSyncItems() throws Exception {
//                BusinessRules.instance().syncFuelReceiptItems();
                Log.d(TAG, "runRefreshTask: executeSyncItems() Start.");
                rules.loadFuelReceiptItems(startDate,endDate);
                Log.d(TAG, "runRefreshTask: executeSyncItems() End.");
            }
        }, "Refreshing FuelReceipt usage data...");
//                    refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        Log.d(TAG, "runRefreshTask() starting task, serial because we don't want two running simultaneously.");
//        refreshTask.execute();
        Log.d(TAG, "runRefreshTask: runRefreshTask() starting task, parallel as a design default in case other AsyncTasks are running in background.");

        boolean IS_DEBUGGING_EXECUTOR = true;
        Executor executor = null;

        if (IS_DEBUGGING_EXECUTOR) executor = Executors.newSingleThreadExecutor(); // ******* Todo: experimental to troubleshoot problem with tasks not starting.
        else executor = AsyncTask.THREAD_POOL_EXECUTOR;

//        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.d(TAG, "runRefreshTask: runRefreshTask() starting task, ***** Experimental - using dedicated executor.");
        refreshTask.executeOnExecutor(executor);
        Log.d(TAG, "runRefreshTask: runRefreshTask() End.");
    }

    // endregion Refresh

    public void runRefreshTask(final ReceiptFragment receiptFragment, String startDate, String endDate) {
        Log.d(TAG, "runRefreshTask() Start.");
        final WeakReference<ReceiptFragment> activityWeakReference = new WeakReference<>(receiptFragment);

        SyncTask refreshTask = new SyncTask(receiptFragment.getActivity(), new SyncTask.IRefreshTaskMethods() {
            @Override
            public void loadScreen() {
                Log.d(TAG, "loadScreen() Start.");
                ReceiptFragment fragment = activityWeakReference.get();
                // Todo: maybe need notify if adapter already attached, optimization.
                if (fragment != null && !fragment.getActivity().isFinishing()) {
                    Log.d(TAG, "Case: fragment != null && !fragment.getActivity().isFinishing() .");
                    fragment.getFuelReceiptsList();
                }
                Log.d(TAG, "loadScreen() End.");
            }

            @Override
            public void executeSyncItems() throws Exception {
//                BusinessRules.instance().syncFuelReceiptItems();
                Log.d(TAG, "executeSyncItems() Start.");
                rules.loadFuelReceiptItems(startDate,endDate);
                Log.d(TAG, "executeSyncItems() End.");
            }
        }, "Refreshing FuelReceipt usage data...");
//                    refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        Log.d(TAG, "runRefreshTask() starting task, serial because we don't want two running simultaneously.");
//        refreshTask.execute();
        Log.d(TAG, "runRefreshTask() starting task, parallel as a design default in case other AsyncTasks are running in background.");

        boolean IS_DEBUGGING_EXECUTOR = true;
        Executor executor = null;

        if (IS_DEBUGGING_EXECUTOR) executor = Executors.newSingleThreadExecutor(); // ******* Todo: experimental to troubleshoot problem with tasks not starting.
        else executor = AsyncTask.THREAD_POOL_EXECUTOR;

//        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.d(TAG, "runRefreshTask() starting task, ***** Experimental - using dedicated executor.");
        refreshTask.executeOnExecutor(executor);
        Log.d(TAG, "runRefreshTask() End.");
    }

    // endregion Refresh

}
