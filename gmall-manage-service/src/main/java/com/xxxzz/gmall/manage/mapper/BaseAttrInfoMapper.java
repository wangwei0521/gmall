package com.xxxzz.gmall.manage.mapper;

import com.xxxzz.gmall.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    /**
     * 根据三级分类查询平台属性集合
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(String catalog3Id);

    //必须要用@Param注解否则${ }无法识别
    List<BaseAttrInfo> selectAttrInfoListByIds(@Param("valueIds")String attrValueIds);
}
