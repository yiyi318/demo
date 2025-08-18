package com.example.myapplication.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.TutorialChapter;

import java.util.ArrayList;
import java.util.List;

public class TutorialSecondAdapter extends RecyclerView.Adapter<TutorialSecondAdapter.ViewHolder> {
    private final List<TutorialChapter> chapters;
    private OnItemClickListener onItemClickListener;

    public TutorialSecondAdapter(List<TutorialChapter> chapters) {
        this.chapters = chapters != null ? chapters : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tutorial_second, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("tutorialchapter", "onBindViewHolder: "+chapters.get(position).getLink());//这边到了
        TutorialChapter chapter = chapters.get(position);
        holder.bind(chapter);

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onChapterClick(chapter, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }



    public void updateData(List<TutorialChapter> newChapters) {
        Log.d("tutorialchapter", "updateData: "+newChapters.size());
        this.chapters.clear();
        this.chapters.addAll(newChapters);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvChapterNumber;
        private final TextView tvChapterTitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChapterNumber = itemView.findViewById(R.id.tv_chapter_number);
            tvChapterTitle = itemView.findViewById(R.id.tv_chapter_title);
        }

        public void bind(TutorialChapter chapter) {
            tvChapterNumber.setText(String.valueOf(chapter.getNumber()));
            tvChapterTitle.setText(chapter.getTitle());
        }
    }

    public interface OnItemClickListener {
        void onChapterClick(TutorialChapter chapter, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}