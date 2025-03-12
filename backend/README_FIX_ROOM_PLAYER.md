# 修复 RoomPlayer 实体类问题

## 问题描述

在尝试离开房间时，出现以下错误：

```
org.springframework.jdbc.BadSqlGrammarException: 
### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column 'id' in 'field list'
### The error may exist in com/dezhou/poker/mapper/RoomPlayerMapper.java (best guess)
### The error may involve defaultParameterMap
### The error occurred while setting parameters
### SQL: SELECT  id,room_id,user_id,seat_number,current_chips,status,last_action,last_bet  FROM room_players     WHERE (room_id = ? AND user_id = ?)
### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'id' in 'field list'
```

这个错误表明 `RoomPlayer` 实体类期望在 `room_players` 表中有一个 `id` 列，但实际上该表没有这个列。相反，该表使用 `room_id` 和 `user_id` 作为复合主键。

## 解决方案

有三种可能的解决方案：

### 方案1：修改 RoomPlayer 实体类使用复合主键（推荐）

1. 修改 `RoomPlayer.java` 文件，使用 `@IdClass` 注解：

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("room_players")
@IdClass(RoomPlayerId.class)
public class RoomPlayer implements Serializable {
    
    @Id
    private Long roomId;
    
    @Id
    private Long userId;
    
    private Integer seatNumber;
    private BigDecimal currentChips;
    private String status;
    private String lastAction;
    private BigDecimal lastBet;
    
    // 其他字段...
}
```

2. 确保 `RoomPlayerId.java` 文件正确定义：

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomPlayerId implements Serializable {
    
    private Long roomId;
    private Long userId;
    
    // equals 和 hashCode 方法...
}
```

### 方案2：使用 MyBatis-Plus 的 @TableId 注解

修改 `RoomPlayer.java` 文件，使用多个 `@TableId` 注解：

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("room_players")
public class RoomPlayer implements Serializable {
    
    @TableId(value = "room_id")
    private Long roomId;
    
    @TableId(value = "user_id")
    private Long userId;
    
    private Integer seatNumber;
    private BigDecimal currentChips;
    private String status;
    private String lastAction;
    private BigDecimal lastBet;
    
    // 其他字段...
}
```

### 方案3：创建包含 id 列的视图

1. 已经创建了 `room_players_view` 视图，它包含一个生成的 `id` 列。
2. 修改 `RoomPlayer.java` 文件，使用该视图：

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("room_players_view")
public class RoomPlayer implements Serializable {
    
    @TableId(value = "id")
    private String id;
    
    private Long roomId;
    private Long userId;
    private Integer seatNumber;
    private BigDecimal currentChips;
    private String status;
    private String lastAction;
    private BigDecimal lastBet;
    
    // 其他字段...
}
```

## 实施步骤

1. 备份当前的 `RoomPlayer.java` 和 `RoomPlayerId.java` 文件
2. 根据上述方案之一修改这些文件
3. 重新编译并启动应用程序
4. 测试离开房间功能是否正常工作

## 注意事项

- 如果使用方案3（视图），确保 Flyway 迁移脚本 `V5__add_room_players_view.sql` 已经成功执行
- 修改实体类可能需要同时修改相关的服务类和映射器类
- 在修改后，可能需要清除应用程序缓存或重新启动应用程序 