package cn.dormitory.maintenance.service.impl;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.CheckPlan;
import cn.dormitory.common.exception.BusinessException;
import cn.dormitory.maintenance.dto.CheckRecordSubmitDTO;
import cn.dormitory.maintenance.mapper.CheckPlanMapper;
import cn.dormitory.maintenance.service.CheckPlanService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CheckPlanServiceImpl implements CheckPlanService {

    private final CheckPlanMapper planMapper;

    public CheckPlanServiceImpl(CheckPlanMapper planMapper) {
        this.planMapper = planMapper;
    }

    @Override
    public PageResult<CheckPlan> page(Long current, Long size, String cycle, Integer active) {
        LambdaQueryWrapper<CheckPlan> w = new LambdaQueryWrapper<>();
        w.like(cycle != null && !cycle.isBlank(), CheckPlan::getCycle, cycle)
                .eq(active != null, CheckPlan::getActive, active)
                .orderByDesc(CheckPlan::getCreatedAt);
        IPage<CheckPlan> p = planMapper.selectPage(new Page<>(current, size), w);
        return PageResult.of(p);
    }

    @Override
    public CheckPlan get(Long id) {
        CheckPlan plan = planMapper.selectById(id);
        if (plan == null) throw BusinessException.notFound("检查计划不存在: " + id);
        return plan;
    }

    @Override
    public CheckPlan create(CheckPlan plan) {
        validateTemplate(plan.getItemTemplate());
        planMapper.insert(plan);
        return plan;
    }

    @Override
    public CheckPlan update(CheckPlan plan) {
        if (plan.getId() == null) throw new BusinessException("更新检查计划必须带 id");
        CheckPlan old = get(plan.getId());
        old.setName(plan.getName());
        old.setCycle(plan.getCycle());
        validateTemplate(plan.getItemTemplate());
        old.setItemTemplate(plan.getItemTemplate());
        old.setActive(plan.getActive());
        old.setRemark(plan.getRemark());
        planMapper.updateById(old);
        return old;
    }

    @Override
    public void delete(Long id) {
        get(id);
        planMapper.deleteById(id);
    }

    @Override
    public CheckPlan active(Long id) {
        CheckPlan plan = get(id);
        if (plan.getDeleted() != null && plan.getDeleted() == 1) throw BusinessException.notFound("检查计划已删除");
        return plan;
    }

    /** 校验 itemTemplate JSON 形如 [{key,name,max}]，保证打分端 key 与模板一致。 */
    private void validateTemplate(String itemTemplate) {
        // itemTemplate 允许为空（自由打分），非空时做轻校验
        if (itemTemplate != null && !itemTemplate.isBlank()) {
            String t = itemTemplate.trim();
            if (!(t.startsWith("[") && t.endsWith("]"))) {
                throw new BusinessException("检查项模板需为 JSON 数组 [{key,name,max}]");
            }
        }
    }
}
