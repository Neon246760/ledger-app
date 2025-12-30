# 使用 Docker 打包与部署

## 一键启动（推荐）
```bash
docker compose up -d --build
```

启动后：
- 前端：HTTP `http://localhost:5000/`
- 后端 API：HTTP `http://localhost/api/...`
- Swagger 文档：`http://localhost/api/docs`
- 静态上传：`http://localhost/uploads/...`

## 结构说明
- `docker-compose.yml`：定义前后端两个服务与持久化卷
- `frontend/Dockerfile`：Node 构建 + Nginx 产线静态部署，Nginx 反向代理到后端 `/api` 与 `/uploads`
- `backend/Dockerfile`：Python 产线镜像，使用 Uvicorn 启动 FastAPI
- `docker/nginx.conf`：前端 Nginx 配置（SPAs 的路由与反代）
- 数据卷：
  - `db`：持久化数据库文件 `/data/app.db`
  - `uploads`：持久化上传目录 `/app/uploads`

## 后端镜像说明
- 工作目录 `/app`
- 暴露端口 `8000`
- 启动命令：`uvicorn app.main:app --host 0.0.0.0 --port 8000`
- 数据库路径通过环境变量 `DB_PATH` 控制，默认为 `app.db`；Compose 中设置为 `/data/app.db`

## 前端镜像说明
- 前端镜像说明
- 构建产物复制到 `/usr/share/nginx/html`
- Nginx 监听容器内 `80` 端口（映射到宿主机 `5000`）
- 反向代理：
  - `/api` -> `http://backend:8000/api`
  - `/uploads` -> `http://backend:8000/uploads`
- SPA 路由使用 `try_files` 回退到 `index.html`

## 常用命令
```bash
# 查看容器日志
docker compose logs -f backend
docker compose logs -f frontend

# 重新构建
docker compose build

# 停止并删除
docker compose down

# 清理卷（会删除数据库与上传文件）
docker compose down -v
```

## 注意事项
- 开发环境中 `vite.config.ts` 已设置代理 `/api` 与 `/uploads`；生产环境由 Nginx 代理完成
- 如需迁移数据库位置，可在后端容器中通过 `DB_PATH` 指定新的路径
- 上传目录在容器内 `/app/uploads`，由 `uploads` 卷持久化
