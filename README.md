# 视障人士智能辅助系统

面向视障用户的视觉理解与语音引导系统：**举起手机拍一张照片，系统告诉你"这是什么、在哪个方向、离你多远"**，并用语音播报出来；家属可以在管理端查看识别记录与用药情况。

项目为课程团队实践项目，采用前后端分离 + 双服务架构：用户端（uni-app）对接用户服务，管理端（Vue 3）对接管理服务，两个服务共用一个 MySQL 数据库。

---

## 功能概览

### 用户端

| 模块 | 能力 |
| --- | --- |
| 场景理解 | 拍照或选图后识别当前场景与画面中的物品，返回物品名称、方位、置信度，并生成一句可直接朗读的自然语言播报 |
| 目标寻找 | 指定要找的物品（如水杯、药盒），按连续帧上传画面，返回水平/垂直方向、估计距离、接近程度、相对角度，并给出震动反馈建议，帮助用户"循着提示走过去" |
| 语音播报 | 调用 Qwen-TTS 将播报文本合成为音频，服务端缓存音频并以 URL 形式返回，避免重复合成 |
| 家属看护 | 家属与视障用户绑定，查看历史识别记录、维护看护档案（联系方式、常用物品、用药信息） |
| 用药安全 | 识别结果命中个人药品档案时给出提醒；同一药品在 30 分钟内被重复扫描时触发告警，避免重复用药 |

### 管理端

| 模块 | 能力 |
| --- | --- |
| 登录鉴权 | 管理员账号登录，JWT 令牌校验，未携带令牌的请求统一拦截 |
| 用户管理 | 应用用户的查询、新增、修改、删除 |
| 绑定关系 | 家属成员与盲人用户的绑定、解绑、按任一方向查询关系 |
| 药品档案 | 药品名称、用法用量的维护，供用户端用药匹配使用 |
| 共享日志 | 查看家属侧可见的识别与告警记录 |

---

## 系统架构

```
┌────────────────────┐        HTTP/JSON         ┌──────────────────────────┐
│  uni-app 用户端     │ ───────────────────────> │  user 服务  :8080         │
│  H5 / Android App  │ <─────────────────────── │  Spring Boot 3.2 + Java 17│
└────────────────────┘   图片 / 播报 / 音频      └───────────┬──────────────┘
                                                             │
                                        ┌────────────────────┼────────────────────┐
                                        │                    │                    │
                                   MyBatis/JDBC        LangChain4j         业务规则
                                        │                    │                    │
                                  ┌─────▼─────┐      ┌───────▼────────┐   ┌───────▼────────┐
                                  │  MySQL 8  │      │ 阿里云百炼      │   │ 药品档案匹配    │
                                  │ homework  │      │ DashScope      │   │ 记录归档/告警   │
                                  └─────▲─────┘      │ qwen-vl-plus   │   └────────────────┘
                                        │            │ qwen-tts       │
                                        │            └────────────────┘
┌────────────────────┐        /api 代理      ┌──────────────────────────┐
│  Vue 3 管理端       │ ───────────────────> │  admin 服务  :8081        │
│  Vite + TS         │ <─────────────────── │  Spring Boot + JWT        │
└────────────────────┘                      └──────────────────────────┘
```

视觉理解与语音合成都由云端模型完成，本地不加载模型权重，因此在无独立显卡的普通笔记本上也能跑通全流程。

---

## 技术栈

| 层次 | 选型 |
| --- | --- |
| 用户端 | uni-app（Vue 3 语法）、原生组件与条件编译，HBuilderX 运行 |
| 管理端 | Vue 3 + TypeScript + Vite，Playwright 端到端测试 |
| 后端 | Spring Boot 3.2.3、Java 17、Spring Web、Spring Security（密码加密）、JWT |
| 持久层 | MyBatis 3.0.3、MySQL 8.0、Flyway（迁移脚本已备，默认关闭） |
| AI 能力 | LangChain4j 1.2.0 → 阿里云百炼 DashScope：`qwen-vl-plus-latest` 视觉理解、`qwen-tts` 语音合成 |
| 工具链 | Maven、Lombok、JUnit 5、Git |

---

## 目录结构

```
Assistance-System-for-the-Visually-Impaired/
├── admin/
│   ├── backend/                 # 管理端服务 :8081
│   │   └── src/main/java/com/example/admin/
│   │       ├── controller/      # 登录、用户、绑定、药品、日志接口
│   │       ├── service/         # 业务逻辑与 JWT
│   │       ├── mapper/          # MyBatis 映射
│   │       └── interceptor/     # 管理端鉴权拦截器
│   └── frontend/                # 管理端页面（Vue 3 + Vite）
│       └── src/views/           # 登录、看板、用户、绑定、药品、日志
├── user/
│   ├── backend/                 # 用户端服务 :8080
│   │   └── src/main/java/com/example/demo/
│   │       ├── controller/      # 认证、视觉、语音、家属
│   │       ├── service/         # 视觉分析、TTS、用药安全、记录归档
│   │       ├── mapper/          # MyBatis 映射
│   │       └── model/ dto/      # 实体与传输对象
│   └── frontend/                # uni-app 用户端
│       ├── pages/               # index 视觉辅助、auth、user-center
│       │                        # family-center、family-records、family-profile
│       ├── utils/api.js         # 后端地址与请求封装
│       └── pages.json           # 页面路由配置
├── deploy/                      # 数据库脚本与环境搭建文档
│   ├── init_database.sql        # 建库、建用户、建表、初始化数据
│   ├── homework_schema.sql      # 表结构
│   ├── cleanup_database.sql     # 重置用
│   └── DATABASE_SETUP.md / FRONTEND_SETUP.md / HBUILDERX_GUIDE.md
├── skill-templates/             # 团队协作规范模板（接口契约、联调清单）
├── AGENTS.md                    # 目录约定、编码风格、提交规范
└── CLAUDE.md                    # 协作代理行为约定
```

---

## 快速开始

### 环境要求

| 依赖 | 版本 |
| --- | --- |
| JDK | 17 |
| Maven | 3.8+ |
| MySQL | 8.0+ |
| Node.js | 18+（仅管理端） |
| HBuilderX | 最新版（运行用户端） |

### 1. 初始化数据库

```bash
mysql -u root -p < deploy/init_database.sql
```

脚本会创建 `homework` 数据库、`homework` 账号并建表。如果之前初始化失败，先执行 `deploy/cleanup_database.sql` 重置。详细说明见 `deploy/DATABASE_SETUP.md`。

### 2. 配置后端

两个服务的 `src/main/resources/application.properties` 都需按本机情况修改：

```properties
# 数据库
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/homework?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=homework
spring.datasource.password=${DB_PASSWORD}

# 视觉与语音（必填，缺失时接口直接报错）
vision.qwen.api-key=${DASHSCOPE_API_KEY}
vision.qwen.vl-model=qwen-vl-plus-latest
vision.qwen.tts-model=qwen-tts
vision.qwen.tts-voice=Cherry
```

`user` 服务默认端口 `8080`，`admin` 服务默认 `8081`，冲突时改 `server.port`。

> 建议用环境变量或 `application-local.properties`（已加入 `.gitignore`）存放密钥，不要把真实 Key 写进提交。

### 3. 启动后端

```bash
# 用户服务
cd user/backend && mvn spring-boot:run

# 管理服务（另开终端）
cd admin/backend && mvn spring-boot:run
```

启动后可访问 `GET http://localhost:8080/api/hello` 验证用户服务是否就绪。

### 4. 启动用户端

`user/frontend` 没有 `package.json`，通过 HBuilderX 运行：

1. HBuilderX 打开目录 `user/frontend`（注意不是 `admin/frontend`）
2. 菜单「运行」→「运行到浏览器」→ Chrome，或按 `Ctrl + R`
3. 确保用户服务已启动，否则页面请求会失败

真机调试时，把 `user/frontend/utils/api.js` 中的 `API_BASE` 改成电脑的局域网 IP（如 `http://192.168.1.10:8080`），手机与电脑需在同一网络。详见 `deploy/HBUILDERX_GUIDE.md`。

### 5. 启动管理端

```bash
cd admin/frontend
npm install
npm run dev
```

开发环境通过 Vite 代理把 `/api` 转发到 `http://localhost:8081`，配置见 `.env.development`。

---

## 接口一览

### 用户服务（:8080）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录 |
| POST | `/api/vision/analyze` | 拍照理解，返回场景、物品列表与播报文本 |
| POST | `/api/vision/find-target` | 目标寻找，返回方向、距离、角度与触觉建议 |
| POST | `/api/voice/tts` | 文本转语音，返回音频 ID 与访问地址 |
| GET | `/api/voice/tts/audio/{audioId}` | 获取合成音频（带缓存与过期策略） |
| GET | `/api/family/dashboard` | 家属看板数据 |
| GET | `/api/family/records` | 识别记录列表 |
| GET、POST | `/api/family/profile` | 看护档案读取与保存 |
| POST | `/api/family/bind-vision` | 家属绑定视障用户 |
| GET | `/api/hello` | 健康检查 |

`/api/vision/analyze` 为 `multipart/form-data`，字段：`image`（必填）、`command`、`userId`、`sessionId`、`latitude`、`longitude`、`locationText`。`command` 为空时默认按"这是什么？"处理。

`/api/vision/find-target` 同样为 `multipart/form-data`，字段：`image`、`targetName`、`sessionId`、`frameIndex` 必填，`userId` 与定位信息可选。`sessionId` + 递增的 `frameIndex` 用于串联同一次寻找的连续帧。

### 管理服务（:8081）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/admin/auth/login` | 管理员登录，返回 JWT |
| GET、POST | `/api/admin/app-users` | 应用用户列表与新增 |
| PUT、DELETE | `/api/admin/app-users/{id}` | 修改与删除用户 |
| GET、POST | `/api/admin/family-members` | 家属成员管理 |
| PUT、DELETE | `/api/admin/family-members/{id}` | 修改与删除家属 |
| GET | `/api/admin/family-members/{id}/blind-users` | 查询某家属绑定的盲人用户 |
| POST | `/api/admin/family-members/{id}/bindings` | 建立绑定 |
| DELETE | `/api/admin/family-members/{id}/bindings/{blindUserId}` | 解除绑定 |
| GET | `/api/admin/family-bindings` | 全部绑定关系 |
| GET | `/api/admin/shared-logs` | 家属可见的共享日志 |
| GET、POST | `/api/admin/medicine-profiles` | 药品档案列表与新增 |
| PUT、DELETE | `/api/admin/medicine-profiles/{id}` | 修改与删除药品档案 |

除登录接口外，管理端请求需在 `Authorization` 头携带 `Bearer <token>`。

---

## 数据库

共 10 张表：

| 表 | 用途 |
| --- | --- |
| `user` | 应用用户（视障用户与家属） |
| `user_care_profile` | 看护档案：联系方式、紧急联系人等 |
| `family_member` | 家属成员信息 |
| `family_binding`、`family_user_binding` | 家属与盲人用户的绑定关系 |
| `scan_record` | 一次识别会话的主记录 |
| `scan_item` | 该次识别出的物品明细（名称、方位、置信度） |
| `medicine_profile` | 药品档案（按用户维护） |
| `medicine_scan_alert` | 用药告警记录 |
| `admin_user` | 管理端账号 |

当前 `spring.flyway.enabled=false`，实际以 `deploy/init_database.sql` 为准；`src/main/resources/db/migration` 下的脚本保留作版本化参考，若要启用迁移，改为 `true` 并确保基线一致。

---

## 测试

```bash
# 用户服务：认证、家属、视觉、知识归档
cd user/backend && mvn test

# 管理服务：登录、用户管理、绑定、药品
cd admin/backend && mvn test

# 管理端端到端：登录、用户管理、家属管理、药品与日志
cd admin/frontend && npx playwright test
```

---

## 常见问题

**启动报数据库连接失败**
`application.properties` 里的账号密码与 `deploy/init_database.sql` 创建的账号不一致。脚本里创建的账号是 `homework`，密码以脚本为准；若你手工改过，两边要统一。

**接口报"未配置 Qwen API Key"**
`vision.qwen.api-key` 为空。填入百炼平台的 Key，或设置 `DASHSCOPE_API_KEY` 环境变量后重启。

**用户端页面能打开但没有数据**
`user/frontend/utils/api.js` 的 `API_BASE` 指向 `localhost:8080`，手机真机访问时 localhost 指向手机自身，需改成电脑局域网 IP。

**语音播报没有声音**
浏览器拦截自动播放时会静音，需用户先有一次点击交互；另外 TTS 返回的音频地址基于请求域名生成，通过局域网访问时确认端口可达。

**识别到药品但没有告警**
用药匹配依赖该用户的药品档案，先在「信息登记」页维护药品名称与用法，才会触发匹配与重复扫描告警。

**管理端 401**
JWT 过期（默认 24 小时）或请求未带 `Authorization` 头，重新登录即可。

---

## 安全须知

仓库历史中曾提交过明文的 DashScope API Key 与数据库密码，**仅从代码里删除并不等于安全**，键值仍留在提交历史中。建议按顺序处理：

1. 登录阿里云百炼控制台，禁用并删除已泄露的 Key，重新生成一个
2. 修改数据库账号密码，同步更新本地配置
3. 把配置改为环境变量或 `application-local.properties`，并确认 `.gitignore` 已覆盖
4. 管理端 `app.jwt.secret` 换成不少于 32 位的随机串，`app.admin.default-password` 不要保留默认值

`.gitignore` 已排除 `target/`、`node_modules/`、`unpackage/`、`dist/` 等构建产物，提交前用 `git status` 确认没有把本地配置带进去。

---

## 协作约定

目录划分、编码风格、测试要求与提交规范见 `AGENTS.md`；接口契约与联调检查项见 `skill-templates/springboot-uniapp-team-skill/references/`。提交信息建议使用 `<模块>: <改动摘要>` 格式，例如 `user: 修正目标寻找的方向判断`。

---

## 说明

本项目为课程实践作品，用于学习与演示，尚未指定开源协议。系统给出的视觉识别与用药提醒结果由云端模型生成，不能替代专业医疗建议。
