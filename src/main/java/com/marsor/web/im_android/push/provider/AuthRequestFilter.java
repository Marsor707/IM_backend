package com.marsor.web.im_android.push.provider;

import com.google.common.base.Strings;
import com.marsor.web.im_android.push.bean.api.base.ResponseModel;
import com.marsor.web.im_android.push.bean.db.User;
import com.marsor.web.im_android.push.factory.UserFactory;
import org.glassfish.jersey.server.ContainerRequest;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;

/**
 * 用于所有的请求的过滤和拦截
 * Created by marsor on 2017/5/27.
 */
@Provider
public class AuthRequestFilter implements ContainerRequestFilter{
    //接口的过滤方法
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        //检查是否是登录注册接口
        String relationPath=((ContainerRequest)requestContext).getPath(false);
        if(relationPath.startsWith("account/login")||relationPath.startsWith("account/register")){
            //直接走正常逻辑 不拦截
            return;
        }
        //从Headers中去找到第一个token节点
        String token=requestContext.getHeaders().getFirst("token");
        if(!Strings.isNullOrEmpty(token)){
            final User self= UserFactory.findByToken(token);
            if(self!=null){
                //给当前请求添加一个上下文
                requestContext.setSecurityContext(new SecurityContext() {
                    //主体部分
                    @Override
                    public Principal getUserPrincipal() {
                        //User 实现 Principal 接口
                        return self;
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        //可以在这里写入权限 role是权限名
                        // 可以管理管理员权限等等
                        return true;
                    }

                    @Override
                    public boolean isSecure() {
                        //默认false HTTPS
                        return false;
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        //不用理会
                        return null;
                    }
                });
                //写入上下文后就返回
                return;
            }
        }
        //直接返回一个账户需要登录的Model
        ResponseModel model=ResponseModel.buildAccountError();
        //构建一个返回
        Response response=Response.status(Response.Status.OK).entity(model).build();
        //停止一个请求的继续下发，调用该方法后返回请求
        //不会走到service中
        requestContext.abortWith(response);
    }
}
