package cn.dormitory.eval.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 数据看板总览出参。按角色返回不同范围（宿管=本楼栋，辅导员=本系，管理员=全量）。
 * 指标统一在 0.0 ~ 1.0 表示比率；时间类指标为小数（小时）。
 */
@Data
public class DashboardVO {

    // ===== 数据范围 =====
    /** 当前统计的数据范围说明：ADMIN="全部" / DORM_MANAGER=楼栋名 / COUNSELOR=系别 ID。 */
    private String scopeLabel;

    // ===== 入住率 =====
    /** 当前 ACTIVE 入住数。 */
    private int activeEnrollmentCount;
    /** 可用床位总数（床位数）。 */
    private int totalBedCount;
    /** 床位使用率 = ACTIVE 入住数 / 总床位数（0~1，两位小数）。 */
    private BigDecimal occupancyRate;

    // ===== 水电费收缴率 =====
    /** 已缴账单数（PAID + OVERDUE 视为已出账，含 PAID）。 */
    private int paidBillCount;
    /** 总账单数。 */
    private int totalBillCount;
    /** 收缴率 = PAID 数 / 总账单数（0~1，两位小数）。 */
    private BigDecimal billCollectionRate;

    // ===== 报修时效 =====
    /** 各状态工单数量。 */
    private List<StatusCount> repairStatusCounts;
    /** 已完成工单的平均处理时长（小时，保留 1 位；无完成工单为 null）。 */
    private BigDecimal avgRepairHours;

    // ===== 违纪趋势（近 6 个月） =====
    /** 近 6 个月每月违纪数（yyyy-MM 升序，覆盖完整区间，无数据补 0）。 */
    private List<DeptViolationStatsVO.MonthCount> violationTrend;

    // ===== 卫生分 Top 宿舍（最近周期） =====
    private List<TopRoomScore> topRooms;

    /** 状态计数。 */
    @Data
    public static class StatusCount {
        private String status;
        private int count;

        public StatusCount() {}

        public StatusCount(String status, int count) {
            this.status = status;
            this.count = count;
        }
    }

    /** 单宿舍卫生分。 */
    @Data
    public static class TopRoomScore {
        private Long roomId;
        private String roomNo;
        private Long buildingId;
        private String buildingName;
        /** 对应最近周期，形如 2026-10。 */
        private String period;
        private BigDecimal totalScore;
        private Integer starLevel;
    }
}
