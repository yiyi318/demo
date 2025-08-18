package com.example.myapplication.model;

public class TutorialChapter {
    // 是否为管理员添加
    private boolean adminAdd;
    // apk 链接
    private String apkLink;
    // 审核状态，1表示已审核
    private int audit;

    private final int number;
    // 作者
    private String author;
    // 是否可编辑
    private boolean canEdit;
    // 章节ID
    private int chapterId;
    // 章节名称
    private String chapterName;
    // 是否收藏
    private boolean collect;
    // 课程ID
    private int courseId;
    // 描述
    private String desc;
    // markdown 格式的描述
    private String descMd;
    // 封面图链接
    private String envelopePic;
    // 是否为新内容
    private boolean fresh;
    // 主机地址
    private String host;
    // 数据ID
    private int id;
    // 是否为管理员添加（同adminAdd，可能为冗余字段）
    private boolean isAdminAdd;
    // 文章链接
    private String link;
    // 格式化后的发布时间
    private String niceDate;
    // 格式化后的分享时间
    private String niceShareDate;
    // 来源
    private String origin;
    // 前缀信息
    private String prefix;
    // 项目链接
    private String projectLink;
    // 发布时间（时间戳）
    private long publishTime;
    // 实际的超级章节ID
    private int realSuperChapterId;
    // 自我可见性
    private int selfVisible;
    // 分享时间（时间戳）
    private long shareDate;
    // 分享用户
    private String shareUser;
    // 超级章节ID
    private int superChapterId;
    // 超级章节名称
    private String superChapterName;
    // 标签列表
    private Object[] tags;
    // 标题
    private String title;
    // 类型
    private int type;
    // 用户ID
    private int userId;
    // 可见性
    private int visible;
    // 点赞数
    private int zan;

    // getter 和 setter 方法
    public boolean isAdminAdd() {
        return adminAdd;
    }

    public void setAdminAdd(boolean adminAdd) {
        this.adminAdd = adminAdd;
    }

    public TutorialChapter(int number, String title, String link) {
        this.number= number;
        this.title = title;
        this.link = link;
    }
    public int getNumber(){
        return number;
    }

    public String getApkLink() {
        return apkLink;
    }

    public void setApkLink(String apkLink) {
        this.apkLink = apkLink;
    }

    public int getAudit() {
        return audit;
    }

    public void setAudit(int audit) {
        this.audit = audit;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public boolean isCollect() {
        return collect;
    }

    public void setCollect(boolean collect) {
        this.collect = collect;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDescMd() {
        return descMd;
    }

    public void setDescMd(String descMd) {
        this.descMd = descMd;
    }

    public String getEnvelopePic() {
        return envelopePic;
    }

    public void setEnvelopePic(String envelopePic) {
        this.envelopePic = envelopePic;
    }

    public boolean isFresh() {
        return fresh;
    }

    public void setFresh(boolean fresh) {
        this.fresh = fresh;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getNiceDate() {
        return niceDate;
    }

    public void setNiceDate(String niceDate) {
        this.niceDate = niceDate;
    }

    public String getNiceShareDate() {
        return niceShareDate;
    }

    public void setNiceShareDate(String niceShareDate) {
        this.niceShareDate = niceShareDate;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getProjectLink() {
        return projectLink;
    }

    public void setProjectLink(String projectLink) {
        this.projectLink = projectLink;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }

    public int getRealSuperChapterId() {
        return realSuperChapterId;
    }

    public void setRealSuperChapterId(int realSuperChapterId) {
        this.realSuperChapterId = realSuperChapterId;
    }

    public int getSelfVisible() {
        return selfVisible;
    }

    public void setSelfVisible(int selfVisible) {
        this.selfVisible = selfVisible;
    }

    public long getShareDate() {
        return shareDate;
    }

    public void setShareDate(long shareDate) {
        this.shareDate = shareDate;
    }

    public String getShareUser() {
        return shareUser;
    }

    public void setShareUser(String shareUser) {
        this.shareUser = shareUser;
    }

    public int getSuperChapterId() {
        return superChapterId;
    }

    public void setSuperChapterId(int superChapterId) {
        this.superChapterId = superChapterId;
    }

    public String getSuperChapterName() {
        return superChapterName;
    }

    public void setSuperChapterName(String superChapterName) {
        this.superChapterName = superChapterName;
    }

    public Object[] getTags() {
        return tags;
    }

    public void setTags(Object[] tags) {
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getVisible() {
        return visible;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }

    public int getZan() {
        return zan;
    }

    public void setZan(int zan) {
        this.zan = zan;
    }
}