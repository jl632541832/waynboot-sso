package com.wayn.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.mall.base.BaseController;
import com.wayn.mall.controller.vo.OrderItemVO;
import com.wayn.mall.entity.Order;
import com.wayn.mall.entity.OrderItem;
import com.wayn.mall.enums.OrderStatusEnum;
import com.wayn.mall.enums.PayStatusEnum;
import com.wayn.mall.exception.BusinessException;
import com.wayn.mall.service.OrderItemService;
import com.wayn.mall.service.OrderService;
import com.wayn.mall.util.MyBeanUtil;
import com.wayn.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("admin/orders")
public class MallOrderController extends BaseController {

    private static final String PREFIX = "admin/order";

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @GetMapping
    public String index(HttpServletRequest request) {
        request.setAttribute("path", "orders");
        return PREFIX + "/order";
    }

    /**
     * 列表
     */
    @GetMapping("/list")
    @ResponseBody
    public IPage list(Order order, HttpServletRequest request) {
        Page<Order> page = getPage(request);
        return orderService.selectPage(page, order);
    }

    @ResponseBody
    @GetMapping("order-items/{orderId}")
    public R getOrderItems(@PathVariable("orderId") Long orderId) {
        List<OrderItem> orderItems = orderItemService.list(new QueryWrapper<OrderItem>().eq("order_id", orderId));
        List<OrderItemVO> orderItemVOS = MyBeanUtil.copyList(orderItems, OrderItemVO.class);
        return R.success().add("data", orderItemVOS);
    }

    @ResponseBody
    @PostMapping("update")
    public R update(@RequestBody Order order) {
        Order order1 = orderService.getById(order.getOrderId());
        if (order1 == null) {
            throw new BusinessException("未查询到该订单");
        }
        if (order1.getOrderStatus() > OrderStatusEnum.OREDER_PACKAGED.getOrderStatus()
                || order1.getOrderStatus() < OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
            throw new BusinessException("当前订单无法更改");

        }
        orderService.updateById(order);
        return R.success();
    }


    /**
     * 配货
     *
     * @param ids
     * @return
     */
    @ResponseBody
    @PostMapping("checkDone")
    public R checkDone(@RequestBody List<Long> ids) {
        List<Long> updateOrderIds = new ArrayList<>();
        List<Order> orders = orderService.listByIds(ids);
        for (Order order : orders) {
            if (order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()
                    || order.getOrderStatus() != OrderStatusEnum.OREDER_PAID.getOrderStatus()) {
                throw new BusinessException("编号：" + order.getOrderNo() + " 订单未支付，不可配货");
            }
            updateOrderIds.add(order.getOrderId());
        }
        orderService.update()
                .set("order_status", OrderStatusEnum.OREDER_PACKAGED.getOrderStatus())
                .in("order_id", updateOrderIds)
                .update();
        return R.success();
    }

    /**
     * 出库
     *
     * @param ids
     * @return
     */
    @ResponseBody
    @PostMapping("checkOut")
    public R checkOut(@RequestBody List<Long> ids) {
        List<Long> updateOrderIds = new ArrayList<>();
        List<Order> orders = orderService.listByIds(ids);
        for (Order order : orders) {
            if (order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()
                    || order.getOrderStatus() != OrderStatusEnum.OREDER_PACKAGED.getOrderStatus()) {
                throw new BusinessException("编号：" + order.getOrderNo() + " 订单未配货，不可出库");
            }
            updateOrderIds.add(order.getOrderId());
        }
        orderService.update()
                .set("order_status", OrderStatusEnum.OREDER_EXPRESS.getOrderStatus())
                .in("order_id", updateOrderIds)
                .update();
        return R.success();
    }

    /**
     * 关闭订单
     *
     * @param ids
     * @return
     */
    @ResponseBody
    @PostMapping("close")
    public R close(@RequestBody List<Long> ids) {
        List<Long> updateOrderIds = new ArrayList<>();
        List<Order> orders = orderService.listByIds(ids);
        for (Order order : orders) {
            if (order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()
                    || order.getOrderStatus() != OrderStatusEnum.OREDER_EXPRESS.getOrderStatus()) {
                throw new BusinessException("编号：" + order.getOrderNo() + " 订单未出库，不可关闭");
            }
            updateOrderIds.add(order.getOrderId());
        }
        orderService.update()
                .set("order_status", OrderStatusEnum.ORDER_SUCCESS.getOrderStatus())
                .in("order_id", updateOrderIds)
                .update();
        return R.success();
    }

}
