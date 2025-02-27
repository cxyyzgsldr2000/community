package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用于代替session来存储用户信息
 * 1. 实现线程级别的隔离
 * 2. 简化过程调用（省去了传递session参数
 * 3.
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clearUser() {
        users.remove();
    }
}
