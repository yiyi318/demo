package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ItemRowBinding;
import com.example.myapplication.model.SettingItem;

import java.util.List;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.SettingViewHolder> {

    private List<SettingItem> items;
    private OnSettingItemClickListener listener;

    public interface OnSettingItemClickListener {
        void onSettingItemClick(SettingItem item);
    }

    public SettingAdapter(List<SettingItem> items, OnSettingItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRowBinding binding = ItemRowBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SettingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingViewHolder holder, int position) {
        SettingItem item = items.get(position);
        holder.bind(item);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSettingItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SettingViewHolder extends RecyclerView.ViewHolder {
        private final ItemRowBinding binding;

        public SettingViewHolder(ItemRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SettingItem item) {
            binding.tvTitle.setText(item.getTitle());
            binding.tvDesc2.setText(item.getDescription());

            if (item.getIconResId() != 0) {
                binding.ivArrow.setImageResource(item.getIconResId());
            }

            // 可以根据需要隐藏不需要的视图
            binding.tvDesc.setVisibility(View.GONE); // 示例：隐藏中间的描述文本
        }
    }
}
