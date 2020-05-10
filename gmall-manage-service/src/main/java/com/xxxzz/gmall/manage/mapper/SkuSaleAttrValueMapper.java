package com.xxxzz.gmall.manage.mapper;

import com.xxxzz.gmall.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu(String spuId);
}
