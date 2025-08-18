package com.example.myapplication.network;

import androidx.annotation.NonNull;

import com.example.myapplication.model.ArticleResponse;
import com.example.myapplication.model.Banner;
import com.example.myapplication.model.BaseResponse;
import com.example.myapplication.model.Chapter;
import com.example.myapplication.model.CoinArticleResponse;
import com.example.myapplication.model.CoinInfo;
import com.example.myapplication.model.HotKey;
import com.example.myapplication.model.ProjectArticleResponse;
import com.example.myapplication.model.ProjectCategory;
import com.example.myapplication.model.QueryArticleResponse;
import com.example.myapplication.model.RankResponse;
import com.example.myapplication.model.ShareArticleResponse;
import com.example.myapplication.model.TutorialArticle;
import com.example.myapplication.model.TutorialChapterResponse;
import com.example.myapplication.model.User;
import com.example.myapplication.model.WebsiteCategory;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * ApiService 接口定义了与 WanAndroid 开放 API 交互的所有网络请求方法。
 * 使用 Retrofit 框架进行网络请求，每个方法对应一个具体的 API 接口。
 */
public interface ApiService {

    /**
     * 获取首页文章列表
     *
     * @param page 页码，从0开始
     * @return 返回封装的文章响应对象
     */
    @GET("article/list/{page}/json")
    Call<ArticleResponse> getArticleList(@Path("page") int page);

    /**
     * 获取收藏的文章列表
     *
     * @param page 页码，从0开始
     * @return 返回封装的收藏文章响应对象
     */
    @GET("lg/collect/list/{page}/json")
    Call<ArticleResponse> getCollectArticleList(@Path("page") int page);

    /**
     * 获取问答文章列表
     *
     * @param page 页码，从0开始
     * @return 返回封装的问答文章响应对象
     */
    @GET("wenda/list/{page}/json ")
    Call<QueryArticleResponse> getQueryArticleList(@Path("page") int page);

    /**
     * 获取广场文章列表
     *
     * @param page 页码，从0开始
     * @return 返回封装的广场文章响应对象
     */
    @GET("user_article/list/{page}/json")
    Call<ArticleResponse> getsquareArticleList(@Path("page") int page);

    /**
     * 获取轮播图数据
     *
     * @return 返回封装的轮播图响应对象
     */
    @GET("banner/json")
    Call<BaseResponse<List<Banner>>> getBanners();

    /**
     * 获取搜索热词
     *
     * @return 返回封装的热词响应对象
     */
    @GET("hotkey/json")
    Call<BaseResponse<List<HotKey>>> getHotKeys();

    /**
     * 获取当前登录用户信息
     *
     * @return 返回封装的用户信息 JSON 对象
     */
    @GET("user/lg/userinfo/json")
    @NonNull
    Call<JsonObject> getUserInfo();

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 返回封装的登录结果响应对象
     */
    @FormUrlEncoded
    @POST("user/login")
    Call<BaseResponse<User>> login(
            @Field("username") String username,
            @Field("password") String password
    );

    /**
     * 用户注册
     *
     * @param username   用户名
     * @param password   密码
     * @param repassword 确认密码
     * @return 返回封装的注册结果响应对象
     */
    @FormUrlEncoded
    @POST("user/register")
    Call<BaseResponse<Object>> register(
            @Field("username") String username,
            @Field("password") String password,
            @Field("repassword") String repassword
    );

    /**
     * 用户退出登录
     *
     * @return 返回封装的退出登录响应对象
     */
    @GET("user/logout/json")
    Call<JsonObject> logout();

    /**
     * 收藏指定 ID 的文章
     *
     * @param id 文章 ID
     * @return 返回封装的收藏操作响应对象
     */
    @POST("lg/collect/{id}/json")
    Call<BaseResponse> collectArticle(@Path("id") int id);

    /**
     * 取消收藏文章（使用 originId）
     *
     * @param originId 原始文章 ID
     * @return 返回封装的取消收藏操作响应对象
     */
    @POST("lg/uncollect_originId/{originId}/json")
    Call<BaseResponse> uncollectArticle(@Path("originId") int originId);

    /**
     * 取消收藏文章（使用文章 ID 和 originId）
     *
     * @param id       文章 ID
     * @param originId 原始文章 ID
     * @return 返回封装的取消收藏操作响应对象
     */
    @FormUrlEncoded
    @POST("lg/uncollect/{id}/json")
    Call<BaseResponse> uncollectArticle_2(
            @Path("id") int id,
            @Field("originId") int originId
    );

    /**
     * 搜索文章
     *
     * @param page 页码，从0开始
     * @param k    搜索关键词
     * @return 返回封装的搜索结果响应对象
     */
    @FormUrlEncoded
    @POST("article/query/{page}/json")
    Call<ArticleResponse> getSearchArticle(
            @Path("page") int page,
            @Field("k") String k
    );

    /**
     * 获取积分排行榜
     *
     * @param page 页码，从1开始
     * @return 返回封装的积分排行榜响应对象
     */
    @GET("coin/rank/{page}/json")
    Call<RankResponse> getCoinRankList(@Path("page") int page);

    /**
     * 获取个人积分信息
     *
     * @return 返回封装的个人积分信息响应对象
     */
    @GET("/lg/coin/userinfo/json")
    Call<BaseResponse<CoinInfo>> getPersonalCoinInfo();

    /**
     * 获取个人积分记录列表
     *
     * @param page 页码，从1开始
     * @return 返回封装的积分记录响应对象
     */
    @GET("lg/coin/list/{page}/json")
    Call<CoinArticleResponse> getCoinList(@Path("page") int page);

    /**
     * 获取知识体系树
     *
     * @return 返回封装的知识体系章节响应对象
     */
    @GET("tree/json")
    Call<BaseResponse<List<Chapter>>> getKnowledgeTree();

    /**
     * 根据章节 ID 获取对应的文章列表
     *
     * @param page      页码，从0开始
     * @param chapterId 章节 ID
     * @return 返回封装的文章响应对象
     */
    @GET("article/list/{page}/json")
    Call<ArticleResponse> getArticlesByChapter(
            @Path("page") int page,
            @Query("cid") int chapterId
    );

    /**
     * 获取项目分类列表
     *
     * @return 返回封装的项目分类响应对象
     */
    @GET("project/tree/json")
    Call<BaseResponse<List<ProjectCategory>>> getProjectTree();

    /**
     * 根据项目分类 ID 获取对应的文章列表
     *
     * @param page      页码，从1开始
     * @param chapterId 项目分类 ID
     * @return 返回封装的项目文章响应对象
     */
    @GET("project/list/{page}/json")
    Call<ProjectArticleResponse> getProjectByChapter(
            @Path("page") int page,
            @Query("cid") int chapterId
    );

    /**
     * 获取公众号列表
     *
     * @return 返回封装的公众号分类响应对象
     */
    @GET("wxarticle/chapters/json ")
    Call<BaseResponse<List<ProjectCategory>>> getAuthorTree();

    /**
     * 根据公众号 ID 获取历史文章列表
     *
     * @param id   公众号 ID
     * @param page 页码，从1开始
     * @return 返回封装的文章响应对象
     */
    @GET("wxarticle/list/{id}/{page}/json")
    Call<ArticleResponse> getAuthorByChapter(
            @Path("id") int id,
            @Path("page") int page
    );

    /**
     * 获取教程文章列表
     *
     * @return 返回封装的教程文章响应对象
     */
    @GET("chapter/547/sublist/json")
    Call<BaseResponse<List<TutorialArticle>>> getTutorialArticle();

    /**
     * 获取教程章节列表
     *
     * @param courseId   教程 ID
     * @param orderType  排序方式（1=正序）
     * @return 返回封装的教程章节响应对象
     */
    @GET("article/list/0/json")
    Call<TutorialChapterResponse> getTutorialChapters(
            @Query("cid") int courseId,
            @Query("order_type") int orderType
    );

    /**
     * 获取导航网站分类列表
     *
     * @return 返回封装的网站分类响应对象
     */
    @GET("navi/json")
    Call<BaseResponse<List<WebsiteCategory>>> getWebsiteCategories();

    /**
     * 获取我的分享文章列表
     *
     * @param page 页码，从1开始
     * @return 返回封装的分享文章响应对象
     */
    @GET("user/lg/private_articles/{page}/json")
    Call<ShareArticleResponse> getShareArticles(@Path("page") int page);

    /**
     * 分享一篇文章
     *
     * @param title 文章标题
     * @param link  文章链接
     * @return 返回封装的分享操作响应对象
     */
    @FormUrlEncoded
    @POST("lg/user_article/add/json")
    Call<BaseResponse> postShareArticle(
            @Field("title") String title,
            @Field("link") String link
    );

    /**
     * 删除我分享的文章
     *
     * @param id 文章 ID
     * @return 返回封装的删除操作响应对象
     */
    @POST("lg/user_article/delete/{id}/json")
    Call<BaseResponse> postDeleteShareArticle(@Path("id") int id);
}
