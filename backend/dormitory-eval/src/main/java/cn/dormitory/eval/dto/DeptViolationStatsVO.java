package cn.dormitory.eval.dto;

import lombok.Data;

import java.util.List;

/** 系别违纪统计出参。 */
@Data
public class DeptViolationStatsVO {

    /** 系别 ID。 */
    private Long deptId;

    /** 统计区间开始（yyyy-MM），含。 */
    private String fromMonth;

    /** 统计区间结束（yyyy-MM），含。缺省到当前月。 */
    private String toMonth;

    /** 违纪总条数。 */
    private int totalRecords;

    /** 涉及学生总人数（去重 studentId）。 */
    private int totalStudents;

    /** 按违纪类型分组的计数。 */
    private List<TypeCount> byType;

    /** 按月份趋势（yyyy-MM 升序，覆盖 from..to 全区间，无数据补 0）。 */
    private List<MonthCount> byMonth;

    /** 类型计数。 */
    @Data
    public static class TypeCount {
        private Long typeId;
        private String typeCode;
        private String typeName;
        private int count;

        public TypeCount() {}

        public TypeCount(Long typeId, String typeCode, String typeName, int count) {
            this.typeId = typeId;
            this.typeCode = typeCode;
            this.typeName = typeName;
            this.count = count;
        }
    }

    /** 月份计数。 */
    @Data
    public static class MonthCount {
        private String month;
        private int count;

        public MonthCount() {}

        public MonthCount(String month, int count) {
            this.month = month;
            this.count = count;
        }
    }
}
