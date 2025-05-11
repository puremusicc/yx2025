package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Business;
import com.example.exception.CustomException;
import com.example.mapper.BusinessMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;


@Service
//商家相关的业务方法
public class BusinessService {

    @Resource
    private BusinessMapper businessMapper;

    public List<Business> selectAll(Business business) {

        return businessMapper.selectAll(business);
    }

    //新增用户
    public  void add(Business business) {
        Business dbBusiness=  this.selectByUsername(business.getUsername());
        //若根据新增数据账号查询到数据，账号不能重复，不允许插入
        if(ObjectUtil.isNotEmpty(dbBusiness)){
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        business.setRole(RoleEnum.BUSINESS.name());//设置好角色
        businessMapper.insert(business);

    }


    public void updateById(Business business) {
        Business dbBusiness1=  this.selectById(business.getId());
        Business dbBusiness2=  this.selectByUsername(business.getUsername());//根据商家名称查询数据库已有的商家
        //id查询到商家不存在，无法修改
        if(ObjectUtil.isEmpty(dbBusiness1)){
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);//商家不存在
        }
        //若根据名称查询到商家所有数据，且该用户名不属于当前正在修改的商家（即其他商家占用了此用户名)
        if(ObjectUtil.isNotEmpty(dbBusiness2)&& !Objects.equals(dbBusiness2.getId(),business.getId())){
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        businessMapper.updateById(business);
    }

    //根据商家名称查询
    public Business selectByUsername(String username){
        Business params=new Business();
        params.setUsername(username);
        List<Business> list=this.selectAll(params);
        return list.isEmpty() ? null : list.get(0);
    }
    //根据商家id查询
    public Business selectById(Integer id){
        Business params=new Business();
        params.setId(id);
        List<Business> list=this.selectAll(params);
        return list.isEmpty() ? null : list.get(0);
    }


    public void deleteById(Integer id) {
        businessMapper.deleteById(id);
    }

//    批量删除
    public void deleteBatch(List<Integer> ids) {
        for(Integer id:ids){
            this.deleteById(id);
        }
    }

    /**
     * 分页查询  startPage是分页插件
     */
    public PageInfo<Business> selectPage(Business business, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Business> list = businessMapper.selectAll(business);
        return PageInfo.of(list);
    }

    //商家注册
    public void register(Account account) {
        Business business = new Business();
        BeanUtils.copyProperties(account, business);//把account信息拷贝到business中
        if (ObjectUtil.isEmpty(account.getName())) {
            business.setName(business.getUsername());
        }
        this.add(business);
    }

    //商家登录
    public Account login(Account account) {

        Account dbBusiness= this.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbBusiness)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!account.getPassword().equals(dbBusiness.getPassword())) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);  //比较密码是否一致
        }
        // 生成token
        String tokenData = dbBusiness.getId() + "-" + RoleEnum.BUSINESS.name();
        String token = TokenUtils.createToken(tokenData, dbBusiness.getPassword());
        dbBusiness.setToken(token);
        return dbBusiness;
    }

    public void updatePassword(Account account) {
        Business dbBusiness = this.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbBusiness)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!account.getPassword().equals(dbBusiness.getPassword())) {
            throw new CustomException(ResultCodeEnum.PARAM_PASSWORD_ERROR);
        }
        dbBusiness.setPassword(account.getNewPassword());
        this.updateById(dbBusiness);
    }


//     检查商家的权限，看看是否可以新增数据

    public void checkBusinessAuth() {
        Account currentUser = TokenUtils.getCurrentUser();  // 获取当前的用户信息
        if (RoleEnum.BUSINESS.name().equals(currentUser.getRole())) {   // 如果是商家 的话
            Business business = selectById(currentUser.getId());
            if (!"通过".equals(business.getStatus())) {   // 未审核通过的商家  不允许添加数据
                throw new CustomException(ResultCodeEnum.NO_AUTH);
            }
        }
    }

}
