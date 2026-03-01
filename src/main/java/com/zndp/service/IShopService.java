package com.zndp.service;
import com.zndp.dto.Result;
import com.zndp.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface IShopService extends IService<Shop> {

    Result queryById(Long id);
    Result update(Shop shop);
}
