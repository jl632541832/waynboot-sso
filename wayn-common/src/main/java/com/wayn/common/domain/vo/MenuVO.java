package com.wayn.common.domain.vo;

import com.wayn.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 菜单表
 * </p>
 *
 * @author wayn
 * @since 2019-04-13
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MenuVO extends BaseEntity<MenuVO> {

    /**
     * 主键
     */
    private Long id;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 父级菜单ID
     */
    private Long pid;

    /**
     * 连接地址
     */
    private String url;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private BigDecimal sort;

    /**
     * 类别，1表示目录，2表示菜单，3表示按钮
     */
    private String type;

    /**
     * 编码
     */
    private String code;

    /**
     * 资源名称（菜单对应权限）
     */
    private String resource;

    private List<MenuVO> children = new ArrayList<>();
}
