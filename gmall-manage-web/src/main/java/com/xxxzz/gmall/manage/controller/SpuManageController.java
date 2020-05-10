package com.xxxzz.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.xxxzz.gmall.SpuInfo;
import com.xxxzz.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class SpuManageController {

    @Reference
    private ManageService manageService;

    @RequestMapping("spuList")
    public List<SpuInfo> spuList(String catalog3Id){
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        List<SpuInfo> spuInfoList = manageService.getSpuInfoList(spuInfo);
        return  spuInfoList;
    }
    @RequestMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return  "OK";
    }
}
