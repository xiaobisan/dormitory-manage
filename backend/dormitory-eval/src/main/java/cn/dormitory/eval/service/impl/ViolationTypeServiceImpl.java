package cn.dormitory.eval.service.impl;

import cn.dormitory.common.entity.ViolationType;
import cn.dormitory.eval.dto.ViolationTypeDTO;
import cn.dormitory.eval.mapper.ViolationTypeEvalMapper;
import cn.dormitory.eval.service.ViolationTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ViolationTypeServiceImpl implements ViolationTypeService {

    private final ViolationTypeEvalMapper typeMapper;

    @Override
    public IPage<ViolationType> page(long current, long size, String keyword) {
        LambdaQueryWrapper<ViolationType> w = new LambdaQueryWrapper<ViolationType>()
                .and(keyword != null && !keyword.isBlank(), q -> q
                        .like(ViolationType::getName, keyword.trim())
                        .or()
                        .like(ViolationType::getCode, keyword.trim()))
                .orderByAsc(ViolationType::getCode);
        return typeMapper.selectPage(Page.of(current, size), w);
    }

    @Override
    public ViolationType getById(Long id) {
        ViolationType t = typeMapper.selectById(id);
        if (t == null) {
            throw cn.dormitory.common.exception.BusinessException.notFound("违纪类型不存在: " + id);
        }
        return t;
    }

    @Override
    @Transactional
    public ViolationType create(ViolationTypeDTO dto) {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) {
            throw new cn.dormitory.common.exception.BusinessException("违纪类型名称不能为空");
        }
        ViolationType t = new ViolationType();
        fill(t, dto);
        typeMapper.insert(t);
        return t;
    }

    @Override
    @Transactional
    public ViolationType update(Long id, ViolationTypeDTO dto) {
        ViolationType t = getById(id);
        fill(t, dto);
        typeMapper.updateById(t);
        return t;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        getById(id);
        typeMapper.deleteById(id);
    }

    private void fill(ViolationType t, ViolationTypeDTO dto) {
        if (dto.getCode() != null) t.setCode(dto.getCode());
        if (dto.getName() != null && !dto.getName().isBlank()) t.setName(dto.getName().trim());
        if (dto.getDefaultDeduction() != null) t.setDefaultDeduction(dto.getDefaultDeduction());
        if (dto.getRemark() != null) t.setRemark(dto.getRemark());
    }
}
