package cn.dormitory.common.entity;

import cn.dormitory.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 评优得分（按周期，宿舍维度，量化可追溯）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("evaluation_score")
public class EvaluationScore extends BaseEntity {
    private Long roomId;
    private String period;
    private BigDecimal hygieneScore;
    private BigDecimal disciplineScore;
    private BigDecimal bonusScore;
    private BigDecimal totalScore;
    private Integer starLevel;
}
