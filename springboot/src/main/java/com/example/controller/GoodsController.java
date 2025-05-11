package com.example.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.example.common.Result;
import com.example.entity.Goods;
import com.example.mapper.GoodsMapper;
import com.example.service.GoodsService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.Servlet;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品信息前端操作接口
 **/
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private GoodsService goodsService;

    @Resource
    private GoodsMapper goodsMapper;
    @Autowired
    private Servlet servlet;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Goods goods) {
        goodsService.add(goods);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        goodsService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        goodsService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Goods goods) {
        goodsService.updateById(goods);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Goods goods = goodsService.selectById(id);
        return Result.success(goods);
    }
    @PostMapping("/selectByIds")
    public Result selectByIds(@RequestBody List<Integer> ids) {
        List<Goods> goodsList = ids.stream()
                .map(id -> goodsService.selectById(id))
                .collect(Collectors.toList());
        return Result.success(goodsList);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Goods goods ) {
        List<Goods> list = goodsService.selectAll(goods);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Goods goods,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Goods> page = goodsService.selectPage(goods, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 批量导出
     */
    @GetMapping("/export")
    public void exportData( @RequestParam(required = false) String ids,  // 接收前端传递的 ids
                            HttpServletResponse response)throws IOException {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        List<Goods> list;

        if (StrUtil.isBlank(ids)) {  // 无 ids 时导出全部
            list = goodsService.selectAll(new Goods());
        } else {  // 有 ids 时导出选中数据
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            list = goodsService.selectByIds(idList);  // 确保已有此方法
        }

        writer.write(list, true);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode("商品信息表","UTF-8") +".xlsx");
        ServletOutputStream out = response.getOutputStream();
        writer.flush(out,true);
        out.flush();
        out.close();
    }

//    private void generateExcel(HttpServletResponse response, List<Goods> list)  throws IOException{
//        ExcelWriter writer = ExcelUtil.getWriter(true);
//        writer.addHeaderAlias("id", "商品ID");
//        writer.addHeaderAlias("name", "商品名称");
//        writer.addHeaderAlias("businessName", "商家名称");
//        writer.addHeaderAlias("categoryName", "分类名称");
//        writer.write(list, true);
//
//        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//        response.setHeader("Content-Disposition", "attachment; filename=goods_export.xlsx");
//
//        ServletOutputStream out = response.getOutputStream();
//        writer.flush(out, true);
//        out.close();
//        writer.close();
//    }


}