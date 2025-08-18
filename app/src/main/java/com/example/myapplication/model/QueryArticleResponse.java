package com.example.myapplication.model;

import java.util.List;

public class QueryArticleResponse {
    private int errorCode;         // 错误码，0表示成功
    private String errorMsg;       // 错误信息，成功时为空
    private Data data;             // 数据主体，非列表类型

    // Getter 和 Setter 方法
    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public Data getData() {
        return data;
    }

    // 内部类：定义文章数据结构
    // 内部类：对应API响应中的data字段
    public static class Data {
        private int curPage;        // 当前页码
        private int pageCount;      // 总页数
        private List<QueryArticle> datas; // 文章列表，注意是List类型

        // Getter 和 Setter 方法
        public int getCurPage() {
            return curPage;
        }

        public int getPageCount() {
            return pageCount;
        }

        public List<QueryArticle> getDatas() {
            return datas;
        }
    }

}
