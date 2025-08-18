package com.example.myapplication.model;

import java.util.List;

/**
 * 网站分类类，对应JSON中的每个分类对象（包含cid、name和articles列表）
 */
public class WebsiteCategory {
    private int cid;
    private String name;
    private List<Website> articles;

    // Getter和Setter
    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Website> getArticles() {
        return articles;
    }

    public void setArticles(List<Website> articles) {
        this.articles = articles;
    }
}