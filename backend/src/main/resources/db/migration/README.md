# Flyway 数据库迁移

本项目使用 Flyway 进行数据库版本控制和迁移管理，确保数据库结构和存储过程的一致性和可维护性。

## 迁移脚本概述

当前项目包含以下迁移脚本：

1. **V1__init_schema.sql** - 初始化数据库表结构
   - 创建用户表 (users)
   - 创建房间表 (rooms)
   - 创建房间玩家表 (room_players)
   - 创建游戏历史表 (game_history)
   - 创建玩家游戏历史表 (player_game_history)
   - 创建游戏动作表 (game_actions)
   - 创建All-in投票表 (allin_votes)
   - 创建筹码交易表 (chip_transactions)

2. **V2__stored_procedures.sql** - 创建所有存储过程
   - 用户相关存储过程
   - 房间相关存储过程
   - 游戏相关存储过程

## 迁移脚本命名规则

Flyway 迁移脚本遵循以下命名规则：

```
V<version>__<description>.sql
```

例如：
- `V1__init_schema.sql` - 初始化数据库结构
- `V2__stored_procedures.sql` - 添加存储过程

## 迁移脚本目录结构

```
db/migration/
├── README.md
├── V1__init_schema.sql
└── V2__stored_procedures.sql
```

## 添加新的迁移脚本

当需要对数据库结构进行修改时，应该创建新的迁移脚本，而不是修改现有的脚本。

例如，如果需要添加一个新的表，应该创建一个新的迁移脚本，如 `V3__add_new_table.sql`。

## 存储过程

本项目使用存储过程来封装常用的数据库操作，主要包括以下几类：

1. **用户相关操作**：
   - `create_user` - 创建新用户
   - `user_login` - 用户登录（更新最后登录时间）
   - `update_user_chips` - 更新用户筹码
   - `get_user_stats` - 获取用户统计信息

2. **房间相关操作**：
   - `create_room` - 创建新房间
   - `join_room` - 加入房间
   - `get_room_info` - 获取房间信息
   - `get_active_rooms` - 获取活跃房间列表

3. **游戏相关操作**：
   - `start_new_game` - 开始新游戏
   - `record_game_action` - 记录游戏动作
   - `record_allin_vote` - 记录All-in投票
   - `end_game` - 结束游戏并结算
   - `update_winner` - 更新获胜者信息

## 存储过程命名规范

存储过程的命名遵循以下规范：

- 使用小写字母和下划线
- 动词开头，表示操作类型（如 create, get, update, delete）
- 后跟操作的对象（如 user, room, game）
- 如有必要，再加上具体的操作（如 stats, chips, info）

例如：`create_user`, `get_user_stats`, `update_user_chips`

## 迁移脚本执行

Flyway 会在应用启动时自动执行未执行过的迁移脚本。它会在数据库中创建一个 `flyway_schema_history` 表来记录已执行的脚本。

## 配置

Flyway 的配置在 `application.properties` 文件中：

```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
spring.flyway.validate-on-migrate=false
spring.flyway.clean-disabled=true
```

## 重置数据库

如需重置数据库，可以按照以下步骤操作：

1. 删除现有的数据库（如果有）
   ```sql
   DROP DATABASE IF EXISTS dezhou;
   ```

2. 创建一个新的空数据库
   ```sql
   CREATE DATABASE dezhou CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. 启动应用程序，Flyway 将自动执行迁移脚本

或者，也可以手动执行迁移脚本：

```sql
USE dezhou;
SOURCE V1__init_schema.sql;
SOURCE V2__stored_procedures.sql;
```

## 注意事项

1. 已执行的迁移脚本不应该被修改，否则会导致校验失败。
2. 迁移脚本应该是幂等的，即多次执行不会产生副作用。
3. 在生产环境中，应该禁用 `flyway.clean()` 方法，以防止意外清除数据库。
4. 存储过程的修改应该通过创建新的迁移脚本来实现，先删除旧的存储过程，再创建新的存储过程。
5. 所有的 SQL 语句都应该以分号结尾，确保正确执行。
6. 在 MySQL 中，存储过程的定义需要使用 `DELIMITER` 语句来更改分隔符，但在 Flyway 迁移脚本中不需要这样做。 