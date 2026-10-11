package cn.dormitory.dorm.service;

import cn.dormitory.common.base.PageResult;
import cn.dormitory.common.entity.Enrollment;
import cn.dormitory.dorm.dto.CheckInDTO;
import cn.dormitory.dorm.dto.CheckOutDTO;

/** 入住/退宿。 */
public interface EnrollmentService {

    /** 入住：校验床位空闲 -> 生成 ACTIVE 入住记录 -> 床位置 OCCUPIED。 */
    Enrollment checkIn(CheckInDTO dto);

    /** 退宿：入住记录置 CHECKED_OUT -> 床位置 EMPTY。 */
    Enrollment checkOut(CheckOutDTO dto);

    Enrollment getById(Long id);

    /**
     * 分页查询入住记录。
     *
     * @param studentId   指定学生（可空）
     * @param buildingId  指定楼栋（可空）
     * @param onlyActive  只看 ACTIVE（可空）
     */
    PageResult<Enrollment> page(Long current, Long size, Long studentId, Long buildingId, Boolean onlyActive);
}
