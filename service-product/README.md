# 商品服务 (service-product)

商品管理员与超级管理员可对商品进行 CRUD。商品含预览图字段 `img`（完整 URL）。

## 数据库

需先创建数据库与表：

```sql
CREATE DATABASE IF NOT EXISTS cold_chain_product
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE cold_chain_product;

CREATE TABLE IF NOT EXISTS t_product (
  id          BIGINT       NOT NULL PRIMARY KEY COMMENT '主键',
  name        VARCHAR(255) NOT NULL COMMENT '商品名称',
  price       DECIMAL(12,2) NOT NULL COMMENT '价格',
  img         VARCHAR(512) DEFAULT NULL COMMENT '商品预览图完整URL',
  create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted     TINYINT      DEFAULT 0 COMMENT '0未删除 1已删除'
) COMMENT '商品表';
```

## 启动

- 依赖 Nacos、MySQL（cold_chain_product）。
- 从根目录：`mvn -pl service-product -am clean package`，然后 `mvn -pl service-product spring-boot:run`。

## 接口

- **管理端**（需登录）：`/api/product/manager` — 分页、详情、新增、更新、删除。
- **对客**（商城）：`/api/product/page` 分页列表，`/api/product/{id}` 详情。
