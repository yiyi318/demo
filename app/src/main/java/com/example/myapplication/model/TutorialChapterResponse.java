package com.example.myapplication.model;

import java.util.List;

public class TutorialChapterResponse {
    // 错误码，0表示成功
    private int errorCode;
    // 错误信息，成功时为空
    private String errorMsg;
    // 具体的教程章节数据
    private Data data;

    // getter 和 setter 方法
    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    /**
     * 数据内部类，包含分页信息和章节列表
     */
    public static class Data {
        // 当前页码
        private int curPage;
        // 章节数据列表
        private List<TutorialChapter> datas;
        // 偏移量
        private int offset;
        // 是否加载完毕
        private boolean over;
        // 总页数
        private int pageCount;
        // 每页大小
        private int size;
        // 总数据量
        private int total;

        // getter 和 setter 方法
        public int getCurPage() {
            return curPage;
        }

        public void setCurPage(int curPage) {
            this.curPage = curPage;
        }

        public List<TutorialChapter> getDatas() {
            return datas;
        }

        public void setDatas(List<TutorialChapter> datas) {
            this.datas = datas;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public boolean isOver() {
            return over;
        }

        public void setOver(boolean over) {
            this.over = over;
        }

        public int getPageCount() {
            return pageCount;
        }

        public void setPageCount(int pageCount) {
            this.pageCount = pageCount;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }
    }
}