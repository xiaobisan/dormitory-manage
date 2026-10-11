package cn.dormitory.dorm.dto;

import lombok.Data;

import java.io.Serializable;

/** 宿舍新增/修改入参。 */
@Data
public class RoomDTO implements Serializable {
    private Long id;
    private Long buildingId;
    private Integer floor;
    private String roomNo;
    private Integer capacity;
    private Long headStudent;
    /** EMPTY/IN_USE/REPAIR。 */
    private String status;
    private String remark;
}
