package cn.dormitory.dorm.service;

import cn.dormitory.common.entity.Usage;
import cn.dormitory.dorm.dto.UsageDTO;

import java.util.List;

/** 水电用量。 */
public interface UsageService {

    Usage create(UsageDTO dto);

    void update(UsageDTO dto);

    void delete(Long id);

    Usage getById(Long id);

    List<Usage> listByRoomPeriod(Long roomId, String period);
}
