/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.example.android.architecture.blueprints.todoapp.R;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Main UI for the settings screen.
 */
public class SettingsFragment extends Fragment implements SettingsContract.View {

    private TextView mStatisticsTV;
    private EditText mServerName;
    private EditText mUserName;
    private EditText mPassword;
    private Button mOk;
    private Button mCancel;
    private SharedPreferences sPref;

    private SettingsContract.Presenter mPresenter;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void setPresenter(@NonNull SettingsContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.settings_frag, container, false);
        mStatisticsTV = (TextView) root.findViewById(R.id.settings);
        mServerName = (EditText) root.findViewById(R.id.serverName);
        mUserName = (EditText) root.findViewById(R.id.userName);
        mPassword = (EditText) root.findViewById(R.id.password);

        mOk = (Button) root.findViewById(R.id.saveButton);
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editor ed = sPref.edit();
                ed.putString(getString(R.string.preferenceServerName), mServerName.getText().toString());
                ed.putString(getString(R.string.preferenceUserName), mUserName.getText().toString());
                ed.putString(getString(R.string.preferencePassword), mPassword.getText().toString());
                ed.apply();
                NavUtils.navigateUpFromSameTask(getActivity());
            }
        });

        mCancel = (Button) root.findViewById(R.id.cancelButton);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavUtils.navigateUpFromSameTask(getActivity());
            }
        });

        sPref = getActivity().getSharedPreferences(getString(R.string.preferenceFileKey), Context.MODE_PRIVATE);

        mServerName.setText(sPref.getString(getString(R.string.preferenceServerName), ""));
        mUserName.setText(sPref.getString(getString(R.string.preferenceUserName), ""));
        mPassword.setText(sPref.getString(getString(R.string.preferencePassword), ""));

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setProgressIndicator(boolean active) {
        if (active) {
            mStatisticsTV.setText(getString(R.string.loading));
        } else {
            mStatisticsTV.setText("");
        }
    }

    @Override
    public void showSettings(int numberOfIncompleteTasks, int numberOfCompletedTasks) {
        if (numberOfCompletedTasks == 0 && numberOfIncompleteTasks == 0) {
            mStatisticsTV.setText(getResources().getString(R.string.statistics_no_tasks));
        } else {
            String displayString = getResources().getString(R.string.statistics_active_tasks) + " "
                    + numberOfIncompleteTasks + "\n" + getResources().getString(
                    R.string.statistics_completed_tasks) + " " + numberOfCompletedTasks;
            mStatisticsTV.setText(displayString);
        }
    }

    @Override
    public void showLoadingSettingsError() {
        mStatisticsTV.setText(getResources().getString(R.string.statistics_error));
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }
}
