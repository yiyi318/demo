package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.Banner;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.ViewHolder> {
    private List<Banner> banners;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.iv_banner);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner_pager, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Banner item = banners.get(position % banners.size());
        Glide.with(holder.imageView.getContext())
                .load(item.getImagePath())
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return banners == null ? 0 : Integer.MAX_VALUE;
    }

    // 添加数据更新方法
    // 修正：添加参数类型 List<BannerResponse.BannerData>
    public void setBanners(List<Banner> banners) {
        this.banners = banners;
        notifyDataSetChanged(); // 刷新列表
    }

}
