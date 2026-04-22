# Admin 前端部署说明

## 1. 接口地址

当前项目已改为优先读取环境变量：

- 开发环境：`admin/frontend/.env.development`
- 生产环境：`admin/frontend/.env.production`

默认配置为：

```env
VITE_API_BASE_URL=/api
```

这表示前端上线后会请求同域名下的 `/api`，例如：

- 前端地址：`http://your-server/`
- 后端接口：`http://your-server/api/...`

如果你准备把后端单独部署到其他域名，也可以把 `VITE_API_BASE_URL` 改成完整地址，例如：

```env
VITE_API_BASE_URL=http://your-server:8081/api
```

## 2. 打包

在 `admin/frontend` 下执行：

```bash
npm install
npm run build
```

打包结果在 `admin/frontend/dist/`。

## 3. Nginx 示例

如果前后端同机部署，推荐：

```nginx
server {
    listen 80;
    server_name your-server-ip-or-domain;

    root /var/www/homework-admin;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8081/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 4. 注意事项

- 当前路由模式是 `history`，所以 Nginx 必须保留 `try_files ... /index.html`
- 如果前端不是部署在站点根路径，而是部署到 `/admin/`，还需要继续调整 Vite 的 `base` 和路由 `history` 基路径
