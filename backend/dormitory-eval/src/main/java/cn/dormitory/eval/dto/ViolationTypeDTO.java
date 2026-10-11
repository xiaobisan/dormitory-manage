package cn.dormitory.eval.dto;

import lombok.Data;

/**
 * 违纪类型字典入参（CRUD）。
 * <pre>
 * { "code": "ILLEGAL_POWER", "name": "违规用电", "defaultDeduction": 10, "remark": "..." }
 * </pre>
 */
@Data
public class ViolationTypeDTO {
    private Long id;
    private String code;
    private String name;
    private java.math.BigDecimal defaultDeduction;
    private String remark;
}
