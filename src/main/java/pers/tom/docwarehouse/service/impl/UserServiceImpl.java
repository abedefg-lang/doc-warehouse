package pers.tom.docwarehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pers.tom.docwarehouse.exception.ServiceException;
import pers.tom.docwarehouse.listener.OperationLogEvent;
import pers.tom.docwarehouse.mapper.UserMapper;
import pers.tom.docwarehouse.model.dto.UserDto;
import pers.tom.docwarehouse.model.entity.OperationLog;
import pers.tom.docwarehouse.model.entity.User;
import pers.tom.docwarehouse.model.param.LoginParam;
import pers.tom.docwarehouse.service.UserService;

/**
 * @author lijia
 * @description user service impl
 * @date 2021-01-29 13:34
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    private final ApplicationEventPublisher applicationEventPublisher;

    public UserServiceImpl(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public UserDto login(LoginParam loginParam) {

        //先通过用户名查询出数据
        User user = baseMapper.selectByUsername(loginParam.getUsername());
        if(user == null || !loginParam.getPassword().equals(user.getPassword())){
            throw new ServiceException("账号或用户名错误");
        }

        //修改登录时间
        user.setLastLoginTime(System.currentTimeMillis());

        UserDto userDto = converterTo(user);

        //发布登录日志
        OperationLog operationLog = new OperationLog("login", "登录系统");
        operationLog.setOperator(loginParam.getUsername());
        applicationEventPublisher.publishEvent(new OperationLogEvent(operationLog));
        return userDto;
    }

    @Override
    public void logout() {

        //发布登出日志
        applicationEventPublisher.publishEvent(new OperationLogEvent(new OperationLog("logout", "登出系统")));
    }

    @Override
    public UserDto converterTo(User user) {
        if(user != null){
            UserDto userDto = new UserDto();
            userDto.converterFrom(user);
            return userDto;
        }
        return null;
    }
}