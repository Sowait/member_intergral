# Repast VIP System 部署与分发指南

本指南说明如何将 `/Users/libra520/JavaProject/repast-vip-system`（Spring Boot + MySQL + Vue）打包为可直接运行的分发包，以及如何基于容器化进行构建、启动、验证与排障。还提供了无需再次构建即可直接运行的镜像分发方式（`repast-vip-system-file`）。

---

## 目标与产物

- 目标：将项目打包为“可直接运行”的交付物，支持本地与分发到其他机器。
- 产物构成：
  - 容器化工程（可构建运行）：位于 `repast-vip-system`，包含 `docker-compose.yml`、后端/前端 `Dockerfile`、`frontend/nginx.conf`、`database/init.sql`、`start.sh`。
  - 直接运行的镜像包（无需构建）：位于 `repast-vip-system-file`，包含 `my-repast-system.tar` 与 `start.sh`。

---

## 前提条件

- 已安装 Docker（Desktop 或 CLI），能使用 `docker`、`docker compose`。
- 默认端口：
  - 后端 `8080`
  - 前端 `8081`
  - MySQL `3306`
- 若 `3306` 被占用，可改为备用端口 `13306`（见下文）。
- 若需要本地构建镜像，需网络可拉取基础镜像；使用镜像分发包则无需构建网络。

---

## 目录与关键配置

- `repast-vip-system/docker-compose.yml`
  - 定义三服务：`mysql`、`backend`、`frontend`
  - 端口映射、依赖关系、健康检查与初始化脚本挂载（`database/init.sql`）
  - MySQL 典型环境变量：
    - `MYSQL_ROOT_PASSWORD=repast`
    - `MYSQL_DATABASE=repast`
    - `MYSQL_USER=repast`
    - `MYSQL_PASSWORD=repast`
- `repast-vip-system/backend/Dockerfile`
  - 多阶段构建：`maven` 构建 JAR → `jre` 运行
- `repast-vip-system/frontend/Dockerfile`
  - 多阶段构建：`node` 打包 → `nginx` 提供静态资源
- `repast-vip-system/frontend/nginx.conf`
  - Nginx 静态部署前端 SPA，支持前端路由
- `repast-vip-system/database/init.sql`
  - MySQL 初始化脚本（建库、建表、种子数据等）
- `repast-vip-system/start.sh`
  - 一键构建与启动脚本（调用 `docker compose build && docker compose up -d`）

---

## 容器化构建与启动（需要网络构建）

- 进入项目目录：

  ```bash
  cd /Users/libra520/JavaProject/repast-vip-system
  ```

- 执行一键脚本：

  ```bash
  chmod +x start.sh && ./start.sh
  ```

- 脚本行为：
  - 构建镜像：`docker compose build`
  - 启动服务：`docker compose up -d`
  - 输出访问地址：
    - 后端 `http://localhost:8080`
    - 前端 `http://localhost:8081`
    - MySQL `localhost:3306`

- 等效手动命令：

  ```bash
  docker compose build
  docker compose up -d
  docker compose ps
  docker compose logs backend -f
  ```

- MySQL 健康：`docker ps` 显示 `mysql` 容器为 `healthy` 后再连接数据库。

---

## 直接运行的镜像分发包（无需再次构建）

- 分发包目录与文件：
  - 目录：`/Users/libra520/JavaProject/repast-vip-system-file`
  - 文件：`my-repast-system.tar`、`start.sh`

- 启动步骤：

  ```bash
  cd /Users/libra520/JavaProject/repast-vip-system-file
  chmod +x start.sh && ./start.sh
  ```

- 脚本逻辑：
  - 加载镜像：`docker load -i my-repast-system.tar` → 导入为 `repast-system:latest`
  - 运行容器：`-p 8080:8080 -p 8081:8081 -p 3306:3306`
  - 打印访问地址：后端 `http://localhost:8080`、前端 `http://localhost:8081`、MySQL `localhost:3306`

- 若 `3306` 被占用（推荐备用端口）：

  ```bash
  docker run -itd \
    -p 8080:8080 -p 8081:8081 -p 13306:3306 \
    repast-system:latest /root/start.sh
  # 宿主机连接 MySQL：localhost:13306
  ```

---

## 如何制作“直接运行”的镜像分发包（复用方法）

当你更新了系统并希望重新生成无需构建的分发包时：

1. 使用 `docker-compose` 启动并验证功能正常。
2. 将整合运行的容器提交为镜像（示例）：

   ```bash
   docker ps  # 找到整合容器ID
   docker commit <容器ID> repast-system:latest
   ```

3. 导出镜像为 tar 包：

   ```bash
   docker save -o my-repast-system.tar repast-system:latest
   ```

4. 准备分发目录与脚本：将 `my-repast-system.tar` 与 `start.sh` 放在同一目录。脚本逻辑为：先 `docker load`，再 `docker run` 暴露端口，最后打印访问地址。

---

## 多镜像 + Compose 的分发（无需构建）

如果希望保持三容器结构（`mysql`、`backend`、`frontend`），但分发时无需构建：

1. 将各镜像导出：

   ```bash
   docker save -o my-repast-backend.tar my-repast-backend:latest
   docker save -o my-repast-frontend.tar my-repast-frontend:latest
   # mysql 使用官方镜像，可不必导出（或按需）
   ```

2. 分发时加载：

   ```bash
   docker load -i my-repast-backend.tar
   docker load -i my-repast-frontend.tar
   ```

3. 使用现有 `docker-compose.yml` 启动但不构建：

   ```bash
   docker compose up -d --no-build
   ```

优点：遵循多容器最佳实践、与开发/测试一致；缺点：分发包体积更大，包含多个 tar 与 compose 文件。

---

## 验证与访问

- 查看运行容器：

  ```bash
  docker ps
  ```

- 后端健康检查（如启用 Actuator）：

  ```bash
  curl -s http://localhost:8080/actuator/health
  docker logs <后端容器ID> -f
  ```

- 前端访问：打开浏览器 `http://localhost:8081`

- MySQL 连接：

  ```text
  host=localhost
  port=3306  # 若使用备用端口则为 13306
  user=repast
  password=repast
  database=repast
  ```

- 容器内验证（可选）：

  ```bash
  docker exec -it <mysql容器ID> sh
  netstat -tunlp | grep 3306
  mysql -urepast -prepast -h127.0.0.1 -P3306
  ```

---

## 常见问题与排障

- 3306 端口占用：

  ```bash
  docker ps --format '{{.ID}}\t{{.Image}}\t{{.Ports}}' | grep 3306
  docker stop <容器ID>  # 释放占用
  # 或改用备用端口 13306:3306
  ```

- 基础镜像拉取失败（构建时）：
  - 配置镜像加速：Docker Desktop → Settings → Docker Engine → `registry-mirrors`

    ```json
    {
      "registry-mirrors": [
        "https://registry.docker-cn.com",
        "https://docker.mirrors.ustc.edu.cn",
        "https://hub-mirror.c.163.com"
      ]
    }
    ```

  - 临时关闭 BuildKit：

    ```bash
    export DOCKER_BUILDKIT=0
    docker compose build
    ```

- 平台架构不匹配（Apple Silicon）：
  - 警告示例：`linux/amd64` 与主机 `linux/arm64` 不匹配
  - 解决：Docker Desktop 勾选 “Use Rosetta for x86/amd64 emulation on Apple Silicon”；或运行时指定：

    ```bash
    docker run --platform=linux/amd64 ...
    ```

- 端口与地址提示：
  - 当前 `repast-vip-system-file/start.sh` 固定提示为：后端 `http://localhost:8080`、前端 `http://localhost:8081`、MySQL `localhost:3306`
  - 若改用备用端口，请以实际映射端口为准（例如 `localhost:13306`）

---

## 维护与更新

- 更新后端/前端代码：

  ```bash
  cd /Users/libra520/JavaProject/repast-vip-system
  docker compose build && docker compose up -d
  ```

- 更新“直接运行镜像包”：按“制作镜像分发包”步骤重新生成 `my-repast-system.tar` 并替换到分发目录。

- 数据库初始化：
  - `database/init.sql` 在 MySQL 首次启动时自动执行；若需重复执行，请确保脚本具备幂等或编写升级脚本。

---

## 快速开始（摘要）

- 构建并运行（需要网络）：

  ```bash
  cd /Users/libra520/JavaProject/repast-vip-system
  chmod +x start.sh && ./start.sh
  ```

- 直接运行（无需构建）：

  ```bash
  cd /Users/libra520/JavaProject/repast-vip-system-file
  chmod +x start.sh && ./start.sh
  # 若 3306 被占用：
  docker run -itd -p 8080:8080 -p 8081:8081 -p 13306:3306 repast-system:latest /root/start.sh
  ```

---

如需在 `start.sh` 中自动检测端口占用并回退到 `13306`，可后续添加改良脚本以提升开箱体验。