# 会员积分系统本地启动指南

## macOS 启动指南

### 1. 安装依赖
```bash
# 安装Homebrew (如未安装)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 安装Java和Maven
brew install openjdk maven

# 安装MySQL
brew install mysql
brew services start mysql

# 安装Node.js
brew install node
```

### 2. 启动后端
```bash
cd backend
mvn spring-boot:run
```

### 3. 启动前端
```bash
cd frontend
npm install
npm run serve
```

## Windows 启动指南

### 1. 安装依赖
1. 安装 [Java 8+ JDK](https://www.oracle.com/java/technologies/javase-downloads.html)
2. 安装 [Maven 3.6+](https://maven.apache.org/download.cgi)
3. 安装 [MySQL 5.7+](https://dev.mysql.com/downloads/installer/)
4. 安装 [Node.js 14+](https://nodejs.org/en/download/)

### 2. 启动后端
```cmd
cd backend
mvn spring-boot:run
```

### 3. 启动前端
```cmd
cd frontend
npm install
npm run serve
```

## 访问系统
- 前端开发环境：http://localhost:8081
- 后端API地址：http://localhost:8080

## 常见问题
1. MySQL连接失败：
   - 检查MySQL服务是否启动
   - 确认application.yml中的数据库配置

2. 端口冲突：
   - 修改backend/src/main/resources/application.yml中的server.port
   - 修改frontend/vue.config.js中的devServer.port