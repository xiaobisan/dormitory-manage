package cn.dormitory.common.security;

import lombok.Data;

/**
 * 当前登录用户上下文。基于 ThreadLocal，由 {@link JwtAuthFilter} 在鉴权后填充。
 * 各模块 Service 通过 {@link #current()} 读取当前操作人做数据范围判断。
 * <p>注意：请求结束（FilterChain 退出）后 {@link #clear()} 必须被调用，避免线程池污染。</p>
 */
@Data
public class UserContext {

    private static final ThreadLocal<UserContext> HOLDER = new ThreadLocal<>();

    /** 用户 ID（sys_user.id）。 */
    private Long userId;
    /** 姓名（展示用）。 */
    private String name;
    /** 角色码：STUDENT / DORM_MANAGER / REPAIR_WORKER / COUNSELOR / ADMIN。 */
    private String role;
    /** 数据范围：楼栋 ID（宿管）或系别 ID（辅导员），其它角色为 null。 */
    private Long dataScopeId;
    /** 数据范围类型：BUILDING / DEPT / null。 */
    private String dataScopeType;

    public static UserContext current() {
        return HOLDER.get();
    }

    public static void set(UserContext ctx) {
        HOLDER.set(ctx);
    }

    public static void clear() {
        HOLDER.remove();
    }

    /** 当前用户是否具备指定角色。 */
    public static boolean hasRole(String... roles) {
        UserContext c = HOLDER.get();
        if (c == null || c.getRole() == null) return false;
        for (String r : roles) {
            if (c.getRole().equals(r)) return true;
        }
        return false;
    }
}
