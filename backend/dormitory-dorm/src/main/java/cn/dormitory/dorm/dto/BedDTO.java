package cn.dormitory.dorm.dto;

import lombok.Data;

import java.io.Serializable;

/** 床位新增/修改入参。 */
@Data
public class BedDTO implements Serializable {
    private Long id;
    private Long roomId;
    private Integer bedNo;
    /** EMPTY/OCCUPIED/REPAIR/LOCKED。 */
    private String status;
    private String remark;
}
