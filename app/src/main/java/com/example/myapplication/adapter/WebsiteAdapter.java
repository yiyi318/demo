package com.example.myapplication.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.ui.WebView.WebViewActivity;
import com.example.myapplication.model.Website;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

// WebsiteAdapter.java
public class WebsiteAdapter extends RecyclerView.Adapter<WebsiteAdapter.ViewHolder> {
    private List<Website> websites = new ArrayList<>();

    private final int[] COLOR_PALETTE = {
            0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
            0xFF3F51B5, 0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4,
            0xFF009688, 0xFF4CAF50, 0xFF8BC34A, 0xFFCDDC39,
            0xFFFFC107, 0xFFFF9800, 0xFFFF5722
    };

    public void setWebsites(List<Website> websites) {
        this.websites = websites;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_website, parent, false);

        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (!(lp instanceof FlexboxLayoutManager.LayoutParams)) {
            lp = new FlexboxLayoutManager.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            view.setLayoutParams(lp);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Website website = websites.get(position);
        int color = COLOR_PALETTE[position % COLOR_PALETTE.length];
        holder.bind(website,color);
    }

    @Override
    public int getItemCount() {
        return websites.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final MaterialCardView cardView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            cardView = (MaterialCardView) itemView;



            // 修改ViewHolder中的点击事件
            itemView.setOnClickListener(v -> {
                Website website = websites.get(getAdapterPosition());
                Intent intent = new Intent(itemView.getContext(), WebViewActivity.class);
                intent.putExtra("url", website.getLink());
                intent.putExtra("title",website.getTitle());
                itemView.getContext().startActivity(intent);
            });
        }

        public void bind(Website website,int color) {
            tvTitle.setText(website.getTitle());
            cardView.setCardBackgroundColor(color);

        }
    }
}