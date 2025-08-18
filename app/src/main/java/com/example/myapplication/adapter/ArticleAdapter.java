package com.example.myapplication.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Article;

import java.util.ArrayList;
import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;
    //数据结构
    private List<Article> articles = new ArrayList<>();;
    //Context 是 Android 的上下文对象，通常用来访问系统资源、启动 Activity、获取服务等。
    //这个变量一般用来保存当前组件（Activity、Fragment 或 Adapter 等）的上下文，方便调用系统功能。
    private Context context;
    //共享的shareViewModel

    private boolean showFooter = false; // 控制是否显示 Footer

    // 构造函数
    public ArticleAdapter(){

    }
    //构造函数
    public ArticleAdapter(List<Article> articles) {
        this.articles = articles != null ? articles : new ArrayList<>();

    }

    // 创建ViewHolder（加载布局）
    //为 RecyclerView 列表项创建并返回一个新的视图持有者（ViewHolder），并拿到上下文对象方便后续使用。
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        if (viewType == TYPE_FOOTER) {
            View footerView = LayoutInflater.from(context)
                    .inflate(R.layout.item_footer, parent, false);
            return new FooterViewHolder(footerView);
        } else {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_article, parent, false);
            return new ViewHolder(itemView);
        }
    }



        /**
     * 为RecyclerView的ViewHolder绑定数据和设置点击事件
     * @param holder ViewHolder实例，用于显示列表项
     * @param position 当前项在列表中的位置
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (getItemViewType(position) == TYPE_FOOTER && holder instanceof FooterViewHolder) {
            // 绑定 Footer 内容（如果需要）
            return;
        }
        Article article = articles.get(position);
        holder.bind(article);

        // 设置收藏图标点击事件
        holder.ivCollect.setOnClickListener(v -> {
            Log.d("COLLECT", "收藏按钮被点击，位置: " + position);

            // 触发收藏/取消收藏操作
            if (onItemClickListener != null) {
                onItemClickListener.onCollectClick(article, position);
            }
        });

        // 设置列表项点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null && position != RecyclerView.NO_POSITION) {
                onItemClickListener.onItemClick(article, position);
            }
        });

        // 设置列表项长按事件
        holder.itemView.setOnLongClickListener(v -> {
            Log.d("share", "onBindViewHolder: ");
            if (onItemClickListener != null ) {
                onItemClickListener.onLongClick(article);
            }
            return true;
        });

    }

    public void showFooter(boolean show) {
        Log.d("footer", "showFooter: "+show);
        if (this.showFooter == show) return; // 状态没变就不处理

        this.showFooter = show;

        if (show) {
            notifyItemInserted(getItemCount()); // 增加 Footer
        } else {
            notifyItemRemoved(getItemCount()); // 移除 Footer
        }
    }


        /**
     * 更新指定位置文章的收藏状态
     * @param position 文章在列表中的位置
     * @param isCollected 新的收藏状态，true表示已收藏，false表示未收藏
     */
    public void updateCollectState(int position, boolean isCollected) {
        Log.d("COLLECT", "更新收藏状态，位置: " + position + "，状态: " + isCollected);
        // 检查位置有效性并更新收藏状态
        if (position >= 0 && position < articles.size()) {
            articles.get(position).setCollect(isCollected);
            notifyItemChanged(position);
        }
    }



    @Override
    public int getItemCount() {
        int count = (articles == null ? 0 : articles.size()) + (showFooter ? 1 : 0);
        Log.d("Adapter", "getItemCount: " + count);
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        int type = (showFooter && position == getItemCount() - 1) ? TYPE_FOOTER : TYPE_ITEM;
        Log.d("Adapter", "getItemViewType at " + position + ": " + type);
        return type;
    }

    // 更新数据的方法
    @SuppressLint("NotifyDataSetChanged")
    public void setArticles(List<Article> articles) {
        Log.d("square", "setArticles: ");

        this.articles = articles;
        notifyDataSetChanged(); // 通知Adapter数据已更新,刷新整个视图
    }

    // 添加数据到列表（用于分页加载）
    public void addArticles(List<Article> newArticles) {
        int startPosition = articles.size();
        articles.addAll(newArticles);
        notifyItemRangeInserted(startPosition, newArticles.size()); // 精准通知更新，刷新视图
    }

    // ViewHolder内部类（持有视图引用）
    public  static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvAuthor;
        private final TextView tvDate;
        private final ImageView ivCollect;

        private final TextView tvsuperChapterName;

        private final TextView tvchapterName;


        public ViewHolder(View itemView) {
            super(itemView);
            //定义的是视图UI组件
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAuthor = itemView.findViewById(R.id.tv_author);
            tvDate = itemView.findViewById(R.id.tv_date);
            ivCollect = itemView.findViewById(R.id.iv_collect);
            tvsuperChapterName=itemView.findViewById(R.id.tv_superChapter);
            tvchapterName=itemView.findViewById(R.id.tv_chapterName);

        }

        // 绑定数据到视图
        public void bind(Article article) {
            if (article == null) return;
            tvTitle.setText(article.getTitle());
            tvAuthor.setText(article.getAuthor());
            tvDate.setText(article.getNiceDate());
            tvsuperChapterName.setText(article.getSuperChapterName());
            tvchapterName.setText(article.getchapterName());
            //状态true就填满 状态false就不填满
            ivCollect.setImageResource(article.isCollect() ?
                    R.drawable.ic_collect_filled : R.drawable.ic_collect_outline);
        }
    }

    public static class FooterViewHolder extends ViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);

        }
    }
    // 点击事件回调接口
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Article article,int position);
        void onCollectClick(Article article, int position);

        default void onLongClick(Article article) {
            // 默认实现，可以空着或者提供默认行为
        }


    }
}
