package cn.dormitory.common.entity;

import cn.dormitory.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 系统用户（RBAC 五级角色）。密码存明文/BCrypt 由 auth 模块决定，此处仅字段。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    private String username;
    private String password;
    private String name;
    /** STUDENT/DORM_MANAGER/REPAIR_WORKER/COUNSELOR/ADMIN。 */
    private String role;
    private String phone;
    private Long deptId;
    private Long buildingId;
    /** BUILDING/DEPT/null。 */
    private String dataScopeType;
    private Long dataScopeId;
    private Integer status;
}
