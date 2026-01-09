-- =====================================================
-- 运输服务数据库初始化脚本
-- Database: cold_chain_transport
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS cold_chain_transport
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE cold_chain_transport;

-- =====================================================
-- 司机表
-- =====================================================
DROP TABLE IF EXISTS t_driver;
CREATE TABLE t_driver (
    id              BIGINT          NOT NULL                COMMENT '主键ID',
    name            VARCHAR(50)     NOT NULL                COMMENT '司机姓名',
    phone           VARCHAR(20)     NOT NULL                COMMENT '联系电话',
    license_plate   VARCHAR(20)     NOT NULL                COMMENT '车牌号',
    status          TINYINT         NOT NULL DEFAULT 0      COMMENT '状态（0-空闲，1-忙碌）',
    latitude        DOUBLE          DEFAULT NULL            COMMENT '当前纬度',
    longitude       DOUBLE          DEFAULT NULL            COMMENT '当前经度',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT         DEFAULT 0               COMMENT '删除标志（0-未删除，1-已删除）',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_phone (phone),
    UNIQUE INDEX uk_license_plate (license_plate),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='司机表';

-- =====================================================
-- 运单表
-- =====================================================
DROP TABLE IF EXISTS t_transport_order;
CREATE TABLE t_transport_order (
    id                      BIGINT          NOT NULL                COMMENT '主键ID',
    order_id                BIGINT          NOT NULL                COMMENT '关联的订单ID',
    driver_id               BIGINT          NOT NULL                COMMENT '司机ID',
    start_address           VARCHAR(500)    DEFAULT NULL            COMMENT '起始地址（仓库地址）',
    end_address             VARCHAR(500)    NOT NULL                COMMENT '目的地址（收货地址）',
    status                  TINYINT         NOT NULL DEFAULT 0      COMMENT '状态（0-待取货，1-运输中，2-已送达，3-已取消）',
    estimated_arrival_time  DATETIME        DEFAULT NULL            COMMENT '预计到达时间',
    actual_arrival_time     DATETIME        DEFAULT NULL            COMMENT '实际到达时间',
    remark                  VARCHAR(500)    DEFAULT NULL            COMMENT '备注',
    create_time             DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time             DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted                 TINYINT         DEFAULT 0               COMMENT '删除标志（0-未删除，1-已删除）',
    PRIMARY KEY (id),
    INDEX idx_order_id (order_id),
    INDEX idx_driver_id (driver_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    CONSTRAINT fk_transport_driver FOREIGN KEY (driver_id) REFERENCES t_driver(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运单表';

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

-- 插入司机测试数据
INSERT INTO t_driver (id, name, phone, license_plate, status, latitude, longitude) VALUES
    (1, '张伟', '13800001001', '京A12345', 0, 40.0150, 116.3070),
    (2, '李强', '13800001002', '京B23456', 0, 39.9042, 116.4074),
    (3, '王刚', '13800001003', '沪A34567', 0, 31.2304, 121.4737),
    (4, '赵磊', '13800001004', '沪B45678', 1, 31.1657, 121.4000),
    (5, '刘洋', '13800001005', '粤A56789', 0, 23.1291, 113.2644),
    (6, '陈明', '13800001006', '粤B67890', 0, 22.5431, 114.0579),
    (7, '杨辉', '13800001007', '川A78901', 0, 30.5728, 104.0668),
    (8, '周鹏', '13800001008', '鄂A89012', 1, 30.5928, 114.3055),
    (9, '吴超', '13800001009', '浙A90123', 0, 30.2741, 120.1551),
    (10, '郑勇', '13800001010', '陕A01234', 0, 34.3416, 108.9398);

-- 插入运单测试数据（模拟历史运单）
INSERT INTO t_transport_order (id, order_id, driver_id, start_address, end_address, status, estimated_arrival_time, actual_arrival_time, remark) VALUES
    (1001, 10001, 4, '上海市嘉定区嘉定工业区冷链仓库', '上海市浦东新区陆家嘴金融中心', 1, '2026-01-08 18:00:00', NULL, '运输中'),
    (1002, 10002, 8, '武汉市东西湖区保税物流园冷链仓库', '武汉市武昌区中南路商圈', 1, '2026-01-08 17:30:00', NULL, '运输中'),
    (1003, 10003, 1, '北京市顺义区空港物流园冷链仓库', '北京市朝阳区三里屯', 2, '2026-01-07 16:00:00', '2026-01-07 15:45:00', '已送达'),
    (1004, 10004, 3, '上海市嘉定区嘉定工业区冷链仓库', '上海市静安区南京西路', 2, '2026-01-07 14:00:00', '2026-01-07 13:50:00', '已送达');
