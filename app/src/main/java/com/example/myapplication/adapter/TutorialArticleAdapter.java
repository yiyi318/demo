package com.example.myapplication.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.TutorialArticle;

import java.util.ArrayList;
import java.util.List;

public class TutorialArticleAdapter extends RecyclerView.Adapter<TutorialArticleAdapter.ViewHolder> {
    private List<TutorialArticle> tutorials;
    private Context context;

    // 构造函数
    public TutorialArticleAdapter() {
        this.tutorials = new ArrayList<>();
    }

    public TutorialArticleAdapter(List<TutorialArticle> tutorials) {
        this.tutorials = tutorials != null ? tutorials : new ArrayList<>();
    }

    // 创建ViewHolder（加载布局）
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_tutorial, parent, false);
        return new ViewHolder(itemView);
    }

    // 绑定数据到ViewHolder
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d("tutorialadapter", "onBindViewHolder: ");
        TutorialArticle tutorial = tutorials.get(position);
        holder.bind(tutorial);

        // 设置收藏图标点击事件

        // 设置整个item的点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(tutorial);
            }
        });
    }


    // 返回数据列表大小
    @Override
    public int getItemCount() {
        return tutorials != null ? tutorials.size() : 0;
    }

    // 更新数据的方法
    public void setArticles(List<TutorialArticle> tutorials) {
        Log.d("TUTORIAL", "setTutorials: " + (tutorials != null ? tutorials.size() : 0));
        this.tutorials = tutorials != null ? tutorials : new ArrayList<>();
        notifyDataSetChanged();
    }

    // 添加数据到列表（用于分页加载）
    public void addTutorials(List<TutorialArticle> newTutorials) {
        if (newTutorials == null || newTutorials.isEmpty()) return;

        int startPosition = tutorials.size();
        tutorials.addAll(newTutorials);
        notifyItemRangeInserted(startPosition, newTutorials.size());
    }

    // ViewHolder内部类（持有视图引用）
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvAuthor;
        private final TextView tvDesc;
        private final ImageView ivCover;


        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            ivCover = itemView.findViewById(R.id.iv_cover);


            // 收藏图标点击事件


            // 整个item的点击事件
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(tutorials.get(pos));
                }
            });
        }

        // 绑定数据到视图
        public void bind(TutorialArticle tutorial) {
            if (tutorial == null) return;

            // 设置标题（处理空值）
            tvTitle.setText(tutorial.getName() != null ? tutorial.getName() : "未知标题");

            // 设置作者（处理空值）
            tvAuthor.setText(tutorial.getAuthor() != null ? tutorial.getAuthor() : "佚名");

            // 设置描述（处理空值）
            tvDesc.setText(tutorial.getDesc() != null ? tutorial.getDesc() : "");

            // 设置封面（这里仅设置占位图，实际项目中应使用图片加载库）
            Glide.with(ivCover.getContext())
                    .load(tutorial.getCover())
                    .placeholder(R.drawable.ic_placeholder) // 占位图
                    .error(R.drawable.ic_error) // 错误图
                    .into(ivCover);

        }

        // 点击事件回调接口

    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(TutorialArticleAdapter.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(TutorialArticle article);

    }
}