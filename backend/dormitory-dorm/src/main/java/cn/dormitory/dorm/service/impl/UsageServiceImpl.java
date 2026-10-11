package cn.dormitory.dorm.service.impl;

import cn.dormitory.common.entity.Room;
import cn.dormitory.common.entity.Usage;
import cn.dormitory.common.exception.BusinessException;
import cn.dormitory.dorm.dto.UsageDTO;
import cn.dormitory.dorm.mapper.RoomMapper;
import cn.dormitory.dorm.mapper.UsageMapper;
import cn.dormitory.dorm.service.UsageService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 水电用量实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsageServiceImpl implements UsageService {

    private static final List<String> VALID_TYPE = List.of("WATER", "ELECTRIC");

    private final UsageMapper usageMapper;
    private final RoomMapper roomMapper;

    @Override
    @Transactional
    public Usage create(UsageDTO dto) {
        validate(dto);
        // 同宿舍同类型同周期唯一
        Long count = usageMapper.selectCount(
                Wrappers.<Usage>lambdaQuery()
                        .eq(Usage::getRoomId, dto.getRoomId())
                        .eq(Usage::getType, dto.getType())
                        .eq(Usage::getPeriod, dto.getPeriod()));
        if (count != null && count > 0) {
            throw new BusinessException("该宿舍该类型该周期用量已存在，请修改");
        }
        Usage u = new Usage();
        u.setRoomId(dto.getRoomId());
        u.setType(dto.getType());
        u.setPeriod(dto.getPeriod());
        u.setReading(dto.getReading());
        u.setPrice(dto.getPrice());
        u.setRemark(dto.getRemark());
        usageMapper.insert(u);
        return u;
    }

    @Override
    @Transactional
    public void update(UsageDTO dto) {
        Usage exist = requireById(dto.getId());
        exist.setReading(dto.getReading());
        exist.setPrice(dto.getPrice());
        exist.setRemark(dto.getRemark());
        usageMapper.updateById(exist);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireById(id);
        usageMapper.deleteById(id);
    }

    @Override
    public Usage getById(Long id) {
        return requireById(id);
    }

    @Override
    public List<Usage> listByRoomPeriod(Long roomId, String period) {
        if (roomMapper.selectById(roomId) == null) {
            throw BusinessException.notFound("宿舍不存在");
        }
        return usageMapper.selectList(
                Wrappers.<Usage>lambdaQuery()
                        .eq(Usage::getRoomId, roomId)
                        .eq(period != null, Usage::getPeriod, period));
    }

    private void validate(UsageDTO dto) {
        if (dto == null || dto.getRoomId() == null) {
            throw new BusinessException("宿舍与周期不能为空");
        }
        if (!VALID_TYPE.contains(dto.getType())) {
            throw new BusinessException("非法用量类型（应为 WATER/ELECTRIC）");
        }
        if (roomMapper.selectById(dto.getRoomId()) == null) {
            throw BusinessException.notFound("宿舍不存在");
        }
    }

    private Usage requireById(Long id) {
        Usage u = usageMapper.selectById(id);
        if (u == null) {
            throw BusinessException.notFound("用量记录不存在");
        }
        return u;
    }
}
