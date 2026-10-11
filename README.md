# 校园宿舍管理系统

全栈宿舍管理平台，覆盖「入住—在住—服务—安全—评优—离校」全生命周期。
**创新点**：宿舍评优量化引擎（权重/阈值可配置，结果全程可追溯）+ 移动端扫码打卡（检查员扫宿舍门牌码直达检查页）。

## 技术基线

| 层 | 技术 | 说明 |
|---|---|---|
| 后端 | Java 21 + Spring Boot 3.2.7 | 模块化单体，JDK 21 编译 |
| ORM | MyBatis-Plus 3.5.7 | 实体继承 `BaseEntity`，查询用 `LambdaQueryWrapper` |
| 数据库 | H2 内存库（`MODE=MySQL`） | 本地零配置，启动自动建表+灌种子数据；MySQL 切换见 `application.yml` |
| 认证 | JWT + RBAC | 五级角色 + 楼栋/系别/本人三级数据隔离 |
| 前端 | Vue 3 + Element Plus + Vite | 全量页面（登录/布局/学生/宿管/维修工/辅导员/管理员六大角色域） |
| 移动端 | uni-app（小程序/H5） | 扫码检查 + 学生核心页 |

## 模块结构（Maven 多模块）

```
dormitory-manage/
├── backend/
│   ├── dormitory-common       公共底座（Result/异常/MP基类/安全/UserContext/共享实体）
│   ├── dormitory-system       用户/角色/字典
│   ├── dormitory-dorm         楼栋/宿舍/床位/入住退宿/水电/报修/访客
│   ├── dormitory-maintenance  卫生检查 + 评优量化引擎
│   ├── dormitory-eval         违纪 + 数据看板
│   ├── dormitory-auth         登录/注册/JWT 签发
│   └── dormitory-bootstrap    可执行启动模块（聚合所有模块）
├── frontend/                  Vue 3 管理后台（全量页面）
└── miniprogram/               uni-app 扫码检查 + 学生核心页
```

依赖方向（单向）：`common → system → dorm → maintenance/eval → bootstrap`，auth 依赖 system+common。

## 快速启动

### 1. 后端（H2 零配置，开箱即跑）

```sh
# 需要 JDK 21 + Maven
export JAVA_HOME="/d/Apps/Java/jdk21"
export PATH="/d/Apps/maven/apache-maven-3.9.9/bin:$PATH"
cd dormitory-manage/backend
mvn -DskipTests install                 # 首次联网拉依赖
mvn -DskipTests -pl dormitory-bootstrap spring-boot:run
```

启动后：
- 应用 http://localhost:8080
- H2 控制台 http://localhost:8080/h2-console（JDBC URL `jdbc:h2:mem:dormitory`，用户 `sa`，密码空）
- 建表+种子数据自动完成

### 2. 前端

```sh
cd dormitory-manage/frontend
npm install
npm run dev        # 开发 http://localhost:5173，Vite 代理 /api → 8080
npm run build      # 构建
```

### 3. 小程序（uni-app）

导入 `dormitory-manage/miniprogram/` 到微信开发者工具/HBuilderX。扫码页 `pages/scan/index` 可调试（`pickRoom` 默认 2001 宿舍）。上线需配后端域名白名单。

## 默认账号（种子数据）

| 账号 | 密码 | 角色 | 数据范围 |
|---|---|---|---|
| admin | admin123 | ADMIN | 全平台 |
| dorm_01 | admin123 | DORM_MANAGER | 1号男生宿舍楼（楼栋 1001） |
| repair_01 | admin123 | REPAIR_WORKER | 已派工单 |
| counselor_01 | admin123 | COUNSELOR | 系别 5001 |
| student_01 | student123 | STUDENT | 本人+本宿舍（101 床 1） |

## 核心 API

统一前缀 `/api`，返回 `Result<T>` = `{code, message, data}`，成功 `code===200`。列表入参 `current,size`，返回 `Result<PageResult<T>>`，`PageResult={current,size,total,records}`。鉴权头 `Authorization: Bearer <token>`。

| 域 | 路径前缀 | 主要接口 |
|---|---|---|
| 认证 | `/api/auth` | `POST /login`、`POST /register`、`GET /me` |
| 系统 | `/api/system` | 用户 CRUD、`/dict/violation-type` 字典维护 |
| 宿舍 | `/api/dorm` | 楼栋/宿舍/床位 CRUD、`/enrollment/check-in`+`/check-out`、`/usage`、`/bill/generate`、`/payment`、`/repair/submit`+`/assign`+`/handle`+`/rate`+`/list`、`/visitor/apply`+`/approve`+`/leave` |
| 后勤 | `/api/maint` | 检查计划/记录、`/eval/score/calculate`（评优引擎）、`/eval/award/generate`+`/review` |
| 统计 | `/api/eval` | 违纪类型/记录、`/violation/stats/by-dept`、`/dashboard/overview` |

## 评优量化引擎（核心创新）

```
总分 total = 卫生分×卫生权重 + 违纪分×违纪权重 + 加分×加分权重
            （默认 50/30/20，来自 EvaluationRule，可配置）
星级/称号：按 rule.thresholds JSON（fiveStar/fourStar/threeStar/twoStar/civilRoom）映射
```

- 卫生分：该宿舍该周期最近一次 `CheckRecord.score`
- 违纪分：`100 - SUM(违纪类型 default_deduction)`（下限 0，按 room+period）
- 加分：预留入参（缺省 0）
- 权重/阈值全部读 `EvaluationRule`，**不写死**，后台可配
- 结果落 `EvaluationScore`（roomId+period 唯一 upsert），全程可追溯
- 流程：`generate`(PENDING) → 宿管 `review` INITIAL → 辅导员 `review` FINAL

## 数据隔离

五级角色 + 三级数据范围：
- 宿管（DORM_MANAGER）：`dataScopeType=BUILDING`、`dataScopeId=楼栋id`，查数据按楼栋过滤
- 辅导员（COUNSELOR）：`dataScopeType=DEPT`、`dataScopeId=系别id`，查数据按系别过滤
- 学生（STUDENT）：只查本人/本宿舍

隔离在 Service 层读 `UserContext.current()` 拼条件实现。

## 切换到真实 MySQL（答辩/生产）

`dormitory-bootstrap/src/main/resources/application.yml` 已注释好 MySQL 段。启用：
1. 注释掉 H2 的 `spring.datasource` + `spring.sql.init`
2. 取消 MySQL 段的注释，改账号密码
3. MySQL 里执行 `h2/schema.sql` 建表（H2 MySQL 模式语法与 MySQL 8 兼容）

## 已知待打磨项（非阻塞）

- `RoomController.listByBuilding` 等带 `@RequestParam` 的接口，Maven 编译未带 `-parameters`，运行时会报「parameter name not available」。**不影响启动，只影响个别带请求参数的列表接口**。修复：在父 POM 加：

  ```xml
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration><parameters>true</parameters></configuration>
  </plugin>
  ```

  或在 `application.yml` 加 `spring.mvc: servlet: registerDefaultServlet: false`（无效）。正确做法是加 `-parameters`。
- 密码目前明文存储（演示用），上生产换 BCrypt，system 存取与 auth 校验需一起改。
- 报修超时升级、消息推送、数据大屏可视化——二期再做。

## 验证清单（已跑通）

- ✅ 全工程 `mvn install` BUILD SUCCESS（7 模块）
- ✅ Spring Boot 启动，H2 建表+种子数据成功
- ✅ `/api/auth/login` 登录拿 token（JWT 含 role/dataScope）
- ✅ `/api/auth/me` 受保护接口
- ✅ `/api/dorm/building/list` 受保护接口返回楼栋列表
- ✅ `/api/eval/dashboard/overview` 看板返回（入住率/收缴率/报修/违纪趋势/卫生Top）
