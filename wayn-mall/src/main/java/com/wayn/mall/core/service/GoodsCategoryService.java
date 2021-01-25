package com.wayn.mall.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wayn.mall.core.entity.GoodsCategory;
import com.wayn.mall.core.entity.vo.GoodsCategoryVO;

import java.util.List;

public interface GoodsCategoryService extends IService<GoodsCategory> {

    IPage<GoodsCategory> selectPage(Page<GoodsCategory> page, GoodsCategory goodsCategory);

    /**
     * 查询商品三级分类树集合
     * @return
     */
    List<GoodsCategoryVO> treeList();

}
