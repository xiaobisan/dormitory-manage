package cn.dormitory.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充：插入/更新时自动写 createdAt/updatedAt，
 * 插入时写 createdBy（取自 {@link cn.dormitory.common.security.UserContext}）。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        cn.dormitory.common.security.UserContext ctx = cn.dormitory.common.security.UserContext.current();
        if (ctx != null && ctx.getUserId() != null && metaObject.hasGetter("createdBy")) {
            this.strictInsertFill(metaObject, "createdBy", Long.class, ctx.getUserId());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
