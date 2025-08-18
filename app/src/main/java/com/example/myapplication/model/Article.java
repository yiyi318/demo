package com.example.myapplication.model;

// Article.java
public class Article {
    // 基础信息

    private int id;
    private String shareUser;
    private String title;              // 文章标题
    private String author;             // 作者
    private String link;               // 文章链接
    private String niceDate;           // 友好格式的发布时间（如"19小时前"）
    private long publishTime;          // 发布时间戳

    // 分类信息
    private int chapterId;             // 章节ID
    private String chapterName;        // 章节名称
    private int superChapterId;        // 上级章节ID
    private String superChapterName;   // 上级章节名称
    private int originId;

    // 互动信息
    private boolean collect;           // 是否已收藏
    private int zan;                   // 点赞数

    // 其他属性
    private boolean fresh;             // 是否为新内容
    private String envelopePic;        // 封面图链接
    private String desc;               // 文章简介

    // Getter和Setter方法
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getshareUser(){
        return shareUser;
    }

    public int getOrginid(){return originId;}
    public void setOrginid(int orginid){this.originId=orginid;}

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getNiceDate() { return niceDate; }
    public void setNiceDate(String niceDate) { this.niceDate = niceDate; }

    public long getPublishTime() { return publishTime; }
    public void setPublishTime(long publishTime) { this.publishTime = publishTime; }

    public int getChapterId() { return chapterId; }
    public void setChapterId(int chapterId) { this.chapterId = chapterId; }

    public String getsuperChapterName() { return superChapterName; }
    public void setsuperChapterName(String superChapterName) { this.superChapterName = superChapterName; }

    public String getchapterName(){return chapterName;}
    public void setchapterName(String chapterName){
        this.chapterName=chapterName;
    }

    public int getSuperChapterId() { return superChapterId; }
    public void setSuperChapterId(int superChapterId) { this.superChapterId = superChapterId; }

    public String getSuperChapterName() { return superChapterName; }
    public void setSuperChapterName(String superChapterName) { this.superChapterName = superChapterName; }

    public boolean isCollect() { return collect; }
    public void setCollect(boolean collect) { this.collect = collect; }

    public int getZan() { return zan; }
    public void setZan(int zan) { this.zan = zan; }

    public boolean isFresh() { return fresh; }
    public void setFresh(boolean fresh) { this.fresh = fresh; }

    public String getEnvelopePic() { return envelopePic; }
    public void setEnvelopePic(String envelopePic) { this.envelopePic = envelopePic; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }

    public boolean isCollected() {
        return collect;
    }

    public void setCollected(boolean newState) {
        collect=newState;
    }

    public int getId() {
        return  chapterId;
    }
    public int getarticleId(){
        return id;
    }

    public void setarticleId(int articleId){
        this.id=articleId;
    }



}