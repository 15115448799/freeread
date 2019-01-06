package com.wooread.wooreaduser.service.impl;

import com.wooread.wooreaduser.dto.BaseServiceOutput;
import com.wooread.wooreaduser.dto.UserServiceInput;
import com.wooread.wooreaduser.model.User;
import com.wooread.wooreaduser.model.UserInfo;
import com.wooread.wooreaduser.service.RoleService;
import com.wooread.wooreaduser.service.UserService;
import cui.shibing.commonrepository.CommonRepository;
import cui.shibing.commonrepository.Specifications;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static com.wooread.wooreaduser.dto.BaseServiceOutput.CODE_FAIL;
import static com.wooread.wooreaduser.dto.BaseServiceOutput.CODE_SUCCESS;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Resource(name = "User")
    private CommonRepository<User, Integer> userCommonRepository;

    @Resource(name = "UserInfo")
    private CommonRepository<UserInfo, Integer> userInfoCommonRepository;

    @Autowired
    private RoleService roleService;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public BaseServiceOutput<User> createUser(UserServiceInput.CreateUserInput input) {
        List<User> users = userCommonRepository.findAll(Specifications.equal("userName", input.getUserName()));
        if (users.size() > CODE_SUCCESS)
            return new BaseServiceOutput<>(CODE_FAIL, "already has user", null);

        BaseServiceOutput<Boolean> validateResult = roleService.isValidRoleId(input.getUserRoleIds());
        BaseServiceOutput<User> roleValidateResult = validateResult.ifSuccess((isValid) -> {
            if (!isValid)
                return new BaseServiceOutput<>(CODE_FAIL, validateResult.getMessage(), null);
            return null;
        });
        if (roleValidateResult != null)
            return roleValidateResult;

        User user = new User();
        BeanUtils.copyProperties(input, user);
        user = userCommonRepository.save(user);

        List<User> userName = userCommonRepository.findAll(Specifications.equal("userName", user.getUserName()));
        if (userName.size() > 1) {// 用户名重复
            // 把这条信息删除
            userCommonRepository.delete(user);
            return new BaseServiceOutput<>(CODE_FAIL, "already has user", null);
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getUserId());
        BeanUtils.copyProperties(input, userInfo);
        userInfoCommonRepository.save(userInfo);
        user.setUserInfoId(userInfo.getUserInfoId());
        userCommonRepository.save(user);
        return new BaseServiceOutput<>(CODE_SUCCESS, "success", user);
    }

    @Override
    public BaseServiceOutput<User> updateUser(UserServiceInput.UpdateUserInput input) {
        return userCommonRepository.findById(input.getUserId()).map(user -> {
            BeanUtils.copyProperties(input, user);
            userCommonRepository.save(user);
            return new BaseServiceOutput<>(CODE_SUCCESS, "success", user);
        }).orElse(new BaseServiceOutput<>(CODE_FAIL, "no such user", null));
    }

    @Override
    public BaseServiceOutput<UserInfo> updateUserInfo(UserServiceInput.UpdateUserInput input) {
        return userCommonRepository.findById(input.getUserId()).map(user -> {
            return userInfoCommonRepository.findById(user.getUserInfoId()).map(userInfo -> {
                BeanUtils.copyProperties(input, userInfo);
                userInfoCommonRepository.save(userInfo);
                return new BaseServiceOutput<>(CODE_SUCCESS, "success", userInfo);
            }).orElse(new BaseServiceOutput<>(CODE_FAIL, "no such user Info", null));
        }).orElse(new BaseServiceOutput<>(CODE_FAIL, "no such user", null));
    }

    @Override
    public BaseServiceOutput<List<User>> findUserLikeName(String userName) {
        List<User> users = userCommonRepository.findAll(Specifications.like("userName", "%" + userName + "%"));
        return new BaseServiceOutput<>(CODE_SUCCESS, "success", users);
    }

    @Override
    public BaseServiceOutput<User> findUserByName(String userName) {
        Optional<User> user = userCommonRepository.findOne(Specifications.equal("userName", userName));
        return new BaseServiceOutput<>(CODE_SUCCESS, "success", user.get());
    }

    @Override
    public BaseServiceOutput<UserInfo> findUserInfo(Integer userId) {
        Optional<UserInfo> userInfo = userInfoCommonRepository.findOne(Specifications.equal("userId", userId));
        return new BaseServiceOutput<>(CODE_SUCCESS, "success", userInfo.get());
    }
}
