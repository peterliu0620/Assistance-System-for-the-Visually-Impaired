# HBuilderX 运行用户端前端指南

## 第一步：打开正确的项目

你现在在 HBuilderX 中打开的是 **管理后台前端** (`admin/frontend`)，但我们要运行的是 **用户端前端** (`user/frontend`)。

### 操作步骤：

1. 在 HBuilderX 中，点击菜单栏：**文件** → **关闭目录**（关闭当前打开的 admin/frontend）
2. 点击菜单栏：**文件** → **打开目录**
3. 在弹出的文件选择器中，选择以下路径：
   ```
   c:\Users\Administrator\Assistance-System-for-the-Visually-Impaired\user\frontend
   ```
4. 点击 **选择文件夹** 按钮

---

## 第二步：运行到浏览器

打开 `user/frontend` 项目后，按以下方式运行：

### 方法一：使用工具栏
1. 点击顶部工具栏的 **运行** 按钮（▶️ 图标）
2. 选择 **运行到浏览器** → **Chrome**（或你喜欢的浏览器）

### 方法二：使用菜单栏
1. 点击菜单栏：**运行** → **运行到浏览器** → **Chrome**

### 方法三：使用快捷键
- 按 `Ctrl + R`（Windows）

---

## 第三步：确保后端服务已启动

在运行前端之前，需要先启动用户后端服务：

### 启动用户后端（在新的终端中）
```bash
cd c:\Users\Administrator\Assistance-System-for-the-Visually-Impaired\user\backend
mvn spring-boot:run
```

用户后端会运行在：http://localhost:8080

---

## 项目说明

### 用户端前端页面
- **首页** (`pages/index/index.vue`) - 智能视觉识别主页面
- **登录/注册** (`pages/auth/auth.vue`) - 用户认证
- **用户中心** (`pages/user-center/user-center.vue`) - 用户个人信息
- **家属中心** (`pages/family-center/family-center.vue`) - 家属管理
- **识别记录** (`pages/family-records/family-records.vue`) - 查看历史识别
- **信息登记** (`pages/family-profile/family-profile.vue`) - 登记用户信息

### 技术栈
- 框架：uni-app + Vue 3
- 跨平台：支持 H5、小程序、App

---

## 常见问题

### Q: 打开目录后看不到文件？
A: 确保选择的是 `user/frontend` 文件夹，而不是 `user` 或 `admin/frontend`。

### Q: 运行后页面空白或报错？
A: 1. 检查用户后端是否已启动（端口 8080）
   2. 查看浏览器控制台的错误信息
   3. 确认 API 地址配置正确

### Q: 如何切换回管理后台前端？
A: 文件 → 关闭目录 → 打开目录 → 选择 `admin/frontend`

---

## 完整运行清单

在运行用户端前端前，请确保：

- [ ] 数据库已初始化（参考 `DATABASE_SETUP.md`）
- [ ] 用户后端已启动（端口 8080）
- [ ] 在 HBuilderX 中打开的是 `user/frontend` 目录
- [ ] 点击"运行到浏览器"启动 H5 版本
