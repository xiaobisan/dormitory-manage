package cn.dormitory.eval.dto;

import cn.dormitory.common.entity.SysUser;
import cn.dormitory.common.entity.Violation;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 违纪记录列表出参（联表展示学生姓名/宿舍号，H2 兼容做法：先查记录再 Java 补全）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ViolationRecordVO extends Violation {

    private String studentName;
    private Long buildingId;
    private String roomNo;
    private String typeName;
    private String typeCode;

    public static ViolationRecordVO of(Violation v) {
        ViolationRecordVO vo = new ViolationRecordVO();
        vo.setId(v.getId());
        vo.setTypeId(v.getTypeId());
        vo.setStudentId(v.getStudentId());
        vo.setRoomId(v.getRoomId());
        vo.setHappenedAt(v.getHappenedAt());
        vo.setSeverity(v.getSeverity());
        vo.setDescription(v.getDescription());
        vo.setHandledAt(v.getHandledAt());
        vo.setHandleNote(v.getHandleNote());
        vo.setRemark(v.getRemark());
        return vo;
    }

    /** 补学生姓名。 */
    public void fillStudent(SysUser student) {
        if (student != null) this.studentName = student.getName();
    }

    /** 补宿舍归属（宿舍号 + 楼栋）。 */
    public void fillRoom(Long roomId, Long buildingId, String roomNo) {
        this.buildingId = buildingId;
        this.roomNo = roomNo;
    }

    /** 补违纪类型名/码。 */
    public void fillType(String code, String name) {
        this.typeCode = code;
        this.typeName = name;
    }

    /** 是否已处理（有处理时间/处理说明）。 */
    public boolean isHandled() {
        return getHandledAt() != null;
    }
}
