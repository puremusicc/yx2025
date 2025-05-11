package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;


/**
 * 用户表业务处理
 **/
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 新增
     */
    public void add(User user) {
        User dbUser = userMapper.selectByUsername(user.getUsername());
        if (ObjectUtil.isNotNull(dbUser)) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        if (ObjectUtil.isEmpty(user.getName())) {
            user.setName(user.getUsername());
        }
        user.setRole(RoleEnum.USER.name());
        userMapper.insert(user);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            userMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(User user) {
        User dbUser2 = userMapper.selectByUsername(user.getUsername());
        //  根据当前更新的用户的账号查询数据库  如果数据库存在跟当前更新用户一样账号的数据  那么当前的更新是不合法的  数据重复了
        if (ObjectUtil.isNotEmpty(dbUser2) && !Objects.equals(dbUser2.getId(), user.getId())) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        userMapper.updateById(user);
    }

    /**
     * 根据ID查询
     */
    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<User> selectAll(User user) {
        return userMapper.selectAll(user);
    }

    /**
     * 分页查询
     */
    public PageInfo<User> selectPage(User user, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userMapper.selectAll(user);
        return PageInfo.of(list);
    }

    //用户注册
    public void register(Account account) {
        User user= new User();
        BeanUtils.copyProperties(account, user);//把account信息拷贝到business中
        if (ObjectUtil.isEmpty(account.getName())) {
            user.setName(user.getUsername());
        }
        this.add(user);
    }

//    //用户登录
//    public Account login(Account account) {
//
//        Account dbUser= this.selectByUsername(account.getUsername());
//        if (ObjectUtil.isNull(dbUser)) {
//            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
//        }
//        if (!account.getPassword().equals(dbUser.getPassword())) {
//            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);  //比较密码是否一致
//        }
//        // 生成token
//        String tokenData = dbUser.getId() + "-" + RoleEnum.USER.name();
//        String token = TokenUtils.createToken(tokenData, dbUser.getPassword());
//        dbUser.setToken(token);
//
//        Map<String, String> ip = IpAddressUtil.getIp();
//        // 调用用户修改的方法，修改一下登录的最后时间，和登录的最后IP地址归属
//        // 查看IP是否为空
//        if (ip == null) {
//            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);// 未知错误
//        }
//        // 修改最后登录的时间和IP
//        userMapper.updateLoginIp(ip.get("pro"),account.getUsername());
//
//
//        return dbUser;
//    }

    //用户登录
    public Account login(Account account) {

        Account dbUser= this.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);  //比较密码是否一致
        }
        // 生成token
        String tokenData = dbUser.getId() + "-" + RoleEnum.USER.name();
        String token = TokenUtils.createToken(tokenData, dbUser.getPassword());
        dbUser.setToken(token);
        return dbUser;
    }


    //根据用户名称查询
    public User selectByUsername(String username){
        User params=new User();
        params.setUsername(username);
        List<User> list=this.selectAll(params);
        return list.isEmpty() ? null : list.get(0);
    }

    public void updatePassword(Account account) {
        User dbUser = this.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException(ResultCodeEnum.PARAM_PASSWORD_ERROR);
        }
        dbUser.setPassword(account.getNewPassword());
        this.updateById(dbUser);
    }

}
