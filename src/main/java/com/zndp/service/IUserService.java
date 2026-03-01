package com.zndp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zndp.dto.LoginFormDTO;
import com.zndp.dto.Result;
import com.zndp.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();
}
