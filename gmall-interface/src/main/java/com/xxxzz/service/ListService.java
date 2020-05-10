package com.xxxzz.service;

import com.xxxzz.gmall.SkuLsInfo;
import com.xxxzz.gmall.SkuLsParams;
import com.xxxzz.gmall.SkuLsResult;

public interface ListService {

    public void saveSkuInfo(SkuLsInfo skuLsInfo);

    public SkuLsResult search(SkuLsParams skuLsParams);

    public void incrHotScore(String skuId);
}
