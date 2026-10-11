package cn.dormitory.common.entity;

import cn.dormitory.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 评优结果/称号（宿管初审 → 辅导员终审）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("evaluation_award")
public class EvaluationAward extends BaseEntity {
    private Long roomId;
    private String period;
    private String awardName;
    /** PENDING/INITIAL/FINAL。 */
    private String finalStatus;
    private Long reviewerId;
}
