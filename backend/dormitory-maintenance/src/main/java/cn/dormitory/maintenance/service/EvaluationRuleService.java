package cn.dormitory.maintenance.service;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.EvaluationRule;

/** 评优规则 Service。 */
public interface EvaluationRuleService {

    PageResult<EvaluationRule> page(Long current, Long size, String name, Integer active);

    EvaluationRule get(Long id);

    EvaluationRule create(EvaluationRule rule);

    EvaluationRule update(EvaluationRule rule);

    void delete(Long id);

    /** 当前生效的评优规则（active=1），无则取最近一条。 */
    EvaluationRule active();
}
