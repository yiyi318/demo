package com.example.myapplication.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.CoinArticle;

import java.util.ArrayList;
import java.util.List;

public class CoinArticleAdapter extends RecyclerView.Adapter<CoinArticleAdapter.ViewHolder> {
    private List<CoinArticle> coinArticles;
    private Context context;

    // 构造函数
    public CoinArticleAdapter() {
        this.coinArticles = new ArrayList<>();
    }

    public CoinArticleAdapter(List<CoinArticle> coinArticles) {
        this.coinArticles = coinArticles != null ? coinArticles : new ArrayList<>();
    }

    // 创建ViewHolder（加载布局）
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_coin_article, parent, false);
        return new ViewHolder(itemView);
    }

    // 绑定数据到视图
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CoinArticle coinArticle = coinArticles.get(position);
        holder.bind(coinArticle);

        // 条目点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null && position != RecyclerView.NO_POSITION) {
                onItemClickListener.onItemClick(coinArticle, position);
            }
        });
    }

    // 返回数据列表大小
    @Override
    public int getItemCount() {
        return coinArticles.size();
    }

    // 更新数据的方法
    public void setCoinArticles(List<CoinArticle> coinArticles) {
        this.coinArticles = coinArticles != null ? coinArticles : new ArrayList<>();
        notifyDataSetChanged();
    }

    // 添加数据（分页加载用）
    public void addCoinArticles(List<CoinArticle> newArticles) {
        if (newArticles == null || newArticles.isEmpty()) return;
        int startPosition = coinArticles.size();
        coinArticles.addAll(newArticles);
        notifyItemRangeInserted(startPosition, newArticles.size());
    }

    // ViewHolder内部类
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCoinCount;  // 积分总数
        private final TextView tvDesc;       // 操作描述
        private final TextView tvReason;     // 操作原因
//        private final TextView tvadd;

        public ViewHolder(View itemView) {
            super(itemView);
            tvCoinCount = itemView.findViewById(R.id.tv_coin_count);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvReason = itemView.findViewById(R.id.tv_reason);
//            tvadd=itemView.findViewById(R.id.tv_add);
        }

        // 绑定数据
        public void bind(CoinArticle coinArticle) {
            if (coinArticle == null) return;
            Log.d("coin", "bind: "+coinArticle.getCoinCount());
            // 显示积分总数（带单位）
            tvCoinCount.setText(String.format("+%d", coinArticle.getCoinCount()));
            // 显示操作描述（如"2025-07-20 00:50:15 签到 , 积分：10 + 9"）
            tvDesc.setText(coinArticle.getDesc());
            // 显示操作原因（如"签到"、"分享文章"）
            tvReason.setText(coinArticle.getReason());
        }
    }

    // 点击事件回调接口
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(CoinArticle coinArticle, int position);
    }
}