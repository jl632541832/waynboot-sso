package com.wayn.mall.core.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wayn.mall.core.entity.Coupon;
import com.wayn.mall.core.entity.vo.CouponVO;

import java.util.List;

public interface CouponDao extends BaseMapper<Coupon> {

    IPage<Coupon> selectListPage(Page<Coupon> page, CouponVO coupon);

    List<Coupon> selectAvailableCoupon();

    int reduceCouponTotal(Long couponId);

}
