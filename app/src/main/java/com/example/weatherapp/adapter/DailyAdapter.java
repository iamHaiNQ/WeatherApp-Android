package com.example.weatherapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.entities.Daily;

import com.example.weatherapp.UpdateUI;

import java.util.ArrayList;

public class DailyAdapter extends RecyclerView.Adapter<DailyAdapter.FutureViewHolder> {
    ArrayList<Daily> items;
    Context context;

    public DailyAdapter(ArrayList<Daily> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public FutureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_future, parent, false);
        return new FutureViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "DiscouragedApi"})
    @Override
    public void onBindViewHolder(@NonNull FutureViewHolder holder, int position) {
        Daily item = items.get(position);
        context = holder.itemView.getContext();
        holder.textDay.setText(item.getDay());
        holder.textStatus.setText(item.getStatus());
        holder.textHigh.setText(item.getHighTemp() + "°C");
        holder.textLow.setText(item.getLowTemp() + "°C");
        int iconResId = UpdateUI.getIconID(item.getPicPath());
        holder.imgPicNext.setImageResource(context.getResources().getIdentifier(String.valueOf(iconResId), "drawable", context.getPackageName()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FutureViewHolder extends RecyclerView.ViewHolder {
        private final TextView textDay;
        private final TextView textStatus;
        private final TextView textHigh;
        private final TextView textLow;
        private final ImageView imgPicNext;

        public FutureViewHolder(@NonNull View itemView) {
            super(itemView);
            textDay = itemView.findViewById(R.id.textDay);
            textStatus = itemView.findViewById(R.id.textStatus);
            textHigh = itemView.findViewById(R.id.textHigh);
            textLow = itemView.findViewById(R.id.textLow);
            imgPicNext = itemView.findViewById(R.id.imgPicNext);
        }
    }
}
