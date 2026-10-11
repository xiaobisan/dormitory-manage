package cn.dormitory.common.entity;

import cn.dormitory.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 卫生检查记录（打分明细）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("check_record")
public class CheckRecord extends BaseEntity {
    private Long planId;
    private Long roomId;
    private BigDecimal score;
    /** JSON：各检查项得分 {key:score}。 */
    private String detail;
    private String photoUrl;
    private Long checkedBy;
    private LocalDateTime checkedAt;
}
