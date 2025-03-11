# 德州扑克游戏

这是一个基于Spring Boot和React的在线德州扑克游戏系统。

## 技术栈

### 后端
- Spring Boot 2.7.x
- Spring Security + JWT认证
- MyBatis-Plus
- MySQL 8.0
- WebSocket
- Docker

### 前端
- React 18
- React Bootstrap
- Axios
- SockJS

## 快速开始

### 使用Docker运行

1. 确保已安装Docker和Docker Compose

2. 克隆项目
```bash
git clone https://github.com/yourusername/game-deZhouPoker.git
cd game-deZhouPoker
```

3. 启动服务
```bash
docker-compose up -d
```

现在可以访问:
- 前端: http://localhost:3000
- 后端API: http://localhost:8080
- Swagger文档: http://localhost:8080/swagger-ui.html

### 本地开发

#### 后端
1. 确保已安装JDK 11和Maven

2. 配置MySQL数据库
```bash
# 创建数据库
mysql -u root -p
create database poker;
```

3. 运行后端
```bash
cd backend
mvn spring-boot:run
```

#### 前端
1. 确保已安装Node.js

2. 安装依赖并运行
```bash
cd frontend
npm install
npm start
```

## 主要功能

- 用户注册和登录
- 创建/加入游戏房间
- 实时游戏对战
- 玩家操作（下注、加注、弃牌等）
- 游戏记录查询
- 玩家统计数据
- 游戏内聊天

## 项目结构

```
.
├── backend/                # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── frontend/              # 前端项目
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   └── services/
│   └── package.json
├── docker-compose.yml     # Docker编排配置
├── Dockerfile            # 后端Docker构建文件
└── README.md
```

## API文档

API文档使用Swagger生成，运行后端服务后访问：
http://localhost:8080/swagger-ui.html

## 贡献指南

1. Fork项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情
