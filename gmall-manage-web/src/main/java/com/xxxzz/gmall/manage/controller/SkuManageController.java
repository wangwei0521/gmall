package com.xxxzz.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.xxxzz.gmall.SkuInfo;
import com.xxxzz.gmall.SkuLsInfo;
import com.xxxzz.gmall.SpuImage;
import com.xxxzz.gmall.SpuSaleAttr;
import com.xxxzz.service.ListService;
import com.xxxzz.service.ManageService;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@RestController
@CrossOrigin
public class SkuManageController {

    @Reference
    ManageService manageService;

    @Reference
    private ListService listService;

    @RequestMapping("spuImageList")
    public List<SpuImage> spuImageList(SpuImage spuImage){
        return manageService.getSpuImageList(spuImage);
    }

    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> spuSaleAttrList(String spuId){
        return manageService.getSpuSaleAttrList(spuId);
    }

    @RequestMapping("saveSkuInfo")
    public void saveSkuInfo(@RequestBody SkuInfo skuInfo){
        if(skuInfo != null){
            manageService.saveSkuInfo(skuInfo);
        }
    }

    @RequestMapping("onSale")
    public void onSale(String skuId){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        SkuLsInfo skuLsInfo = new SkuLsInfo();
        // 属性拷贝
        try {
            BeanUtils.copyProperties(skuLsInfo,skuInfo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        listService.saveSkuInfo(skuLsInfo);
    }
}
