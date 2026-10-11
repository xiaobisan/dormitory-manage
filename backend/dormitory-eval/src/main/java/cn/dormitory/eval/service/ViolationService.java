package cn.dormitory.eval.service;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.Violation;
import cn.dormitory.eval.dto.DeptViolationStatsVO;
import cn.dormitory.eval.dto.ViolationHandleDTO;
import cn.dormitory.eval.dto.ViolationRecordDTO;
import cn.dormitory.eval.dto.ViolationRecordVO;

/**
 * 违纪记录管理（/api/eval/violation/record）+ 系别统计（/api/eval/violation/stats/by-dept）。
 */
public interface ViolationService {

    /** 录入违纪（宿管/辅导员/管理员）。校验学生与宿舍存在。 */
    Violation createRecord(ViolationRecordDTO dto);

    /**
     * 分页查违纪记录。
     *
     * @param studentId 学生过滤
     * @param roomId    宿舍过滤
     * @param deptId    系别过滤（辅导员/管理员）
     * @param fromMonth 月份起（yyyy-MM，按 happenedAt）
     * @param toMonth   月份止（yyyy-MM）
     */
    PageResult<ViolationRecordVO> page(long current, long size,
                                       Long studentId, Long roomId, Long deptId,
                                       String fromMonth, String toMonth);

    /** 处理违纪（标记已处理 + 处理说明）。 */
    void handle(Long id, ViolationHandleDTO dto);

    /** 单条详情（含学生/宿舍/类型补全）。 */
    ViolationRecordVO detail(Long id);

    /**
     * 系别违纪统计。
     *
     * @param deptId    辅导员传 null 表示统计自己的系（读 UserContext）
     * @param fromMonth 区间起 yyyy-MM（可空，缺省近 12 个月）
     * @param toMonth   区间止 yyyy-MM（可空，缺省当前月）
     */
    DeptViolationStatsVO statsByDept(Long deptId, String fromMonth, String toMonth);
}
