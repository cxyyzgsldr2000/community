package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private EventProducer eventProducer;


    ///
    /// 当前界面以及bootstrap版本的问题，导致调用js不成功，关注相关功能均无法正常实现
    ///
    @LoginRequired
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);
        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId); // 当前版本功能只能关注人
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0,"关注成功");
    }

    @LoginRequired
    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0,"取关成功");
    }

    // 查询某用户关注的人
    @LoginRequired
    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if(user == null) {
            throw new RuntimeException("当前用户不存在");
        }
        model.addAttribute("user", user);
        page.setRows((int)followService.findFolloweeCount(userId,ENTITY_TYPE_USER));
        page.setPath("/followees/" + userId);
        page.setLimit(5);
        List<Map<String, Object>> followees = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (followees != null) {
            for(Map<String, Object> map : followees) {
                User target = (User) map.get("user");
                boolean hasFollowed = hasFollowed(target.getId());
                map.put("hasFollowed", hasFollowed);
            }
        }
        model.addAttribute("users", followees);
        return "/site/followee";
    }

    // 查询某用户的粉丝
    @LoginRequired
    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Model model, Page page) {
        User user = userService.findUserById(userId);
        if(user == null) {
            throw new RuntimeException("当前用户不存在");
        }
        model.addAttribute("user", user);
        page.setRows((int)followService.findFollowerCount(ENTITY_TYPE_USER,userId));
        page.setPath("/followers/" + userId);
        page.setLimit(5);
        List<Map<String, Object>> followers = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if(followers != null) {
            for(Map<String, Object> map : followers) {
                User target = (User) map.get("user");
                boolean hasFollowed = hasFollowed(target.getId());
                map.put("hasFollowed", hasFollowed);
            }
        }
        model.addAttribute("users", followers);
        return "/site/follower";
    }

    // 查看当前登陆用户对目标用户是否关注
    private boolean hasFollowed(int userId) {
        User user = hostHolder.getUser();
        return followService.hasFollowed(user.getId(),ENTITY_TYPE_USER,userId);
    }
}
