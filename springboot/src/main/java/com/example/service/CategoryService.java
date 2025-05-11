package com.example.service;

import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Category;
import com.example.mapper.CategoryMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
/**
 * 商品分类业务处理
 **/
@Service
public class CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private BusinessService businessService;//用于检查权限

    /**
     * 新增
     */
    public void add(Category category) {

        businessService.checkBusinessAuth();    //检查商家权限是否“已通过”
        Account currentUser= TokenUtils.getCurrentUser();
        if(RoleEnum.BUSINESS.name().equals(currentUser.getRole())){
            category.setBusinessId(currentUser.getId());
        }
        categoryMapper.insert(category);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        categoryMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            categoryMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(Category category) {
        categoryMapper.updateById(category);
    }

    /**
     * 根据ID查询
     */
    public Category selectById(Integer id) {
        return categoryMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Category> selectAll(Category category) {
        //当前登录用户信息
        Account currentUser= TokenUtils.getCurrentUser();
        String role=currentUser.getRole();
        if(RoleEnum.BUSINESS.name().equals(role)){
            category.setBusinessId(currentUser.getId());
        }
        else {
            category.setBusinessId(null);  // 用户和管理员：强制清空businessId，查看所有
        }
        return categoryMapper.selectAll(category);
    }

    /**
     * 分页查询
     */
    public PageInfo<Category> selectPage(Category category, Integer pageNum, Integer pageSize) {
        //当前登录用户信息
        Account currentUser= TokenUtils.getCurrentUser();
        String role=currentUser.getRole();
        if(RoleEnum.BUSINESS.name().equals(role)){
            category.setBusinessId(currentUser.getId());
        }
        else {
            category.setBusinessId(null);  // 用户和管理员：强制清空businessId，查看所有
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Category> list = categoryMapper.selectAll(category);
        return PageInfo.of(list);
    }

}
