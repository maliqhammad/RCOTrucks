package com.rco.rcotrucks.activities.drive;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.orhanobut.hawk.Hawk;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.drive.adapter.IOnClickPlace;
import com.rco.rcotrucks.activities.drive.adapter.OnSetRoute;
import com.rco.rcotrucks.activities.drive.adapter.RecentSearchAdapter;
import com.rco.rcotrucks.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.rco.rcotrucks.utils.Constants.KEY_SEARCHED_LIST;

public class RecentSearchFragment extends Fragment {

    private RecyclerView recentSearchRv;

    private List<PlaceModel> filteredSearchList = new ArrayList<>();
    private RecentSearchAdapter mAdapter;

    private OnSetRoute iSetRoute;

    public void setISetRoute(OnSetRoute onSetRoute) {
        iSetRoute = onSetRoute;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recent_search, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        try {
            super.onViewCreated(view, savedInstanceState);
            recentSearchRv = view.findViewById(R.id.rv_recent_search);

            fillSearchList();

            mAdapter = new RecentSearchAdapter(filteredSearchList, new IOnClickPlace() {
                @Override
                public void onGOClicked(PlaceModel placeModel) {
                    try {
                        final String placeId = placeModel.getPlaceId();
                        if(iSetRoute != null){
                            iSetRoute.setARoute(placeId, placeModel.getAddress());
                        }

                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            });

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
            recentSearchRv.setLayoutManager(linearLayoutManager);
            recentSearchRv.setAdapter(mAdapter);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private void fillSearchList() {
        List<PlaceModel> allSearchedPlaces = Hawk.get(KEY_SEARCHED_LIST, new ArrayList<>());

        List<PlaceModel> todaySearches = new ArrayList<>();
        List<PlaceModel> lastWeekSearches = new ArrayList<>();
        List<PlaceModel> otherSearches = new ArrayList<>();

        Collections.reverse(allSearchedPlaces);
        for (PlaceModel placemodel : allSearchedPlaces) {
            if (placemodel.getCreated_time() != null && placemodel.getCreated_time().equals(DateUtils.getCreatedDate())) {
                todaySearches.add(placemodel);
            } else if (placemodel.getCreated_time() != null && DateUtils.isCreateDateLastWeek(placemodel.getCreated_time())) {
                lastWeekSearches.add(placemodel);
            } else {
                otherSearches.add(placemodel);
            }
        }

        if (todaySearches.size() > 0) {
            filteredSearchList.add(new PlaceModel("TODAY"));
            filteredSearchList.addAll(todaySearches);
        }

        if (lastWeekSearches.size() > 0) {
            filteredSearchList.add(new PlaceModel("LAST WEEK"));
            filteredSearchList.addAll(lastWeekSearches);
        }

        if (otherSearches.size() > 0) {
            filteredSearchList.add(new PlaceModel("PREVIOUS SEARCHES"));
            filteredSearchList.addAll(otherSearches);
        }
    }
}
