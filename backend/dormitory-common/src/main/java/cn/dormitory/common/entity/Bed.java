package cn.dormitory.common.entity;

import cn.dormitory.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 床位。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bed")
public class Bed extends BaseEntity {
    private Long roomId;
    private Integer bedNo;
    /** EMPTY/OCCUPIED/REPAIR/LOCKED。 */
    private String status;
}
