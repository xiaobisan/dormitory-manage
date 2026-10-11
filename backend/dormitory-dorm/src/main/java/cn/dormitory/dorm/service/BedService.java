package cn.dormitory.dorm.service;

import cn.dormitory.common.entity.Bed;
import cn.dormitory.dorm.dto.BedDTO;

import java.util.List;

/** 床位管理。 */
public interface BedService {

    Bed create(BedDTO dto);

    void update(BedDTO dto);

    void delete(Long id);

    Bed getById(Long id);

    /** 按宿舍查床位。 */
    List<Bed> listByRoom(Long roomId);

    /** 设置床位状态（EMPTY/OCCUPIED/REPAIR/LOCKED）。 */
    void updateStatus(Long bedId, String status);
}
