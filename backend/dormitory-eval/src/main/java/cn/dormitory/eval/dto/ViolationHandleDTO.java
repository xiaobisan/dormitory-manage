package cn.dormitory.eval.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 违纪记录处理入参。
 * <pre>
 * { "handleNote": "已批评教育，家长谈话" }
 * </pre>
 * handledAt 缺省由后端取当前时间。
 */
@Data
public class ViolationHandleDTO {
    private String handleNote;
    private LocalDateTime handledAt;
}
