package cn.dormitory.dorm.service;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.Bill;
import cn.dormitory.dorm.dto.BillGenerateDTO;

/** 账单。 */
public interface BillService {

    /** 按 period + room 汇总该宿舍当期用量（金额=SUM(用量*单价)）生成 UNPAID 账单。 */
    Bill generate(BillGenerateDTO dto);

    void updateStatus(Long billId, String status);

    Bill getById(Long id);

    /** 分页查账单。 */
    PageResult<Bill> page(Long current, Long size, Long roomId, String period, String status);
}
