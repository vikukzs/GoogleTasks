package com.example.quickstart;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.services.tasks.model.Task;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zsuzska on 2017. 10. 15..
 */

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.MyViewHolder> {

    private List<Task> taskList;

    public TasksAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @Override
    public TasksAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_task, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TasksAdapter.MyViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.name.setText(task.getTitle());
        holder.description.setText(task.getNotes());
//        holder.date.setText(task.getDue().toString());
        holder.date.setText(task.getStatus());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name) TextView name;
        @BindView(R.id.description) TextView description;
        @BindView(R.id.date) TextView date;
        @BindView(R.id.state) TextView state;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

