package com.example.myapplication.model;

import java.util.List;

/**
 * 文章/教程信息实体类
 * 对应阮一峰C语言入门教程相关数据结构
 */
public class TutorialArticle {
    // 文章列表（为空列表）
    private List<Object> articleList;

    // 作者
    private String author;

    // 子章节列表
    private List<Object> children;

    // 课程ID
    private int courseId;

    // 封面图片URL
    private String cover;

    // 描述信息
    private String desc;

    // 文章ID
    private int id;

    // 授权协议
    private String lisense;

    // 授权协议链接
    private String lisenseLink;

    // 文章名称
    private String name;

    // 排序序号
    private int order;

    // 父章节ID
    private int parentChapterId;

    // 类型标识
    private int type;

    // 是否用户置顶
    private boolean userControlSetTop;

    // 可见性标识
    private int visible;

    // 以下为getter和setter方法
    public List<Object> getArticleList() {
        return articleList;
    }

    public void setArticleList(List<Object> articleList) {
        this.articleList = articleList;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Object> getChildren() {
        return children;
    }

    public void setChildren(List<Object> children) {
        this.children = children;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLisense() {
        return lisense;
    }

    public void setLisense(String lisense) {
        this.lisense = lisense;
    }

    public String getLisenseLink() {
        return lisenseLink;
    }

    public void setLisenseLink(String lisenseLink) {
        this.lisenseLink = lisenseLink;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getParentChapterId() {
        return parentChapterId;
    }

    public void setParentChapterId(int parentChapterId) {
        this.parentChapterId = parentChapterId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isUserControlSetTop() {
        return userControlSetTop;
    }

    public void setUserControlSetTop(boolean userControlSetTop) {
        this.userControlSetTop = userControlSetTop;
    }

    public int getVisible() {
        return visible;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }
}
