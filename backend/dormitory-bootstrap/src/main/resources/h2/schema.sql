-- H2 (MySQL 模式) 建表脚本 —— 宿舍管理系统
-- 主键统一 BIGINT（雪花，应用生成）；逻辑删除字段 deleted DEFAULT 0

-- 系统用户
CREATE TABLE sys_user (
  id           BIGINT PRIMARY KEY,
  username     VARCHAR(64)  NOT NULL,
  password     VARCHAR(128) NOT NULL,
  name         VARCHAR(64)  NOT NULL,
  role         VARCHAR(32)  NOT NULL,        -- STUDENT/DORM_MANAGER/REPAIR_WORKER/COUNSELOR/ADMIN
  phone        VARCHAR(32),
  dept_id      BIGINT,
  building_id  BIGINT,                        -- 宿管管哪栋
  data_scope_type VARCHAR(16),               -- BUILDING/DEPT/null
  data_scope_id   BIGINT,
  status       TINYINT DEFAULT 1,
  created_by   BIGINT,
  created_at   DATETIME,
  updated_at   DATETIME,
  deleted      INT DEFAULT 0,
  remark       VARCHAR(255)
);
CREATE UNIQUE INDEX uk_sys_user_username ON sys_user(username);

-- 楼栋
CREATE TABLE building (
  id         BIGINT PRIMARY KEY,
  name       VARCHAR(64) NOT NULL,
  floor_count INT DEFAULT 0,
  created_by BIGINT, created_at DATETIME, updated_at DATETIME, deleted INT DEFAULT 0, remark VARCHAR(255)
);

-- 宿舍
CREATE TABLE room (
  id           BIGINT PRIMARY KEY,
  building_id  BIGINT NOT NULL,
  floor        INT,
  room_no      VARCHAR(32) NOT NULL,
  capacity     INT DEFAULT 4,
  head_student BIGINT,                          -- 宿舍长（sys_user.id）
  status       VARCHAR(16) DEFAULT 'IN_USE',    -- EMPTY/IN_USE/REPAIR
  created_by   BIGINT, created_at DATETIME, updated_at DATETIME, deleted INT DEFAULT 0, remark VARCHAR(255)
);
CREATE UNIQUE INDEX uk_room_no ON room(building_id, room_no);

-- 床位
CREATE TABLE bed (
  id          BIGINT PRIMARY KEY,
  room_id     BIGINT NOT NULL,
  bed_no      INT NOT NULL,
  status      VARCHAR(16) DEFAULT 'EMPTY',      -- EMPTY/OCCUPIED/REPAIR/LOCKED
  created_by  BIGINT, created_at DATETIME, updated_at DATETIME, deleted INT DEFAULT 0, remark VARCHAR(255)
);
CREATE UNIQUE INDEX uk_bed_no ON bed(room_id, bed_no);

-- 入住记录（学生↔床位绑定）
CREATE TABLE enrollment (
  id           BIGINT PRIMARY KEY,
  student_id   BIGINT NOT NULL,
  building_id  BIGINT,
  room_id      BIGINT NOT NULL,
  bed_id       BIGINT NOT NULL,
  check_in_at  DATETIME,
  check_out_at DATETIME,
  status       VARCHAR(16) DEFAULT 'ACTIVE',    -- ACTIVE/CHECKED_OUT/ON_LEAVE
  reason       VARCHAR(255),
  created_by   BIGINT, created_at DATETIME, updated_at DATETIME, deleted INT DEFAULT 0, remark VARCHAR(255)
);

-- 水电用量
CREATE TABLE usage (
  id            BIGINT PRIMARY KEY,
  room_id       BIGINT NOT NULL,
  type          VARCHAR(8) NOT NULL,            -- WATER/ELECTRIC
  period        VARCHAR(16) NOT NULL,           -- 2026-09
  reading       DECIMAL(10,2),
  price         DECIMAL(10,2) DEFAULT 0,
  created_by    BIGINT, created_at DATETIME, updated_at DATETIME, deleted INT DEFAULT 0, remark VARCHAR(255)
);
CREATE UNIQUE INDEX uk_usage ON usage(room_id, type, period);

-- 费用单
CREATE TABLE bill (
  id          BIGINT PRIMARY KEY,
  room_id     BIGINT NOT NULL,
  period      VARCHAR(16) NOT NULL,
  amount      DECIMAL(10,2) DEFAULT 0,
  status      VARCHAR(16) DEFAULT 'UNPAID',     -- UNPAID/PAID/OVERDUE
  due_at      DATETIME,
  paid_at     DATETIME,
  created_by  BIGINT, created_at DATETIME, updated_at DATETIME, deleted INT DEFAULT 0, remark VARCHAR(255)
);
CREATE UNIQUE INDEX uk_bill ON bill(room_id, period);

-- 缴费记录
CREATE TABLE payment (
  id          BIGINT PRIMARY KEY,
  bill_id     BIGINT NOT NULL,
  amount      DECIMAL(10,2),
  paid_at     DATETIME,
  payer_id    BIGINT,
  method      VARCHAR(16),
  created_by  BIGINT, created_at DATETIME, updated_at DATETIME, deleted INT DEFAULT 0, remark VARCHAR(255)
);

-- 报修工单
CREATE TABLE repair_order (
  id           BIGINT PRIMARY KEY,
  room_id      BIGINT,
  student_id   BIGINT,
  category     VARCHAR(32),
  description  VARCHAR(500),
  photo_url    VARCHAR(255),
  status       VARCHAR(16) DEFAULT 'PENDING',   -- PENDING/ASSIGNED/PROCESSING/DONE/REJECTED
  assignee_id  BIGINT,
  resolved_at  DATETIME,
  rating       INT,
  rating_note  VARCHAR(255),
  created_by   BIGINT, created_at DATETIME, updated_at DATETIME, deleted INT DEFAULT 0, remark VARCHAR(255)
);

-- 访客登记
CREATE TABLE visitor (
  id           BIGINT PRIMARY KEY,
  student_id   BIGINT NOT NULL,
  name         VARCHAR(64),
  relation     VARCHAR(32),
  phone        VARCHAR(32),
  visit_from   DATETIME,
  visit_to     DATETIME,
  status       VARCHAR(16) DEFAULT 'APPROVED',  -- PENDING/APPROVED/REJECTED/LEAVED
  created_by   BIGINT, created_at DATETIME, updated_at DATETIME, deleted INT DEFAULT 0, remark VARCHAR(255)
);

-- 卫生检查计划
CREATE TABLE check_plan (
  id            BIGINT PRIMARY KEY,
  name          VARCHAR(64) NOT NULL,
  cycle         VARCHAR(16),                    -- WEEKLY/MONTHLY
  item_template TEXT,                            -- JSON 检查项模板
  active        TINYINT DEFAULT 1,
  created_by    BIGINT, created_at DATETIME, updated_at DATETIME, deleted INT DEFAULT 0, remark VARCHAR(255)
);

-- 卫生检查记录
CREATE TABLE check_record (
  id           BIGINT PRIMARY KEY,
  plan_id      BIGINT NOT NULL,
  room_id      BIGINT NOT NULL,
  score        DECIMAL(5,1),
  detail       TEXT,                              -- JSON 各检查项得分
  photo_url    VARCHAR(255),
  checked_by   BIGINT,
  checked_at   DATETIME,
  created_by   BIGINT, created_at DATETIME, updated_at DATETIME, deleted INT DEFAULT 0, remark VARCHAR(255)
);

-- 评优规则（可配权重/阈值）
CREATE TABLE evaluation_rule (
  id            BIGINT PRIMARY KEY,
  name          VARCHAR(64) NOT NULL,
  hygiene_weight DECIMAL(4,2) DEFAULT 50,
  discipline_weight DECIMAL(4,2) DEFAULT 30,
  bonus_weight  DECIMAL(4,2) DEFAULT 20,
  thresholds    TEXT,                            -- JSON: 各星级分数阈值
  active        TINYINT DEFAULT 1,
  created_by    BIGINT, created_at DATETIME, updated_at DATETIME, deleted INT DEFAULT 0, remark VARCHAR(255)
);

-- 评优得分
CREATE TABLE evaluation_score (
  id            BIGINT PRIMARY KEY,
  room_id       BIGINT NOT NULL,
  period        VARCHAR(16) NOT NULL,
  hygiene_score DECIMAL(6,2),
  discipline_score DECIMAL(6,2),
  bonus_score   DECIMAL(6,2),
  total_score   DECIMAL(6,2),
  star_level    INT,
  created_by    BIGINT, created_at DATETIME, updated_at DATETIME, deleted INT DEFAULT 0, remark VARCHAR(255)
);
CREATE UNIQUE INDEX uk_eval ON evaluation_score(room_id, period);

-- 评优结果/称号
CREATE TABLE evaluation_award (
  id            BIGINT PRIMARY KEY,
  room_id       BIGINT NOT NULL,
  period        VARCHAR(16) NOT NULL,
  award_name    VARCHAR(64),
  final_status  VARCHAR(16) DEFAULT 'PENDING',   -- PENDING/INITIAL/FINAL
  reviewer_id   BIGINT,
  created_by    BIGINT, created_at DATETIME, updated_at DATETIME, deleted INT DEFAULT 0, remark VARCHAR(255)
);

-- 违纪类型字典
CREATE TABLE violation_type (
  id          BIGINT PRIMARY KEY,
  code        VARCHAR(32),
  name        VARCHAR(64) NOT NULL,
  default_deduction DECIMAL(5,1) DEFAULT 5,
  created_by  BIGINT, created_at DATETIME, updated_at DATETIME, deleted INT DEFAULT 0, remark VARCHAR(255)
);

-- 违纪记录
CREATE TABLE violation (
  id           BIGINT PRIMARY KEY,
  type_id      BIGINT,
  student_id   BIGINT,
  room_id      BIGINT,
  happened_at  DATETIME,
  severity     VARCHAR(16),
  description  VARCHAR(500),
  handled_at   DATETIME,
  handle_note  VARCHAR(255),
  created_by   BIGINT, created_at DATETIME, updated_at DATETIME, deleted INT DEFAULT 0, remark VARCHAR(255)
);
