-- 用户服务数据库初始化脚本
-- 数据库：cold_chain_user（统一用户数据库，auth与user共用）

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS cold_chain_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cold_chain_user;

-- ====================================
-- 统一用户表
-- ====================================
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname VARCHAR(50) COMMENT '昵称',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(500) COMMENT '头像URL',
    user_type TINYINT DEFAULT 0 COMMENT '用户类型（0-普通用户，1-系统管理员）',
    warehouse_id BIGINT NULL COMMENT '绑定仓库ID（仓库管理员必填）',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-正常）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
    UNIQUE KEY uk_username (username),
    KEY idx_phone (phone),
    KEY idx_status (status),
    KEY idx_user_type (user_type),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ====================================
-- 用户地址表
-- ====================================
DROP TABLE IF EXISTS t_user_address;
CREATE TABLE t_user_address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '地址ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    contact_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    contact_phone VARCHAR(20) NOT NULL COMMENT '收货人电话',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    district VARCHAR(50) NOT NULL COMMENT '区/县',
    detail VARCHAR(500) NOT NULL COMMENT '详细地址',
    longitude DECIMAL(10, 7) COMMENT '经度',
    latitude DECIMAL(10, 7) COMMENT '纬度',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认地址（0-否，1-是）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
    KEY idx_user_id (user_id),
    KEY idx_is_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户地址表';

-- ====================================
-- 角色表
-- ====================================
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(200) COMMENT '描述',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-正常）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
    UNIQUE KEY uk_code (code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ====================================
-- 权限/菜单表
-- ====================================
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    name VARCHAR(50) NOT NULL COMMENT '权限名称',
    code VARCHAR(100) COMMENT '权限编码',
    type TINYINT NOT NULL COMMENT '类型（1-菜单，2-按钮，3-接口）',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    icon VARCHAR(100) COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-正常）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
    KEY idx_parent_id (parent_id),
    KEY idx_type (type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限/菜单表';

-- ====================================
-- 用户角色关联表
-- ====================================
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ====================================
-- 角色权限关联表
-- ====================================
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (role_id, permission_id),
    KEY idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ====================================
-- 初始化数据
-- ====================================

-- 插入系统管理员用户（密码: admin123，user_type=1 表示管理员）
INSERT INTO t_user (username, password, nickname, phone, user_type, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', '13800000000', 1, 1),
('operator', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '运营人员', '13800000001', 1, 1),
('warehouse', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '仓库管理员', '13800000002', 1, 1);

-- 仓库管理员绑定仓库（仓库 id=1 需在 cold_chain_inventory 已存在）
UPDATE t_user SET warehouse_id = 1 WHERE username = 'warehouse';

-- 插入普通测试用户（user_type=0）
INSERT INTO t_user (username, password, nickname, phone, user_type, status) VALUES
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '测试用户', '13800138000', 0, 1),
('customer1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '客户用户', '13800138001', 0, 1);

-- 插入角色
INSERT INTO sys_role (name, code, description, sort, status) VALUES
('超级管理员', 'ROLE_ADMIN', '拥有系统所有权限', 1, 1),
('运营管理员', 'ROLE_OPERATOR', '运营相关权限', 2, 1),
('仓库管理员', 'ROLE_WAREHOUSE', '仓库相关权限', 3, 1),
('司机', 'ROLE_DRIVER', '司机相关权限', 4, 1),
('客户', 'ROLE_CUSTOMER', '客户相关权限', 5, 1);

-- 插入权限/菜单
INSERT INTO sys_permission (parent_id, name, code, type, path, component, icon, sort, status) VALUES
-- 一级菜单
(0, '首页', 'dashboard', 1, '/dashboard', 'Dashboard', 'dashboard', 1, 1),
(0, '订单管理', 'order', 1, '/order', 'Layout', 'order', 2, 1),
(0, '库存管理', 'inventory', 1, '/inventory', 'Layout', 'inventory', 3, 1),
(0, '运输管理', 'transport', 1, '/transport', 'Layout', 'transport', 4, 1),
(0, 'IoT监控', 'iot', 1, '/iot', 'Layout', 'monitor', 5, 1),
(0, '系统管理', 'system', 1, '/system', 'Layout', 'setting', 6, 1),

-- 订单管理子菜单
(2, '订单列表', 'order:list', 1, '/order/list', 'order/list', NULL, 1, 1),
(2, '创建订单', 'order:create', 2, NULL, NULL, NULL, 2, 1),
(2, '编辑订单', 'order:edit', 2, NULL, NULL, NULL, 3, 1),
(2, '删除订单', 'order:delete', 2, NULL, NULL, NULL, 4, 1),

-- 库存管理子菜单
(3, '仓库列表', 'inventory:warehouse', 1, '/inventory/warehouse', 'inventory/warehouse', NULL, 1, 1),
(3, '库存查询', 'inventory:stock', 1, '/inventory/stock', 'inventory/stock', NULL, 2, 1),
(3, '入库管理', 'inventory:inbound', 1, '/inventory/inbound', 'inventory/inbound', NULL, 3, 1),
(3, '出库管理', 'inventory:outbound', 1, '/inventory/outbound', 'inventory/outbound', NULL, 4, 1),

-- 运输管理子菜单
(4, '司机管理', 'transport:driver', 1, '/transport/driver', 'transport/driver', NULL, 1, 1),
(4, '运单管理', 'transport:waybill', 1, '/transport/waybill', 'transport/waybill', NULL, 2, 1),
(4, '车辆管理', 'transport:vehicle', 1, '/transport/vehicle', 'transport/vehicle', NULL, 3, 1),

-- IoT监控子菜单
(5, '设备管理', 'iot:device', 1, '/iot/device', 'iot/device', NULL, 1, 1),
(5, '实时监控', 'iot:monitor', 1, '/iot/monitor', 'iot/monitor', NULL, 2, 1),
(5, '告警管理', 'iot:alarm', 1, '/iot/alarm', 'iot/alarm', NULL, 3, 1),

-- 系统管理子菜单
(6, '用户管理', 'system:user', 1, '/system/user', 'system/user', NULL, 1, 1),
(6, '角色管理', 'system:role', 1, '/system/role', 'system/role', NULL, 2, 1),
(6, '权限管理', 'system:permission', 1, '/system/permission', 'system/permission', NULL, 3, 1);

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),  -- admin -> 超级管理员
(2, 2),  -- operator -> 运营管理员
(3, 3);  -- warehouse -> 仓库管理员

-- 角色权限关联（超级管理员拥有所有权限）
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- 运营管理员权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 1), (2, 2), (2, 7), (2, 8), (2, 9), (2, 4), (2, 15), (2, 16);

-- 仓库管理员权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(3, 1), (3, 3), (3, 11), (3, 12), (3, 13), (3, 14);

-- 插入测试地址
INSERT INTO t_user_address (user_id, contact_name, contact_phone, province, city, district, detail, longitude, latitude, is_default) VALUES
(4, '张三', '13800138000', '北京市', '北京市', '朝阳区', '望京SOHO T1栋1001室', 116.4800, 40.0050, 1),
(4, '张三', '13800138000', '北京市', '北京市', '海淀区', '中关村软件园二期10号楼', 116.3100, 40.0550, 0),
(5, '李四', '13800138001', '上海市', '上海市', '浦东新区', '陆家嘴金融中心88号', 121.5050, 31.2400, 1);
