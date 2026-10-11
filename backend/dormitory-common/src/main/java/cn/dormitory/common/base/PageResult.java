package cn.dormitory.common.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 统一分页返回。各 List 接口返回 {@code Result<PageResult<T>>}，前端读 {@code current/size/total/records}。
 */
@Data
public class PageResult<T> implements Serializable {

    private long current;
    private long size;
    private long total;
    private List<T> records;

    public static <T> PageResult<T> empty(long current, long size) {
        PageResult<T> p = new PageResult<>();
        p.current = current;
        p.size = size;
        p.total = 0;
        p.records = Collections.emptyList();
        return p;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> p = new PageResult<>();
        p.current = page.getCurrent();
        p.size = page.getSize();
        p.total = page.getTotal();
        p.records = page.getRecords();
        return p;
    }
}
