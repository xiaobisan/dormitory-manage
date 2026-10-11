package cn.dormitory.eval.controller;

import cn.dormitory.common.result.Result;
import cn.dormitory.eval.dto.DashboardVO;
import cn.dormitory.eval.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 数据看板统计（/api/eval/dashboard）。按当前角色返回不同数据范围。 */
@RestController
@RequestMapping("/api/eval/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** 看板总览：入住率 / 收缴率 / 报修时效 / 违纪趋势 / 卫生分 Top。 */
    @GetMapping("/overview")
    public Result<DashboardVO> overview() {
        return Result.ok(dashboardService.overview());
    }
}
