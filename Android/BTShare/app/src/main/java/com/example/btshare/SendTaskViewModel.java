package com.example.btshare;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class SendTaskViewModel extends ViewModel {
    private LiveData<List<BTTask>> _tasks;
    private final MediatorLiveData<List<BTTask>> sortedTasks = new MediatorLiveData<>();

    public SendTaskViewModel() {
        _tasks = DatabaseWorker.getSendList();
        sortedTasks.addSource(_tasks, items -> {
            if (items != null) {
                items.sort((task1, task2) -> {
                    int taskid1 = Integer.parseInt(task1.getTaskID());
                    int taskid2 = Integer.parseInt(task2.getTaskID());
                    return -Integer.compare(taskid1,taskid2);
                });
             /*   Collections.sort(items, (task1, task2) -> {
                    int taskid1 = Integer.parseInt(task1.getTaskID());
                    int taskid2 = Integer.parseInt(task2.getTaskID());
                    return -Integer.compare(taskid1,taskid2);
                }); */
                sortedTasks.setValue(items);
            }
        });
    }

    public LiveData<List<BTTask>> getSortedTasks() {
        return sortedTasks ;
    }

}
