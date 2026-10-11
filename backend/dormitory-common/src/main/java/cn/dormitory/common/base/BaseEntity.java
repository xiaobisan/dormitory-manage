package cn.dormitory.common.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 所有实体继承本基类，统一主键与审计字段。
 * <p>逻辑删除通过 {@link #deleted} 由 MyBatis-Plus 自动处理（需配置 {@code logic-delete-field}）。</p>
 */
@Data
@ToString
public abstract class BaseEntity implements Serializable {

    /** 雪花 ID（MyBatis-Plus 默认雪花算法，无需自增）。 */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 创建人。 */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /** 创建时间，插入时自动填充。 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间，插入与更新时自动填充。 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0 正常，1 已删除。 */
    @TableField(select = false)
    private Integer deleted;

    /** 备注。 */
    private String remark;
}
