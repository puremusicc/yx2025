package com.example.service;

import com.example.common.enums.RoleEnum;
import com.example.dto.request.OrderDTO;
import com.example.dto.request.OrderItemDTO;
import com.example.entity.Account;
import com.example.entity.Goods;
import com.example.entity.Orders;
import com.example.entity.OrdersItem;
import com.example.mapper.GoodsMapper;
import com.example.mapper.OrdersItemMapper;
import com.example.mapper.OrdersMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 业务处理
 **/
@Service
public class OrdersService {

    private static final Logger log = LoggerFactory.getLogger(OrdersService.class);

    @Resource
    private OrdersMapper ordersMapper;

    @Resource
    private OrdersItemMapper ordersItemMapper;

    @Resource
    private GoodsMapper goodsMapper;
    @Autowired
    private GoodsService goodsService;


    /**
     * 新增
     */
    public void add(Orders orders) {
        ordersMapper.insert(orders);
    }



    /**
     * 删除
     */
    @Transactional
    public void deleteById(Integer id) {
        ordersMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            ordersMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(Orders orders) {
        ordersMapper.updateById(orders);
    }

    /**
     * 根据ID查询
     */
    public Orders selectById(Integer id) {
        return ordersMapper.selectById(id);
    }



    /**
     * 查询所有
     */
    public List<Orders> selectAll(Orders orders) {
        //当前登录用户信息
        Account currentUser= TokenUtils.getCurrentUser();
        String role=currentUser.getRole();
        if(RoleEnum.BUSINESS.name().equals(role)){
            orders.setBusinessId(currentUser.getId());
            orders.setUserId(null);
        }
        else if(RoleEnum.ADMIN.name().equals(role)){
            orders.setBusinessId(null);
            orders.setUserId(null);// 管理员：强制清空businessId，userId，查看所有
        }
        return ordersMapper.selectAll(orders);
    }

    /**
     * 分页查询
     */
    public PageInfo<Orders> selectPage(Orders orders, Integer pageNum, Integer pageSize) {
        //当前登录用户信息
        Account currentUser= TokenUtils.getCurrentUser();
        String role=currentUser.getRole();
        if(RoleEnum.BUSINESS.name().equals(role)){
            orders.setBusinessId(currentUser.getId());
            orders.setUserId(null);
        }
        else if(RoleEnum.ADMIN.name().equals(role)){
            orders.setBusinessId(null);
            orders.setUserId(null);// 管理员：强制清空businessId，userId，查看所有
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> list = ordersMapper.selectAll(orders);
        return PageInfo.of(list);
    }

    @Transactional
    public Integer createOrder(OrderDTO orderDTO) {
        // 1. 保存主订单
        Orders order = convertToOrder(orderDTO);
        this.add(order); // 使用已有add方法

        // 2. 获取商品信息
        Map<Integer, Goods> goodsMap = getGoodsInfoMap(orderDTO.getItems());

        // 3. 保存订单明细
        saveOrderItems(orderDTO.getItems(), order.getId(), goodsMap);

        return order.getId();
    }


    private Orders convertToOrder(OrderDTO orderDTO) {
        Orders order = new Orders();
        BeanUtils.copyProperties(orderDTO, order);
        order.setOrderNo(generateOrderNo());
        order.setTime(orderDTO.getTime() != null ? orderDTO.getTime() : LocalDateTime.now().toString());
        order.setStatus(orderDTO.getStatus() != null ? orderDTO.getStatus() : "待付款");
        // 处理金额字段（防止DTO中为null）
        order.setAmount(orderDTO.getAmount() != null ? orderDTO.getAmount() : new BigDecimal(0));
        order.setDiscount(orderDTO.getDiscount() != null ? orderDTO.getDiscount() : new BigDecimal(0));
        order.setActual(orderDTO.getActual() != null ? orderDTO.getActual() :new BigDecimal(0));
        return order;
    }

    private Map<Integer, Goods> getGoodsInfoMap(List<OrderItemDTO> items) {
        List<Integer> goodsIds = items.stream()
                .map(OrderItemDTO::getGoodsId)
                .distinct() // 添加去重
                .collect(Collectors.toList());

        // 改用这种方式避免多次查询
        Map<Integer, Goods> goodsMap = new HashMap<>();
        for (Integer id : goodsIds) {
            Goods goods = goodsService.selectById(id);
            if (goods != null) {
                goodsMap.put(id, goods);
            }
        }
        return goodsMap;
    }
    private void saveOrderItems(List<OrderItemDTO> itemDTOs, Integer orderId, Map<Integer, Goods> goodsMap) {
        List<OrdersItem> orderItems = itemDTOs.stream()
                .map(item -> convertToOrdersItem(item, orderId, goodsMap))
                .collect(Collectors.toList());

        if (!orderItems.isEmpty()) {
            ordersItemMapper.insertBatch(orderItems);
        }
    }
    private OrdersItem convertToOrdersItem(OrderItemDTO itemDTO, Integer orderId, Map<Integer, Goods> goodsMap) {
        Goods goods = goodsMap.get(itemDTO.getGoodsId());
        if (goods == null) {
            throw new RuntimeException("商品ID " + itemDTO.getGoodsId() + " 不存在");
        }

        OrdersItem detail = new OrdersItem();
        detail.setOrderId(orderId);
        detail.setGoodsId(itemDTO.getGoodsId());
        detail.setGoodsName(goods.getName());
        detail.setGoodsImg(goods.getImg());
        detail.setPrice(itemDTO.getPrice());
        detail.setNum(itemDTO.getQuantity());
        return detail;
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() +
                ThreadLocalRandom.current().nextInt(1000, 9999);
    }

}