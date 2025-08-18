package com.example.myapplication.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.CoinInfo;

import java.util.List;
public class CoinRankAdapter extends RecyclerView.Adapter<CoinRankAdapter.ViewHolder> {
    private List<CoinInfo> data;

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<CoinInfo> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("RANK", "onCreateViewHolder: coinrankadapter_oncreateviewholder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coin_rank,parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CoinInfo item = data.get(position);
        holder.tvRank.setText(item.getRank());
        holder.tvUsername.setText(item.getUsername());
        holder.tvCoinCount.setText(String.valueOf(item.getCoinCount()));
        holder.tvLevel.setText(String.valueOf(item.getLevel()));

        // 设置不同等级的颜色
        int level = item.getLevel(); // 假设是 int 类型
        int color;

        if (level >= 1000) {
            color = holder.itemView.getContext().getResources().getColor(R.color.gold, null);
        } else if (level >= 100) {
            color = holder.itemView.getContext().getResources().getColor(R.color.green, null);
        } else if (level >= 10) {
            color = holder.itemView.getContext().getResources().getColor(R.color.orange, null);
        } else {
            color = holder.itemView.getContext().getResources().getColor(R.color.blue, null);
        }

        holder.tvLevel.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0; // 修复：返回实际数据量
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvUsername, tvCoinCount,tvLevel;

        public ViewHolder(View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvUsername = itemView.findViewById(R.id.tv_Username);
            tvCoinCount = itemView.findViewById(R.id.tvCoinCount);
            tvLevel=itemView.findViewById(R.id.tvLevel);

        }
    }
}