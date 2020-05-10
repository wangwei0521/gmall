package com.xxxzz.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.xxxzz.gmall.SkuInfo;
import com.xxxzz.gmall.SkuSaleAttrValue;
import com.xxxzz.gmall.SpuSaleAttr;
import com.xxxzz.gmall.config.LoginRequire;
import com.xxxzz.service.ListService;
import com.xxxzz.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    ManageService manageService;
    @Reference
    ListService listService;

    @RequestMapping("{skuId}.html")
    @LoginRequire
    public String skuInfoPage(@PathVariable String skuId, HttpServletRequest httpServletRequest){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        httpServletRequest.setAttribute("skuInfo",skuInfo);
        List<SpuSaleAttr> saleAttrList = manageService.getSpuSaleAttrListCheckBySku(skuInfo);
        httpServletRequest.setAttribute("saleAttrList",saleAttrList);

        //获取销售属性值id
        List<SkuSaleAttrValue> skuSaleAttrValueList= manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());
        //遍历集合拼接字符串{"118|120":"33","119|122":"34",""118|122:""36}
        //当本次循环的skuid和下次循环的skuid不一致的时候，停止拼接。拼接到最后停止拼接

        String keyIds="";

        Map<String,String> map=new HashMap<>();
        for (int i = 0; i < skuSaleAttrValueList.size(); i++) {
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueList.get(i);
            if(keyIds.length() > 0){
                keyIds= keyIds+"|";
            }
            keyIds=keyIds+skuSaleAttrValue.getSaleAttrValueId();
            //当本次循环的skuid和下次循环的skuid不一致的时候，停止拼接。拼接到最后停止拼接
            if((i+1)== skuSaleAttrValueList.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueList.get(i+1).getSkuId())  ){
                map.put(keyIds,skuSaleAttrValue.getSkuId());
                keyIds="";
            }
        }
        //把map变成json串
        String valuesSkuJson = JSON.toJSONString(map);
        httpServletRequest.setAttribute("valuesSkuJson",valuesSkuJson);

        listService.incrHotScore(skuId);  //最终应该由异步方式调用
        return "item";
    }
}
