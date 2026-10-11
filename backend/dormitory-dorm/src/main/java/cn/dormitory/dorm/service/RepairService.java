package cn.dormitory.dorm.service;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.RepairOrder;
import cn.dormitory.dorm.dto.RepairAssignDTO;
import cn.dormitory.dorm.dto.RepairHandleDTO;
import cn.dormitory.dorm.dto.RepairRateDTO;
import cn.dormitory.dorm.dto.RepairSubmitDTO;

/** 报修工单闭环。 */
public interface RepairService {

    /** 提交报修（PENDING）。 */
    RepairOrder submit(RepairSubmitDTO dto);

    /** 派单给维修工（PENDING/PROCESSING -> ASSIGNED + assigneeId）。 */
    RepairOrder assign(RepairAssignDTO dto);

    /** 维修工接单/反馈（ASSIGNED -> PROCESSING/DONE/REJECTED；DONE 时写 resolvedAt）。 */
    RepairOrder handle(RepairHandleDTO dto);

    /** 验收评价（仅 DONE 可评，写 rating/ratingNote）。 */
    RepairOrder rate(RepairRateDTO dto);

    RepairOrder getById(Long id);

    /**
     * 按角色分页查工单：
     * <ul>
     *   <li>STUDENT：查本人（报修人=当前用户）或本人宿舍的工单</li>
     *   <li>REPAIR_WORKER：查已派给自己的（assigneeId=当前用户）</li>
     *   <li>DORM_MANAGER：查本楼栋的工单</li>
     *   <li>其它/ADMIN：查全部</li>
     * </ul>
     */
    PageResult<RepairOrder> pageByRole(Long current, Long size, String status);
}
