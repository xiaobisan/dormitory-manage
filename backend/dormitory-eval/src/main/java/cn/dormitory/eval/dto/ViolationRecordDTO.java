package cn.dormitory.eval.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 违纪记录录入入参。
 * <pre>
 * {
 *   "studentId": 100, "roomId": 2001, "typeId": 9101,
 *   "happenedAt": "2026-09-12T01:30:00",
 *   "severity": "MAJOR",
 *   "description": "私接大功率电器"
 * }
 * </pre>
 * studentId/roomId 必填（后端校验存在）；typeId 建议必填。
 */
@Data
public class ViolationRecordDTO {
    private Long studentId;
    private Long roomId;
    private Long typeId;
    private LocalDateTime happenedAt;
    /** MINOR/MODERATE/MAJOR（严重程度，可空）。 */
    private String severity;
    private String description;
}
