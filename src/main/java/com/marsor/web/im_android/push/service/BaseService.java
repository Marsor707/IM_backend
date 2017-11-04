package com.marsor.web.im_android.push.service;

import com.marsor.web.im_android.push.bean.db.User;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by marsor on 2017/5/27.
 */
public class BaseService {
    //添加一个上下文注解 该注解会给securityContext赋值
    //具体的值为拦截器中所返回的SecurityContext
    @Context
    protected SecurityContext securityContext;

    /**
     * 从上下文中直接获取自己的信息
     * @return User
     */
    protected User getSelf(){
        return (User) securityContext.getUserPrincipal();
    }
}
