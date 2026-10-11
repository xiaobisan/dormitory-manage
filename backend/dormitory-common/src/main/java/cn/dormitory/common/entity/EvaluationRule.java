package cn.dormitory.common.entity;

import cn.dormitory.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 评优规则（可配权重/阈值，核心创新支撑）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("evaluation_rule")
public class EvaluationRule extends BaseEntity {
    private String name;
    private BigDecimal hygieneWeight;
    private BigDecimal disciplineWeight;
    private BigDecimal bonusWeight;
    /** JSON：各星级/称号阈值 {fiveStar,fourStar,...,civilRoom}。 */
    private String thresholds;
    private Integer active;
}
