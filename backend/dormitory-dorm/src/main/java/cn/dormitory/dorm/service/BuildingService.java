package cn.dormitory.dorm.service;

import cn.dormitory.common.entity.Building;
import cn.dormitory.dorm.dto.BuildingDTO;

import java.util.List;

/** 楼栋管理。 */
public interface BuildingService {

    Building create(BuildingDTO dto);

    void update(BuildingDTO dto);

    void delete(Long id);

    Building getById(Long id);

    List<Building> listAll();
}
