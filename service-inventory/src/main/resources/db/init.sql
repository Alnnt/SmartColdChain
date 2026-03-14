-- =====================================================
-- 库存服务数据库初始化脚本
-- Database: cold_chain_inventory
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS cold_chain_inventory
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE cold_chain_inventory;

-- =====================================================
-- 仓库表
-- =====================================================
DROP TABLE IF EXISTS t_warehouse;
CREATE TABLE t_warehouse (
    id              BIGINT          NOT NULL                COMMENT '主键ID',
    name            VARCHAR(100)    NOT NULL                COMMENT '仓库名称',
    address         VARCHAR(500)    NOT NULL                COMMENT '仓库地址',
    latitude        DOUBLE          NOT NULL                COMMENT '纬度',
    longitude       DOUBLE          NOT NULL                COMMENT '经度',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         DEFAULT 0               COMMENT '删除标志（0-未删除，1-已删除）',
    PRIMARY KEY (id),
    INDEX idx_name (name),
    INDEX idx_location (latitude, longitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='仓库表';

-- =====================================================
-- 库存表
-- =====================================================
DROP TABLE IF EXISTS t_inventory;
CREATE TABLE t_inventory (
    id              BIGINT          NOT NULL                COMMENT '主键ID',
    product_id      BIGINT          NOT NULL                COMMENT '商品ID',
    warehouse_id    BIGINT          NOT NULL                COMMENT '仓库ID',
    total_stock     INT             NOT NULL DEFAULT 0      COMMENT '总库存',
    frozen_stock    INT             NOT NULL DEFAULT 0      COMMENT '冻结库存',
    version         INT             NOT NULL DEFAULT 1      COMMENT '版本号（乐观锁）',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         DEFAULT 0               COMMENT '删除标志（0-未删除，1-已删除）',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_product_warehouse (product_id, warehouse_id),
    INDEX idx_product_id (product_id),
    INDEX idx_warehouse_id (warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

-- =====================================================
-- Seata AT 模式 undo_log 表（必需）
-- =====================================================
DROP TABLE IF EXISTS undo_log;
CREATE TABLE undo_log (
    branch_id       BIGINT          NOT NULL                COMMENT '分支事务ID',
    xid             VARCHAR(128)    NOT NULL                COMMENT '全局事务ID',
    context         VARCHAR(128)    NOT NULL                COMMENT '上下文',
    rollback_info   LONGBLOB        NOT NULL                COMMENT '回滚信息',
    log_status      INT             NOT NULL                COMMENT '日志状态',
    log_created     DATETIME(6)     NOT NULL                COMMENT '创建时间',
    log_modified    DATETIME(6)     NOT NULL                COMMENT '修改时间',
    PRIMARY KEY (branch_id),
    UNIQUE INDEX ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Seata AT模式undo_log表';

-- =====================================================
-- 测试数据
-- =====================================================

-- 插入仓库测试数据（模拟全国主要城市的冷链仓库）
INSERT INTO t_warehouse (id, name, address, latitude, longitude) VALUES
    (1, '北京顺义冷链仓', '北京市顺义区空港物流园区', 40.1282, 116.6542),
    (2, '上海嘉定冷链仓', '上海市嘉定区嘉定工业区', 31.3947, 121.2653),
    (3, '广州白云冷链仓', '广州市白云区太和镇物流园', 23.2316, 113.4255),
    (4, '深圳龙岗冷链仓', '深圳市龙岗区坪地物流中心', 22.7198, 114.3321),
    (5, '成都青白江冷链仓', '成都市青白江区物流园区', 30.8789, 104.2517),
    (6, '武汉东西湖冷链仓', '武汉市东西湖区保税物流园', 30.6518, 114.1368),
    (7, '杭州萧山冷链仓', '杭州市萧山区空港经济区', 30.2432, 120.4267),
    (8, '西安高陵冷链仓', '西安市高陵区泾渭工业园', 34.5634, 109.0882);

-- 插入库存测试数据（商品ID: 1001-生鲜牛肉, 1002-冷冻虾仁, 1003-有机蔬菜）
INSERT INTO t_inventory (id, product_id, warehouse_id, total_stock, frozen_stock) VALUES
    -- 生鲜牛肉 (1001) 在各仓库的库存
    (101, 1001, 1, 500, 0),
    (102, 1001, 2, 800, 0),
    (103, 1001, 3, 600, 0),
    (104, 1001, 4, 300, 0),
    (105, 1001, 5, 450, 0),
    (106, 1001, 6, 550, 0),
    (107, 1001, 7, 700, 0),
    (108, 1001, 8, 400, 0),

    -- 冷冻虾仁 (1002) 在各仓库的库存
    (201, 1002, 1, 1000, 0),
    (202, 1002, 2, 1500, 0),
    (203, 1002, 3, 2000, 0),
    (204, 1002, 4, 1200, 0),
    (205, 1002, 5, 800, 0),
    (206, 1002, 6, 900, 0),
    (207, 1002, 7, 1100, 0),
    (208, 1002, 8, 600, 0),

    -- 有机蔬菜 (1003) 在各仓库的库存
    (301, 1003, 1, 200, 0),
    (302, 1003, 2, 350, 0),
    (303, 1003, 3, 280, 0),
    (304, 1003, 4, 150, 0),
    (305, 1003, 5, 180, 0),
    (306, 1003, 6, 220, 0),
    (307, 1003, 7, 300, 0),
    (308, 1003, 8, 120, 0);
