package cn.dormitory.common.entity;

import cn.dormitory.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 违纪记录（驱动评优扣分 + 辅导员统计）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("violation")
public class Violation extends BaseEntity {
    private Long typeId;
    private Long studentId;
    private Long roomId;
    private LocalDateTime happenedAt;
    private String severity;
    private String description;
    private LocalDateTime handledAt;
    private String handleNote;
}
