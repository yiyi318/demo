package com.example.myapplication.adapter;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Chapter;
import com.example.myapplication.utils.DisplayUtils;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemCategoryAdapter extends RecyclerView.Adapter<SystemCategoryAdapter.ViewHolder> {
    private static final String TAG = "systemAdapter";
    private List<Chapter> primaryCategories = new ArrayList<>(); // 初始化空列表
//    private final OnCategoryClickListener listener;

    private OnCategoryClickListener listener;
    private final Map<Integer, List<Chapter>> secondaryCategoriesMap = new HashMap<>();



    public void setData(List<Chapter> primaryCategories) {
        Log.d(TAG, "setData() called | 一级数据量: " +
                (primaryCategories != null ? primaryCategories.size() : "null") );


        List<Chapter> safeData = primaryCategories != null ? primaryCategories : Collections.emptyList();
        Log.d(TAG, "接收数据量: " + safeData.size());

        if (safeData.equals(this.primaryCategories)) return; // 避免重复更新

        this.primaryCategories = safeData;
        Log.d(TAG, "准备调用 notifyDataSetChanged()");
        notifyDataSetChanged();
        Log.d(TAG, "notifyDataSetChanged() 已触发");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder() 创建新Item");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_system_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (primaryCategories == null || position >= primaryCategories.size()) {
            Log.e(TAG, "数据异常: position=" + position + " size=" +
                    (primaryCategories != null ? primaryCategories.size() : "null"));
            return;
        }

        // 2. 获取当前一级分类数据
        Chapter primaryCategory = primaryCategories.get(position);

        // 3. 绑定数据到视图
        holder.tvPrimaryTitle.setText(primaryCategory.getName());
        holder.tvPrimaryTitle.setTextColor(Color.parseColor("#FF03A9F4")); // 改为蓝色
        holder.tvPrimaryTitle.setTextSize(16);

        // 2. 清空并填充二级标签
        holder.flexboxTags.removeAllViews();
        for (Chapter child : primaryCategory.getChildren()) {
            TextView tagView = new TextView(holder.itemView.getContext());
            tagView.setText(child.getName());

            // 设置标签样式（灰色圆角背景）
            tagView.setBackgroundResource(R.drawable.bg_tag);
            tagView.setTextColor(Color.parseColor("#666666")); // 灰色文字
            tagView.setTextSize(14);

            int paddingHorizontal = DisplayUtils.dpToPx(holder.itemView.getContext(), 12);
            int paddingVertical = DisplayUtils.dpToPx(holder.itemView.getContext(), 6);
            tagView.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);


            // 设置Flexbox布局参数
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
//            params.setMargins(0, dpToPx(4), dpToPx(8), dpToPx(4));
            tagView.setLayoutParams(params);

            // 添加点击事件
            tagView.setOnClickListener(v -> {
                // 跳转到标签详情页
            });

            holder.flexboxTags.addView(tagView);
        }


    // 4. 调试日志
        Log.d(TAG, "绑定位置-" + position + " | 标题: " + primaryCategory.getName());

        // 5. 可选：添加点击效果
        holder.ivArrow.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(primaryCategory.getChildren(),primaryCategory.getName());
            }
        });

    }



    @Override
    public int getItemCount() {
        int count = primaryCategories == null ? 0 : primaryCategories.size();
        Log.d(TAG, "getItemCount() 返回: " + count);
        return count;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private static final String HOLDER_TAG = "ViewHolder";
        FlexboxLayout flexboxTags;
        TextView tvPrimaryTitle;
        ImageView ivArrow;
        LinearLayout layoutSecondaryContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(HOLDER_TAG, "初始化ViewHolder");

            tvPrimaryTitle = itemView.findViewById(R.id.tv_primary_title);
            ivArrow = itemView.findViewById(R.id.iv_arrow);
            flexboxTags = itemView.findViewById(R.id.flexbox_tags);

            if (tvPrimaryTitle == null) Log.e(HOLDER_TAG, "tvPrimaryTitle 未找到!");
            if (ivArrow == null) Log.e(HOLDER_TAG, "ivArrow 未找到!");
//            if (layoutSecondaryContainer == null) Log.e(HOLDER_TAG, "layoutSecondaryContainer 未找到!");
        }
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(List<Chapter> children, String name);
    }

    // 保留唯一的监听器引用


    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }
}