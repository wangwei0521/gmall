package com.xxxzz.gmall.config;

import com.alibaba.fastjson.JSON;
import com.xxxzz.gmall.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getParameter("newToken");
        //把token保存到cookie
        if(token!=null){
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }
        if(token==null){
            token = CookieUtil.getCookieValue(request, "token", false);
        }

        if(token!=null) {
            //读取token
            Map map = getUserMapByToken(token);
            String nickName = (String) map.get("nickName");
            request.setAttribute("nickName", nickName);
        }
        //在拦截器中获取方法上的注解
        HandlerMethod handlerMethod =(HandlerMethod) handler;
        //获取方法上的注解LoginRequire
        LoginRequire loginRequireAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if(loginRequireAnnotation!=null){
            //获取ip
            String remoteAddr = request.getHeader("x-forwarded-for");
            //校验是否认证成功
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&currentIp=" + remoteAddr);
            if("success".equals(result)){//认证成功
                Map map = getUserMapByToken(token);
                String userId =(String) map.get("userId");
                request.setAttribute("userId",userId);
                return true;
            }else{//认证失败
                if(loginRequireAnnotation.autoRedirect()){
                    //必须登录！跳转到页面
                    //先获取到url
                    String  requestURL = request.getRequestURL().toString();
                    System.out.println("requestURL:"+requestURL);
                    //将url进行转换
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                    System.out.println("encodeURL:"+encodeURL);
                    //重定向到指定url
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 进行解密token，获取map
     * @param token
     * @return
     */
    private  Map getUserMapByToken(String  token){
        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] tokenBytes = base64UrlCodec.decode(tokenUserInfo);
        String tokenJson = null;
        try {
            tokenJson = new String(tokenBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map map = JSON.parseObject(tokenJson, Map.class);
        return map;
    }
}
