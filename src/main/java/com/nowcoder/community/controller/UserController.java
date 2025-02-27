package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    @Value("${community.path.domain}")
    private String domain;
    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if(headerImage == null) {
            model.addAttribute("error","请先上传文件");
            return "/site/setting";
        }
        // 校验文件名后缀
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
//        System.out.println(suffix);
        if(StringUtils.isBlank(suffix)) {
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }
        // 存储文件
        fileName = CommunityUtil.generateUUID() + "." + suffix;
//        System.out.println(fileName);
        String headerPath = uploadPath + "/" + fileName;
//        System.out.println(headerPath);
        File dest = new File(headerPath);
        try{
            System.out.println(dest.getAbsolutePath());
            // 存储
            /**
             headerImage.transferTo(dest);   !!!bug
             *
             */
            headerImage.transferTo(dest);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        // 更新user头像路径
        // http://localhost:8081/community/user/header/xxx.jpg
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header" + fileName;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index" ;
    }


    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        String headerPath = uploadPath + "/" + fileName;
        //
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        response.setContentType("image/" + suffix);
        // 读文件
        try {
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(headerPath);
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
            fis.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @LoginRequired
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if(user == null) {
            throw new RuntimeException("用户不存在");
        }
        else {
            model.addAttribute("user", user);
            // 点赞数量
            long likeCount = likeService.findUserLikeCount(userId);
            model.addAttribute("likeCount", likeCount);
            // 关注用户数量
            long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
            model.addAttribute("followeeCount", followeeCount);
            // 粉丝数量
            long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
            model.addAttribute("followerCount", followerCount);
            // 查询是否已关注
            boolean hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
            model.addAttribute("hasFollowed", hasFollowed);
        }
        return "/site/profile";
    }
}
