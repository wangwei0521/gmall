package com.xxxzz.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.xxxzz.gmall.CartInfo;
import com.xxxzz.gmall.SkuInfo;
import com.xxxzz.gmall.cart.mapper.CartInfoMapper;
import com.xxxzz.gmall.util.RedisUtil;
import com.xxxzz.service.CartService;
import com.xxxzz.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartInfoMapper cartInfoMapper;
    @Reference
    ManageService manageService;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
        //先查购物车中是否保存了该商品
        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        CartInfo cartInfoExist  = cartInfoMapper.selectOne(cartInfo);
        if(cartInfoExist != null){//购物车中有该商品
            //更新商品的数量
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
            // 给实时价格赋值
            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
        }else{
            //如果不存在，保存到购物车
            //先获取sku的信息
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo1 = new CartInfo();
            cartInfo1.setSkuId(skuId);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuNum(skuNum);
            // 保存到购物车
            cartInfoMapper.insertSelective(cartInfo1);
            cartInfoExist = cartInfo1;
        }
        //加入缓存
        // 构建key user:userid:cart
        String userCartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        //准备数据
        Jedis jedis = redisUtil.getJedis();
        //将对象序列化
        String cartJson  = JSON.toJSONString(cartInfoExist);
        //用hash结构存储
        //em:如果购物车中有两件商品，使用String类型的话，要想修改第一个商品，
        // 则必须将所有的value全部进行反序列化。设计到Io操作，性能降低。
        //如果用hash，Hset(key,field,value):key = user:userId:cart  field=skuId
        //要想修改商品则jedis.hget(userCartKey, skuId);
        jedis.hset(userCartKey,skuId,cartJson);
        // 更新购物车过期时间
        String userInfoKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
        Long ttl = jedis.ttl(userInfoKey);
        jedis.expire(userCartKey,ttl.intValue());
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartList(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String userCartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        //从redis中获取
        List<String> cartJsons = jedis.hvals(userCartKey);
        if(cartJsons!=null && cartJsons.size() > 0){//如果缓存中有数据加入到list集合中
            List<CartInfo> cartInfoList  = new ArrayList<>();
            for (String cartJson: cartJsons){
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoList.add(cartInfo);
            }
            // 排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return Long.compare(Long.parseLong(o2.getId()),Long.parseLong(o1.getId()));
                }
            });
            return cartInfoList;
        }else{//从数据库中查询
            // 其中cart_price 可能是旧值，所以需要关联sku_info 表信息。
            List<CartInfo> cartInfoList = loadCartCache(userId);
            return  cartInfoList;
        }
    }

    //缓存中没有购物车数据，则从数据库中加载
    @Override
    public List<CartInfo> loadCartCache(String userId) {
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        if (cartInfoList==null && cartInfoList.size()==0){
            return null;
        }
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        Map<String,String> map = new HashMap<>(cartInfoList.size());
        for (CartInfo cartInfo : cartInfoList) {
            String cartJson = JSON.toJSONString(cartInfo);
            // key 都是同一个，值会产生重复覆盖！
            map.put(cartInfo.getSkuId(),cartJson);
        }
        // 将java list - redis hash
        jedis.hmset(userCartKey,map);
        jedis.close();
        return  cartInfoList;
    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId) {
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
        // 循环开始匹配
        for (CartInfo cartInfoCk : cartListFromCookie) {
            boolean isMatch =false;
            for (CartInfo cartInfoDB : cartInfoListDB) {
                if (cartInfoDB.getSkuId().equals(cartInfoCk.getSkuId())){
                    cartInfoDB.setSkuNum(cartInfoCk.getSkuNum()+cartInfoDB.getSkuNum());
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatch = true;
                }
            }
            // 数据库中没有购物车，则直接将cookie中购物车添加到数据库
            if (!isMatch){
                cartInfoCk.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCk);
            }
        }
        // 从新在数据库中查询并返回数据
        List<CartInfo> cartInfoList = loadCartCache(userId);
        for (CartInfo cartInfo : cartInfoList) {
            for (CartInfo info : cartListFromCookie) {
                if (cartInfo.getSkuId().equals(info.getSkuId())){
                    // 只有被勾选的才会进行更改
                    if (info.getIsChecked().equals("1")){
                        cartInfo.setIsChecked(info.getIsChecked());
                        // 更新redis中的isChecked
                        checkCart(cartInfo.getSkuId(),info.getIsChecked(),userId);
                    }
                }
            }
        }
        return cartInfoList;
    }

    @Override
    public void checkCart(String skuId, String isChecked, String userId) {
        // 更新购物车中的isChecked标志
        Jedis jedis = redisUtil.getJedis();
        // 取得购物车中的信息
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        String cartJson = jedis.hget(userCartKey, skuId);
        // 将cartJson 转换成对象
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        String cartCheckdJson = JSON.toJSONString(cartInfo);
        jedis.hset(userCartKey,skuId,cartCheckdJson);
        // 新增到已选中购物车
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        if (isChecked.equals("1")){
            jedis.hset(userCheckedKey,skuId,cartCheckdJson);
        }else{
            jedis.hdel(userCheckedKey,skuId);
        }
        jedis.close();
    }

    // 得到选中购物车列表
    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        // 获得redis中的key
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        List<String> cartCheckedList = jedis.hvals(userCheckedKey);
        List<CartInfo> newCartList = new ArrayList<>();
        for (String cartJson : cartCheckedList) {
            CartInfo cartInfo = JSON.parseObject(cartJson,CartInfo.class);
            newCartList.add(cartInfo);
        }
        return newCartList;
    }
}
