package com.example.btshare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

public class ReceiveTasksFragment extends Fragment {
    private ReceiveTaskViewModel viewModel;
    private RecyclerView rv;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ReceiveTaskViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_receive_tasks, container, false);

        rv = view.findViewById(R.id.receiveTasksRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(ReceiveTasksFragment.this.requireContext()));
        rv.setAdapter(new TaskAdapter(Collections.emptyList()));
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        viewModel.getSortedTasks().observe(ReceiveTasksFragment.this.getViewLifecycleOwner(),
                this::updateUI);
    }
    private void updateUI(List<BTTask> tasks) {
        rv.setAdapter(new TaskAdapter(tasks));
    }
    private static class ReceiveTaskHolder extends RecyclerView.ViewHolder {
        private BTTask task;
        TextView tv;
        public ReceiveTaskHolder(View v) {
            super(v);
        }
    }
    private class TaskAdapter extends RecyclerView.Adapter<ReceiveTaskHolder> {
        private List<BTTask> taskList;
        public TaskAdapter(List<BTTask> tl) {
            this.taskList = tl;
        }


        @NonNull
        @Override
        public ReceiveTaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = getLayoutInflater().inflate(R.layout.list_item_task, parent, false);
            return new ReceiveTaskHolder(view);
        }

        @Override
        public int getItemCount() {
            return taskList.size();
        }

        @Override
        public void onBindViewHolder(ReceiveTaskHolder holder, int pos) {
            holder.task = taskList.get(pos);
            holder.tv = holder.itemView.findViewById(R.id.taskTextView);
            String text = holder.task.getHfName() + "\n" + holder.task.getBtAddr()
                    + "\n" + holder.task.getFileName();
            int size = holder.task.getSize(); String sizestr = "";
            double fs = (double) size;
            DecimalFormat df = new DecimalFormat("#.##");

            if (size < 1000 ) sizestr = size + " bytes";
            else if ( size < 1000000) sizestr = df.format(fs/1000) + " KB";
            else sizestr = df.format(fs/1000000) + " MB";
            text +="\n" + sizestr;
            text += "\n" + holder.task.getTime();
            holder.tv.setText( text);

        }
    }






}

