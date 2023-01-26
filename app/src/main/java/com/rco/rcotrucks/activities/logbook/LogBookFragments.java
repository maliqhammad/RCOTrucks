package com.rco.rcotrucks.activities.logbook;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.MainMenuActivity;
import com.rco.rcotrucks.activities.logbook.activity.DutyAndEldEvents;
import com.rco.rcotrucks.activities.logbook.activity.EldEdit;
import com.rco.rcotrucks.activities.logbook.sign.SignActivity;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.EldEvent;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.UiUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import static com.rco.rcotrucks.adapters.Cadp.EXTRA_LASTACTIVITY;

public class LogBookFragments extends Fragment implements View.OnClickListener {

    private static final String TAG = LogBookFragments.class.getSimpleName();
    private BusinessRules businessRules;
    public static final int DISPLAYED_MONTH = 6;
    private CustomViewPager logBookViewPager;
    private List<LogBookFragment> fragments = new ArrayList<>();

    private String todayDate, sentVia = "", outputComment = "";
    TextView prevButton, nextButton, certifyButton, signButton;
    ProgressBar progress;

    private List<String> logBookDaysList;
    private int certifyCount = 0;

    ImageView moreBtn;
    ConstraintLayout optionsLayout, transferRODsLayout, outputFileCommandLayout;
    EditText commentET;
    TextView viaEmail, viaWebService, cancelRODsLayout,
            unidentifiedProfile, transferRODS, email, print, dutyAndELDEvents, certifiedELDs,
            eldEdit, eLDMonitorService, hideViolations, cancel, addComment, cancelComment;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragments_list_log_book, container, false);

        setIds(view);
        initialize();
        setupView(view);
        setupLogBookViewPager();
        setTodayDate();
        setCertifyButton();
        setupMenuEvents();
        setListeners();

        return view;
    }


    private void setupView(View view) {
        prevButton = (TextView) view.findViewById(R.id.prev_button);
        nextButton = (TextView) view.findViewById(R.id.next_button);
        certifyButton = (TextView) view.findViewById(R.id.certify_button);
        signButton = (TextView) view.findViewById(R.id.sign_button);
        logBookViewPager = (CustomViewPager) view.findViewById(R.id.log_book_viewPager);
        progress = (ProgressBar) view.findViewById(R.id.progress);

        prevButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        certifyButton.setOnClickListener(this);
        signButton.setOnClickListener(this);
    }

    private void setupLogBookViewPager() {
//        logBookDaysList = DateUtils.getDaysList(DISPLAYED_MONTH, DateUtils.FORMAT_DATE_YYYY_MMM_DD);
        logBookDaysList = DateUtils.getDaysList(DISPLAYED_MONTH, DateUtils.FORMAT_DATE_YYYY_MM_DD);

        Log.d(TAG, "setupLogBookViewPager: logBookDaysList: " + logBookDaysList.size());
        for (String logBookDay : logBookDaysList) {
            Log.d(TAG, "setupLogBookViewPager: logBookDay: " + logBookDay);
            fragments.add(LogBookFragment.newInstance(logBookDay));
        }

        logBookViewPager.setAdapter(new FragmentStatePagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        });

        Log.d(TAG, "setupLogBookViewPager: currentItem: " + (logBookDaysList.get(logBookDaysList.size() - 1)));
        logBookViewPager.setCurrentItem(logBookDaysList.size() - 1);
        logBookViewPager.setOffscreenPageLimit(1);

        //Hide Next Button for Current day
        nextButton.setVisibility(View.INVISIBLE);
        logBookViewPager.setOnPageChangeListener(new CustomViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    prevButton.setVisibility(View.INVISIBLE);
                } else {
                    prevButton.setVisibility(View.VISIBLE);
                }
                if (position < logBookViewPager.getAdapter().getCount() - 1) {
                    nextButton.setVisibility(View.VISIBLE);
                } else {
                    nextButton.setVisibility(View.INVISIBLE);
                }

                ((MainMenuActivity) getActivity()).setActionBarTitle(logBookDaysList.get(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        ((MainMenuActivity) getActivity()).setActionBarTitle(logBookDaysList.get(logBookDaysList.size() - 1));
    }

    void setTodayDate() {
        Log.d(TAG, "getCertifyNumber: setTodayDate: logBookDaysList: size: " + logBookDaysList.size());
        todayDate = logBookDaysList.get(logBookDaysList.size() - 1);
        Log.d(TAG, "getCertifyNumber: setTodayDate: todayDate: " + todayDate);
    }

    private void setCertifyButton() {

        certifyCount = businessRules.getCertifyNumber(todayDate);
        Log.d(TAG, "getCertifyNumber: setCertifyButton: certifyCount: " + certifyCount);

        if (certifyCount > 0) {
            certifyButton.setText(getResources().getString(R.string.recertify));
        } else {
            certifyButton.setText(getResources().getString(R.string.certify));
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.prev_button:
                Log.d(TAG, "onClick: logBookViewPager: " + logBookViewPager);
                Log.d(TAG, "onClick: logBookViewPager.getCurrentItem(): " + logBookViewPager.getCurrentItem());
                logBookViewPager.setCurrentItem(logBookViewPager.getCurrentItem() - 1, true);
                break;

            case R.id.next_button:
                logBookViewPager.setCurrentItem(logBookViewPager.getCurrentItem() + 1, true);
                break;

            case R.id.certify_button:
                String currentDay = logBookDaysList.get(logBookViewPager.getCurrentItem());

                UiUtils.showBooleanDialog2(getActivity(), getString(R.string.certify_title) + currentDay, android.R.drawable.ic_dialog_info, getString(R.string.not_ready), getString(R.string.agree),
                        getString(R.string.certify_msg),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "onClick: yes click listener: ");

                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "onClick: no click listener");
                                new HttpSyncCertification().execute();
//                                setupLogBookViewPager();
                            }
                        });
                break;

            case R.id.sign_button:

                if (certifyCount > 0) {
                    EldEvent eldEvent = businessRules.getLastCertifyEvent();

                    if (eldEvent != null && eldEvent.objectType != null && eldEvent.objectId != null) {
                        Intent intent = new Intent(getActivity(), SignActivity.class);
                        intent.putExtra(Cadp.EXTRA_OBJECT_ID, eldEvent.objectId);
                        intent.putExtra(Cadp.EXTRA_OBJECT_TYPE, eldEvent.objectType);
                        intent.putExtra(EXTRA_LASTACTIVITY, "LOG_BOOK_ACTIVITY");
                        startActivity(intent);
                    } else {

                        UiUtils.showToast(getActivity(), "Error sync certification");
                    }

                } else {
                    UiUtils.showOkDialog(getActivity(), getString(R.string.notification_title), android.R.drawable.ic_dialog_info,
                            getString(R.string.certify_first_msg), true, null);
                }
                break;
        }
    }

    public class HttpSyncCertification extends AsyncTask<String, Void, EldEvent> {

        EldEvent eldEvent = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            final int todayCertifyEventCount = businessRules.getCertifyNumber(todayDate) > 9 ? 9 : businessRules.getCertifyNumber(todayDate);

            eldEvent = businessRules.generateCertifyEvent(getActivity(), "Cert" + todayCertifyEventCount);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected EldEvent doInBackground(String... params) {

            try {
                String json = Rms.setTruckEldEvent(eldEvent);
                Log.d(TAG, "doInBackground: json: " + json);

                if (json == null || json.equalsIgnoreCase("[]"))
                    return null;
                JSONArray response = new JSONArray(json);

                int lObjectId = response.getJSONObject(0).getInt("LobjectId");
                String objectType = response.getJSONObject(0).getString("objectType");

                eldEvent.objectId = "" + lObjectId;
                eldEvent.objectType = "" + objectType;

                businessRules.updateEldEvent(eldEvent, "" + lObjectId, objectType);

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "doInBackground: exception: " + e.getMessage());
            }

            return eldEvent;
        }

        @Override
        protected void onPostExecute(EldEvent eldEvent) {
            super.onPostExecute(eldEvent);
            progress.setVisibility(View.GONE);
            setCertifyButton();
        }
    }


    private void setupMenuEvents() {
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                menuButtonPreviousImplementation(view);
                optionsLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        moreBtn.setVisibility(View.GONE);
    }

    void setIds(View view) {

        optionsLayout = view.findViewById(R.id.optionsLayout);
        unidentifiedProfile = view.findViewById(R.id.unidentifiedProfile);
        transferRODS = view.findViewById(R.id.transferRODS);
        email = view.findViewById(R.id.email);
        print = view.findViewById(R.id.print);
        dutyAndELDEvents = view.findViewById(R.id.dutyAndELDEvents);
        certifiedELDs = view.findViewById(R.id.certifiedELDs);
        eldEdit = view.findViewById(R.id.eldEdit);
        eLDMonitorService = view.findViewById(R.id.eLDMonitorService);
        hideViolations = view.findViewById(R.id.hideViolations);
        cancel = view.findViewById(R.id.cancel);

        transferRODsLayout = view.findViewById(R.id.transferRODsLayout);
        viaEmail = view.findViewById(R.id.viaEmail);
        viaWebService = view.findViewById(R.id.viaWebService);
        cancelRODsLayout = view.findViewById(R.id.cancelRODsLayout);

        outputFileCommandLayout = view.findViewById(R.id.outputFileCommandLayout);
        commentET = view.findViewById(R.id.commentEditText);
        addComment = view.findViewById(R.id.addComment);
        cancelComment = view.findViewById(R.id.cancelComment);

        moreBtn = getActivity().findViewById(R.id.btn_share);
    }

    void initialize() {
        businessRules = ((MainMenuActivity) getActivity()).rules;
        moreBtn.setVisibility(View.VISIBLE);
    }

    void setListeners() {

        optionsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsLayout.setVisibility(View.GONE);
            }
        });

        transferRODsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferRODsLayout.setVisibility(View.GONE);
            }
        });

        unidentifiedProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        transferRODS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                optionsLayout.setVisibility(View.GONE);
                transferRODsLayout.setVisibility(View.VISIBLE);
            }
        });

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        dutyAndELDEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getContext(), DutyAndEldEvents.class);
                startActivity(intent);
            }
        });

        certifiedELDs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Certifications.class);
                startActivity(intent);
            }
        });

        eldEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getContext(), EldEdit.class);
                startActivity(intent);
            }
        });

        eLDMonitorService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MonitorService.class);
                startActivity(intent);
            }
        });

        hideViolations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsLayout.setVisibility(View.GONE);
            }
        });

        viaEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCommandLayout("Email");
            }
        });

        viaWebService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCommandLayout("Web Service");
            }
        });

        cancelRODsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsLayout.setVisibility(View.GONE);
                transferRODsLayout.setVisibility(View.GONE);
            }
        });

        outputFileCommandLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outputFileCommandLayout.setVisibility(View.GONE);
                UiUtils.closeKeyboard(commentET);
            }
        });


        addComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    outputFileCommandLayout.setVisibility(View.GONE);
                    UiUtils.closeKeyboard(commentET);
                    businessRules.transferEldFile(
                            getActivity(),
                            sentVia,
                            businessRules.generateEldTransferFile(outputComment, getActivity()),
                            outputComment);
                }
            }
        });


        cancelComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outputFileCommandLayout.setVisibility(View.GONE);
                UiUtils.closeKeyboard(commentET);
            }
        });


    }

    boolean validate() {
        boolean validate = true;
        outputComment = commentET.getText().toString();

        if (outputComment.isEmpty()) {
            commentET.setError("Please enter comment");
            return false;
        }

        return validate;
    }

    void menuButtonPreviousImplementation(View view) {

//        PopupMenu popup = new PopupMenu(getActivity(), view);
//        MenuInflater inflater = popup.getMenuInflater();
//        inflater.inflate(R.menu.menu_logbook_options, popup.getMenu());
//        int position = logBookViewPager.getCurrentItem();
//        LogBookFragment logBookFragment = fragments.get(position);
//
//
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                switch (menuItem.getItemId()) {
//                    case R.id.option_info:
//                        if (logBookFragment != null) {
//                            logBookFragment.HideShowEldInfo();
//                        }
//
//                        break;
//                    case R.id.option_events:
//                        if (logBookFragment != null) {
//                            logBookFragment.HideShowEldEvent();
//                        }
//                        break;
//
//                }
//                return false;
//            }
//        });
//
//        if (logBookFragment != null && logBookFragment.isEldInfoVisible()) {
//            popup.getMenu().findItem(R.id.option_info).setTitle("Hide Info");
//
//        } else {
//            popup.getMenu().findItem(R.id.option_info).setTitle("Show Info");
//        }
//        if (logBookFragment != null && logBookFragment.isEldEventVisible()) {
//            popup.getMenu().findItem(R.id.option_events).setTitle("Hide Events");
//
//        } else {
//            popup.getMenu().findItem(R.id.option_events).setTitle("Show Events");
//        }
//        popup.show();
    }

    void showSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    void openCommandLayout(String openVia) {
        sentVia = openVia;
        transferRODsLayout.setVisibility(View.GONE);
        outputFileCommandLayout.setVisibility(View.VISIBLE);
        showSoftKeyboard();
        commentET.requestFocus();
    }
}
