package com.xxxzz.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.xxxzz.gmall.*;
import com.xxxzz.service.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class ManageController {

    @Reference
    private ManageService manageService;

    @RequestMapping("getCatalog1")
    public List<BaseCatalog1> getCatalog1(){
        return manageService.getCatalog1();
    }

    @RequestMapping("getCatalog2")
    public List<BaseCatalog2> getCatalog2(String catalog1Id){
        return manageService.getCatalog2(catalog1Id);
    }

    @RequestMapping("getCatalog3")
    public List<BaseCatalog3> getCatalog3(String catalog2Id){
        return manageService.getCatalog3(catalog2Id);
    }

    @RequestMapping("attrInfoList")
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){
        return manageService.getAttrList(catalog3Id);
    }

    @RequestMapping("saveAttrInfo")
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        // 调用服务层做保存方法
        manageService.saveAttrInfo(baseAttrInfo);
    }

    @RequestMapping("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId){
//        return manageService.getAttrValueList(attrId);
        //先通过attrId查询平台属性
        BaseAttrInfo baseAttrInfo = manageService.getAttrInfo(attrId);
        return baseAttrInfo.getAttrValueList();
    }
    @RequestMapping("baseSaleAttrList")
    public List<BaseSaleAttr> getBaseSaleAttrList(){
        return   manageService.getBaseSaleAttrList();
    }
}
