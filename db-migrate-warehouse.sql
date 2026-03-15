-- 仓库与订单表结构迁移（本地数据库 root/root）
-- 执行: cmd /c "cd /d d:\Dev\Projects\ColdChain\cloud && mysql -u root -proot < db-migrate-warehouse.sql"

-- cold_chain_user.t_user 增加 warehouse_id
USE cold_chain_user;
ALTER TABLE t_user ADD COLUMN warehouse_id BIGINT NULL COMMENT '绑定仓库ID（仓库管理员必填）' AFTER user_type;
UPDATE t_user SET warehouse_id = 1 WHERE username = 'warehouse';

-- cold_chain_order.t_order 增加 warehouse_id
USE cold_chain_order;
ALTER TABLE t_order ADD COLUMN warehouse_id BIGINT DEFAULT NULL COMMENT '履约仓库ID' AFTER status;
ALTER TABLE t_order ADD INDEX idx_warehouse_id (warehouse_id);
