package cn.dormitory.dorm.service.impl;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.Visitor;
import cn.dormitory.common.exception.BusinessException;
import cn.dormitory.common.security.UserContext;
import cn.dormitory.dorm.dto.VisitorApplyDTO;
import cn.dormitory.dorm.dto.VisitorApproveDTO;
import cn.dormitory.dorm.dto.VisitorLeaveDTO;
import cn.dormitory.dorm.mapper.VisitorMapper;
import cn.dormitory.dorm.service.VisitorService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 访客登记实现：申请 -> 审批 -> 销号。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VisitorServiceImpl implements VisitorService {

    private final VisitorMapper visitorMapper;

    @Override
    @Transactional
    public Visitor apply(VisitorApplyDTO dto) {
        if (dto == null || dto.getStudentId() == null) {
            throw new BusinessException("申请学生不能为空");
        }
        UserContext ctx = UserContext.current();
        // 学生本人申请时强制绑定当前用户
        if (ctx != null && "STUDENT".equals(ctx.getRole()) && !ctx.getUserId().equals(dto.getStudentId())) {
            throw BusinessException.forbidden("只能为本人申请访客登记");
        }
        if (dto.getVisitFrom() != null && dto.getVisitTo() != null && dto.getVisitTo().isBefore(dto.getVisitFrom())) {
            throw new BusinessException("来访结束时间不能早于开始时间");
        }
        Visitor v = new Visitor();
        v.setStudentId(dto.getStudentId());
        v.setName(dto.getName());
        v.setRelation(dto.getRelation());
        v.setPhone(dto.getPhone());
        v.setVisitFrom(dto.getVisitFrom());
        v.setVisitTo(dto.getVisitTo());
        v.setStatus("PENDING");
        v.setRemark(dto.getRemark());
        visitorMapper.insert(v);
        return v;
    }

    @Override
    @Transactional
    public Visitor approve(VisitorApproveDTO dto) {
        Visitor v = requireById(dto.getVisitorId());
        if (!"PENDING".equals(v.getStatus())) {
            throw new BusinessException("该访客登记不是 PENDING 状态，无法审批");
        }
        if (!List.of("APPROVED", "REJECTED").contains(dto.getStatus())) {
            throw new BusinessException("审批结果必须为 APPROVED 或 REJECTED");
        }
        v.setStatus(dto.getStatus());
        v.setRemark(dto.getRemark());
        visitorMapper.updateById(v);
        return v;
    }

    @Override
    @Transactional
    public Visitor leave(VisitorLeaveDTO dto) {
        Visitor v = requireById(dto.getVisitorId());
        if (!"APPROVED".equals(v.getStatus())) {
            throw new BusinessException("只有已批准的访客登记可销号（当前：" + v.getStatus() + "）");
        }
        v.setStatus("LEAVED");
        v.setRemark(dto.getRemark());
        visitorMapper.updateById(v);
        return v;
    }

    @Override
    public Visitor getById(Long id) {
        return requireById(id);
    }

    @Override
    public PageResult<Visitor> page(Long current, Long size, Long studentId, String status) {
        // 学生只能查本人的
        UserContext ctx = UserContext.current();
        Long scopeStudentId = studentId;
        if (ctx != null && "STUDENT".equals(ctx.getRole())) {
            scopeStudentId = ctx.getUserId();
        }
        LambdaQueryWrapper<Visitor> wrapper =
                Wrappers.<Visitor>lambdaQuery()
                        .eq(scopeStudentId != null, Visitor::getStudentId, scopeStudentId)
                        .eq(status != null, Visitor::getStatus, status)
                        .orderByDesc(Visitor::getCreatedAt);
        IPage<Visitor> page = visitorMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    private Visitor requireById(Long id) {
        Visitor v = visitorMapper.selectById(id);
        if (v == null) {
            throw BusinessException.notFound("访客登记不存在");
        }
        return v;
    }
}
