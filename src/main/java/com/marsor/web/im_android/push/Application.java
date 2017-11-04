package com.marsor.web.im_android.push;

import com.marsor.web.im_android.push.provider.AuthRequestFilter;
import com.marsor.web.im_android.push.provider.GsonProvider;
import com.marsor.web.im_android.push.service.AccountService;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.logging.Logger;

/**
 * Created by marsor on 2017/5/14.
 */
public class Application extends ResourceConfig{
    public Application(){
        //注册package
        packages(AccountService.class.getPackage().getName());
        //注册全局请求拦截器
        register(AuthRequestFilter.class);
        //注册json解析器
        //register(JacksonJsonProvider.class);
        //替换解析器为Gson
        register(GsonProvider.class);
        //注册日志打印输出
        register(Logger.class);
    }
}
