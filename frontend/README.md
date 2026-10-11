# 宿舍管理系统 · 前端（Vue 3 管理后台 + 移动端 H5）

## 技术栈

Vue 3 + Vite + Element Plus + Pinia + Vue Router + Axios + ECharts

## 快速开始

```bash
cd frontend
npm install
npm run dev        # 端口 5173，/api 代理到后端 8080
npm run build      # 构建到 dist/
```

> 后端（Spring Boot，端口 8080）需并行启动：`cd backend && mvn -pl dormitory-bootstrap spring-boot:run`。
> 后端未启动时前端可正常打开，接口请求会失败（404/502）。

## 目录结构

```
frontend/
├── index.html
├── vite.config.js          # 5173 端口，/api -> http://localhost:8080
└── src/
    ├── main.js             # 入口（Element Plus / Pinia / Router 注册）
    ├── App.vue
    ├── api/                # 按域封装的后端接口 service 层（Result/PageResult 约定）
    │   ├── request.js      # Axios 实例 + 拦截器（token 注入、401 跳登录、code!=200 报错）
    │   ├── auth.js         # 登录/注册/me
    │   ├── system.js       # 用户/字典
    │   ├── dorm.js         # 楼栋/宿舍/床位/入住退宿/水电/报修/访客
    │   ├── maint.js        # 卫生检查/评优
    │   └── eval.js         # 违纪/看板统计
    ├── store/
    │   └── user.js         # Pinia：token + 当前用户（localStorage 持久）
    ├── router/
    │   └── index.js        # 路由（按角色 meta.roles）+ 登录守卫
    ├── utils/
    │   ├── format.js       # 字典/状态映射、日期格式化
    │   └── echarts.js      # ECharts 初始化助手
    ├── components/
    │   └── DashboardCharts.vue  # ECharts 看板图表组件
    ├── views/
    │   ├── Login.vue
    │   ├── layout/AdminLayout.vue   # 侧边菜单 + 顶栏 + 主内容区
    │   ├── student/                 # 学生：工作台/报修/缴费/访客/违纪/评优
    │   ├── dorm/                    # 宿管：楼栋宿舍床位/入住退宿/水电/报修派单/访客审批/卫生/评优
    │   ├── repair/                  # 维修工：工单
    │   ├── counselor/               # 辅导员：违纪统计/评优终审/看板
    │   ├── admin/                   # 管理员：用户/字典/看板
    │   └── mobile/                  # 移动端 H5：扫码检查/学生核心页
    └── ...
```

## 接口约定（与后端契约一致）

- 所有接口返回 `Result<T> = {code, message, data}`，成功 `code === 200`。
- 列表接口入参 `current, size`，返回 `Result<PageResult<T>>`，`PageResult = {current, size, total, records}`。
- 鉴权：请求头 `Authorization: Bearer <token>`；401 或 `code===401` 自动清 token 跳登录。

## 角色与路由

| 角色 | 可见路由 |
|---|---|
| STUDENT 学生 | /workbench /my-repair /my-payment /visitor /my-violation /my-award /mobile |
| DORM_MANAGER 宿管 | 全部 dorm/ 下管理页 |
| REPAIR_WORKER 维修工 | /repair/orders |
| COUNSELOR 辅导员 | /counselor/violation /counselor/award-final /counselor/dashboard |
| ADMIN 管理员 | 用户管理、字典管理、全局看板、全部页面 |

侧边菜单按 `userStore.role` 过滤显示；路由 `meta.roles` 控制访问。
