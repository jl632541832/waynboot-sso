package com.wayn.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.mall.controller.vo.GoodsCategoryVO;
import com.wayn.mall.dao.GoodsCategoryDao;
import com.wayn.mall.entity.GoodsCategory;
import com.wayn.mall.service.GoodsCategoryService;
import com.wayn.mall.util.MyBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoodsCategoryServiceImpl extends ServiceImpl<GoodsCategoryDao, GoodsCategory> implements GoodsCategoryService {

    @Autowired
    private GoodsCategoryDao goodsCategoryDao;

    @Override
    public IPage selectPage(Page<GoodsCategory> page, GoodsCategory goodsCategory) {
        return goodsCategoryDao.selectListPage(page, goodsCategory);
    }

    @Override
    public List<GoodsCategoryVO> treeList() {
        List<GoodsCategory> list = list(new QueryWrapper<GoodsCategory>().eq("is_deleted", 0));
        List<GoodsCategoryVO> voList = MyBeanUtil.copyList(list, GoodsCategoryVO.class);
        List<GoodsCategoryVO> root = new ArrayList<>();
        for (GoodsCategoryVO goodsCategoryVO : voList) {
            if (goodsCategoryVO.getParentId() == 0) {
                root.add(goodsCategoryVO);
            }
            Long id = goodsCategoryVO.getCategoryId();
            List<GoodsCategoryVO> subList = new ArrayList<>();
            for (GoodsCategoryVO categoryVO : voList) {
                if (categoryVO.getParentId().equals(id)) {
                    subList.add(categoryVO);
                    goodsCategoryVO.setSubCategoryVOS(subList);
                }
            }
        }
        return root;
    }
}
