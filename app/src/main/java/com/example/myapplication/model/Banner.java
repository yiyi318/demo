package com.example.myapplication.model;

public class Banner {
    private String imagePath;  // Banner 图片地址
    private String title;      // Banner 标题
    private String url;        // 点击跳转链接

    // Getter & Setter
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
