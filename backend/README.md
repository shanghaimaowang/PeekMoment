# PeekMoment 后端

后端使用 Java 17、Spring Boot 3.5 和 MyBatis-Plus。发布内容、公开列表和管理员查询共用一套服务和数据表。开发机可用 H2 跑通接口，团队环境使用 MySQL 8。

## 本地启动

安装 JDK 17 或 21 和 Maven 3.6.3 以上版本。在仓库根目录打开 PowerShell，执行以下命令。两项随机值仅供本次本地联调，复制到 Apifox 私有环境变量即可使用。

```powershell
$env:PEEK_ADMIN_USERNAME = 'week1-admin'
$env:PEEK_ADMIN_PASSWORD = [Guid]::NewGuid().ToString('N')
$env:PEEK_DEMO_PUBLISH_TOKEN = [Guid]::NewGuid().ToString('N')
mvn -f backend/pom.xml spring-boot:run
```

服务默认监听 `http://127.0.0.1:8080`。在另一个终端运行 `Invoke-RestMethod http://127.0.0.1:8080/api/health`。当数据库可读时返回 `{"code":0,"message":"ok","data":{"status":"ok"}}`。首次启动会创建表和开发账号“小满”。H2 文件位于服务工作目录的 `data/` 下；按上述 Maven 命令启动时位于 `backend/data/`。

| 设置 | 用途 |
| --- | --- |
| `HOST`、`PORT` | 服务监听地址和端口。 |
| `PEEK_CORS_ORIGIN` | H5 跨域联调时填预览页的实际来源，例如 `http://localhost:5173`。改动后重启后端。 |
| `PEEK_ADMIN_USERNAME`、`PEEK_ADMIN_PASSWORD` | 设置本实例的管理员登录信息。登录成功后使用服务签发的两小时会话令牌查询管理接口。重启后需要重新登录。 |
| `PEEK_DEMO_PUBLISH_TOKEN` | 在 `local`、`test` 或显式 `demo` 配置下允许使用固定开发身份 `demo-xiaoman` 发布。公开列表允许游客访问。 |

## MySQL 8

预建数据库 `peekmoment`，字符集选择 `utf8mb4`。给项目数据库账号建表、索引、外键和读写权限。在同一终端设置连接信息后启动。服务依次执行 `schema.sql` 和 `content.sql`；再次启动会保留已有记录。

```powershell
$env:SPRING_PROFILES_ACTIVE = 'mysql'
$env:PEEK_DB_URL = 'jdbc:mysql://127.0.0.1:3306/peekmoment?characterEncoding=UTF-8&serverTimezone=UTC'
$env:PEEK_DB_USERNAME = 'peekmoment'
$env:PEEK_DB_PASSWORD = Read-Host '输入 MySQL 项目账号密码' -MaskInput
mvn -f backend/pom.xml spring-boot:run
```

团队开发库需要复测发布时，将 `SPRING_PROFILES_ACTIVE` 设置为 `mysql,demo`，并设置开发发布令牌。单独启用 `mysql` 时，服务仅启用正式数据源，演示发布凭证不可用。本周管理员会话保存在单个服务进程中，统一 JWT 和 Redis 会话能力按后续迭代接入。

## 回归测试

在仓库根目录执行 `mvn -f backend/pom.xml test`。JUnit 使用独立的内存 H2 数据库，并在随机端口访问实际 HTTP 接口。Apifox 可导入 `tools/qa/peekmoment-week1.postman_collection.json` 复核同一组业务路径。
