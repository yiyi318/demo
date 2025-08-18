package com.example.myapplication.Respository;

import android.util.Log;

import com.example.myapplication.model.Article;
import com.example.myapplication.model.ArticleResponse;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.HotKey;
import com.example.myapplication.model.ProjectCategory;
import com.example.myapplication.model.ShareArticleResponse;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.network.RetrofitClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class Repository {
    private final ApiService apiService;
    private String TAG = "systemarticle";

    public Repository(ApiService service) {
        // 从RetrofitClient获取API服务，建议通过依赖注入方式传入
        this.apiService = RetrofitClient.getService();
    }

    // 新增方法：收藏文章
    public Call<BaseResponse> collectArticle(int articleId) {
        return RetrofitClient.getService().collectArticle(articleId);
    }

    // 新增方法：取消收藏
    public Call<BaseResponse> uncollectArticle(int articleId) {
        return RetrofitClient.getService().uncollectArticle(articleId);
    }
    //获取文章
    public void getArticles(int page, Callback<List<Article>> callback) {
        apiService.getArticleList(page)
                .enqueue(new retrofit2.Callback<>() {
                    @Override
                    public void onResponse(Call<ArticleResponse> call, Response<ArticleResponse> response) {
                        Log.d("API_DEBUG", "Request URL: " + call.request().url());
                        if (response.isSuccessful() && response.body() != null) {
                            // 1. 解析API响应中的文章列表
                            List<Article> articleList = mapToArticleList(response.body());
                            Log.d("Repository", "成功获取文章数据，数量: " + articleList.size());
                            // 2. 成功回调
                            callback.onSuccess(articleList);
                        } else {
                            // 3. 错误响应处理
                            Log.e("Repository", "响应失败，状态码: " + (response.code() != 0 ? response.code() : "null"));
                            String errorMsg = "获取文章失败：" + (response.message() != null ? response.message() : "未知错误");
                            callback.onFailure(new Exception(errorMsg));
                        }
                    }

                    @Override
                    public void onFailure(Call<ArticleResponse> call, Throwable t) {
                        // 4. 网络异常处理
                        Log.e("Repository", "网络请求失败", t);
                        callback.onFailure(t);
                    }
                });
    }
    //获取分享文章
    public void getShareArticles(int page, Callback<List<Article>> callback) {
        apiService.getShareArticles(page)
                .enqueue(new retrofit2.Callback<>() {
                    @Override
                    public void onResponse(Call<ShareArticleResponse> call, Response<ShareArticleResponse> response) {
                        Log.d("share_API_DEBUG", "Request URL: " + call.request().url());
                        if (response.isSuccessful() && response.body() != null) {
                            ShareArticleResponse apiResponse = response.body();

                            // 检查错误码
                            if (apiResponse.getErrorCode() == 0) {
                                // 获取文章列表数据
                                List<Article> articles = new ArrayList<>();
                                for (Article apiArticle : apiResponse.getData().getShareArticles().getDatas()) {
                                    Article article = new Article();
                                    article.setTitle(apiArticle.getTitle() != null
                                            ? apiArticle.getTitle()
                                            : "未知标题");

                                    // 5.4 映射author字段（含空值处理）
                                    article.setAuthor(apiArticle.getshareUser() != null
                                            ? apiArticle.getshareUser()
                                            : "佚名");

                                    //映射nicedata字段
                                    article.setNiceDate(apiArticle.getNiceDate() != null
                                            ? apiArticle.getNiceDate()
                                            : "");

                                    //映射superChapterName字段
                                    article.setsuperChapterName(apiArticle.getsuperChapterName() != null
                                            ? apiArticle.getsuperChapterName()
                                            : "");

                                    article.setchapterName(apiArticle.getchapterName() != null
                                            ? apiArticle.getchapterName()
                                            : "");

                                    article.setarticleId(apiArticle.getarticleId()
                                    );

                                    article.setCollect(apiArticle.isCollect()
                                    );
                                    article.setLink(apiArticle.getLink());
                                    articles.add(article);
                                }

                                Log.d("share_Repository", "成功获取文章数据，数量: " + articles.size());
                                callback.onSuccess(articles);
                            } else {
                                String errorMsg = apiResponse.getErrorMsg();
                                Log.e("Repository", "API错误: " + errorMsg);
                                callback.onFailure(new Exception(errorMsg));
                            }
                        } else {
                            String errorMsg = "响应失败: " + (response.errorBody() != null ?
                                    response.errorBody().toString() : "未知错误");
                            Log.e("Repository", errorMsg);
                            callback.onFailure(new Exception(errorMsg));
                        }
                    }

                    @Override
                    public void onFailure(Call<ShareArticleResponse> call, Throwable t) {
                        Log.e("Repository", "网络请求失败", t);
                        callback.onFailure(t);
                    }
                });
    }
    //上传分享文章
    public Call<BaseResponse>postShareArticle(String title,String link){
        return RetrofitClient.getService().postShareArticle(title,link);
    }
    //上传删除分享文章
    public Call<BaseResponse>postDeleteShareArticle(int id){

        return RetrofitClient.getService().postDeleteShareArticle(id);
    }

    //对搜索的数据做处理
    public void searchArticles(int page, String k, Callback<List<Article>> callback) {
        apiService.getSearchArticle(page, k)
                .enqueue(new retrofit2.Callback<>() {
                    @Override
                    public void onResponse(Call<ArticleResponse> call, Response<ArticleResponse> response) {
                        Log.d("API_DEBUG", "Search Request URL: " + call.request().url());
                        if (response.isSuccessful() && response.body() != null) {
                            // 1. 解析API响应中的文章列表
                            List<Article> articleList = mapToArticleList(response.body());
                            Log.d("Repository", "成功获取搜索结果，数量: " + articleList.size());
                            // 2. 成功回调
                            callback.onSuccess(articleList);
                        } else {
                            // 3. 错误响应处理
                            Log.e("Repository", "搜索响应失败，状态码: " +
                                    (response.code() != 0 ? response.code() : "null"));
                            String errorMsg = "搜索失败：" +
                                    (response.message() != null ? response.message() : "未知错误");
                            callback.onFailure(new Exception(errorMsg));
                        }
                    }

                    @Override
                    public void onFailure(Call<ArticleResponse> call, Throwable t) {
                        // 4. 网络异常处理
                        Log.e("Repository", "搜索请求失败", t);
                        callback.onFailure(t);
                    }
                });
    }
    //得到公众号标题对象

    public void getAuthorTree(Repository.Callback<List<ProjectCategory>> callback) {
        apiService.getAuthorTree().enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<BaseResponse<List<ProjectCategory>>> call,
                                   Response<BaseResponse<List<ProjectCategory>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    //这步骤是获取对应的json转为实体吗
                    BaseResponse<List<ProjectCategory>> body = response.body();

                    if (body.getErrorCode() == 0) {
                        callback.onSuccess(body.getData());
                    } else {
                        callback.onFailure(new Exception("请求失败，错误码: " + response.code()));
                    }
                } else {
                    callback.onFailure(new Exception("请求失败"));
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<ProjectCategory>>> call, Throwable t) {
                Log.e("API_ERROR", "Network failure: " + t.getMessage(), t);
                callback.onFailure(t);
            }
        });
    }
    //得到公众号子标题响应文章对象

    public void getAuthorByChapter(int tabId, int page, Callback<List<Article>> callback) {
        apiService.getAuthorByChapter(tabId, page)
                .enqueue(new retrofit2.Callback<>() {
                    @Override
                    public void onResponse(Call<ArticleResponse> call, Response<ArticleResponse> response) {
                        Log.d("API_DEBUG", "Request URL: " + call.request().url());
                        if (response.isSuccessful() && response.body() != null) {
                            // 1. 解析API响应中的文章列表
                            List<Article> articleList = mapToArticleList(response.body());
                            Log.d("Repository", "成功获取文章数据，数量: " + articleList.size());
                            // 2. 成功回调
                            callback.onSuccess(articleList);
                        } else {
                            // 3. 错误响应处理
                            Log.e("Repository", "响应失败，状态码: " + (response.code() != 0 ? response.code() : "null"));
                            String errorMsg = "获取文章失败：" + (response.message() != null ? response.message() : "未知错误");
                            callback.onFailure(new Exception(errorMsg));
                        }
                    }

                    @Override
                    public void onFailure(Call<ArticleResponse> call, Throwable t) {
                        // 4. 网络异常处理
                        Log.e("Repository", "网络请求失败", t);
                        callback.onFailure(t);
                    }
                });
    }
    //加载系统文章

    public void loadSystemArticles(int page, int chapterId, Callback<List<Article>> callback) {
        Log.d(TAG, "开始加载文章 | page=" + page + ", chapterId=" + chapterId);

        if (chapterId == -1) {
            Log.e(TAG, "无效的chapterId: -1");
            callback.onFailure(new Exception("无效的章节ID"));
            return;
        }

        apiService.getArticlesByChapter(page, chapterId).enqueue(new retrofit2.Callback<ArticleResponse>()  {
            @Override
            public void onResponse(Call<ArticleResponse> call, Response<ArticleResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    List<Article> articles = mapToArticleList(response.body());
                    Log.d(TAG, "收到文章数量: " + articles.size());
                    callback.onSuccess(articles);
                } else {
                    Log.e(TAG, "请求失败 | code=" + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "错误响应: " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    callback.onFailure(new Exception("请求失败，错误码: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<ArticleResponse> call, Throwable t) {
                Log.e(TAG, "请求异常", t);
                // 打印最终请求URL（需Retrofit 2.6.0+）
                if (call != null && call.request() != null) {
                    Log.d(TAG, "失败请求URL: " + call.request().url());
                }
                callback.onFailure(new Exception("网络请求失败: " + t.getMessage()));
            }
        });
    }
    //加载热词
    public void getHotKeys(Callback<List<HotKey>> callback) {
        apiService.getHotKeys().enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<BaseResponse<List<HotKey>>> call, Response<BaseResponse<List<HotKey>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<List<HotKey>> result = response.body();
                    if (result.getErrorCode() == 0) {
                        callback.onSuccess(result.getData());
                    } else {
                        callback.onFailure(new RuntimeException("错误码: " + result.getErrorCode()));
                    }
                } else {
                    callback.onFailure(new RuntimeException("响应体为空"));
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<HotKey>>> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }
    //我的收藏的文章的逻辑
    public void loadCollectArticle(int page, Repository.Callback<List<Article>> callback){

        apiService.getCollectArticleList(page).enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(Call<ArticleResponse> call, Response<ArticleResponse> response) {
                if (response.isSuccessful()) {
                    ArticleResponse body = response.body();
                    List<Article> newArticles = new ArrayList<>();
                    if (body != null && body.getData() != null) {
                        newArticles = body.getData().getDatas();
                        for (Article artcile : newArticles) {
                            String author = artcile.getAuthor();
                            artcile.setAuthor(
                                    (author != null && !author.trim().isEmpty())
                                            ? author
                                            : "佚名"
                            );
                        }
                    }
                    callback.onSuccess(newArticles);
                } else {
                    callback.onFailure(new Exception("数据获取失败"));
                }
            }
            @Override
            public void onFailure(Call<ArticleResponse> call, Throwable t) {
                Log.e(TAG, "请求异常", t);
                // 打印最终请求URL（需Retrofit 2.6.0+）
                if (call != null && call.request() != null) {
                    Log.d(TAG, "失败请求URL: " + call.request().url());
                }
                callback.onFailure(new Exception("网络请求失败: " + t.getMessage()));
            }
        });
    }

    /**
     * 创建取消收藏网络请求回调
     *
     * @param position 文章位置
     * @return Callback<BaseResponse> 实例
     */
    public void uncollectArticle_2(int articleId, int originId, Callback<BaseResponse> callback) {
        apiService.uncollectArticle_2(articleId, originId).enqueue(new retrofit2.Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse body = response.body();
                    if (body.getErrorCode() == 0) {
                        callback.onSuccess(body);
                    } else {
                        callback.onFailure(new Exception(body.getErrorMsg()));
                    }
                } else {
                    callback.onFailure(new Exception("Response error"));
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                callback.onFailure(t);
            }
        });
    }



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
            //获取文章链接
            article.setLink(apiArticle.getLink()
            );

            article.setOrginid(apiArticle.getOrginid());
            result.add(article);
        }

        // 6. 打印映射结果日志
        Log.d("Repository", "成功映射文章列表，数量: " + result.size());

        return result;
    }


    // 定义回调接口
    public interface Callback<T> {
        void onSuccess(T data);
        void onFailure(Throwable t);


    }






}
