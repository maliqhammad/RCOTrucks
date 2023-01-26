package com.rco.rcotrucks.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.rco.rcotrucks.model.ClusterForMarkers;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MarkerClusterRenderer extends DefaultClusterRenderer<ClusterForMarkers> {   // 1

    private static final String TAG = MarkerClusterRenderer.class.getSimpleName();
    private static final int MARKER_DIMENSION = 48;  // 2
    private static final int MARKER_DIMENSION_WIDTH = 40;  // 2
    private static final int MARKER_DIMENSION_HEIGHT = 55;  // 2

    private final IconGenerator iconGenerator;
    private final ImageView markerImageView;
    int drawableId;
    MarkerOptions options;

    public MarkerClusterRenderer(Context context, GoogleMap map, ClusterManager<ClusterForMarkers> clusterManager) {
        super(context, map, clusterManager);
        Log.d(TAG, "MarkerClusterRenderer: ");
        iconGenerator = new IconGenerator(context);  // 3
        markerImageView = new ImageView(context);
        markerImageView.setLayoutParams(new ViewGroup.LayoutParams(MARKER_DIMENSION, MARKER_DIMENSION));
        iconGenerator.setContentView(markerImageView);  // 4
    }

    public MarkerClusterRenderer(Context context, GoogleMap map, ClusterManager<ClusterForMarkers> clusterManager, MarkerOptions options) {
        super(context, map, clusterManager);
        Log.d(TAG, "MarkerClusterRenderer: ");
        iconGenerator = new IconGenerator(context);  // 3
        markerImageView = new ImageView(context);
        markerImageView.setLayoutParams(new ViewGroup.LayoutParams(MARKER_DIMENSION_WIDTH, MARKER_DIMENSION_HEIGHT));
        iconGenerator.setContentView(markerImageView);  // 4
//        this.drawableId = drawableId;
        this.options = options;
    }

//    Methods responsible for Single Markers STARTS
    @Override
    protected void onBeforeClusterItemRendered(@NonNull ClusterForMarkers item, MarkerOptions markerOptions) {
        // Draw a single person - show their profile photo and set the info window to show their name
        Log.d(TAG, "onBeforeClusterItemRendered: ");
        markerOptions
                .icon(item.markerIcon)
                .title(item.getTitle());
    }

    @Override
    protected void onClusterItemRendered(@NonNull @NotNull ClusterForMarkers clusterItem, @NonNull @NotNull Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);
        Log.d(TAG, "onClusterItemRendered: ");
        marker.setIcon(clusterItem.markerIcon);
        marker.setTitle(clusterItem.getTitle());
    }

    @Override
    protected void onClusterItemUpdated(@NonNull ClusterForMarkers clusterForMarkers, Marker marker) {
        // Same implementation as onBeforeClusterItemRendered() (to update cached markers)
        Log.d(TAG, "onClusterItemUpdated: ");
        marker.setIcon(clusterForMarkers.markerIcon);
        marker.setTitle(clusterForMarkers.getTitle());
    }
//    Methods responsible for Single Markers ENDS

//    @Override
//    protected void onBeforeClusterRendered(@NonNull Cluster<ClusterForMarkers> cluster, MarkerOptions markerOptions) {
//        Log.d(TAG, "onBeforeClusterRendered: ");
//        // Draw multiple people.
//        // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
////        markerOptions.icon(getClusterIcon(cluster));
//    }

//    @Override
//    protected void onClusterUpdated(@NonNull Cluster<ClusterForMarkers> cluster, Marker marker) {
//        Log.d(TAG, "onClusterUpdated: ");
//        // Same implementation as onBeforeClusterRendered() (to update cached markers)
////        marker.setIcon(getClusterIcon(cluster));
//    }

    /**
     * Get a descriptor for multiple people (a cluster) to be used for a marker icon. Note: this
     * method runs on the UI thread. Don't spend too much time in here (like in this example).
     *
     * @param cluster cluster to draw a BitmapDescriptor for
     * @return a BitmapDescriptor representing a cluster
     */
    private BitmapDescriptor getClusterIcon(Cluster<ClusterForMarkers> cluster) {
        Log.d(TAG, "getClusterIcon: ");
//        List<Drawable> profilePhotos = new ArrayList<>(Math.min(4, cluster.getSize()));
//        int width = mDimension;
//        int height = mDimension;
//
//        for (ClusterForMarkers p : cluster.getItems()) {
//            // Draw 4 at most.
//            if (profilePhotos.size() == 4) break;
//            Drawable drawable = getResources().getDrawable(p.profilePhoto);
//            drawable.setBounds(0, 0, width, height);
//            profilePhotos.add(drawable);
//        }
//        MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
//        multiDrawable.setBounds(0, 0, width, height);
//
//        mClusterImageView.setImageDrawable(multiDrawable);
//        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
//        return BitmapDescriptorFactory.fromBitmap(icon);
        return null;
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        // Always render clusters.
        Log.d(TAG, "shouldRenderAsCluster: ");
        return cluster.getSize() > 1;
    }

}