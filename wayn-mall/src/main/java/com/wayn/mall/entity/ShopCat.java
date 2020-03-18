package com.wayn.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@TableName("tb_newbee_mall_shopping_cart_item")
@Data
public class ShopCat {

    @TableId(type = IdType.AUTO)
    private Long cartItemId;

    private Long userId;

    private Long goodsId;

    private Integer goodsCount;

    @TableLogic
    private Byte isDeleted;

    private Date createTime;

    private Date updateTime;
}
