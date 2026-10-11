package cn.dormitory.eval.controller;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.Violation;
import cn.dormitory.common.result.Result;
import cn.dormitory.eval.dto.DeptViolationStatsVO;
import cn.dormitory.eval.dto.ViolationHandleDTO;
import cn.dormitory.eval.dto.ViolationRecordDTO;
import cn.dormitory.eval.dto.ViolationRecordVO;
import cn.dormitory.eval.service.ViolationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 违纪记录（/api/eval/violation/record）+ 系别统计（/api/eval/violation/stats）。
 * 数据范围由 Service 读 UserContext 判定。
 */
@RestController
@RequestMapping("/api/eval/violation")
@RequiredArgsConstructor
public class ViolationController {

    private final ViolationService violationService;

    // ==================== 违纪记录 ====================

    /** 录入违纪（宿管/辅导员/管理员）。 */
    @PostMapping("/record")
    public Result<Violation> create(@RequestBody ViolationRecordDTO dto) {
        return Result.ok(violationService.createRecord(dto));
    }

    /** 分页查违纪记录。 */
    @GetMapping("/record/list")
    public Result<PageResult<ViolationRecordVO>> list(@RequestParam(defaultValue = "1") long current,
                                                      @RequestParam(defaultValue = "20") long size,
                                                      @RequestParam(required = false) Long studentId,
                                                      @RequestParam(required = false) Long roomId,
                                                      @RequestParam(required = false) Long deptId,
                                                      @RequestParam(required = false) String fromMonth,
                                                      @RequestParam(required = false) String toMonth) {
        return Result.ok(violationService.page(current, size, studentId, roomId, deptId, fromMonth, toMonth));
    }

    /** 单条详情。 */
    @GetMapping("/record/{id}")
    public Result<ViolationRecordVO> detail(@PathVariable Long id) {
        return Result.ok(violationService.detail(id));
    }

    /** 处理违纪（标记已处理 + 处理说明）。 */
    @PostMapping("/record/handle")
    public Result<Void> handle(@RequestParam("id") Long id, @RequestBody(required = false) ViolationHandleDTO dto) {
        violationService.handle(id, dto);
        return Result.ok();
    }

    // ==================== 系别统计 ====================

    /** 系别违纪统计（辅导员传 deptId 缺省=统计本系；管理员可传指定系别）。 */
    @GetMapping("/stats/by-dept")
    public Result<DeptViolationStatsVO> statsByDept(@RequestParam(required = false) Long deptId,
                                                    @RequestParam(required = false) String fromMonth,
                                                    @RequestParam(required = false) String toMonth) {
        return Result.ok(violationService.statsByDept(deptId, fromMonth, toMonth));
    }
}
