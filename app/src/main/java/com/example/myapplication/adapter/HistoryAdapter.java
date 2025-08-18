package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.History;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<History> mHistories;
    private OnItemClickListener listener;

    public void setData(List<History> histories) {
        mHistories = histories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        History history = mHistories.get(position);
        holder.tvTitle.setText(history.getTitle());
        // 格式化时间（如：2023-10-01 15:30）
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date(history.getVisitTime()));
        holder.tvTime.setText(time);

        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(history);
            }
        });

        // 长按删除（可选）
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onLongClick(history);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mHistories == null ? 0 : mHistories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_history_title);
            tvTime = itemView.findViewById(R.id.tv_history_time);
        }
    }

    // 点击事件接口
    public interface OnItemClickListener {
        void onClick(History history);
        void onLongClick(History history); // 用于删除
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}