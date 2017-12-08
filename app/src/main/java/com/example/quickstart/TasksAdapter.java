package com.example.quickstart;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zsuzska on 2017. 10. 15..
 */

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.MyViewHolder> {

    private List<String> info;

    public TasksAdapter(List<String> infoList) {
        this.info = infoList;
    }

    @Override
    public TasksAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_fragment, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TasksAdapter.MyViewHolder holder, int position) {
        String calendarTask = info.get(position);
        holder.name.setText(calendarTask.getName());
        holder.description.setText(calendarTask.getDescription());
        holder.date.setText(calendarTask.getDate());
        holder.date.setText(calendarTask.getState());
        holder.info.setText(calendarTask);
    }

    @Override
    public int getItemCount() {
        return info.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.description)
        TextView description;
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.state)
        TextView state;
        @BindView(R.id.info)
        TextView info;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
