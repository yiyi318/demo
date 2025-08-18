package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.QueryArticle;

import java.util.ArrayList;
import java.util.List;

public class QueryArticleAdapter extends RecyclerView.Adapter<QueryArticleAdapter.ViewHolder> {
    private List<QueryArticle> articles = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public QueryArticleAdapter() {}

    public QueryArticleAdapter(List<QueryArticle> articles) {
        if (articles != null) {
            this.articles = new ArrayList<>(articles);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_query_article, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QueryArticle article = articles.get(position);
        holder.bind(article);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void setArticles(List<QueryArticle> articles) {
        this.articles = new ArrayList<>(articles);
        notifyDataSetChanged();
    }

    public void addArticles(List<QueryArticle> newArticles) {
        int startPosition = articles.size();
        articles.addAll(newArticles);
        notifyItemRangeInserted(startPosition, newArticles.size());
    }

    public void updateCollectState(int position, boolean isCollected) {
        if (position >= 0 && position < articles.size()) {
            articles.get(position).setCollect(isCollected);
            notifyItemChanged(position);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvAuthor, tvDate, tvDesc, tvSuperChapterName, tvChapterName;
        private final ImageView ivCollect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvSuperChapterName = itemView.findViewById(R.id.tv_superChapter);
            tvChapterName = itemView.findViewById(R.id.tv_chapterName);
            ivCollect = itemView.findViewById(R.id.iv_collect);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(articles.get(pos),pos);
                }
            });

            ivCollect.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    QueryArticle article = articles.get(pos);
                    boolean newState = !article.isCollect();
                    ivCollect.setImageResource(newState ?
                            R.drawable.ic_collect_filled : R.drawable.ic_collect_outline);
                    onItemClickListener.onCollectClick(article, pos);
                }
            });
        }

        public void bind(QueryArticle article) {
            tvTitle.setText(article.getTitle());
            tvAuthor.setText(article.getAuthor());
            tvDate.setText(article.getNiceDate());
            tvDesc.setText(article.getDesc());
            tvSuperChapterName.setText(article.getSuperChapterName());
            tvChapterName.setText(article.getChapterName());
            ivCollect.setImageResource(article.isCollect() ?
                    R.drawable.ic_collect_filled : R.drawable.ic_collect_outline);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(QueryArticle article,int position);
        void onCollectClick(QueryArticle article, int position);
    }
}