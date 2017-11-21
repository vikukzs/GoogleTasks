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
 * Created by vikuk.zsuzsanna on 2017. 11. 21..
 */

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.MyViewHolder> {

    private List<String> listList;

    public DrawerAdapter(List<String> listList) {
        this.listList = listList;
    }

    @Override
    public DrawerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_drawer, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DrawerAdapter.MyViewHolder holder, int position) {
        String listTitle = listList.get(position);
        holder.drawerListItemText.setText(listTitle);
    }

    @Override
    public int getItemCount() {
        return listList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.drawer_listitem_text)
        TextView drawerListItemText;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
