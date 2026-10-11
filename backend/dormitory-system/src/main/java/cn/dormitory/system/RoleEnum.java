package cn.dormitory.system;

import java.util.Arrays;
import java.util.List;

/**
 * 系统五级角色码。与 {@code sys_user.role}、JWT claims、{@code UserContext} 对应。
 * 角色与数据范围的绑定在用户上配置：宿管→buildingId（BUILDING 范围），辅导员→deptId（DEPT 范围）。
 */
public final class RoleEnum {

    /** 学生。 */
    public static final String STUDENT = "STUDENT";
    /** 宿管（管理某栋楼，数据范围 BUILDING）。 */
    public static final String DORM_MANAGER = "DORM_MANAGER";
    /** 维修工。 */
    public static final String REPAIR_WORKER = "REPAIR_WORKER";
    /** 辅导员（管理某系别，数据范围 DEPT）。 */
    public static final String COUNSELOR = "COUNSELOR";
    /** 系统管理员。 */
    public static final String ADMIN = "ADMIN";

    /** 全部合法角色码。 */
    public static final List<String> ALL =
            Arrays.asList(STUDENT, DORM_MANAGER, REPAIR_WORKER, COUNSELOR, ADMIN);

    private RoleEnum() {
    }

    /** 校验角色码是否合法。 */
    public static boolean isValid(String role) {
        return role != null && ALL.contains(role);
    }
}
