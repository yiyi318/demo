package com.example.myapplication.model;

public class SettingItem {
    private int id;
    private String title;
    private String description;
    private int iconResId;
    private SettingItemType type;

    public enum SettingItemType {
        MODE, // 夜间模式
        STORAGE, // 清除缓存
        ACTION, // 普通点击动作
        LINK, // 网页链接
        DIALOG // 弹出对话框
    }

    public SettingItem(int id, String title, String description, int iconResId, SettingItemType type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIconResId() {
        return iconResId;
    }

    public SettingItemType getType() {
        return type;
    }
}