package com.xxxzz.service;

import com.xxxzz.gmall.*;

import java.util.List;

public interface ManageService {
    public List<BaseCatalog1> getCatalog1();

    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    public void  saveAttrInfo(BaseAttrInfo baseAttrInfo);

    public List<BaseAttrValue> getAttrValueList(String attrId);

    public BaseAttrInfo getAttrInfo(String attrId);

    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    public List<BaseSaleAttr> getBaseSaleAttrList();

    public void saveSpuInfo(SpuInfo spuInfo);

    public List<SpuImage> getSpuImageList(SpuImage spuImage);

    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    public void saveSkuInfo(SkuInfo skuInfo);

    public SkuInfo getSkuInfo(String skuId);

    /**
     * 用于查询商品详情页的销售属性、销售属性值、并锁定属性值
     * @param skuInfo
     * @return
     */
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    /**
     * 根据spuId查询销售属性值id
     * @param spuId
     * @return
     */
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);

    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
