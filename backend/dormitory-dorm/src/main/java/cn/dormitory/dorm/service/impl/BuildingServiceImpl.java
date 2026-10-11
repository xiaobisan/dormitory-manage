package cn.dormitory.dorm.service.impl;

import cn.dormitory.common.entity.Building;
import cn.dormitory.common.exception.BusinessException;
import cn.dormitory.dorm.dto.BuildingDTO;
import cn.dormitory.dorm.mapper.BuildingMapper;
import cn.dormitory.dorm.service.BuildingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/** 楼栋管理实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BuildingServiceImpl implements BuildingService {

    private final BuildingMapper buildingMapper;

    @Override
    @Transactional
    public Building create(BuildingDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getName())) {
            throw new BusinessException("楼栋名称不能为空");
        }
        // 同名校验
        Long count = buildingMapper.selectCount(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<Building>lambdaQuery().eq(Building::getName, dto.getName()));
        if (count != null && count > 0) {
            throw new BusinessException("楼栋名称已存在");
        }
        Building b = new Building();
        b.setName(dto.getName());
        b.setFloorCount(dto.getFloorCount());
        b.setRemark(dto.getRemark());
        buildingMapper.insert(b);
        return b;
    }

    @Override
    @Transactional
    public void update(BuildingDTO dto) {
        Building exist = requireById(dto.getId());
        if (StringUtils.hasText(dto.getName())) {
            exist.setName(dto.getName());
        }
        exist.setFloorCount(dto.getFloorCount());
        exist.setRemark(dto.getRemark());
        buildingMapper.updateById(exist);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        requireById(id);
        buildingMapper.deleteById(id);
    }

    @Override
    public Building getById(Long id) {
        return requireById(id);
    }

    @Override
    public List<Building> listAll() {
        return buildingMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<Building>lambdaQuery().orderByDesc(Building::getId));
    }

    private Building requireById(Long id) {
        Building b = buildingMapper.selectById(id);
        if (b == null) {
            throw BusinessException.notFound("楼栋不存在");
        }
        return b;
    }
}
