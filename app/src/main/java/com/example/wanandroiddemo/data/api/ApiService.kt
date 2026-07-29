package com.example.wanandroiddemo.data.api

import com.example.wanandroiddemo.data.model.domain.Banner
import com.example.wanandroiddemo.data.model.dto.ArticleDto
import com.example.wanandroiddemo.data.model.dto.BaseResponse
import com.example.wanandroiddemo.data.model.dto.BookmarkDto
import com.example.wanandroiddemo.data.model.dto.CoinRecordDto
import com.example.wanandroiddemo.data.model.dto.CollectArticleDto
import com.example.wanandroiddemo.data.model.dto.HomeArticleDto
import com.example.wanandroiddemo.data.model.dto.HotKeyDto
import com.example.wanandroiddemo.data.model.dto.NavigationDto
import com.example.wanandroiddemo.data.model.dto.PageResponse
import com.example.wanandroiddemo.data.model.dto.ProjectCategoryDto
import com.example.wanandroiddemo.data.model.dto.ShareArticleDto
import com.example.wanandroiddemo.data.model.dto.SystemCategoryDto
import com.example.wanandroiddemo.data.model.dto.TodoDto
import com.example.wanandroiddemo.data.model.dto.UserCoinDto
import com.example.wanandroiddemo.data.model.dto.UserDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * WanAndroid API 接口定义
 */
interface ApiService {

    @GET("banner/json")
    suspend fun getBanners(): BaseResponse<List<Banner>>
    // 获取首页置顶文章
    @GET("article/top/json")
    suspend fun getTopArticles(): BaseResponse<List<ArticleDto>>
    /**
     * 注册接口
     * */
    @FormUrlEncoded
    @POST("user/register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("repassword") repassword: String
    ): BaseResponse<UserDto>

    /**
     * 登录接口
     *
     * */
    @FormUrlEncoded
    @POST("user/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): BaseResponse<UserDto>

    @GET("article/list/{page}/json")
    suspend fun getArticles(@Path("page") page: Int): BaseResponse<HomeArticleDto>

    //获取广场文章
    @GET("user_article/list/{page}/json")
    suspend fun getSquareArticles(@Path("page") page: Int): BaseResponse<HomeArticleDto>

    //获取体系分类
    @GET("tree/json")
    suspend fun getSystemCategories(): BaseResponse<List<SystemCategoryDto>>

    //获取体系分类下的文章
    @GET("article/list/{page}/json")
    suspend fun getSystemArticles(
        @Path("page") page: Int,
        @retrofit2.http.Query("cid") cid: Int
    ): BaseResponse<HomeArticleDto>

    @GET("project/tree/json")
    suspend fun getProjectCategories(): BaseResponse<List<ProjectCategoryDto>>

    @GET("navi/json")
    suspend fun getNavigationData(): BaseResponse<List<NavigationDto>>


    /****************************************************************
     *                     我的收藏模块
     * ****************************************************************/
    // 1. 获取个人积分、等级等信息
    @GET("lg/coin/userinfo/json")
    suspend fun getUserCoinInfo(): BaseResponse<UserCoinDto>

    // 2. 获取个人积分获取历史列表（分页）
    @GET("lg/coin/list/{page}/json")
    suspend fun getCoinHistoryList(
        @Path("page") page: Int
    ): BaseResponse<PageResponse<CoinRecordDto>>

    // 3. 获取我的收藏文章列表（分页）
    @GET("lg/collect/list/{page}/json")
    suspend fun getCollectList(
        @Path("page") page: Int
    ): BaseResponse<PageResponse<CollectArticleDto>>

    //4. 取消收藏（在我的收藏列表页面调用，使用 originId）
    @POST("lg/uncollect_originId/{id}/json")
    suspend fun uncollectArticle(
        @Path("id") id: Int
    ): BaseResponse<Any>

    @POST("lg/collect/{id}/json")
    suspend fun collectArticle(
        @Path("id") id: Int
    ): BaseResponse<Any>

    /****************************************************************
     *                     我的书签模块
     * ****************************************************************/
    // 1. 获取书签（收藏网站）列表
    @GET("lg/collect/usertools/json")
    suspend fun getBookmarkList(): BaseResponse<List<BookmarkDto>>

    // 2. 添加书签（收藏网址）
    @FormUrlEncoded
    @POST("lg/collect/addtool/json")
    suspend fun addBookmark(
        @Field("name") name: String,
        @Field("link") link: String
    ): BaseResponse<BookmarkDto>

    // 3. 编辑/修改书签
    @FormUrlEncoded
    @POST("lg/collect/updatetool/json")
    suspend fun updateBookmark(
        @Field("id") id: Int,
        @Field("name") name: String,
        @Field("link") link: String
    ): BaseResponse<BookmarkDto>

    // 4. 删除书签
    @FormUrlEncoded
    @POST("lg/collect/deletetool/json")
    suspend fun deleteBookmark(
        @Field("id") id: Int
    ): BaseResponse<Any>

    /****************************************************************
     *                     我的分享
     * ****************************************************************/
    // 1. 获取自己的分享列表（已登录）
    @GET("user/lg/private_articles/{page}/json")
    suspend fun getPrivateArticles(
        @Path("page") page: Int
    ): BaseResponse<ShareArticleDto>

    // 2. 删除自己分享的文章
    @POST("lg/user_article/delete/{id}/json")
    suspend fun deleteSharedArticle(
        @Path("id") id: Int
    ): BaseResponse<Any>
    /**
     * 分享文章
     * @param title 文章标题
     * @param link 文章链接
     */
    @FormUrlEncoded
    @POST("lg/user_article/add/json")
    suspend fun shareArticle(
        @Field("title") title: String,
        @Field("link") link: String
    ): BaseResponse<Any>

    /****************************************************************
     *                     TO DO 模块
     * ****************************************************************/
    // 1. 获取 TODO 列表（支持分页，status：0未完成，1已完成，不传全部）
    @GET("lg/todo/v2/list/{page}/json")
    suspend fun getTodoList(
        @Path("page") page: Int,
        @Query("status") status: Int?
    ): BaseResponse<PageResponse<TodoDto>>

    // 2. 新增一个 TODO
    @FormUrlEncoded
    @POST("lg/todo/add/json")
    suspend fun addTodo(
        @Field("title") title: String,
        @Field("content") content: String,
        @Field("date") date: String, // 格式：yyyy-MM-dd
        @Field("priority") priority: Int
    ): BaseResponse<TodoDto>

    // 3. 更新一个 TODO（必须包含完整参数）
    @FormUrlEncoded
    @POST("lg/todo/update/{id}/json")
    suspend fun updateTodo(
        @Path("id") id: Int,
        @Field("title") title: String,
        @Field("content") content: String,
        @Field("date") date: String,
        @Field("status") status: Int,// 0为未完成，1为完成
        @Field("priority") priority: Int
    ): BaseResponse<TodoDto>

    // 4. 仅更新 TODO 的状态（完成 / 恢复未完成）
    @FormUrlEncoded
    @POST("lg/todo/done/{id}/json")
    suspend fun toggleTodoStatus(
        @Path("id") id: Int,
        @Field("status") status: Int // 0未完成，1已完成
    ): BaseResponse<TodoDto>

    // 5. 删除一个 TODO
    @POST("lg/todo/delete/{id}/json")
    suspend fun deleteTodo(
        @Path("id") id: Int
    ): BaseResponse<Any>


    /****************************************************************
     *                    搜索 模块
     * ****************************************************************/

    // 1. 获取热门搜索词
    @GET("hotkey/json")
    suspend fun getHotKeys(): BaseResponse<List<HotKeyDto>>

    // 2. 搜索接口（POST 请求，关键字通过表单参数 k 传递，page 从 0 开始）
    @FormUrlEncoded
    @POST("article/query/{page}/json")
    suspend fun searchArticles(
        @Path("page") page: Int,
        @Field("k") keyboard: String
    ): BaseResponse<HomeArticleDto>

    /**
     * 获取每日一问列表数据
     * 💡 注意：玩 Android 官方规范中，每日一问的页码【从 1 开始】请求！
     */
    @GET("wenda/list/{page}/json")
    suspend fun getWendaList(
        @Path("page") page: Int
    ): BaseResponse<HomeArticleDto> // 用首页文章的分页结构体 ArticlePageDto
}
