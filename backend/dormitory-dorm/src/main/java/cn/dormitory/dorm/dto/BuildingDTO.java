package cn.dormitory.dorm.dto;

import lombok.Data;

import java.io.Serializable;

/** 楼栋新增/修改入参。 */
@Data
public class BuildingDTO implements Serializable {
    /** 更新时必传，新增时为空。 */
    private Long id;
    private String name;
    private Integer floorCount;
    private String remark;
}
