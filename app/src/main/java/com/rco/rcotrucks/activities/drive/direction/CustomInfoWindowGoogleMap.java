package com.rco.rcotrucks.activities.drive.direction;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.ConnectionMonitor;
import com.rco.rcotrucks.utils.ImageUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class CustomInfoWindowGoogleMap implements GoogleMap.InfoWindowAdapter {
    private static final String TAG = CustomInfoWindowGoogleMap.class.getSimpleName();
    private Context context;
    View mView;

    public CustomInfoWindowGoogleMap(Context ctx) {
        Log.d(TAG, "CustomInfoWindowGoogleMap: ");
        context = ctx;
    }


    //    Nov 14, 2022  -   "calloutMarker" is responsible for showing sign boards(inside blue bubble)
//    We are only showing it when we are online else
//    (offline) we are hiding it because when we apply tiles it zoom outs so everything looks messy so
//    we are not showing blue bubble sign board when offline
//    Note: "calloutMarker" is in DriveFragmentBase class
//    (hide it when apply applyOfflineCustomTilesOverlay)
//    (show it on onNetworkAvailable)
    @Override
    public View getInfoWindow(Marker marker) {
        Log.d(TAG, "onNetwork: getInfoWindow: ");
        if (marker.getTag() instanceof RouteSignInfoWindow) {
            Log.d(TAG, "onNetwork: getInfoWindow: tag: instruction: " + ((RouteSignInfoWindow) marker.getTag()).getInstruction());
            RouteSignInfoWindow routeSignInfoWindow = (RouteSignInfoWindow) marker.getTag();
//            July 15, 2022 -   App crashed when routeSignInfoWindow is null
            if (routeSignInfoWindow == null) {
                return null;
            }
            View view = ((Activity) context).getLayoutInflater()
                    .inflate(R.layout.layout_sign_route_left, null);


//            Aug 02, 2022    -   If we are not getting the manoeuvre info from google api then don't show the blue baloon
            if (routeSignInfoWindow.getManoeuvreSign() == null || routeSignInfoWindow.getManoeuvreSign().isEmpty()) {
                return null;
            }
            if (routeSignInfoWindow.getManoeuvreSign().contains("right")) {
                view = ((Activity) context).getLayoutInflater()
                        .inflate(R.layout.layout_sign_route_right, null);
            }
            mView = view;

            TextView instruction = view.findViewById(R.id.instruction);
            ImageView img = view.findViewById(R.id.pic);


//            if (segment.getIsRoundAbout().equalsIgnoreCase("true")) {
//                manoeuvreImg = ImageUtils.FetchDirectionImageForRoundAbout((int) segment.getTurnAngle());
//            } else {
//                manoeuvreImg = ImageUtils.FetchDirectionImage(segment.getManeuver());
//            }
            String manoeuvreImg = ImageUtils.FetchDirectionImage(routeSignInfoWindow.getManoeuvreSign());
            int manoeuvreResId = context.getResources().getIdentifier(manoeuvreImg, "drawable", context.getPackageName());
            img.setImageResource(manoeuvreResId);
            Log.e("rotaion", "" + routeSignInfoWindow.getRotation());
            // img.setRotation(45 + routeSignInfoWindow.getRotation());
//            instruction.setText(refineInstruction(routeSignInfoWindow.getInstruction()));
            instruction.setText(refineInstruction(routeSignInfoWindow.getInstruction()));

            Log.d(TAG, "getInfoWindow: hide: ");

            return view;
        } else {

            return null;
        }
    }

    private String refineInstruction(String instruction) {
        Log.d(TAG, "refineInstruction: ");
        if (!instruction.contains("onto"))
            return instruction;

        /* 2022.07.26  we should not convert it to lowercase
        String formattedInstruction = instruction.toLowerCase().substring(instruction.indexOf("onto"));
         */
        String formattedInstruction = instruction.substring(instruction.indexOf("onto"));

        formattedInstruction = formattedInstruction.replace("onto ", "");
        if (formattedInstruction.contains("destination"))
            formattedInstruction = formattedInstruction.substring(0, formattedInstruction.indexOf("destination"));

        return formattedInstruction;
    }

    @Override
    public View getInfoContents(Marker marker) {
        Log.d(TAG, "getInfoContents: ");
        if (marker.getTag() instanceof DestinationDetailInfoWindow) {
            View view = ((Activity) context).getLayoutInflater()
                    .inflate(R.layout.layout_destination_detail, null);

            TextView destination = view.findViewById(R.id.destination);
            ImageView img = view.findViewById(R.id.pic);

            destination.setText(marker.getTitle());

            DestinationDetailInfoWindow destinationDetailInfoWindow = (DestinationDetailInfoWindow) marker.getTag();
            if (destinationDetailInfoWindow == null || destinationDetailInfoWindow.getImage() == null || destinationDetailInfoWindow.getImage().isEmpty()) {
                img.setVisibility(View.GONE);
            } else {
                Picasso.with(context).load(destinationDetailInfoWindow.getImage()).placeholder(R.drawable.progress_drawable)
                        .into(img, new MarkerCallback(marker));
            }

            return view;
        } else {
            return null;
        }
    }

    static class MarkerCallback implements Callback {
        Marker marker = null;

        MarkerCallback(Marker marker) {
            Log.d(TAG, "MarkerCallback: ");
            this.marker = marker;
        }

        @Override
        public void onError() {
            Log.d(TAG, "onError: ");
        }

        @Override
        public void onSuccess() {
            Log.d(TAG, "onSuccess: ");
            if (marker == null) {
                return;
            }

            if (!marker.isInfoWindowShown()) {
                return;
            }

            marker.hideInfoWindow(); // Calling only showInfoWindow() throws an error
            marker.showInfoWindow();
        }
    }

}
