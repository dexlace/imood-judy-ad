package com.dexlace.sponsor.service.impl;


import com.dexlace.common.exception.AdException;
import com.dexlace.sponsor.constant.Constants;
import com.dexlace.sponsor.dao.AdUserRepository;
import com.dexlace.sponsor.entity.AdUser;
import com.dexlace.sponsor.service.IUserService;
import com.dexlace.sponsor.utils.CommonUtils;
import com.dexlace.sponsor.vo.user.CreateUserRequest;
import com.dexlace.sponsor.vo.user.CreateUserResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    private final AdUserRepository userRepository;

    @Autowired
    public UserServiceImpl(AdUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 创建用户 注意一些同名创建异常
     * 请求参数错误
     * @param request
     * @return
     * @throws AdException
     */
    @Override
    @Transactional
    public CreateUserResponse createUser(CreateUserRequest request)
            throws AdException {

        // 请求参数校验
        if (!request.validate()) {
            throw new AdException(Constants.ErrorMsg.REQUEST_PARAM_ERROR);
        }

        AdUser oldUser = userRepository.
                findByUsername(request.getUsername());
        // 同名校验
        if (oldUser != null) {
            throw new AdException(Constants.ErrorMsg.SAME_NAME_ERROR);
        }

        // 正式创建
        AdUser newUser = userRepository.save(new AdUser(
                request.getUsername(),
                CommonUtils.md5(request.getUsername())
        ));

        return new CreateUserResponse(
                newUser.getId(), newUser.getUsername(), newUser.getToken(),
                newUser.getCreateTime(), newUser.getUpdateTime()
        );
    }
}
