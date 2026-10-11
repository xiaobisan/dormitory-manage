package cn.dormitory.system.dto;

import lombok.Data;

import java.io.Serializable;

/** 用户新增/修改入参。 */
@Data
public class UserDTO implements Serializable {

    /** 修改时必传，新增时为空。 */
    private Long id;

    private String username;

    /** 明文密码（演示用）。新增必填；修改时传则更新，不传则保留原密码。 */
    private String password;

    private String name;

    /** STUDENT/DORM_MANAGER/REPAIR_WORKER/COUNSELOR/ADMIN。 */
    private String role;

    private String phone;

    /** 系别 ID（辅导员）。 */
    private Long deptId;

    /** 楼栋 ID（宿管）。 */
    private Long buildingId;

    /** BUILDING/DEPT/null。 */
    private String dataScopeType;

    private Long dataScopeId;

    private Integer status;

    private String remark;
}
