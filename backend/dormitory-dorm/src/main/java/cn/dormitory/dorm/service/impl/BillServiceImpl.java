package cn.dormitory.dorm.service.impl;

import cn.dormitory.common.entity.Bill;
import cn.dormitory.common.entity.Usage;
import cn.dormitory.common.exception.BusinessException;
import cn.dormitory.common.base.PageResult;
import cn.dormitory.dorm.dto.BillGenerateDTO;
import cn.dormitory.dorm.mapper.BillMapper;
import cn.dormitory.dorm.mapper.UsageMapper;
import cn.dormitory.dorm.service.BillService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** 账单实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillMapper billMapper;
    private final UsageMapper usageMapper;

    @Override
    @Transactional
    public Bill generate(BillGenerateDTO dto) {
        if (dto == null || dto.getRoomId() == null || dto.getPeriod() == null) {
            throw new BusinessException("宿舍与周期不能为空");
        }
        // 同宿舍同周期唯一
        Long count = billMapper.selectCount(
                Wrappers.<Bill>lambdaQuery()
                        .eq(Bill::getRoomId, dto.getRoomId())
                        .eq(Bill::getPeriod, dto.getPeriod()));
        if (count != null && count > 0) {
            throw new BusinessException("该宿舍该周期账单已存在");
        }
        // 汇总用量：金额 = SUM(读数 * 单价)，四舍五入 2 位
        List<Usage> usages = usageMapper.selectList(
                Wrappers.<Usage>lambdaQuery()
                        .eq(Usage::getRoomId, dto.getRoomId())
                        .eq(Usage::getPeriod, dto.getPeriod()));
        if (usages.isEmpty()) {
            throw new BusinessException("该宿舍该周期没有用量记录，无法生成账单");
        }
        BigDecimal total = usages.stream()
                .map(u -> {
                    BigDecimal r = u.getReading() == null ? BigDecimal.ZERO : u.getReading();
                    BigDecimal p = u.getPrice() == null ? BigDecimal.ZERO : u.getPrice();
                    return r.multiply(p);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        Bill bill = new Bill();
        bill.setRoomId(dto.getRoomId());
        bill.setPeriod(dto.getPeriod());
        bill.setAmount(total);
        bill.setStatus("UNPAID");
        bill.setDueAt(dto.getDueAt());
        billMapper.insert(bill);
        return bill;
    }

    @Override
    @Transactional
    public void updateStatus(Long billId, String status) {
        Bill bill = requireById(billId);
        bill.setStatus(status);
        billMapper.updateById(bill);
    }

    @Override
    public Bill getById(Long id) {
        return requireById(id);
    }

    @Override
    public PageResult<Bill> page(Long current, Long size, Long roomId, String period, String status) {
        LambdaQueryWrapper<Bill> wrapper =
                Wrappers.<Bill>lambdaQuery()
                        .eq(roomId != null, Bill::getRoomId, roomId)
                        .eq(period != null, Bill::getPeriod, period)
                        .eq(status != null, Bill::getStatus, status)
                        .orderByDesc(Bill::getPeriod);
        IPage<Bill> page = billMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    private Bill requireById(Long id) {
        Bill b = billMapper.selectById(id);
        if (b == null) {
            throw BusinessException.notFound("账单不存在");
        }
        return b;
    }
}
