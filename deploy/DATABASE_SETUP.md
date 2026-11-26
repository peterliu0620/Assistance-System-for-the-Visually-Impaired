# 数据库初始化指南

## 前置条件
- MySQL 8.0+ 已安装并运行
- 具有 root 或管理员权限的 MySQL 用户

## 快速开始

### 如果之前执行失败，先清理数据库

```bash
mysql -u root -p < c:/Users/Administrator/Assistance-System-for-the-Visually-Impaired/deploy/cleanup_database.sql
```

### 方法一：使用 MySQL 命令行

```bash
# 1. 以 root 用户登录 MySQL
mysql -u root -p

# 2. 执行初始化脚本
source c:/Users/Administrator/Assistance-System-for-the-Visually-Impaired/deploy/init_database.sql
```

### 方法二：使用 MySQL 客户端工具（如 Navicat、DBeaver 等）

1. 打开 MySQL 客户端工具
2. 连接到 MySQL 服务器（使用 root 或有管理员权限的账号）
3. 打开并执行 `deploy/init_database.sql` 脚本

### 方法三：使用命令行直接执行

```bash
mysql -u root -p < c:/Users/Administrator/Assistance-System-for-the-Visually-Impaired/deploy/init_database.sql
```

## 数据库配置信息

| 配置项 | 值 |
|--------|-----|
| 主机 | 127.0.0.1 |
| 端口 | 3306 |
| 数据库名 | homework |
| 用户名 | homework |
| 密码 | Dog.1128620 |

## 初始化脚本说明

`init_database.sql` 脚本会执行以下操作：

1. 创建数据库用户 `homework`
2. 创建数据库 `homework`
3. 授予用户权限
4. 创建所有必需的数据表

**注意**：默认管理员账号会在管理后台首次启动时自动创建（admin / 123456）

## 验证安装

执行以下 SQL 验证数据库是否初始化成功：

```sql
USE homework;
SHOW TABLES;
SELECT * FROM admin_user;
```

## 常见问题

### Q: 提示 "Access denied for user 'root'@'localhost'"
A: 请确保您输入的 root 密码正确，或者使用具有足够权限的其他用户。

### Q: 提示 "Can't connect to MySQL server"
A: 请确保 MySQL 服务已启动，并且端口 3306 没有被防火墙阻止。

### Q: 如何修改数据库密码？
A: 
```sql
ALTER USER 'homework'@'%' IDENTIFIED BY '新密码';
FLUSH PRIVILEGES;
```
然后同步修改两个后端配置文件中的 `spring.datasource.password`。

## 下一步

数据库初始化完成后，可以启动后端服务：

```bash
# 启动用户后端
cd user/backend
mvn spring-boot:run

# 启动管理后端（另一个终端）
cd admin/backend
mvn spring-boot:run
```
