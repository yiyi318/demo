package com.example.myapplication.Respository;

import android.util.Log;

import com.example.myapplication.model.Article;
import com.example.myapplication.model.ArticleResponse;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.Chapter;
import com.example.myapplication.model.QueryArticle;
import com.example.myapplication.model.QueryArticleResponse;
import com.example.myapplication.model.TutorialArticle;
import com.example.myapplication.model.TutorialChapter;
import com.example.myapplication.model.TutorialChapterResponse;
import com.example.myapplication.model.WebsiteCategory;
import com.example.myapplication.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SystemRepository {
    private final ApiService api;

    public SystemRepository(ApiService api) {
        this.api = api;
    }

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onFailure(String error);
    }
    //获取体系一级标题
    public void getPrimaryCategories(DataCallback<List<Chapter>> callback) {
        Log.d("systemrepository", "getPrimaryCategories: ");
        api.getKnowledgeTree().enqueue(new Callback<BaseResponse<List<Chapter>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<Chapter>>> call,
                                   Response<BaseResponse<List<Chapter>>> response) {
                Log.d("systemrepository", "响应数据: " + response.body()); // 打印原始数据
                if (response.isSuccessful() && response.body() != null) {
                    //这步骤是获取对应的json转为实体吗
                    BaseResponse<List<Chapter>> body = response.body();

                    if (body.getErrorCode() == 0) {
                        callback.onSuccess(body.getData());//这边返回的就是json中的data吗
                    } else {
                        callback.onFailure(body.getErrorMsg());
                    }
                } else {
                    callback.onFailure("Response not successful");
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<Chapter>>> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }
    //获取广场数据
    public void getSquareArticles(int page, Repository.Callback<List<Article>> callback) {
        Log.d("square", "api_getSquareArticles: ");
        api.getsquareArticleList(page)
                .enqueue(new retrofit2.Callback<ArticleResponse>() {
                    @Override
                    public void onResponse(Call<ArticleResponse> call, Response<ArticleResponse> response) {
                        Log.d("square_API_DEBUG", "Request URL: " + call.request().url());
                        if (response.isSuccessful() && response.body() != null) {
                            // 1. 解析API响应中的文章列表
                            List<Article> articleList = mapToArticleList(response.body());
                            Log.d("square_Repository", "成功获取文章数据，数量: " + articleList.size());
                            // 2. 成功回调
                            callback.onSuccess(articleList);
                        } else {
                            // 3. 错误响应处理
                            Log.e("square_Repository", "响应失败，状态码: " + (response.code() != 0 ? response.code() : "null"));
                            String errorMsg = "获取文章失败：" + (response.message() != null ? response.message() : "未知错误");
                            callback.onFailure(new Exception(errorMsg));
                        }
                    }

                    @Override
                    public void onFailure(Call<ArticleResponse> call, Throwable t) {
                        // 4. 网络异常处理
                        Log.e("square_Repository", "网络请求失败", t);
                        callback.onFailure(t);
                    }
                });
    }

    public void getQueryArticleListint(int page, Repository.Callback<List<QueryArticle>> callback) {
        Log.d("query", "api_getSquareArticles: ");
        api.getQueryArticleList(page)
                .enqueue(new retrofit2.Callback<QueryArticleResponse>() {
                    @Override
                    public void onResponse(Call<QueryArticleResponse> call, Response<QueryArticleResponse> response) {
                        Log.d("query_API_DEBUG", "Request URL: " + call.request().url());
                        if (response.isSuccessful() && response.body() != null) {
                            // 1. 解析API响应中的文章列表
                            List<QueryArticle> articleList = mapToQueryArticleList(response.body());
                            Log.d("query_Repository", "成功获取文章数据，数量: " + articleList.size());
                            // 2. 成功回调
                            callback.onSuccess(articleList);
                        } else {
                            // 3. 错误响应处理
                            Log.e("query_Repository", "响应失败，状态码: " + (response.code() != 0 ? response.code() : "null"));
                            String errorMsg = "获取文章失败：" + (response.message() != null ? response.message() : "未知错误");
                            callback.onFailure(new Exception(errorMsg));
                        }
                    }

                    @Override
                    public void onFailure(Call<QueryArticleResponse> call, Throwable t) {
                        Log.d("query_API_DEBUG", "Request URL: " + call.request().url());
                        // 4. 网络异常处理
                        Log.e("query_Repository", "网络请求失败", t);
                        callback.onFailure(t);
                    }
                });
    }

    public void getTutorialArticleList(Repository.Callback<List<TutorialArticle>> callback) {
        api.getTutorialArticle().enqueue(new Callback<BaseResponse<List<TutorialArticle>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<TutorialArticle>>> call, Response<BaseResponse<List<TutorialArticle>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        BaseResponse<List<TutorialArticle>> apiResponse = response.body();

                        // 检查业务错误码
                        if (apiResponse.getErrorCode() == 0 && apiResponse.getData() != null) {
                            List<TutorialArticle> articles = apiResponse.getData();
                            callback.onSuccess(articles);
                        } else {
                            String errorMsg = apiResponse.getErrorMsg() != null ?
                                    apiResponse.getErrorMsg() : "服务器返回错误";
                            callback.onFailure(new Exception(errorMsg));
                        }
                    } else {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "无错误详情";
                        callback.onFailure(new Exception("HTTP " + response.code()));
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<TutorialArticle>>> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    public void getWebsiteCategories(Repository.Callback<List<WebsiteCategory>> callback) {
        api.getWebsiteCategories().enqueue(new Callback<BaseResponse<List<WebsiteCategory>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<WebsiteCategory>>> call, Response<BaseResponse<List<WebsiteCategory>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        BaseResponse<List<WebsiteCategory>> apiResponse = response.body();

                        // 检查业务错误码
                        if (apiResponse.getErrorCode() == 0 && apiResponse.getData() != null) {
                            List<WebsiteCategory> articles = apiResponse.getData();
                            callback.onSuccess(articles);
                        } else {
                            String errorMsg = apiResponse.getErrorMsg() != null ?
                                    apiResponse.getErrorMsg() : "服务器返回错误";
                            callback.onFailure(new Exception(errorMsg));
                        }
                    } else {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "无错误详情";
                        callback.onFailure(new Exception("HTTP " + response.code()));
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<WebsiteCategory>>> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    public void getTutorialChapters(int courseId, Repository.Callback<List<TutorialChapter>> callback) {
        api.getTutorialChapters(courseId, 1).enqueue(new Callback<TutorialChapterResponse>() {
            @Override
            public void onResponse(Call<TutorialChapterResponse> call, Response<TutorialChapterResponse> response) {
                Log.d("tutorialchapter", "onResponse: "+ call.request().url());
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        TutorialChapterResponse apiResponse = response.body();

                        if (apiResponse.getErrorCode() == 0 &&
                                apiResponse.getData() != null &&
                                apiResponse.getData().getDatas() != null) {

                            // 转换数据格式
                            List<TutorialChapter> chapters = convertToChapterList(
                                    apiResponse.getData().getDatas()
                            );
                            Log.d("tutorialchapter", "onResponse: "+chapters.size());
                            callback.onSuccess(chapters);
                        } else {
                            callback.onFailure(new Exception(apiResponse.getErrorMsg()));
                        }
                    } else {
                        String errorMsg = response.errorBody() != null ?
                                response.errorBody().string() : "请求失败";
                        callback.onFailure(new Exception(errorMsg));
                    }
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }

            @Override
            public void onFailure(Call<TutorialChapterResponse> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }

    // 将API数据转换为章节列表
    private List<TutorialChapter> convertToChapterList(List<TutorialChapter> articles) {
        List<TutorialChapter> chapters = new ArrayList<>();
        for (int i = 0; i < articles.size(); i++) {
            TutorialChapter article = articles.get(i);
            chapters.add(new TutorialChapter(
                    i + 1,                  // 章节序号(从1开始)
                    article.getTitle(),     // 章节标题
                    article.getLink()       // 章节链接
            ));
        }
        return chapters;
    }

//    public interface Callback<T> {
//        void onSuccess(T data);
//        void onFailure(Throwable t);
//    }



    // 数据转换：将API响应对象映射为Article实体
    private List<Article> mapToArticleList(ArticleResponse response) {
        List<Article> result = new ArrayList<>();

        // 1. 检查response是否为null
        if (response == null) {
            Log.e("Repository", "ArticleResponse 对象为null");
            return result;
        }

        // 2. 检查data字段是否为null
        ArticleResponse.Data apiData = response.getData();
        if (apiData == null) {
            Log.e("Repository", "响应中的data字段为null，跳过映射");
            return result;
        }

        // 3. 检查文章列表datas是否为null
        List<Article> articleList = apiData.getDatas();
        if (articleList == null) {
            Log.e("Repository", "文章列表datas字段为null，返回空列表");
            return result;
        }

        // 4. 检查列表是否为空
        if (articleList.isEmpty()) {
            Log.d("Repository", "文章列表datas为空，返回空列表");
            return result;
        }

        // 5. 安全遍历文章列表
        for (Article apiArticle : articleList) {
            // 5.1 检查单个文章对象是否为null
            if (apiArticle == null) {
                Log.w("Repository", "文章对象为null，跳过处理");
                continue;
            }

            //5.2 创建目标Article对象
            Article article = new Article();
            // 5.3 映射title字段（含空值处理）
            article.setTitle(apiArticle.getTitle() != null
                    ? apiArticle.getTitle()
                    : "未知标题");

            // 5.4 映射author字段（含空值处理）
            String author = apiArticle.getAuthor();
            article.setAuthor(
                    (author != null && !author.trim().isEmpty())
                            ? author
                            : "佚名"
            );
//            Log.d("article", "mapToArticleList: "+article.getAuthor());

            //映射nicedata字段
            article.setNiceDate(apiArticle.getNiceDate()!=null
                    ?apiArticle.getNiceDate()
                    :"");

            //映射superChapterName字段
            article.setsuperChapterName(apiArticle.getsuperChapterName()!=null
                    ?apiArticle.getsuperChapterName()
                    :"");

            article.setchapterName(apiArticle.getchapterName()!=null
                    ?apiArticle.getchapterName()
                    :"");

            article.setarticleId(apiArticle.getarticleId()
            );

            article.setCollect(apiArticle.isCollect()
            );
            article.setOrginid(apiArticle.getOrginid());
            article.setLink(apiArticle.getLink());
            result.add(article);
        }

        // 6. 打印映射结果日志
        Log.d("Repository", "成功映射文章列表，数量: " + result.size());

        return result;
    }
    private List<QueryArticle> mapToQueryArticleList(QueryArticleResponse response) {
        List<QueryArticle> result = new ArrayList<>();

        // 1. 检查response是否为null
        if (response == null) {
            Log.e("Repository", "QueryArticleResponse 对象为null");
            return result;
        }

        // 2. 检查data字段是否为null（假设结构与ArticleResponse一致，包含Data内部类）
        QueryArticleResponse.Data apiData = response.getData();
        if (apiData == null) {
            Log.e("Repository", "查询响应中的data字段为null，跳过映射");
            return result;
        }

        // 3. 检查查询文章列表datas是否为null
        List<QueryArticle> queryArticleList = apiData.getDatas();
        if (queryArticleList == null) {
            Log.e("Repository", "查询文章列表datas字段为null，返回空列表");
            return result;
        }

        // 4. 检查列表是否为空
        if (queryArticleList.isEmpty()) {
            Log.d("Repository", "查询文章列表datas为空，返回空列表");
            return result;
        }

        // 5. 安全遍历查询文章列表
        for (QueryArticle apiQueryArticle : queryArticleList) {
            // 5.1 检查单个查询文章对象是否为null
            if (apiQueryArticle == null) {
                Log.w("Repository", "查询文章对象为null，跳过处理");
                continue;
            }

            // 5.2 创建目标QueryArticle对象
            QueryArticle queryArticle = new QueryArticle();

            // 5.3 映射title字段（含空值处理）
            queryArticle.setTitle(apiQueryArticle.getTitle() != null
                    ? apiQueryArticle.getTitle()
                    : "未知标题");

            // 5.4 映射author字段（含空值处理）

            String author = apiQueryArticle.getAuthor();
            queryArticle.setAuthor(
                    (author != null && !author.trim().isEmpty())
                            ? author
                            : "佚名"
            );

            // 映射niceDate字段
            queryArticle.setNiceDate(apiQueryArticle.getNiceDate() != null
                    ? apiQueryArticle.getNiceDate()
                    : "");

            // 映射desc字段（查询文章可能包含描述信息）
            queryArticle.setDesc(apiQueryArticle.getDesc() != null
                    ? cleanHtml(apiQueryArticle.getDesc())
                    : "");

            // 映射superChapterName字段
            queryArticle.setSuperChapterName(apiQueryArticle.getSuperChapterName() != null
                    ? apiQueryArticle.getSuperChapterName()
                    : "");

            // 映射chapterName字段
            queryArticle.setChapterName(apiQueryArticle.getChapterName() != null
                    ? apiQueryArticle.getChapterName()
                    : "");


            queryArticle.setLink(apiQueryArticle.getLink());
            // 映射收藏状态
            queryArticle.setCollect(apiQueryArticle.isCollect());
//            Log.d("CollectQuer", "mapToQueryArticleList: "+apiQueryArticle.isCollect());
            // 可根据QueryArticle特有字段继续补充映射
            queryArticle.setId(apiQueryArticle.getId());

            result.add(queryArticle);
        }

        // 6. 打印映射结果日志
        Log.d("Repository", "成功映射查询文章列表，数量: " + result.size());

        return result;
    }

    public static String cleanHtml(String rawHtml) {
        if (rawHtml == null) return "";

        // 去掉常见的HTML标签
        String cleaned = rawHtml
                .replaceAll("(?i)<pre.*?>", "")
                .replaceAll("(?i)</pre>", "")
                .replaceAll("(?i)<code.*?>", "")
                .replaceAll("(?i)</code>", "")
                .replaceAll("(?i)<p.*?>", "")
                .replaceAll("(?i)</p>", "")
                .replaceAll("(?i)<br.*?>", "")
                .replaceAll("(?i)<.*?>", ""); // 清除其他HTML标签

        // 还原 HTML 实体字符
        cleaned = cleaned.replaceAll("&quot;", "\"")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&");

        // 去除多余空白
        cleaned = cleaned.trim();

        return cleaned;
    }


}
