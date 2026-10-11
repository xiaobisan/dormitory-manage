package cn.dormitory.maintenance.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 违纪表只读 Mapper（维护侧跨模块查 eval 域数据，H2 兼容写法：substring 提取 'yyyy-MM'）。
 * <p>表 violation 由 eval 模块维护，本接口只做查询，不做建表。</p>
 */
@Mapper
public interface ViolationQueryMapper {

    /** 某宿舍某周期（yyyy-MM）违纪总扣分（按违纪类型字典默认扣分）。 */
    @Select("SELECT COALESCE(SUM(vt.default_deduction), 0) "
            + "FROM violation v "
            + "LEFT JOIN violation_type vt ON v.type_id = vt.id AND vt.deleted = 0 "
            + "WHERE v.room_id = #{roomId} AND v.deleted = 0 "
            + "AND substring(cast(v.happened_at AS varchar), 1, 7) = #{period}")
    BigDecimal sumDeductionByRoomAndPeriod(@Param("roomId") Long roomId, @Param("period") String period);

    /** 某宿舍某周期违纪扣分明细（每条违纪扣多少，便于追溯）。 */
    @Select("SELECT v.id AS violationId, v.description, "
            + "vt.code AS typeCode, COALESCE(vt.name, '未知违纪') AS typeName, "
            + "COALESCE(vt.default_deduction, 0) AS deduction "
            + "FROM violation v "
            + "LEFT JOIN violation_type vt ON v.type_id = vt.id AND vt.deleted = 0 "
            + "WHERE v.room_id = #{roomId} AND v.deleted = 0 "
            + "AND substring(cast(v.happened_at AS varchar), 1, 7) = #{period} "
            + "ORDER BY v.id")
    List<ViolationDeductionRow> deductionDetailByRoomAndPeriod(@Param("roomId") Long roomId, @Param("period") String period);

    /** 单条扣分明细行。 */
    class ViolationDeductionRow {
        private Long violationId;
        private String description;
        private String typeCode;
        private String typeName;
        private BigDecimal deduction;

        public Long getViolationId() { return violationId; }
        public void setViolationId(Long violationId) { this.violationId = violationId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getTypeCode() { return typeCode; }
        public void setTypeCode(String typeCode) { this.typeCode = typeCode; }
        public String getTypeName() { return typeName; }
        public void setTypeName(String typeName) { this.typeName = typeName; }
        public BigDecimal getDeduction() { return deduction; }
        public void setDeduction(BigDecimal deduction) { this.deduction = deduction; }
    }
}
