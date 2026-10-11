package cn.dormitory.maintenance.service.impl;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.EvaluationRule;
import cn.dormitory.common.exception.BusinessException;
import cn.dormitory.maintenance.mapper.EvaluationRuleMapper;
import cn.dormitory.maintenance.service.EvaluationRuleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EvaluationRuleServiceImpl implements EvaluationRuleService {

    private final EvaluationRuleMapper ruleMapper;

    public EvaluationRuleServiceImpl(EvaluationRuleMapper ruleMapper) {
        this.ruleMapper = ruleMapper;
    }

    @Override
    public PageResult<EvaluationRule> page(Long current, Long size, String name, Integer active) {
        LambdaQueryWrapper<EvaluationRule> w = new LambdaQueryWrapper<>();
        w.like(name != null && !name.isBlank(), EvaluationRule::getName, name)
                .eq(active != null, EvaluationRule::getActive, active)
                .orderByDesc(EvaluationRule::getCreatedAt);
        IPage<EvaluationRule> p = ruleMapper.selectPage(new Page<>(current, size), w);
        return PageResult.of(p);
    }

    @Override
    public EvaluationRule get(Long id) {
        EvaluationRule rule = ruleMapper.selectById(id);
        if (rule == null) throw BusinessException.notFound("评优规则不存在: " + id);
        return rule;
    }

    @Override
    public EvaluationRule create(EvaluationRule rule) {
        validate(rule);
        ruleMapper.insert(rule);
        return rule;
    }

    @Override
    public EvaluationRule update(EvaluationRule rule) {
        if (rule.getId() == null) throw new BusinessException("更新评优规则必须带 id");
        EvaluationRule old = get(rule.getId());
        old.setName(rule.getName());
        validate(old);
        old.setHygieneWeight(rule.getHygieneWeight());
        old.setDisciplineWeight(rule.getDisciplineWeight());
        old.setBonusWeight(rule.getBonusWeight());
        old.setThresholds(rule.getThresholds());
        old.setActive(rule.getActive());
        old.setRemark(rule.getRemark());
        ruleMapper.updateById(old);
        return old;
    }

    @Override
    public void delete(Long id) {
        get(id);
        ruleMapper.deleteById(id);
    }

    @Override
    public EvaluationRule active() {
        LambdaQueryWrapper<EvaluationRule> w = new LambdaQueryWrapper<>();
        w.eq(EvaluationRule::getActive, 1).orderByDesc(EvaluationRule::getActive)
                .orderByDesc(EvaluationRule::getCreatedAt).last("LIMIT 1");
        EvaluationRule rule = ruleMapper.selectOne(w);
        if (rule == null) {
            throw BusinessException.notFound("无生效的评优规则，请先在评优规则中启用一条");
        }
        return rule;
    }

    /** 权重必须齐备（量化引擎依赖三项权重算分）。 */
    private void validate(EvaluationRule rule) {
        if (rule.getHygieneWeight() == null || rule.getDisciplineWeight() == null
                || rule.getBonusWeight() == null) {
            throw new BusinessException("评优规则三项权重（卫生/违纪/加分）都不能为空");
        }
        if (rule.getName() == null || rule.getName().isBlank()) {
            throw new BusinessException("评优规则名称不能为空");
        }
    }
}
