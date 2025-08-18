package com.example.myapplication.model;

import java.util.List;

public class ShareArticleResponse {
    private int errorCode;
    private String errorMsg;
    private ShareArticleData data;

    // Getters and Setters
    public int getErrorCode() { return errorCode; }
    public String getErrorMsg() { return errorMsg; }
    public ShareArticleData getData() { return data; }

    public static class ShareArticleData {
        private ShareArticleList shareArticles;

        // Getters and Setters
        public ShareArticleList getShareArticles() { return shareArticles; }
    }

    public static class ShareArticleList {
        private int curPage;
        private List<Article> datas;
        private boolean over;
        private int pageCount;
        private int size;
        private int total;


        // Getters and Setters

        public int getCurPage() { return curPage; }
        public List<Article> getDatas() { return datas; }
        public boolean isOver() { return over; }
        public int getPageCount() { return pageCount; }
        public int getSize() { return size; }
        public int getTotal() { return total; }
    }
}
