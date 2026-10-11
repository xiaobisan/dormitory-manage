package cn.dormitory.common.entity;

import cn.dormitory.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 入住记录（学生↔床位绑定，含退宿）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("enrollment")
public class Enrollment extends BaseEntity {
    private Long studentId;
    private Long buildingId;
    private Long roomId;
    private Long bedId;
    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;
    /** ACTIVE/CHECKED_OUT/ON_LEAVE。 */
    private String status;
    private String reason;
}
