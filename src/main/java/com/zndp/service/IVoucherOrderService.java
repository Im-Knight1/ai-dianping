package com.zndp.service;

import com.zndp.dto.Result;
import com.zndp.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId);
//
//    void createVoucherOrder(VoucherOrder voucherOrder);
}
