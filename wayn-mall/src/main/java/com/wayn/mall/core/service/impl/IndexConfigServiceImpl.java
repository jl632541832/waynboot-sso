package com.wayn.mall.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wayn.mall.core.dao.IndexConfigDao;
import com.wayn.mall.core.entity.Goods;
import com.wayn.mall.core.entity.IndexConfig;
import com.wayn.mall.core.service.GoodsService;
import com.wayn.mall.core.service.IndexConfigService;
import com.wayn.mall.enums.IndexConfigTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IndexConfigServiceImpl extends ServiceImpl<IndexConfigDao, IndexConfig> implements IndexConfigService {

    @Autowired
    private IndexConfigDao indexConfigDao;

    @Autowired
    private GoodsService goodsService;

    @Override
    public IPage<IndexConfig> selectPage(Page<IndexConfig> page, IndexConfig indexConfig) {
        return indexConfigDao.selectListPage(page, indexConfig);
    }

    @Override
    public List<Goods> listIndexConfig(IndexConfigTypeEnum indexGoodsHot, int limit) {
        List<IndexConfig> list = list(new QueryWrapper<IndexConfig>()
                .eq("config_type", indexGoodsHot.getType())
                .last("limit " + limit)
                .orderByDesc("config_rank"));
        List<Long> collect = list.stream().map(IndexConfig::getGoodsId).collect(Collectors.toList());
        return goodsService.listByIds(collect);
    }
}
