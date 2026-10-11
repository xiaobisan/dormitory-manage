package cn.dormitory.system.controller;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.result.Result;
import cn.dormitory.system.dto.UserDTO;
import cn.dormitory.system.dto.UserVO;
import cn.dormitory.system.service.SysUserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 系统用户管理（/api/system/user）。写操作仅 ADMIN。 */
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService userService;

    /** 分页列表（可按 role/status 过滤）。 */
    @GetMapping("/page")
    public Result<PageResult<UserVO>> page(@RequestParam(defaultValue = "1") Long current,
                                            @RequestParam(defaultValue = "10") Long size,
                                            @RequestParam(required = false) String role,
                                            @RequestParam(required = false) Integer status) {
        IPage<UserVO> p = userService.page(current, size, role, status);
        return Result.ok(PageResult.of(p));
    }

    /** 查单个。 */
    @GetMapping("/{id}")
    public Result<UserVO> get(@PathVariable Long id) {
        return Result.ok(userService.get(id));
    }

    /** 新增。 */
    @PostMapping
    public Result<Void> create(@RequestBody UserDTO dto) {
        AdminGuard.requireAdmin();
        userService.create(dto);
        return Result.ok();
    }

    /** 修改。 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UserDTO dto) {
        AdminGuard.requireAdmin();
        dto.setId(id);
        userService.update(dto);
        return Result.ok();
    }

    /** 停用 / 启用。 */
    @PostMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam int status) {
        AdminGuard.requireAdmin();
        userService.changeStatus(id, status);
        return Result.ok();
    }

    /** 删除（逻辑删除）。 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        AdminGuard.requireAdmin();
        userService.delete(id);
        return Result.ok();
    }
}
