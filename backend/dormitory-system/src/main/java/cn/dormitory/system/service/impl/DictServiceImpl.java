package cn.dormitory.system.service.impl;

import cn.dormitory.common.entity.ViolationType;
import cn.dormitory.system.mapper.ViolationTypeMapper;
import cn.dormitory.system.service.DictService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 字典维护实现（违纪类型）。 */
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final ViolationTypeMapper violationTypeMapper;

    @Override
    public IPage<ViolationType> violationTypePage(long current, long size, String keyword) {
        LambdaQueryWrapper<ViolationType> w = new LambdaQueryWrapper<ViolationType>()
                .like(keyword != null && !keyword.isBlank(), ViolationType::getName, keyword)
                .orderByAsc(ViolationType::getCode);
        return violationTypeMapper.selectPage(Page.of(current, size), w);
    }

    @Override
    @Transactional
    public void addViolationType(ViolationType type) {
        violationTypeMapper.insert(type);
    }

    @Override
    @Transactional
    public void updateViolationType(ViolationType type) {
        if (type.getId() == null) {
            throw new cn.dormitory.common.exception.BusinessException("违纪类型 ID 不能为空");
        }
        violationTypeMapper.updateById(type);
    }

    @Override
    @Transactional
    public void deleteViolationType(Long id) {
        violationTypeMapper.deleteById(id);
    }
}
