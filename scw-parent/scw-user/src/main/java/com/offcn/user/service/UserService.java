package com.offcn.user.service;

import com.offcn.user.po.TMember;
import com.offcn.user.po.TMemberAddress;

import java.util.List;

public interface UserService {
    public void registerUser(TMember member);
    public TMember login(String username,String password);
    //根据用户id 查询用户信息
    public TMember findMemberById(Integer id);
    //获取用户地址
    List<TMemberAddress> findAddressList(Integer memberId);
}
