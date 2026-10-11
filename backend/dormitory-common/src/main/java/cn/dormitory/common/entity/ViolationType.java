package cn.dormitory.common.entity;

import cn.dormitory.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 违纪类型字典。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("violation_type")
public class ViolationType extends BaseEntity {
    private String code;
    private String name;
    private BigDecimal defaultDeduction;
}
