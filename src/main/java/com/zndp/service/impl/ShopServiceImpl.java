package com.zndp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zndp.dto.Result;
import com.zndp.entity.Shop;
import com.zndp.mapper.ShopMapper;
import com.zndp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zndp.utils.CacheClient;
import com.zndp.utils.RedisData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.zndp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    @Override
    public Result queryById(Long id) {
        // 解决缓存穿透
        Shop shop = cacheClient
                .queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 互斥锁解决缓存击穿
        // Shop shop = cacheClient
        //         .queryWithMutex(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 逻辑过期解决缓存击穿
        // Shop shop = cacheClient
        //         .queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, 20L, TimeUnit.SECONDS);

        if(shop == null){
            return Result.fail("店铺不存在");
        }
        return Result.ok(shop);
    }

//    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
//    //击穿----逻辑过期
//    public Shop queryWithLogicalExpire(Long id){
//        String key = "cache:shop:"+id;
//        String shopjson = stringRedisTemplate.opsForValue().get(key);
//        if(StrUtil.isBlank(shopjson)){
//            return null;
//        }
//        RedisData redisData = JSONUtil.toBean(shopjson,RedisData.class);
//        Shop shop =JSONUtil.toBean((JSONObject) redisData.getData(),Shop.class);
//        LocalDateTime expireTime = redisData.getExpireTime();
//        if(expireTime.isAfter(LocalDateTime.now())){
//            return shop;
//        }
//        String lockKey = LOCK_SHOP_KEY + id;
//        boolean isLock = tryLock(lockKey);
//        if(isLock){
//            CACHE_REBUILD_EXECUTOR.submit(() ->{
//                try {
//                    this.saveShop2Redis(id,20L);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                } finally {
//                    unLock(lockKey);
//                }
//
//            });
//        }
//        return shop;
//    }
//
//    //击穿---互斥锁解决方案
//    public Shop queryWithMutxe(Long id){
//        String key = "cache:shop:"+id;
//        String shopjson = stringRedisTemplate.opsForValue().get(key);
//        if(StrUtil.isNotBlank(shopjson)){
//            return JSONUtil.toBean(shopjson,Shop.class);
//        }
//        if(shopjson != null){
//            return null;
//        }
//
//        //获取锁
//        String lockKey = "lock:shop:" + id;
//        Shop shop = null;
//        try {
//            boolean isLock = tryLock(lockKey);
//            if(!isLock){
//                Thread.sleep(50);
//                return queryWithMutxe(id);
//            }
//
//            shop = getById(id);
//            if(shop == null){
//                //写入空值，减少穿透
//                stringRedisTemplate.opsForValue().set(key,"",2L, TimeUnit.MINUTES);
//                return null;
//
//            }
//            stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),30L, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            unLock(lockKey);
//        }
//        return shop;
//    }
//
//    //缓存穿透
//    public Shop queryWithPassThrough(Long id){
//        String key = "cache:shop:"+id;
//        String shopjson = stringRedisTemplate.opsForValue().get(key);
//        if(StrUtil.isNotBlank(shopjson)){
//            return JSONUtil.toBean(shopjson,Shop.class);
//        }
//        if(shopjson != null){
//            return null;
//        }
//        Shop shop = getById(id);
//        if(shop == null){
//            //写入空值，减少穿透
//            stringRedisTemplate.opsForValue().set(key,"",2L, TimeUnit.MINUTES);
//            return null;
//
//        }
//        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),30L, TimeUnit.MINUTES);
//        return shop;
//    }
//
//    private boolean tryLock(String key){
//        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key,"1",10,TimeUnit.SECONDS);
//        return BooleanUtil.isTrue(flag);
//    }
//
//    private void unLock(String key){
//        stringRedisTemplate.delete(key);
//    }
//
//    //设置逻辑控制时间
//    public void saveShop2Redis(Long id,Long expireSeconds){
//        Shop shop = getById(id);
//        RedisData redisData = new RedisData();
//        redisData.setData(shop);
//        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
//        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id,JSONUtil.toJsonStr(redisData));
//    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if(id == null){
            return Result.fail("店铺id不能为空");
        }
        updateById(shop);

        stringRedisTemplate.delete("cache:shop:"+shop.getId());
        return Result.ok();
    }
}
