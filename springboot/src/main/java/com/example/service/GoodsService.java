package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Category;
import com.example.entity.Goods;
import com.example.mapper.GoodsMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 商品信息业务处理
 **/
@Service
public class GoodsService {

    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private BusinessService businessService;
    @Resource
    private CategoryService categoryService;//用于检查权限



    /**
     * 新增
     */
    public void add(Goods goods) {
        businessService.checkBusinessAuth();    //检查商家权限是否“已通过”
        Category category=categoryService.selectById(goods.getCategoryId());

        if(ObjectUtil.isNotEmpty(category)){
            goods.setBusinessId(category.getBusinessId());
        }

        goodsMapper.insert(goods);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        businessService.checkBusinessAuth();
        goodsMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        businessService.checkBusinessAuth();
        for (Integer id : ids) {
            goodsMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(Goods goods) {

        //当前登录用户信息,如果是商家只能看到自己的商品
        Account currentUser= TokenUtils.getCurrentUser();
        String role=currentUser.getRole();
        if(RoleEnum.BUSINESS.name().equals(role)){
            businessService.checkBusinessAuth();
        }
        Category category=categoryService.selectById(goods.getCategoryId());
        if(ObjectUtil.isNotEmpty(category)){
            goods.setBusinessId(category.getBusinessId());
        }
        goodsMapper.updateById(goods);
    }

    /**
     * 根据ID查询
     */
    public Goods selectById(Integer id) {
        businessService.checkBusinessAuth();
        return goodsMapper.selectById(id);
    }


    public List<Goods> selectByIds(List<Integer> idList) {
        // 1. 权限检查（复用 selectById 中的逻辑）
        businessService.checkBusinessAuth();  // 确保用户有权限操作
        // 2. 批量查询商品（直接通过 Mapper 批量查询，避免循环调用 selectById）
        List<Goods> goodsList = goodsMapper.selectByIds(idList);
        // 3. 返回结果
        return goodsList;
    }
    /**
     * 查询所有
     */
    public List<Goods> selectAll(Goods goods) {

        //当前登录用户信息,如果是商家只能看到自己的商品
        Account currentUser= TokenUtils.getCurrentUser();
        String role=currentUser.getRole();
        if(RoleEnum.BUSINESS.name().equals(role)){
            goods.setBusinessId(currentUser.getId());
        }
        return goodsMapper.selectAll(goods);
    }

    /**
     * 分页查询
     */
    public PageInfo<Goods> selectPage(Goods goods, Integer pageNum, Integer pageSize) {

        //当前登录用户信息,如果是商家只能看到自己的商品
        Account currentUser= TokenUtils.getCurrentUser();
        String role=currentUser.getRole();
        if(RoleEnum.BUSINESS.name().equals(role)){
            goods.setBusinessId(currentUser.getId());
        }else {
            goods.setBusinessId(null);  // 用户和管理员查看所有商品
        }

        PageHelper.startPage(pageNum, pageSize);
        List<Goods> list = goodsMapper.selectAll(goods);
        return PageInfo.of(list);
    }


}