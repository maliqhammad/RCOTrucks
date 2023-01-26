package com.rco.rcotrucks.activities.dvir;


import android.os.AsyncTask;
import android.util.Log;

import com.rco.rcotrucks.activities.pretrip.PreTripListFragment;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.SyncTask;
import com.rco.rcotrucks.utils.BuildUtils;

import java.lang.ref.WeakReference;

public class UiHelperDvirList {
    public static final String TAG = "UiHelperDvirList";

    private boolean isDebugMode = BuildUtils.IS_DEBUG;
    private BusHelperDvir rules = BusHelperDvir.instance();
    private static UiHelperDvirList instance;

    public static synchronized UiHelperDvirList instance() {
        if (instance == null) {
            instance = new UiHelperDvirList();
        }

        return instance;
    }

    //region Refresh

    /**
     * Runs background task to refetch the ListView items and redisplay them.  Must be called from UI thread
     * if refreshTask uses serial ".execute()" method.
     *
     * @param fragment
     */
//    public void runRefreshTask(final DvirListFragment fragment) {
//        Log.d(TAG, "runRefreshTask() Start.");
//        final WeakReference<DvirListFragment> activityWeakReference = new WeakReference<>(fragment);
//
//        SyncTask refreshTask = new SyncTask(fragment.getActivity(), new SyncTask.IRefreshTaskMethods() {
//            @Override
//            public void loadScreen() {
//                DvirListFragment dvirListFragment = activityWeakReference.get();
//                // Todo: maybe need notify if adapter already attached, optimization.
//                if (dvirListFragment != null && !dvirListFragment.getActivity().isFinishing()) dvirListFragment.loadContentView();
//            }
//
//            @Override
//            public void executeSyncItems() throws Exception {
////                BusHelperDvir.instance().syncDvirItems();
//                Log.d(TAG, "executeSyncItems() Start.");
//                rules.loadDvirItems("","");
//                Log.d(TAG, "executeSyncItems() End.");
//            }
//        }, "Refreshing Dvir usage data...");
////                    refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
////        Log.d(TAG, "runRefreshTask() starting task, serial because we don't want two running simultaneously.");
////        refreshTask.execute();
//        Log.d(TAG, "runRefreshTask() starting task, parallel as a design default in case other AsyncTasks are running in background.");
//        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//    }

    public void runRefreshTask(final DvirListFragment fragment, String startDate, String endDate) {
        Log.d(TAG, "runRefreshTask() Start.");
        final WeakReference<DvirListFragment> activityWeakReference = new WeakReference<>(fragment);

        SyncTask refreshTask = new SyncTask(fragment.getActivity(), new SyncTask.IRefreshTaskMethods() {
            @Override
            public void loadScreen() {
                DvirListFragment dvirListFragment = activityWeakReference.get();
                // Todo: maybe need notify if adapter already attached, optimization.
                if (dvirListFragment != null && !dvirListFragment.getActivity().isFinishing())
                    dvirListFragment.loadContentView();
            }

            @Override
            public void executeSyncItems() throws Exception {
//                BusHelperDvir.instance().syncDvirItems();
                Log.d(TAG, "executeSyncItems() Start.");
                rules.loadDvirItems(startDate, endDate);
                Log.d(TAG, "executeSyncItems() End.");
            }
        }, "Refreshing Dvir usage data...");
//                    refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        Log.d(TAG, "runRefreshTask() starting task, serial because we don't want two running simultaneously.");
//        refreshTask.execute();
        Log.d(TAG, "runRefreshTask() starting task, parallel as a design default in case other AsyncTasks are running in background.");
        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void runRefreshTask(final PreTripListFragment fragment, String startDate, String endDate) {
        Log.d(TAG, "runRefreshTask() Start.");
        final WeakReference<PreTripListFragment> activityWeakReference = new WeakReference<>(fragment);

        SyncTask refreshTask = new SyncTask(fragment.getActivity(), new SyncTask.IRefreshTaskMethods() {
            @Override
            public void loadScreen() {
                PreTripListFragment dvirListFragment = activityWeakReference.get();
                // Todo: maybe need notify if adapter already attached, optimization.
//                if (dvirListFragment != null && !dvirListFragment.getActivity().isFinishing())
//                    dvirListFragment.loadContentView();
            }

            @Override
            public void executeSyncItems() throws Exception {
//                BusHelperDvir.instance().syncDvirItems();
                Log.d(TAG, "executeSyncItems() Start.");
                rules.loadDvirItems(startDate, endDate);
                Log.d(TAG, "executeSyncItems() End.");
            }
        }, "Refreshing Dvir usage data...");
//                    refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        Log.d(TAG, "runRefreshTask() starting task, serial because we don't want two running simultaneously.");
//        refreshTask.execute();
        Log.d(TAG, "runRefreshTask() starting task, parallel as a design default in case other AsyncTasks are running in background.");
        refreshTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    // endregion Refresh

}
