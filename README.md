# PeekMoment

PeekMoment 是一个校园动态分享项目。小程序支持发布和浏览图文、视频，管理服务向后台提供用户与内容查询。

## 项目结构

```text
PeekMoment/
├── backend/                  # Spring Boot 3 + MyBatis-Plus 后端（Maven 标准目录）
├── frontend/
│   ├── admin_web/            # Vue 3 + Element Plus 管理后台
│   └── miniprogram/          # uni-app + Vue 3 小程序（CLI 标准目录）
│       ├── src/              # 应用源码：pages、components、services、static 等
│       └── tests/            # Node.js 单元测试
├── docs/                     # 接口契约、设计说明与周报
├── tools/
│   ├── mock/                 # 本地 HTTP Mock 服务
│   └── qa/                   # Postman 集合、编译检查等验证脚本
└── .github/workflows/        # CI 检查
```

## 先跑一次第一周的接口

从仓库根目录执行以下命令即可启动本地 Mock。需要 Node.js 22.13 或更高版本，无需安装 npm 依赖。

```sh
node tools/mock/server.mjs
```

打开 `http://127.0.0.1:3100/api/moments` 可以看到一条示例动态。用 Apifox 导入 `tools/qa/peekmoment-week1.postman_collection.json`，按下表设置私有环境变量，再按集合顺序运行。集合会登录管理员、发布一条带标签的测试动态，并分别从公开列表和管理接口核对这条记录。

| 变量 | 本地 Mock 的值 |
| --- | --- |
| `baseUrl` | `http://127.0.0.1:3100` |
| `adminUsername` | `mock-admin` |
| `adminPassword` | `mock-password` |
| `demoToken` | `mock-publisher` |

Mock 固定监听本机，重启后重置数据。Spring 服务默认使用 8080 端口，可用 H2 持久化数据，也可接入 MySQL 8。已具备 JDK 17 和 Maven 的环境可运行 `mvn -f backend/pom.xml spring-boot:run`。发布令牌、管理员凭据及 H5 来源按开发环境配置。[服务端启动说明](backend/README.md)

## 第一周接口

| 方法 | 路径 | 用途 | 谁可以调用 |
| --- | --- | --- | --- |
| GET | `/api/health` | 检查服务与数据库连接；Mock 返回就绪示例 | 游客 |
| GET | `/api/moments` | 按时间倒序分页浏览公开动态 | 游客 |
| POST | `/api/moments` | 发布动态，返回新动态及其 ID | 持有开发发布令牌的小程序联调用户 |
| POST | `/api/admin/login` | 校验管理员账号和密码，返回两小时会话令牌 | 管理员账号 |
| GET | `/api/admin/users` | 按用户 ID 或昵称查询并分页 | 已登录管理员 |
| GET | `/api/admin/moments` | 跨状态查询动态，支持 ID、标题、正文和作者昵称 | 已登录管理员 |

完整字段、分页与失败响应见 [OpenAPI 契约](docs/openapi-week1.json)。测试从仓库根目录运行。

```sh
node --test frontend/miniprogram/tests/*.test.mjs tools/mock/server.test.mjs tools/qa/collection.test.mjs
mvn -f backend/pom.xml verify
```

小程序使用仓库已有的 uni-app / Vue 3 工程。打开「我的」可以从开发构建进入联调广场，浏览时间线并发布文字。若要直接连本机 Mock，在启动它的 PowerShell 中先设置 `$env:PORT = '8080'`，在联调广场填写 `mock-publisher`。此时可用同一端口在 Apifox 回读刚发出的动态。H5 的本地演示与联调广场各自保存数据。媒体服务和真机联调按第二周计划接入。[小程序开发说明](docs/小程序前端开发说明.md)

项目分工和本次汇报见 [六周分工表](docs/分工表.md) 和 [第一周汇报稿](docs/第一周汇报稿.md)。
