package cn.dormitory.common.entity;

import cn.dormitory.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 宿舍。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("room")
public class Room extends BaseEntity {
    private Long buildingId;
    private Integer floor;
    private String roomNo;
    private Integer capacity;
    private Long headStudent;
    /** EMPTY/IN_USE/REPAIR。 */
    private String status;
}
