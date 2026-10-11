package cn.dormitory.dorm.service.impl;

import cn.dormitory.common.entity.Bed;
import cn.dormitory.common.entity.Room;
import cn.dormitory.common.exception.BusinessException;
import cn.dormitory.dorm.dto.BedDTO;
import cn.dormitory.dorm.mapper.BedMapper;
import cn.dormitory.dorm.mapper.RoomMapper;
import cn.dormitory.dorm.service.BedService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/** 床位管理实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BedServiceImpl implements BedService {

    private static final List<String> VALID_STATUS = Arrays.asList("EMPTY", "OCCUPIED", "REPAIR", "LOCKED");

    private final BedMapper bedMapper;
    private final RoomMapper roomMapper;

    @Override
    @Transactional
    public Bed create(BedDTO dto) {
        if (dto == null || dto.getRoomId() == null || dto.getBedNo() == null) {
            throw new BusinessException("宿舍与床号不能为空");
        }
        requireRoom(dto.getRoomId());
        Long count = bedMapper.selectCount(
                Wrappers.<Bed>lambdaQuery()
                        .eq(Bed::getRoomId, dto.getRoomId())
                        .eq(Bed::getBedNo, dto.getBedNo()));
        if (count != null && count > 0) {
            throw new BusinessException("该宿舍下床位号已存在");
        }
        Bed b = new Bed();
        b.setRoomId(dto.getRoomId());
        b.setBedNo(dto.getBedNo());
        b.setStatus(dto.getStatus() == null ? "EMPTY" : dto.getStatus());
        b.setRemark(dto.getRemark());
        bedMapper.insert(b);
        return b;
    }

    @Override
    @Transactional
    public void update(BedDTO dto) {
        Bed exist = requireById(dto.getId());
        exist.setBedNo(dto.getBedNo());
        if (dto.getRoomId() != null) {
            requireRoom(dto.getRoomId());
            exist.setRoomId(dto.getRoomId());
        }
        if (dto.getStatus() != null) {
            checkStatus(dto.getStatus());
            exist.setStatus(dto.getStatus());
        }
        exist.setRemark(dto.getRemark());
        bedMapper.updateById(exist);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Bed exist = requireById(id);
        if ("OCCUPIED".equals(exist.getStatus())) {
            throw new BusinessException("床位已有人入住，不能删除");
        }
        bedMapper.deleteById(id);
    }

    @Override
    public Bed getById(Long id) {
        return requireById(id);
    }

    @Override
    public List<Bed> listByRoom(Long roomId) {
        requireRoom(roomId);
        return bedMapper.selectList(
                Wrappers.<Bed>lambdaQuery()
                        .eq(Bed::getRoomId, roomId)
                        .orderByAsc(Bed::getBedNo));
    }

    @Override
    @Transactional
    public void updateStatus(Long bedId, String status) {
        Bed exist = requireById(bedId);
        checkStatus(status);
        exist.setStatus(status);
        bedMapper.updateById(exist);
    }

    private void checkStatus(String status) {
        if (!VALID_STATUS.contains(status)) {
            throw new BusinessException("非法床位状态：" + status);
        }
    }

    private void requireRoom(Long roomId) {
        if (roomId == null) {
            throw new BusinessException("所属宿舍不能为空");
        }
        if (roomMapper.selectById(roomId) == null) {
            throw BusinessException.notFound("宿舍不存在");
        }
    }

    private Bed requireById(Long id) {
        Bed b = bedMapper.selectById(id);
        if (b == null) {
            throw BusinessException.notFound("床位不存在");
        }
        return b;
    }
}
