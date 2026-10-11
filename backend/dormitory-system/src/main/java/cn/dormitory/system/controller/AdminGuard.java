package cn.dormitory.system.controller;

import cn.dormitory.common.exception.BusinessException;
import cn.dormitory.common.security.UserContext;
import cn.dormitory.system.RoleEnum;

/** 系统管理侧权限守卫：仅 ADMIN 可操作。 */
final class AdminGuard {

    private AdminGuard() {
    }

    static void requireAdmin() {
        if (!UserContext.hasRole(RoleEnum.ADMIN)) {
            throw BusinessException.forbidden("仅管理员可操作");
        }
    }
}
