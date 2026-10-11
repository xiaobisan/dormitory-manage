package cn.dormitory.common.base;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 全局拦截器：分页 + 全表更新/删除拦截（防止误写）。
 * <p>独立成配置类，避免与 {@link PageResult} 等公共类混在同一个文件（Java 不允许一个 .java 多个 public 类）。</p>
 */
@Configuration
public class MybatisPlusSetup {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 全表 update/delete 拦截，防止误写
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        // 分页：MySQL 方言（H2 在 MODE=MySQL 下兼容）
        PaginationInnerInterceptor page = new PaginationInnerInterceptor();
        page.setOverflow(true);
        interceptor.addInnerInterceptor(page);
        return interceptor;
    }
}
