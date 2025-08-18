package com.example.myapplication.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.ProjectArticle;

import java.util.ArrayList;
import java.util.List;

public class ProjectArticleAdapter extends RecyclerView.Adapter<ProjectArticleAdapter.ViewHolder> {
    private final List<ProjectArticle> articles = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    // 更新数据
    public void submitList(List<ProjectArticle> newArticles) {
        articles.clear();
        if (newArticles != null) {
            articles.addAll(newArticles);
        }
        notifyDataSetChanged();
    }

    public void updateCollectState(int position, boolean isCollected) {
        Log.d("COLLECT", "更新收藏状态，位置: " + position + "，状态: " + isCollected);
        if (position >= 0 && position < articles.size()) {
            articles.get(position).setCollect(isCollected);
            notifyItemChanged(position);
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("project", "onBindViewHolder: ");
        holder.bind(articles.get(position));

        //  item 点击事件
        holder.itemView.setOnClickListener(v -> {
            Log.d("project", "onBindViewHolder: "+"文章被点击");
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(articles.get(position), position);
            }
        });


        // 收藏按钮点击事件
        holder.ivCollect.setOnClickListener(v -> {
            ProjectArticle article = articles.get(position);
            if (onItemClickListener != null) {
                onItemClickListener.onCollectClick(article, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvSuperChapter;
        private final TextView tvChapter;
        private final TextView tvTitle;
        private final TextView tvDesc;
        private final ImageView ivEnvelope;
        private final TextView tvAuthor;
        private final TextView tvDate;
        private final ImageView ivCollect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 初始化视图
            tvSuperChapter = itemView.findViewById(R.id.tv_super_chapter);
            tvChapter = itemView.findViewById(R.id.tv_chapter);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            ivEnvelope = itemView.findViewById(R.id.iv_envelope);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvDate = itemView.findViewById(R.id.tv_date);
            ivCollect = itemView.findViewById(R.id.iv_collect);
        }

        public void bind(ProjectArticle article) {
            // 绑定数据
            tvSuperChapter.setText(article.getSuperChapterName());
            tvChapter.setText(article.getChapterName());
            tvTitle.setText(article.getTitle());
            tvDesc.setText(article.getDesc());
            tvAuthor.setText(article.getAuthor());
            tvDate.setText(article.getNiceDate());

            // 加载封面图（使用 Glide）
            Glide.with(ivEnvelope.getContext())
                    .load(article.getEnvelopePic())
                    .placeholder(R.drawable.ic_placeholder) // 占位图
                    .error(R.drawable.ic_error) // 错误图
                    .into(ivEnvelope);

            // 设置收藏图标
            ivCollect.setImageResource(article.isCollect()
                    ? R.drawable.ic_collect_filled
                    : R.drawable.ic_collect_outline);
        }
    }

    // 点击事件接口
    public interface OnItemClickListener {
        void onItemClick(ProjectArticle article, int position);
        void onCollectClick(ProjectArticle article, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}