package com.xxxzz.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.xxxzz.gmall.UserAddress;
import com.xxxzz.gmall.UserInfo;
import com.xxxzz.gmall.user.mapper.UserAddressMapper;
import com.xxxzz.gmall.user.mapper.UserInfoMapper;
import com.xxxzz.gmall.util.RedisUtil;
import com.xxxzz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;

    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setId(userId);
        return userAddressMapper.select(userAddress);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        //对密码进行md5加密
        String password = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        userInfo.setPasswd(password);
        //查询数据库
        UserInfo info = userInfoMapper.selectOne(userInfo);

        if(info!=null){
            Jedis jedis = redisUtil.getJedis();
            jedis.setex(userKey_prefix + info.getId() + userinfoKey_suffix,userKey_timeOut, JSON.toJSONString(info));
            jedis.close();
            return info;
        }
        return null;
    }

    @Override
    public UserInfo verify(String userId) {
        // 去缓存中查询是否有token
        Jedis jedis = redisUtil.getJedis();
        String key = userKey_prefix+userId+userinfoKey_suffix;
        String userJson = jedis.get(key);
        // 延长时效
        jedis.expire(key,userKey_timeOut);
        if (userJson!=null){
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return  userInfo;
        }
        return  null;
    }
}
