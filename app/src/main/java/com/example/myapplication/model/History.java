package com.example.myapplication.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// 定义数据库表名
@Entity(tableName = "history_records")
public class History {
    @PrimaryKey(autoGenerate = true) // 自增主键
    private long id;

    // 章节ID（关联教程章节，可选）
    private int chapterId;

    // 章节标题（必填，记录用户查看的内容）
    private String title;

    // 章节链接（必填，用于再次跳转）
    private String link;

    // 访问时间（默认当前时间）
    private long visitTime;

    // 构造方法（不含id，因为自增）
    public History(int chapterId, String title, String link, long visitTime) {
        this.chapterId = chapterId;
        this.title = title;
        this.link = link;
        this.visitTime = visitTime;
    }

    // getter 和 setter 方法
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getChapterId() { return chapterId; }
    public void setChapterId(int chapterId) { this.chapterId = chapterId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public long getVisitTime() { return visitTime; }
    public void setVisitTime(long visitTime) { this.visitTime = visitTime; }
}