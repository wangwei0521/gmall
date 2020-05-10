package com.xxxzz.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.xxxzz.gmall.*;
import com.xxxzz.gmall.manage.constant.ManageConst;
import com.xxxzz.gmall.manage.mapper.*;
import com.xxxzz.gmall.util.RedisUtil;
import com.xxxzz.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2=new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3=new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
//        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
//        baseAttrInfo.setCatalog3Id(catalog3Id);
//        return baseAttrInfoMapper.select(baseAttrInfo);
        return baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //如果有主键就进行更新，如果没有就插入
        if(baseAttrInfo.getId()!=null&&baseAttrInfo.getId().length()>0){
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        }else{
            baseAttrInfo.setId(null);
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        //把原属性值全部清空
        BaseAttrValue baseAttrValue4Del = new BaseAttrValue();
        baseAttrValue4Del.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue4Del);

        //重新插入属性值
        if(baseAttrInfo.getAttrValueList()!=null&&baseAttrInfo.getAttrValueList().size()>0) {
            for (BaseAttrValue attrValue : baseAttrInfo.getAttrValueList()) {
                //防止主键被赋上一个空字符串
                attrValue.setId(null);
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        return baseAttrValueMapper.select(baseAttrValue);
    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        //查询获取平台属性对象
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        //创建平台属性值对象
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        //根据attrId查询平台属性值集合
        List<BaseAttrValue> attrValueList = baseAttrValueMapper.select(baseAttrValue);
        //给平台属性中的属性值赋值
        baseAttrInfo.setAttrValueList(attrValueList);
        return baseAttrInfo;
    }

    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
        if (spuInfo.getId()==null || spuInfo.getId().length()==0){
            //保存数据
            spuInfo.setId(null);
            spuInfoMapper.insertSelective(spuInfo);
        }else {
            spuInfoMapper.updateByPrimaryKeySelective(spuInfo);
        }
        //  spuImage 图片列表 先删除，在新增
        //  delete from spuImage where spuId =?
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuInfo.getId());
        spuImageMapper.delete(spuImage);

        // 保存数据，先获取数据
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList!=null && spuImageList.size()>0){
            // 循环遍历
            for (SpuImage image : spuImageList) {
                image.setId(null);
                image.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(image);
            }
        }
        // 销售属性 删除，插入
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttr);

        // 销售属性值 删除，插入
        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValue);

        // 获取数据
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList!=null && spuSaleAttrList.size()>0){
            // 循环遍历
            for (SpuSaleAttr saleAttr : spuSaleAttrList) {
                saleAttr.setId(null);
                saleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(saleAttr);

                // 添加销售属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList!=null && spuSaleAttrValueList.size()>0){
                    // 循环遍历
                    for (SpuSaleAttrValue saleAttrValue : spuSaleAttrValueList) {
                        saleAttrValue.setId(null);
                        saleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValueMapper.insertSelective(saleAttrValue);
                    }
                }

            }
        }
    }

    @Override
    public List<SpuImage> getSpuImageList(SpuImage spuImage) {
        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //sku_info
        skuInfoMapper.insertSelective(skuInfo);
        //sku_image
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if(skuImageList != null && skuImageList.size() > 0){
            for (SkuImage skuImage:skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(skuImage);
            }
        }
        //sku_attr_value
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if(skuAttrValueList != null && skuAttrValueList.size() > 0){
            for(SkuAttrValue skuAttrValue : skuAttrValueList){
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }
        //sku_sale_attr_value
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if(skuSaleAttrValueList != null && skuSaleAttrValueList.size() >0){
            for(SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList){
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }
    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {
        SkuInfo skuInfo = null;
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            //定义key名称
            String skuInfoKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKUKEY_SUFFIX;
            if(jedis.exists(skuInfoKey)){//缓存中存在
                //取出数据
                String skuInfoJson = jedis.get(skuInfoKey);
                skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);
                return skuInfo;
            }else{//缓存中不存在
                System.out.println("缓存中没有数据！！！尝试对数据加锁");
                //d定义lockKey
                String skuLockKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKULOCK_SUFFIX;
                String lockResult = jedis.set(skuLockKey, "OK1", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                if("OK".equals(lockResult)){//加锁成功
                    // 从数据库中取得数据
                    skuInfo = getSkuInfoDB(skuId);
                    //将对象转化成json字符串
                    String jsonString = JSON.toJSONString(skuInfo);
                    //将数据放入到缓存中
                    jedis.setex(skuInfoKey,ManageConst.SKUKEY_TIMEOUT,jsonString);
                    //删除锁
                    jedis.del(skuLockKey);
                    return skuInfo;
                }else{
                    Thread.sleep(1000);
                    return getSkuInfoDB(skuId);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(null != jedis){
                jedis.close();
            }
        }
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoRedisson(String skuId){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.64.101:6379");
        RedissonClient redisson = Redisson.create(config);
        RLock myLock = redisson.getLock("myLock");
        //加锁
        myLock.lock(10, TimeUnit.SECONDS);
        //业务代码

        SkuInfo skuInfo =null;
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String skuKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            // 判断redis 中是否有key
            if (jedis.exists(skuKey)){
                // 取得key 中的value
                String skuJson = jedis.get(skuKey);
                // 将字符串转换为对象
                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
                return skuInfo;
            }else {
                skuInfo = getSkuInfoDB(skuId);
                // 放redis 并设置过期时间
                jedis.setex(skuKey,ManageConst.SKUKEY_TIMEOUT,JSON.toJSONString(skuInfo));
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis!=null){
                jedis.close();
            }
            //解锁
            myLock.unlock();
        }
        return getSkuInfoDB(skuId);

    }
    private SkuInfo getSkuInfoDB(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);

        //将平台属性值set进去
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> attrValues = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(attrValues);
        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        String spuId = skuInfo.getSpuId();
        String skuId = skuInfo.getId();
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {
        String attrValueIds = StringUtils.join(attrValueIdList.toArray(), ",");
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectAttrInfoListByIds(attrValueIds);
        return baseAttrInfoList;
    }

}
