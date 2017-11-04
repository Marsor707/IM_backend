package com.marsor.web.im_android.push.service;

import com.google.common.base.Strings;
import com.marsor.web.im_android.push.bean.api.base.PushModel;
import com.marsor.web.im_android.push.bean.api.base.ResponseModel;
import com.marsor.web.im_android.push.bean.api.user.UpdateInfoModel;
import com.marsor.web.im_android.push.bean.card.UserCard;
import com.marsor.web.im_android.push.bean.db.User;
import com.marsor.web.im_android.push.factory.PushFactory;
import com.marsor.web.im_android.push.factory.UserFactory;
import com.marsor.web.im_android.push.utils.PushDispatcher;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息处理
 * Created by marsor on 2017/5/27.
 */
//127.0.0.1/api/user
@Path("/user")
public class UserService extends BaseService {

    //用户信息修改接口
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> update(UpdateInfoModel model) {
        if (!UpdateInfoModel.check(model)) {
            //返回参数异常
            return ResponseModel.buildParameterError();
        }

        User self = getSelf();
        //更新用户信息
        self = model.updateToUser(self);
        self = UserFactory.update(self);
        //构架自己的用户信息
        UserCard card = new UserCard(self, true);
        return ResponseModel.buildOk(card);
    }

    //拉取联系人
    @GET
    @Path("/contact")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<UserCard>> contact() {
        User self = getSelf();
        //拿到我的联系人
        List<User> users = UserFactory.contacts(self);
        List<UserCard> userCards = users.stream()
                //map操作，相当于转置操作 User->UserCard
                .map(user -> new UserCard(user, true))
                .collect(Collectors.toList());
        return ResponseModel.buildOk(userCards);
    }

    //关注人
    //简化为 关注为双方同时关注
    @PUT//修改类使用put
    @Path("/follow/{followId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> follow(@PathParam("followId") String followId) {
        User self = getSelf();
        //不能关注我自己
        if (self.getId().equalsIgnoreCase(followId) || Strings.isNullOrEmpty(followId)) {
            //返回参数异常
            return ResponseModel.buildParameterError();
        }
        //找到我要关注的人
        User followUser = UserFactory.findById(followId);
        if (followUser == null) {
            //未找到
            return ResponseModel.buildNotFoundUserError(null);
        }
        //备注默认没有 后面可以添加
        followUser = UserFactory.follow(self, followUser, null);
        if (followUser == null) {
            //关注失败 返回服务器异常
            return ResponseModel.buildServiceError();
        }
        // 通知我关注的人我关注了他
        //给他发送一个我的信息过去
        PushFactory.pushFollow(followUser,new UserCard(self));

        //返回关注人的信息
        return ResponseModel.buildOk(new UserCard(followUser, true));
    }

    //获取某人的信息
    @GET
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> getUser(@PathParam("id") String id) {
        if (Strings.isNullOrEmpty(id)) {
            return ResponseModel.buildParameterError();
        }
        User self = getSelf();
        if (self.getId().equalsIgnoreCase(id)) {
            //返回自己 不必查询数据库
            return ResponseModel.buildOk(new UserCard(self, true));
        }
        User user = UserFactory.findById(id);
        if (user == null) {
            //没找到
            return ResponseModel.buildNotFoundUserError(null);
        }
        //如果有关注记录 则我已关注查询的用户
        boolean isFollow = UserFactory.getUserFollow(self, user) != null;
        return ResponseModel.buildOk(new UserCard(user, isFollow));
    }

    //搜索人的接口实现
    //为了简化分页 每次只返回20条数据
    @GET
    @Path("/search/{name:(.*)?}")//名字为任意字符 可以为空 正则表达式
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<UserCard>> search(@DefaultValue("") @PathParam("name") String name) {
        User self = getSelf();
        //先查询数据
        List<User> searchUsers = UserFactory.search(name);
        //把查询的人封装为UserCard
        //判断这些人是否有我已经关注的
        // 如果有则返回关注状态中应设置好状态
        //拿出我的联系人
        final List<User> contacts = UserFactory.contacts(self);
        List<UserCard> userCards = searchUsers.stream()
                .map(user -> {
                    //判断这个人是否是我自己 或在我的联系人中
                    boolean isFollow = user.getId().equalsIgnoreCase(self.getId()) ||
                            //进行联系人的任意id匹配
                            contacts.stream().anyMatch(contactUser -> contactUser.getId().equalsIgnoreCase(user.getId()));
                    return new UserCard(user, isFollow);
                }).collect(Collectors.toList());
        return ResponseModel.buildOk(userCards);
    }
}
