# 前端运行指南

## 项目概述

本项目包含两个前端应用：

1. **管理后台前端** (`admin/frontend`) - Vue 3 + Vite + Ant Design Vue
2. **用户端前端** (`user/frontend`) - uni-app (跨平台移动应用)

---

## 一、管理后台前端运行

### 前置要求
- Node.js 16+
- npm 8+

### 运行步骤

#### 1. 安装依赖
```bash
cd admin/frontend
npm install
```

#### 2. 启动开发服务器
```bash
npm run dev
```

#### 3. 访问应用
- 开发地址：http://localhost:5174
- 默认账号：admin / 123456

### 其他可用命令

```bash
# 类型检查
npm run typecheck

# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 运行端到端测试
npm run test:e2e
```

### 配置说明

管理后台前端配置在 `vite.config.ts` 中：
- 开发端口：5174
- API 代理：`/api` -> `http://localhost:8081` (管理后端)

---

## 二、用户端前端运行

### 前置要求
- **HBuilderX** (推荐) 或 uni-app CLI

### 方式一：使用 HBuilderX (推荐)

1. 下载并安装 HBuilderX：https://www.dcloud.io/hbuilderx.html
2. 打开 HBuilderX
3. 文件 -> 打开目录 -> 选择 `user/frontend` 文件夹
4. 运行 -> 运行到浏览器 -> Chrome (或其他浏览器)

### 方式二：使用 uni-app CLI (需要先配置)

如果要使用 CLI 方式运行，需要先初始化项目：

```bash
# 安装 @vue/cli
npm install -g @vue/cli

# 安装 uni-app CLI
npm install -g @vue/cli-service-global

# 进入用户前端目录
cd user/frontend

# 运行到 H5
npm run dev:h5
```

**注意**：当前 `user/frontend` 目录没有 `package.json`，建议使用 HBuilderX 方式。

### 用户端功能页面

- 首页 - 智能视觉识别
- 登录/注册
- 用户中心
- 家属中心
- 识别记录
- 信息登记

---

## 三、完整运行流程（推荐）

### 1. 确保数据库已初始化
参考 `DATABASE_SETUP.md`

### 2. 启动后端服务

**终端 1 - 用户后端**
```bash
cd user/backend
mvn spring-boot:run
```
- 地址：http://localhost:8080

**终端 2 - 管理后端**
```bash
cd admin/backend
mvn spring-boot:run
```
- 地址：http://localhost:8081

### 3. 启动前端服务

**终端 3 - 管理后台前端**
```bash
cd admin/frontend
npm install
npm run dev
```
- 地址：http://localhost:5174

**终端 4 - 用户端前端**（使用 HBuilderX 或 CLI）
- H5 地址：通常是 http://localhost:8080 或 HBuilderX 分配的端口

---

## 四、端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| 用户后端 | 8080 | Spring Boot 服务 |
| 管理后端 | 8081 | Spring Boot 服务 |
| 管理前端 | 5174 | Vite 开发服务器 |
| 用户前端 | (可变) | uni-app H5 开发服务器 |

---

## 五、常见问题

### Q: npm install 失败怎么办？
A: 尝试使用淘宝镜像：
```bash
npm install --registry=https://registry.npmmirror.com
```

### Q: 管理前端无法连接后端？
A: 确保管理后端已启动在 8081 端口，检查 `vite.config.ts` 中的代理配置。

### Q: 用户前端在哪里运行？
A: 用户端是 uni-app 项目，需要用 HBuilderX 打开并运行到浏览器。
