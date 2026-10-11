package cn.dormitory.maintenance.controller;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.CheckRecord;
import cn.dormitory.common.result.Result;
import cn.dormitory.maintenance.dto.CheckRecordSubmitDTO;
import cn.dormitory.maintenance.service.CheckRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 卫生检查记录接口（/api/maint/check/record）。 */
@RestController
@RequestMapping("/api/maint/check/record")
public class CheckRecordController {

    private final CheckRecordService checkRecordService;

    public CheckRecordController(CheckRecordService checkRecordService) {
        this.checkRecordService = checkRecordService;
    }

    /** 检查打分：按各检查项得分汇总 score，生成记录。 */
    @PostMapping("/submit")
    public Result<CheckRecord> submit(@RequestBody CheckRecordSubmitDTO dto) {
        return Result.ok(checkRecordService.submit(dto));
    }

    /** 查检查记录（按楼栋/周期/宿舍）。 */
    @GetMapping("/list")
    public Result<PageResult<CheckRecord>> list(@RequestParam(defaultValue = "1") Long current,
                                                @RequestParam(defaultValue = "10") Long size,
                                                @RequestParam(required = false) Long buildingId,
                                                @RequestParam(required = false) Long roomId,
                                                @RequestParam(required = false) String period) {
        return Result.ok(checkRecordService.page(current, size, buildingId, roomId, period));
    }

    /** 宿舍卫生分：取该宿舍该周期最近一次检查得分（供评优/展示，缺省 0）。 */
    @GetMapping("/room-score")
    public Result<CheckRecord> roomScore(@RequestParam Long roomId, @RequestParam String period) {
        return Result.ok(checkRecordService.latestRecordOfRoomPeriod(roomId, period));
    }
}
