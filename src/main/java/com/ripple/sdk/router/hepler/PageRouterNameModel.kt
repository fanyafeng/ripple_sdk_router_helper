package com.ripple.sdk.router.hepler

/**
 * Author： fanyafeng
 * Date： 2020-01-07 18:02
 * Email: fanyafeng@live.cn
 */
class PageRouterNameModel {
    /**
     * 包名加类名全称
     * 用来通过反射生成类
     * 别名：pageClassName
     */
    var pageClassName: String? = null

    /**
     * 自定义的pageName
     */
    var pageName: String? = null

    /**
     * 跳转名称
     * 正常的类名字
     */
    var forwardName: String? = null

    /**
     * 是否需要登录
     */
    var isNeedLogin = false

    /**
     * 生成类字段上的注释
     */
    var note: String? = null

    constructor() {}
    constructor(
        pageClassName: String?,
        pageName: String?,
        forwardName: String?,
        needLogin: Boolean,
        note: String?
    ) {
        this.pageClassName = pageClassName
        this.pageName = pageName
        this.forwardName = forwardName
        this.isNeedLogin = needLogin
        this.note = note
    }

    override fun toString(): String {
        return "PageNameData{" +
                "pageClassName='" + pageClassName + '\'' +
                ", pageName='" + pageName + '\'' +
                ", forwardName='" + forwardName + '\'' +
                ", needLogin=" + isNeedLogin +
                ", note='" + note + '\'' +
                '}'
    }
}