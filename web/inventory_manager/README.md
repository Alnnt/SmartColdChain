# 库存管理前端

基于 Vue 3 + Vite 的库存管理后台，对接冷链云仓库存服务。

## 功能

- **登录**：使用网关统一认证（/api/auth/user/login）
- **库存列表**：查看各仓库各商品库存（总库存、冻结、可用），支持按条调整
- **仓库管理**：查看仓库列表（名称、地址、经纬度）

## 技术栈

- Vue 3 (Composition API)
- Vue Router 4
- Vite 6
- Axios

## 开发

```bash
cd web/inventory_manager
npm install
npm run dev
```

默认运行在 http://localhost:5175，API 通过 Vite 代理到 `http://localhost:8070`（需先启动网关与库存服务）。

## 构建

```bash
npm run build
```

产物在 `dist/`。

## 后端接口（需网关路由 /api/inventory/**）

- `GET /api/inventory/manager/warehouses` 仓库列表
- `GET /api/inventory/manager/items` 库存列表
- `POST /api/inventory/manager/adjust` 调整库存 `{ inventoryId, delta }`
