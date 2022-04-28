package com.ripple.sdk.router.hepler

import java.lang.annotation.Inherited

/**
 * Author： fanyafeng
 * Date： 2020-01-07 17:15
 * Email: fanyafeng@live.cn
 *
 * 基于DMALL框架编写，如有更改请联系yafeng.fan@dmall.com
 * 注解此类的必须要继承View
 */
@MustBeDocumented
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@Inherited
annotation class PageRouterName(
    /**
     * 生成类的全部链接,不能为空
     * 此为以后所有value的key值
     * 勿动
     *
     * @return
     */
    val value: String,
    /**
     * 生成的为pageName
     * 如果为空则默认为全部大写字母
     * 否则取当前注解值
     * @return
     */
    val pageName: String = "",
    /**
     * 是否需要登录，鉴于实际情况需要登录情况居多默认为true
     * 如需登录此注解可空
     * 否则直接设置为false即可
     * @return
     */
    val needLogin: Boolean = true,
    /**
     * 此为note消息，主要是给自己或者同事看，最好加上
     * 此类的作用或者是那个页面等等
     * @return
     */
    val note: String = ""
)