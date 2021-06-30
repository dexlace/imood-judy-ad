package com.dexlace.sponsor.service;

import com.dexlace.common.exception.AdException;
import com.dexlace.sponsor.vo.user.CreateUserRequest;
import com.dexlace.sponsor.vo.user.CreateUserResponse;


public interface IUserService {

    /**
     * <h2>创建用户</h2>
     * */
    CreateUserResponse createUser(CreateUserRequest request)
            throws AdException;
}
