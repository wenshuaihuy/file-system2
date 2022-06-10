package cn.org.xinke.controller;

import cn.org.xinke.annotation.Login;
import cn.org.xinke.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

/**
 * @author wsh
 * @date 2022/6/10 8:31 下午
 */
@Controller
@Slf4j
public class BaseController {

    @Value("${admin.uname}")
    private String uname;

    @Value("${admin.pwd}")
    private String pwd;


    /**
     * 登录页
     *
     * @return
     */
    @RequestMapping("/login")
    public String loginPage() {
        return "login.html";
    }

    /**
     * 登录提交认证
     *
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/auth")
    public String auth(User user, HttpSession session) {
        if (user.getUname().equals(uname) && user.getPwd().equals(pwd)) {
            session.setAttribute("LOGIN_USER", user);
            return "redirect:/";
        }
        return "redirect:/login";
    }

    /**
     * 首页
     *
     * @return
     */
    @Login
    @RequestMapping("/")
    public String index() {
        return "index.html";
    }




}
