package com.wayn.mall.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.mall.controller.vo.MallUserVO;
import com.wayn.mall.controller.vo.OrderListVO;
import com.wayn.mall.controller.vo.OrderVO;
import com.wayn.mall.controller.vo.ShopCatVO;
import com.wayn.mall.dao.OrderDao;
import com.wayn.mall.entity.Goods;
import com.wayn.mall.entity.Order;
import com.wayn.mall.entity.OrderItem;
import com.wayn.mall.exception.BusinessException;
import com.wayn.mall.service.GoodsService;
import com.wayn.mall.service.OrderItemService;
import com.wayn.mall.service.OrderService;
import com.wayn.mall.service.ShopCatService;
import com.wayn.mall.util.NumberUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderDao, Order> implements OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private ShopCatService shopCatService;

    @Autowired
    private OrderItemService orderItemService;

    @Override
    public IPage selectMyOrderPage(Page<OrderListVO> page, Order order) {
        return orderDao.selectListVOPage(page, order);
    }

    @Override
    public IPage selectPage(Page<Order> page, OrderVO order) {
        return orderDao.selectListPage(page, order);
    }

    @Transactional
    @Override
    public String saveOrder(MallUserVO mallUserVO, List<ShopCatVO> shopcatVOList) {
        List<Long> goodsIdList = shopcatVOList.stream().map(ShopCatVO::getGoodsId).collect(Collectors.toList());
        List<Long> cartItemIdList = shopcatVOList.stream().map(ShopCatVO::getCartItemId).collect(Collectors.toList());
        List<Goods> goods = goodsService.listByIds(goodsIdList);
        // 检查是否包含已下架商品
        List<Goods> collect = goods.stream().filter(goods1 -> goods1.getGoodsSellStatus() == 1).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(collect)) {
            throw new BusinessException(collect.get(0).getGoodsName() + "已下架，无法生成订单");
        }
        Map<Long, Goods> goodsMap = goods.stream().collect(Collectors.toMap(Goods::getGoodsId, goods1 -> goods1));
        for (ShopCatVO shopCatVO : shopcatVOList) {
            // 查出的商品中不存在购物车中的这条关联商品数据，直接返回错误提醒
            if (!goodsMap.containsKey(shopCatVO.getGoodsId())) {
                throw new BusinessException("购物车中商品数据异常");
            }
            // 存在数量大于库存的情况，直接返回错误提醒
            if (shopCatVO.getGoodsCount() > goodsMap.get(shopCatVO.getGoodsId()).getStockNum()) {
                throw new BusinessException("购物车中:" + goodsMap.get(shopCatVO.getGoodsId()).getGoodsName() + " 库存不足");
            }
        }
        if (CollectionUtils.isNotEmpty(goodsIdList)
                && CollectionUtils.isNotEmpty(cartItemIdList)
                && CollectionUtils.isNotEmpty(goods)) {
            // 删除购物项
            if (shopCatService.removeByIds(cartItemIdList)) {
                List<Goods> collect1 = shopcatVOList.stream().map(shopCatVO -> {
                    Goods goods1 = new Goods();
                    goods1.setGoodsId(shopCatVO.getGoodsId());
                    Integer stockNum = goodsMap.get(shopCatVO.getGoodsId()).getStockNum();
                    goods1.setStockNum(stockNum - shopCatVO.getGoodsCount());
                    return goods1;
                }).collect(Collectors.toList());
                // 更新商品库存
                if (goodsService.updateBatchById(collect1)) {
                    // 生成订单号
                    String orderNo = NumberUtil.genOrderNo();
                    double priceTotal = 0;
                    for (ShopCatVO shopCatVO : shopcatVOList) {
                        priceTotal += shopCatVO.getGoodsCount() * shopCatVO.getSellingPrice();
                    }
                    // 总价异常
                    if (priceTotal <= 0) {
                        throw new BusinessException("订单价格异常");
                    }
                    // 保存订单
                    Order order = new Order();
                    order.setOrderNo(orderNo);
                    order.setTotalPrice(priceTotal);
                    order.setUserId(mallUserVO.getUserId());
                    order.setUserAddress(mallUserVO.getAddress());
                    //todo 订单body字段，用来作为生成支付单描述信息，暂时未接入第三方支付接口，故该字段暂时设为空字符串
                    String extraInfo = "";
                    order.setExtraInfo(extraInfo);
                    // 生成订单项并保存订单项纪录
                    if (save(order)) {
                        // 生成所有的订单项快照，并保存至数据库
                        List<OrderItem> orderItems = shopcatVOList.stream().map(shopCatVO -> {
                            OrderItem orderItem = new OrderItem();
                            BeanUtils.copyProperties(shopCatVO, orderItem);
                            orderItem.setOrderId(order.getOrderId());
                            return orderItem;
                        }).collect(Collectors.toList());
                        if (orderItemService.saveBatch(orderItems)) {
                            //所有操作成功后，将订单号返回，以供Controller方法跳转到订单详情
                            return orderNo;
                        }
                    }
                }
            }
        }
        throw new BusinessException("结算异常");
    }
}
