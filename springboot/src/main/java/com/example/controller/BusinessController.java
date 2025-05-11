package com.example.controller;
//商家管理接口


import cn.hutool.core.util.ObjectUtil;
import com.example.common.Result;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.Business;
import com.example.service.BusinessService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController

@RequestMapping("/business")
public class BusinessController {

    @Resource
    private BusinessService businessService;

    //查询所有商家
    @GetMapping("/selectAll")
    public Result selectAll(@PathVariable Integer id) {
        Business business=businessService.selectById(id);
            return Result.success(business);
    }

    //查询所有商家
    @GetMapping("/selectById/{id}")
    public Result selectById(Business business) {
        List<Business> list=businessService.selectAll(business);
        return Result.success(list);
    }

    //分页查询
    @GetMapping("/selectPage")
    public Result selectPage(Business business
    , @RequestParam (defaultValue = "1")Integer pageNum
    , @RequestParam (defaultValue = "10")Integer pageSize) {

        PageInfo<Business> pageInfo =businessService.selectPage(business,pageNum,pageSize);
        return Result.success(pageInfo);
    }


    //新增商家
    @PostMapping("/add")
    public Result add(@RequestBody Business business){
        //校验
        if(ObjectUtil.isEmpty(business.getUsername())||ObjectUtil.isEmpty(business.getPassword())){
            return Result.error(ResultCodeEnum.PARAM_LOST_ERROR);
        }

        businessService.add(business);
        return Result.success();

    }

    //修改商家信息
    @PutMapping("/update")
    public Result update(@RequestBody Business business){

        businessService.updateById(business);
        return Result.success();
    }

    //删除商家信息
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){

        businessService.deleteById(id);
        return Result.success();
    }

    //批量删除商家信息
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids){

        businessService.deleteBatch(ids);
        return Result.success();
    }



}
