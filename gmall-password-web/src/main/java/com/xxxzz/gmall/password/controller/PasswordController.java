package com.xxxzz.gmall.password.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.xxxzz.gmall.UserInfo;
import com.xxxzz.gmall.password.util.JwtUtil;
import com.xxxzz.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PasswordController {

    @Value("${token.key}")
    String signKey;

    @Reference
    UserService userService;

    @RequestMapping("index")
    public String index(HttpServletRequest httpServletRequest){
        //originUrl是从哪里点击的登录url
        String originUrl = httpServletRequest.getParameter("originUrl");
        httpServletRequest.setAttribute("originUrl",originUrl);
        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo,HttpServletRequest request){
        // 取得ip地址 nginx有配置
        String remoteAddr  = request.getHeader("X-forwarded-for");

        UserInfo info = userService.login(userInfo);
        if(info != null){
            // 生成token
            Map map = new HashMap();
            map.put("userId", info.getId());
            map.put("nickName", info.getNickName());
            String token = JwtUtil.encode(signKey, map, remoteAddr);
            System.out.println("token:"+ token);
            return token;
        }else{
            return "fail";
        }
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        String token = request.getParameter("token");
        String currentIp = request.getParameter("currentIp");
        // 检查token
        // Map<String, Object> map = JwtUtil.decode(token, signKey, currentIp);
        Map<String, Object> map = JwtUtil.decode(token, signKey, currentIp);
        if (map!=null){
            // 检查redis信息
            String userId = (String) map.get("userId");
            //去缓存中查询是否有token
            UserInfo userInfo = userService.verify(userId);
            if (userInfo!=null){
                return "success";
            }
        }
        return "fail";
    }
}
