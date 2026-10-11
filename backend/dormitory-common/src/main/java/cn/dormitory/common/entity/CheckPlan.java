package cn.dormitory.common.entity;

import cn.dormitory.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 卫生检查计划（含检查项 JSON 模板）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("check_plan")
public class CheckPlan extends BaseEntity {
    private String name;
    /** WEEKLY/MONTHLY。 */
    private String cycle;
    /** JSON：[{key,name,max}] 检查项模板。 */
    private String itemTemplate;
    private Integer active;
}
