package cn.dormitory.dorm.service;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.Visitor;
import cn.dormitory.dorm.dto.VisitorApplyDTO;
import cn.dormitory.dorm.dto.VisitorApproveDTO;
import cn.dormitory.dorm.dto.VisitorLeaveDTO;

/** 访客登记：申请 -> 审批 -> 销号。 */
public interface VisitorService {

    /** 学生提交申请（PENDING）。 */
    Visitor apply(VisitorApplyDTO dto);

    /** 宿管/管理员审批（PENDING -> APPROVED / REJECTED）。 */
    Visitor approve(VisitorApproveDTO dto);

    /** 来访结束销号（APPROVED -> LEAVED）。 */
    Visitor leave(VisitorLeaveDTO dto);

    Visitor getById(Long id);

    /** 分页查访客记录。 */
    PageResult<Visitor> page(Long current, Long size, Long studentId, String status);
}
