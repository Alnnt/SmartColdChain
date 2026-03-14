-- 认证授权服务数据库初始化脚本
-- 注意：auth与user服务共用 cold_chain_user 数据库
-- 完整建表脚本请参见 service-user/src/main/resources/db/init.sql
-- 本文件仅作为 service-auth 所依赖表结构的参考

USE cold_chain_user;

-- service-auth 依赖的表：
--   t_user           - 统一用户表（与 service-user 共用）
--   sys_role          - 角色表
--   sys_permission    - 权限/菜单表
--   sys_user_role     - 用户角色关联表
--   sys_role_permission - 角色权限关联表
