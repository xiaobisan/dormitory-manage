package cn.dormitory.common.entity;

import cn.dormitory.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 访客登记。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("visitor")
public class Visitor extends BaseEntity {
    private Long studentId;
    private String name;
    private String relation;
    private String phone;
    private LocalDateTime visitFrom;
    private LocalDateTime visitTo;
    /** PENDING/APPROVED/REJECTED/LEAVED。 */
    private String status;
}
