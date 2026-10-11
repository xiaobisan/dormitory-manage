package cn.dormitory.maintenance.service;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.CheckRecord;
import cn.dormitory.maintenance.dto.CheckRecordSubmitDTO;

/** 卫生检查记录 Service。 */
public interface CheckRecordService {

    /** 检查打分：按各检查项得分汇总 score，生成检查记录。 */
    CheckRecord submit(CheckRecordSubmitDTO dto);

    /** 按楼栋/周期分页查检查记录（宿管只查本楼栋）。 */
    PageResult<CheckRecord> page(Long current, Long size, Long buildingId, Long roomId, String period);

    /** 某宿舍某周期最近一次卫生分（供评优用；无记录返回 0）。 */
    cn.dormitory.common.entity.CheckRecord latestRecordOfRoomPeriod(Long roomId, String period);
}
