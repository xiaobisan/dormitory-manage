-- H2 种子数据（仅本地演示用）
-- 默认账号（密码均 admin123 / 学生 student123）

-- 系统管理员
INSERT INTO sys_user (id, username, password, name, role, status, created_at, updated_at, deleted)
VALUES (1, 'admin', 'admin123', '系统管理员', 'ADMIN', 1, NOW(), NOW(), 0);

-- 宿管（管理 1 号楼）
INSERT INTO sys_user (id, username, password, name, role, building_id, data_scope_type, data_scope_id, status, created_at, updated_at, deleted)
VALUES (2, 'dorm_01', 'admin123', '王宿管', 'DORM_MANAGER', 1001, 'BUILDING', 1001, 1, NOW(), NOW(), 0);

-- 维修工
INSERT INTO sys_user (id, username, password, name, role, status, created_at, updated_at, deleted)
VALUES (3, 'repair_01', 'admin123', '李维修', 'REPAIR_WORKER', 1, NOW(), NOW(), 0);

-- 辅导员
INSERT INTO sys_user (id, username, password, name, role, dept_id, data_scope_type, data_scope_id, status, created_at, updated_at, deleted)
VALUES (4, 'counselor_01', 'admin123', '张辅导员', 'COUNSELOR', 5001, 'DEPT', 5001, 1, NOW(), NOW(), 0);

-- 学生
INSERT INTO sys_user (id, username, password, name, role, status, created_at, updated_at, deleted)
VALUES (100, 'student_01', 'student123', '小明', 'STUDENT', 1, NOW(), NOW(), 0);
INSERT INTO sys_user (id, username, password, name, role, status, created_at, updated_at, deleted)
VALUES (101, 'student_02', 'student123', '小红', 'STUDENT', 1, NOW(), NOW(), 0);

-- 楼栋
INSERT INTO building (id, name, floor_count, created_at, updated_at, deleted) VALUES (1001, '1号男生宿舍楼', 6, NOW(), NOW(), 0);
INSERT INTO building (id, name, floor_count, created_at, updated_at, deleted) VALUES (1002, '2号女生宿舍楼', 5, NOW(), NOW(), 0);

-- 宿舍（1号楼 1 层）
INSERT INTO room (id, building_id, floor, room_no, capacity, status, created_at, updated_at, deleted) VALUES (2001, 1001, 1, '101', 4, 'IN_USE', NOW(), NOW(), 0);
INSERT INTO room (id, building_id, floor, room_no, capacity, status, created_at, updated_at, deleted) VALUES (2002, 1001, 1, '102', 4, 'IN_USE', NOW(), NOW(), 0);
INSERT INTO room (id, building_id, floor, room_no, capacity, status, created_at, updated_at, deleted) VALUES (2003, 1002, 1, '201', 4, 'IN_USE', NOW(), NOW(), 0);

-- 床位
INSERT INTO bed (id, room_id, bed_no, status, created_at, updated_at, deleted) VALUES (3001, 2001, 1, 'OCCUPIED', NOW(), NOW(), 0);
INSERT INTO bed (id, room_id, bed_no, status, created_at, updated_at, deleted) VALUES (3002, 2001, 2, 'OCCUPIED', NOW(), NOW(), 0);
INSERT INTO bed (id, room_id, bed_no, status, created_at, updated_at, deleted) VALUES (3003, 2001, 3, 'EMPTY', NOW(), NOW(), 0);
INSERT INTO bed (id, room_id, bed_no, status, created_at, updated_at, deleted) VALUES (3004, 2002, 1, 'OCCUPIED', NOW(), NOW(), 0);

-- 入住
INSERT INTO enrollment (id, student_id, building_id, room_id, bed_id, check_in_at, status, created_at, updated_at, deleted)
VALUES (4001, 100, 1001, 2001, 3001, NOW(), 'ACTIVE', NOW(), NOW(), 0);
INSERT INTO enrollment (id, student_id, building_id, room_id, bed_id, check_in_at, status, created_at, updated_at, deleted)
VALUES (4002, 101, 1001, 2001, 3002, NOW(), 'ACTIVE', NOW(), NOW(), 0);

-- 水电（2026-09）
INSERT INTO usage (id, room_id, type, period, reading, price, created_at, updated_at, deleted) VALUES (5001, 2001, 'ELECTRIC', '2026-09', 120.00, 0.55, NOW(), NOW(), 0);
INSERT INTO usage (id, room_id, type, period, reading, price, created_at, updated_at, deleted) VALUES (5002, 2001, 'WATER', '2026-09', 12.00, 2.00, NOW(), NOW(), 0);
INSERT INTO bill (id, room_id, period, amount, status, created_at, updated_at, deleted) VALUES (6001, 2001, '2026-09', 66.00, 'UNPAID', NOW(), NOW(), 0);

-- 报修示例
INSERT INTO repair_order (id, room_id, student_id, category, description, status, created_at, updated_at, deleted)
VALUES (7001, 2001, 100, 'ELECTRIC', '宿舍灯开关不灵', 'PENDING', NOW(), NOW(), 0);

-- 检查计划（检查项 JSON 模板）
INSERT INTO check_plan (id, name, cycle, item_template, active, created_at, updated_at, deleted)
VALUES (8001, '每周卫生检查', 'WEEKLY',
        '[{"key":"floor","name":"地面整洁","max":20},{"key":"desk","name":"桌面整理","max":20},{"key":"bed","name":"床铺平整","max":20},{"key":"doorwindow","name":"门窗清洁","max":20},{"key":"trash","name":"垃圾分类","max":20}]',
        1, NOW(), NOW(), 0);

-- 评优规则（默认）
INSERT INTO evaluation_rule (id, name, hygiene_weight, discipline_weight, bonus_weight, thresholds, active, created_at, updated_at, deleted)
VALUES (9001, '默认评优口径', 50, 30, 20,
        '{"fiveStar":90,"fourStar":80,"threeStar":70,"twoStar":60,"civilRoom":85}',
        1, NOW(), NOW(), 0);

-- 违纪类型字典
INSERT INTO violation_type (id, code, name, default_deduction, created_at, updated_at, deleted) VALUES (9101, 'ILLEGAL_POWER', '违规用电', 10, NOW(), NOW(), 0);
INSERT INTO violation_type (id, code, name, default_deduction, created_at, updated_at, deleted) VALUES (9102, 'UNAUTHORIZED_HOST', '留宿外人', 15, NOW(), NOW(), 0);
INSERT INTO violation_type (id, code, name, default_deduction, created_at, updated_at, deleted) VALUES (9103, 'NOISE', '噪音扰民', 5, NOW(), NOW(), 0);
INSERT INTO violation_type (id, code, name, default_deduction, created_at, updated_at, deleted) VALUES (9104, 'DAMAGE', '损坏公物', 8, NOW(), NOW(), 0);
