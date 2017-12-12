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

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsContract;
import com.example.android.architecture.blueprints.todoapp.statistics.StatisticsFragment;
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link StatisticsFragment}), retrieves the data and updates
 * the UI as required.
 */
public class SettingsPresenter implements SettingsContract.Presenter {

    private final TasksRepository mTasksRepository;

    private final SettingsContract.View mSettingsView;

    public SettingsPresenter(@NonNull TasksRepository tasksRepository,
                             @NonNull SettingsContract.View settingsView) {
        mTasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null");
        mSettingsView = checkNotNull(settingsView, "SettingsView cannot be null!");

        mSettingsView.setPresenter(this);
    }

    @Override
    public void start() {
        loadSettings();
    }

    private void loadSettings() {
        mSettingsView.setProgressIndicator(true);

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        mTasksRepository.getTasks(new TasksDataSource.LoadTasksCallback() {
            @Override
            public void onTasksLoaded(List<Task> tasks) {
                int activeTasks = 0;
                int completedTasks = 0;

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement(); // Set app as idle.
                }

                // We calculate number of active and completed tasks
                for (Task task : tasks) {
                    if (task.isCompleted()) {
                        completedTasks += 1;
                    } else {
                        activeTasks += 1;
                    }
                }
                // The view may not be able to handle UI updates anymore
                if (!mSettingsView.isActive()) {
                    return;
                }
                mSettingsView.setProgressIndicator(false);

                mSettingsView.showSettings(activeTasks, completedTasks);
            }

            @Override
            public void onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                if (!mSettingsView.isActive()) {
                    return;
                }
                mSettingsView.showLoadingSettingsError();
            }
        });
    }
}
