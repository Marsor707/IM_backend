package com.marsor.web.im_android.push.service;

import com.google.common.base.Strings;
import com.marsor.web.im_android.push.bean.api.account.AccountRspModel;
import com.marsor.web.im_android.push.bean.api.account.LoginModel;
import com.marsor.web.im_android.push.bean.api.account.RegisterModel;
import com.marsor.web.im_android.push.bean.api.base.ResponseModel;
import com.marsor.web.im_android.push.bean.db.User;
import com.marsor.web.im_android.push.factory.UserFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by marsor on 2017/5/14.
 */
//注册路径127.0.0.1/api/account/...
@Path("/account")
public class AccountService extends BaseService {
    //登陆
    //添加post请求
    @POST
    @Path("/login")
    //指定请求和返回的响应体为json
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<AccountRspModel> login(LoginModel model) {
        if (!LoginModel.check(model)) {
            //返回参数异常
            return ResponseModel.buildParameterError();
        }
        User user = UserFactory.login(model.getAccount(), model.getPassword());
        if (user != null) {
            //如果有携带pushId
            if (!Strings.isNullOrEmpty(model.getPushId())) {
                return bind(user, model.getPushId());
            }
            //返回当前的账户
            AccountRspModel rspModel = new AccountRspModel(user);
            return ResponseModel.buildOk(rspModel);
        } else {
            //登陆失败
            return ResponseModel.buildLoginError();
        }
    }

    //注册
    //添加post请求
    @POST
    @Path("/register")
    //指定请求和返回的响应体为json
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<AccountRspModel> register(RegisterModel model) {
        if (!RegisterModel.check(model)) {
            //返回参数异常
            return ResponseModel.buildParameterError();
        }
        User user = UserFactory.findByPhone(model.getAccount().trim());
        if (user != null) {
            //已有账户
            return ResponseModel.buildHaveAccountError();
        }
        user = UserFactory.findByName(model.getName().trim());
        if (user != null) {
            //已有用户名
            return ResponseModel.buildHaveNameError();
        }

        //开始注册逻辑
        user = UserFactory.register(model.getAccount(), model.getPassword(), model.getName());
        if (user != null) {
            //如果有携带pushId
            if (!Strings.isNullOrEmpty(model.getPushId())) {
                return bind(user, model.getPushId());
            }
            //返回当前的账户
            AccountRspModel rspModel = new AccountRspModel(user);
            return ResponseModel.buildOk(rspModel);
        } else {
            //注册异常
            return ResponseModel.buildRegisterError();
        }
    }

    //绑定设备id
    @POST
    @Path("/bind/{pushId}")
    //指定请求和返回的响应体为json
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    //从请求头中获取token字段
    //pushId从url地址中获取
    public ResponseModel<AccountRspModel> bind(@PathParam("pushId") String pushId) {
        if (Strings.isNullOrEmpty(pushId)) {
            //返回参数异常
            return ResponseModel.buildParameterError();
        }
        //拿到用户信息
        // User user = UserFactory.findByToken(token);
        User self = getSelf();
        return bind(self, pushId);
    }

    /**
     * 绑定的操作
     *
     * @param self   自己
     * @param pushId pushId
     * @return User
     */
    private ResponseModel<AccountRspModel> bind(User self, String pushId) {
        //进行设备id绑定操作
        User user = UserFactory.bindPushId(self, pushId);
        if (user == null) {
            //绑定失败则是服务器异常
            return ResponseModel.buildServiceError();
        }
        //返回当前的账户 并且已经绑定了
        AccountRspModel rspModel = new AccountRspModel(user, true);
        return ResponseModel.buildOk(rspModel);
    }
}
