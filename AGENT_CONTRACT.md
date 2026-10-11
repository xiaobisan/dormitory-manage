# 宿舍管理系统 · 多 Agent 并行开发规范

> 本文件是所有开发/验证 agent 的**共享契约**。动手前先读一遍，全程遵守。
> 目标：多个 agent 同时开发不同模块时，代码风格、接口约定、数据访问方式保持一致，最后能一起编译、一起跑通。

## 0. 技术基线（已定死，不要改）

- 语言/版本：Java 21，Spring Boot 3.2.7
- ORM：MyBatis-Plus（`mybatis-plus-spring-boot3-starter`），实体继承 `BaseEntity`，查询用 `LambdaQueryWrapper`
- 数据库：默认 **H2 内存库**（`MODE=MySQL`），配置在 `application.yml`；MySQL 切换靠注释/启用 yml 段落，**不要写死 MySQL 方言 SQL**
- 认证：JWT（`JwtService`），RBAC 五级角色 + 楼栋/系别/本人数据隔离（`UserContext`）
- 前端：Vue 3 + Element Plus + Vite；小程序：uni-app
- 构建：Maven 多 module；`backend/pom.xml` 为父

## 1. Maven 模块边界（谁能依赖谁，不要搞错方向）

```
dormitory-common      公共底座（Result/异常/MP 基类/安全/拦截器）   ← 最底层，依赖所有模块
   ↑
dormitory-system      用户/角色/字典/权限                          ← 依赖 common
   ↑
dormitory-dorm        宿舍域：楼栋/宿舍/床位/入住退宿/水电/报修/访客 ← 依赖 system+common
   ↑
dormitory-maintenance 后勤域：卫生检查 + 评优量化引擎               ← 依赖 dorm+system+common
   ↑
dormitory-eval        违纪 + 看板统计                               ← 依赖 dorm+system+common
dormitory-auth        登录/注册/JWT 签发                            ← 依赖 system+common
   ↑
dormitory-bootstrap   可执行启动，聚合上面所有模块
```

规则：
- 模块间**只允许向下依赖**，禁止循环依赖（尤其 dorm 不能被 maintenance 反向依赖）。
- 跨模块调用的业务对象（DTO）放在**发起方模块**；被调方只暴露 Service 接口 + 实体。
- 每个 agent **只在自己模块的目录里写代码**：`backend/dormitory-<模块>/src/main/java/cn/dormitory/<模块>/`。

## 2. 包结构约定（每个模块统一）

```
cn.dormitory.<模块>/
  entity/       实体（继承 BaseEntity，@TableName 对应 schema.sql 表名）
  mapper/       MyBatis-Plus BaseMapper（纯接口 extends BaseMapper<T>）
  service/      业务接口
  service/impl/ 业务实现（@Service）
  controller/   REST Controller（@RestController，前缀 /api/<域>）
  dto/          入参/出参 DTO
```

## 3. 接口与返回约定（前后端 + 各模块统一）

- 所有接口路径以 `/api/` 开头，按域分组：`/api/system`、`/api/auth`、`/api/dorm`、`/api/maint`、`/api/eval`。
- 方法返回**一律** `Result<T>` 或 `Result<PageResult<T>>`（用 `Result.ok(data)` / `PageResult.of(page)`）。
- 业务错误**抛 `BusinessException`**（或静态方法 `unauthorized/forbidden/notFound`），由 `GlobalExceptionHandler` 统一转 `Result.fail`，**Controller 里不要手写 try/catch 返回 500**。
- 分页入参用 `Long current, Long size`；出参 `PageResult<T>`。

## 4. 数据访问约定

- 实体字段与 `schema.sql` 表一一对应（下划线↔驼峰由 MP 自动映射，yml 已开）。
- 列表查询用 `LambdaQueryWrapper` + 分页；按角色的数据范围过滤**写在 Service**（读 `UserContext.current()` 判断 role/dataScopeId）。
- 禁止在 SQL 里写 H2 不支持的函数；需要复杂统计就 Java 侧聚合。

## 5. 公共底座 API（已实现，直接用，不要重复造）

| 类 | 用途 |
|---|---|
| `cn.dormitory.common.result.Result<T>` | 统一返回，`Result.ok/fail` |
| `cn.dormitory.common.base.PageResult<T>` | 分页返回，`PageResult.of(iPage)` |
| `cn.dormitory.common.base.BaseEntity` | 实体基类（id/createdBy/createdAt/updatedAt/deleted/remark） |
| `cn.dormitory.common.exception.BusinessException` | 业务异常 |
| `cn.dormitory.common.security.UserContext` | 当前用户（userId/name/role/dataScopeId/hasRole） |
| `cn.dormitory.common.security.JwtService` | 签发/解析 JWT |
| `cn.dormitory.common.base.MybatisPlusSetup` | MP 拦截器（已注册分页/全表攻击） |

## 6. 各 Agent 模块清单与交付标准

| Agent | 模块 | 交付物（必须全给） | 验收主链路 |
|---|---|---|---|
| A | dorm | 楼栋/宿舍/床位/入住退宿/水电/报修/访客 的 entity+mapper+service+controller+DTO | 登录→建宿舍→分配入住→录水电生成账单→缴费→报修派单→维修反馈→验收 |
| B | maintenance | 卫生检查计划/记录 + 评优规则/得分/结果 的 entity+service+controller | 建检查计划→打分→录入→按规则算星级→评优结果 |
| C | eval | 违纪类型/记录 + 看板统计接口 | 录入违纪→统计本系违纪分布 |
| D | system+auth | 用户 CRUD/角色/字典 + 登录/注册/JWT | 登录拿 token→按 token 访问受保护接口 |
| E | 前端 Vue | 全量页面（登录/工作台/各模块页面）+ API service 封装 + 路由 | 能登录进系统，核心页面调通后端 |
| F | 小程序 | uni-app 扫码检查 + 学生核心页（报修/缴费/查分） | 扫码进检查页 |

## 7. 验证 Agent 职责

1. 编译：`JAVA_HOME=/d/Apps/Java/jdk21 PATH=/d/Apps/maven/apache-maven-3.9.9/bin:$PATH cd backend && mvn -DskipTests clean install` 必须 BUILD SUCCESS。
2. 启动：`java -jar` 或 `mvn -pl dormitory-bootstrap spring-boot:run`，确认 H2 建表+种子数据成功、`/api/auth/login` 可登录。
3. 一致性：抽查各模块 Controller 是否都返回 `Result`、实体是否都继承 `BaseEntity`、有无循环依赖。
4. 修复编译/启动错误，输出 PASS/FAIL 清单。

## 8. 编译命令（agent 内部用）

首次编译会联网拉取依赖（约 1-2 分钟）。**不要加 `-o`（离线）**，否则新模块缺依赖会失败。

```sh
export JAVA_HOME="/d/Apps/Java/jdk21"
export PATH="/d/Apps/maven/apache-maven-3.9.9/bin:$PATH"
cd /d/project/claude/dormitory-manage/backend
mvn -DskipTests clean install 2>&1 | grep -E "BUILD|ERROR|error:|cannot find" | tail -30
```

只看自己模块编译：`mvn -DskipTests -pl dormitory-<模块> -am install`。

## 9. 关键约定速记（给 agent 的红线）

1. 实体已在 `cn.dormitory.common.entity` 写好并共享，**直接 import 用，不要重复建**（Building/Room/Bed/Enrollment/Usage/Bill/Payment/RepairOrder/Visitor/CheckPlan/CheckRecord/EvaluationRule/EvaluationScore/EvaluationAward/ViolationType/Violation/SysUser）。
2. 每个 Mapper 一个接口 `extends BaseMapper<实体>`，包 `cn.dormitory.<模块>.mapper`，由 bootstrap 的 `@MapperScan("cn.dormitory.**.mapper")` 自动扫描。
3. 所有 Controller 返回 `Result`/`Result<PageResult>`，业务错误抛 `BusinessException`。
4. 数据隔离：Service 里读 `UserContext.current()` 拼楼栋/系别/本人条件。
5. 不要改 `dormitory-common` 的公共类（若需新增公共能力，加新方法不删旧的）。
6. H2 不兼容的 SQL 函数要避开；统计聚合尽量 Java 侧做。
