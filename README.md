# 德州扑克游戏系统

这是一个基于Spring Boot的德州扑克游戏后端系统，提供了完整的游戏逻辑、用户管理、房间管理等功能。

## 项目结构

```
game-deZhouPoker/
├── backend/                 # 后端代码
│   ├── src/                 # 源代码
│   │   ├── main/
│   │   │   ├── java/        # Java代码
│   │   │   └── resources/   # 配置文件
│   │   └── test/            # 测试代码
│   └── pom.xml              # Maven配置
└── .vscode/                 # VSCode配置
    ├── launch.json          # 启动配置
    └── tasks.json           # 任务配置
```

## 技术栈

- Spring Boot 2.x
- Spring Security + JWT
- Spring Data JPA
- MyBatis-Plus
- WebSocket
- MySQL 8.x

## 快速开始

### 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Visual Studio Code

### 数据库配置

1. 创建名为`dezhou`的数据库
2. 根据需要修改`application-dev.properties`中的数据库连接信息

### 使用VSCode启动项目

本项目已配置VSCode的启动文件，可以通过以下步骤快速启动：

1. 在VSCode中打开项目
2. 按下`F5`或点击"运行"菜单中的"启动调试"
3. 在弹出的启动配置中选择以下选项之一：
   - `启动德州扑克后端` - 直接启动应用
   - `使用Maven启动德州扑克` - 通过Maven启动
   - `调试德州扑克后端` - 启动并开启远程调试

### 手动启动项目

```bash
# 进入后端目录
cd backend

# 编译项目
mvn clean compile

# 启动项目（开发环境）
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## API文档

启动项目后，可以通过以下地址访问API文档：

- Swagger UI: http://localhost:8080/swagger-ui/index.html

## 开发指南

### 配置文件

- `application.properties` - 通用配置
- `application-dev.properties` - 开发环境配置
- `application-prod.properties` - 生产环境配置（需自行创建）

### 代码规范

请参考项目根目录下的`开发规范.md`文件，遵循统一的编码和设计规范。

## 许可证

[MIT License](LICENSE)
