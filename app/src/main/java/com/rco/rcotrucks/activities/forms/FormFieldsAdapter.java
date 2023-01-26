package com.rco.rcotrucks.activities.forms;

import android.app.DatePickerDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.adapters.Cadp;

import org.zakariya.stickyheaders.SectioningAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FormFieldsAdapter extends SectioningAdapter {
    List<Forms> applicationTypesItem = new ArrayList<>();


    public FormFieldsAdapter(List<Forms> listSpe) {
        this.applicationTypesItem = listSpe;
    }

    @Override
    public int getNumberOfSections() {
        return applicationTypesItem.size();
    }

    @Override
    public int getNumberOfItemsInSection(int sectionIndex) {
        return applicationTypesItem.get(sectionIndex).formsList.size();
    }

    @Override
    public int getSectionItemUserType(int sectionIndex, int itemIndex) {
        switch (applicationTypesItem.get(sectionIndex).formsList.get(itemIndex).getFormat()) {
            case Cadp.HTML_ELEM_TYPE_SPAN:
            case Cadp.HTML_ELEM_TYPE_TEXT:
                return 1;
            case Cadp.HTML_ELEM_TYPE_CHECKBOX:
                return 2;
            case Cadp.HTML_FORMAT_AMPM_FROM_DATE:
            case Cadp.HTML_FORMAT_DATE:
            case Cadp.HTML_FORMAT_TIME_FROM_DATE:
                return 3;
            default:
                return 4;
        }
    }

    @Override
    public boolean doesSectionHaveHeader(int sectionIndex) {
        return true;
    }

    @Override
    public boolean doesSectionHaveFooter(int sectionIndex) {
        return false;
    }


    @Override
    public ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int itemUserType) {
        switch (itemUserType) {
            case 1://Text
                return new FieldViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_field, parent, false));
            case 2://CHECKBOX
                return new CheckboxViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_field_switcher, parent, false));
            case 3://DATE
                return new CalendarViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_field_calendar, parent, false));
            default:
                return new FieldViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_field, parent, false));
        }
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent, int headerUserType) {
        return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header_app_type, parent, false));
    }

    @Override
    public void onBindItemViewHolder(ItemViewHolder viewHolder, int sectionIndex, int itemIndex, int itemUserType) {
        super.onBindItemViewHolder(viewHolder, sectionIndex, itemIndex, itemUserType);

        FormField sp = applicationTypesItem.get(sectionIndex).formsList.get(itemIndex);

        if (itemUserType == 2) {
            CheckboxViewHolder ivhC = (CheckboxViewHolder) viewHolder;
            ivhC.bindData(sp);
        } else if (itemUserType == 3) {
            CalendarViewHolder ivhCl = (CalendarViewHolder) viewHolder;
            ivhCl.bindData(sp);
        } else {
            FieldViewHolder ivh = (FieldViewHolder) viewHolder;
            ivh.bindData(sp);
        }
    }

    @Override
    public void onBindHeaderViewHolder(SectioningAdapter.HeaderViewHolder viewHolder, int sectionIndex, int headerUserType) {
        super.onBindHeaderViewHolder(viewHolder, sectionIndex, headerUserType);
        Forms section = applicationTypesItem.get(sectionIndex);
        HeaderViewHolder hvh = (HeaderViewHolder) viewHolder;
        hvh.bindData(section.formsHeader);
    }

    class HeaderViewHolder extends SectioningAdapter.HeaderViewHolder {
        TextView title;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_header);
        }

        public void bindData(String header) {
            title.setText(header);
        }
    }

    class FieldViewHolder extends SectioningAdapter.ItemViewHolder {
        private EditText title;
        private ImageView clear;

        public FieldViewHolder(View itemView) {
            super(itemView);
            title = (EditText) itemView.findViewById(R.id.tv_title);
            clear = (ImageView) itemView.findViewById(R.id.btn_close);
        }

        public void bindData(final FormField formField) {
            setIsRecyclable(false);
            if (formField.getValue() != null && !formField.getValue().isEmpty()) {
                title.setText(formField.getValue());
            } else
                title.setHint(formField.getKey());
            title.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (!editable.toString().isEmpty()) {
                        clear.setVisibility(View.VISIBLE);
                        formField.setValue(editable.toString());
                    } else {
                        clear.setVisibility(View.GONE);
                        formField.setValue("");
                    }
                }
            });

            clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    formField.setValue("");
                    title.setText("");
                    clear.setVisibility(View.GONE);
                }
            });
        }
    }

    class CalendarViewHolder extends SectioningAdapter.ItemViewHolder {
        private ImageView cal;
        private TextView date;
        private TextView tittle;

        public CalendarViewHolder(View itemView) {

            super(itemView);
            cal = (ImageView) itemView.findViewById(R.id.iv_calendar);
            date = (TextView) itemView.findViewById(R.id.tv_date);
            tittle = (TextView) itemView.findViewById(R.id.tv_tittle);

        }

        public void bindData(final FormField formField) {
            if (formField.getKey() != null && !formField.getKey().isEmpty()) {
                tittle.setText(formField.getKey());
            }
            Calendar calendar = Calendar.getInstance();
            final int selectedYear = calendar.get(Calendar.YEAR);
            final int selectedMonth = calendar.get(Calendar.MONTH);
            final int selectedDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            cal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            String selctedDate = String.format("%02d-%02d-%02d", year, (monthOfYear + 1), dayOfMonth);

                            date.setText(selctedDate);
                            formField.setValue(selctedDate);
                        }
                    };
                    DatePickerDialog datePickerDialog = new DatePickerDialog(itemView.getContext(),
                            dateSetListener, selectedYear, selectedMonth, selectedDayOfMonth);

                    datePickerDialog.show();
                }
            });
        }
    }

    class CheckboxViewHolder extends SectioningAdapter.ItemViewHolder {
        private TextView tvField;
        private Switch switchField;

        public CheckboxViewHolder(View itemView) {
            super(itemView);
            tvField = (TextView) itemView.findViewById(R.id.tv_field);
            switchField = (Switch) itemView.findViewById(R.id.switch_field);
        }

        public void bindData(final FormField formField) {
            tvField.setText(formField.getKey());
            if (formField.getValue() != null && formField.getValue().equals("false")) {
                switchField.setChecked(false);
            } else {
                switchField.setChecked(true);
            }
            switchField.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        formField.setValue("true");
                    } else {
                        formField.setValue("false");
                    }
                }
            });
        }
    }
}
