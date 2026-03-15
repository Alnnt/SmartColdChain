# 冷链商城前端 (order_mail)

基于 **Vue 3**（最新版）+ Vite 6 的商城前端，支持用户登录、注册、下单与订单状态追踪。

## 功能

- **登录 / 注册**：对接 `/api/auth/user/login`、`/api/auth/register`
- **下单**：选择商品、数量、收货地址，提交到 `/api/order/create`
- **我的订单**：分页列表 `/api/order/my`，支持取消待支付订单
- **订单详情与追踪**：查看单笔订单状态（待支付 / 已支付 / 已发货 / 已完成 / 已取消）

## 技术栈

- Vue 3.5
- Vue Router 4
- Vite 6
- Axios

## 开发

```bash
cd web/order_mail
npm install
npm run dev
```

默认访问：http://localhost:5174  
前端将 `/api` 代理到后端网关（默认 `http://localhost:8080`），请在 `vite.config.js` 中按需修改。

## 构建

```bash
npm run build
```

产物在 `dist/`，可部署到任意静态服务器。

## 说明

- 商品列表为前端静态数据（`src/data/products.js`），因当前后端无商品列表接口，仅用于演示下单；实际商品 ID 需与后端库存/商品数据一致。
- 收货地址来自 `/api/user/address/list`，需先登录并在用户中心维护地址（本前端未做地址管理页，可后续扩展）。
