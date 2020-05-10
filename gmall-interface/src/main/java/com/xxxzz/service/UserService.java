package com.xxxzz.service;

import com.xxxzz.gmall.UserAddress;
import com.xxxzz.gmall.UserInfo;

import java.util.List;

public interface UserService {
    List<UserInfo> findAll();
    public List<UserAddress> getUserAddressList(String userId);

    UserInfo login(UserInfo userInfo);

    UserInfo verify(String userId);
}
