/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;

import java.util.Collections;
import java.util.List;

import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.activities.ScheduleCreationActivity;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.util.ScheduleHelper;

/**
 * Created by joseangel.pineiro on 12/2/13.
 */
public class SelectMedicineListFragment extends Fragment {


    List<Medicine> mMedicines;
    ArrayAdapter adapter;
    ListView listview;
    long selectedId = -1;

    ScheduleCreationActivity mActivity;
    int pColor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_select_medicine_list, container, false);
        listview = (ListView) rootView.findViewById(R.id.medicines_list);
        pColor = DB.patients().getActive(getActivity()).color();
        Medicine med = ScheduleHelper.instance().getSelectedMed();
        if (med != null)
            selectedId = med.getId();

        rootView.findViewById(R.id.add_medicine_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), MedicinesActivity.class);
                i.putExtra("create", true);
                startActivity(i);
            }
        });

        mMedicines = DB.medicines().findAllForActivePatient(getContext());
        Collections.sort(mMedicines);
        adapter = new MedicinesListAdapter(getActivity(), R.layout.medicines_list_item, mMedicines);
        listview.setAdapter(adapter);
        return rootView;
    }

    private View createMedicineListItem(LayoutInflater inflater, final Medicine medicine) {

        final View item = inflater.inflate(R.layout.select_medicines_list_item, null);

        ((TextView) item.findViewById(R.id.medicines_list_item_name)).setText(medicine.name());

        ImageView icon = (ImageView) item.findViewById(R.id.imageButton);
        icon.setImageDrawable(iconFor(medicine.presentation()));

        View overlay = item.findViewById(R.id.medicines_list_item_container);
        overlay.setTag(medicine);

        if (selectedId == medicine.getId()) {
            item.findViewById(R.id.selection_indicator).setVisibility(View.VISIBLE);
            item.findViewById(R.id.selection_mask).setVisibility(View.VISIBLE);
            item.findViewById(R.id.imageView2).setVisibility(View.VISIBLE);
        } else {
            item.findViewById(R.id.selection_indicator).setVisibility(View.INVISIBLE);
            item.findViewById(R.id.selection_mask).setVisibility(View.INVISIBLE);
            item.findViewById(R.id.imageView2).setVisibility(View.INVISIBLE);
        }

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Medicine m = (Medicine) view.getTag();
                selectedId = m.getId();
                if (mActivity != null) {
                    mActivity.onMedicineSelected(m, true);
                }
                adapter.notifyDataSetChanged();
            }
        };
        overlay.setOnClickListener(clickListener);
        return item;
    }

    IconicsDrawable iconFor(Presentation p){
        return new IconicsDrawable(getContext())
                .icon(Presentation.iconFor(p))
                .colorRes(R.color.agenda_item_title)
                .paddingDp(5)
                .sizeDp(55);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ScheduleCreationActivity) {
            mActivity = (ScheduleCreationActivity) activity;
        }
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    public void setSelectedMed(Long id) {
        this.selectedId = id;
        Medicine m = Medicine.findById(id);
        if (m != null) {
            mMedicines.clear();
            mMedicines.addAll(DB.medicines().findAllForActivePatient(getContext()));
            Collections.sort(mMedicines);
            mActivity.onMedicineSelected(m, false);
            adapter.notifyDataSetChanged();
        }
    }

    private class MedicinesListAdapter extends ArrayAdapter<Medicine> {

        public MedicinesListAdapter(Context context, int layoutResourceId, List<Medicine> items) {
            super(context, layoutResourceId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            return createMedicineListItem(layoutInflater, mMedicines.get(position));
        }
    }


}