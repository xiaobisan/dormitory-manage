package cn.dormitory.maintenance.service;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.CheckPlan;

/** 卫生检查计划 Service。 */
public interface CheckPlanService {

    PageResult<CheckPlan> page(Long current, Long size, String cycle, Integer active);

    CheckPlan get(Long id);

    CheckPlan create(CheckPlan plan);

    CheckPlan update(CheckPlan plan);

    void delete(Long id);

    /** 启用某计划（供检查打分时按 cycle 找计划用）。 */
    CheckPlan active(Long id);
}
