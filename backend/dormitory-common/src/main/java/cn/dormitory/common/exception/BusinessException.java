package cn.dormitory.common.exception;

import lombok.Getter;

/**
 * 业务异常。各模块用它抛出可预期的业务错误（如"床位已住""未授权"），
 * 由 {@link cn.dormitory.common.config.GlobalExceptionHandler} 统一转成 {@code Result.fail}。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /** 401 未认证（令牌缺失/过期）。 */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message);
    }

    /** 403 无权限。 */
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }

    /** 404 资源不存在。 */
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }
}
