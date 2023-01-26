package com.rco.rcotrucks.activities.drive.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.route.Route;

import java.util.List;

public class DirectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = DirectionAdapter.class.getSimpleName();

    private List<Route> routes;

    private IDirection iDirection;
    int shortestRouteIndex = 0;

    public DirectionAdapter(List<Route> routes, int shortestRouteIndex, IDirection iDirection) {
        this.routes = routes;
        this.shortestRouteIndex = shortestRouteIndex;
        this.iDirection = iDirection;
    }

    @Override
    public DirectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_direction, parent, false);
        return new DirectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DirectionViewHolder detailViewHolder = (DirectionViewHolder) holder;
        detailViewHolder.setData(routes.get(position));
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    public class DirectionViewHolder extends RecyclerView.ViewHolder {
        private TextView hours, miles, go_btn;

        public DirectionViewHolder(View v) {
            super(v);
            hours = (TextView) v.findViewById(R.id.hours);
            miles = (TextView) v.findViewById(R.id.miles);
            go_btn = (TextView) v.findViewById(R.id.go_btn);
        }

        public void setData(Route route) {

            if (getPosition() == shortestRouteIndex) {
                hours.setPressed(true);
                miles.setPressed(true);
            } else {
                hours.setPressed(false);
                miles.setPressed(false);
            }

            if (route.getDurationText() != null) {
//                July 22, 2022 -   Added duration with traffic
//                hours.setText(route.getDurationText());
                Log.d(TAG, "setData: route.getDurationText(): "+route.getDurationText());
                hours.setText(route.getDurationWithTrafficText());
            }

            if (route.getDistanceText() != null) {
                miles.setText(route.getDistanceText());
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shortestRouteIndex = getPosition();
                    notifyDataSetChanged();
                    iDirection.onRoute(getPosition(), true);
                }
            });

            go_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shortestRouteIndex = getPosition();
                    iDirection.onRoute(getPosition(), false);
                }
            });
        }
    }


    public interface IDirection {
        void onRoute(int selectedPosition, boolean showAllRoutes);
    }
}
