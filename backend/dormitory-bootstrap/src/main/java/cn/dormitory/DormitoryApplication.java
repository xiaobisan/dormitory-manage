package cn.dormitory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用启动类（唯一可执行入口，聚合所有业务模块）。
 * <p>Mapper 包按模块分布，统一在此扫描；组件扫描根包 {@code cn.dormitory} 覆盖所有模块。</p>
 */
@SpringBootApplication
@MapperScan(basePackages = "cn.dormitory.**.mapper")
public class DormitoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DormitoryApplication.class, args);
    }
}
