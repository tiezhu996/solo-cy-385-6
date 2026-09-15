# 宝宝成长记录与辅食管理平台

帮助新手父母管理宝宝档案、生长曲线、疫苗计划、辅食食谱和喂养记录。

## 快速启动

```bash
cp .env.example .env
docker compose up -d --build
```

访问地址：前端 http://localhost:18405 ，后端 http://localhost:19405/health 。

## 项目主要功能

- 创建多个宝宝档案，记录出生日期、身高、体重和血型。
- 定期记录身高体重，自动生成成长曲线百分位。
- 内置疫苗计划，支持已接种和未接种状态。
- 按月龄推荐辅食食谱，并支持过敏原筛选。
- 按宝宝标记食谱反馈（喜欢/一般/不喜欢），不喜欢的食谱自动收纳进折叠区，可随时改回未标记。
- 记录每日喂养内容、时间和宝宝反应。
- 维护成长里程碑时间线，预留照片上传扩展。
- 统计月度喂养频次、辅食多样性和生长趋势。

## 本地开发方式

```bash
cd backend
mvn spring-boot:run
```

运行自动化测试（使用内嵌 H2 内存库，无需外部数据库）：

```bash
cd backend
mvn test
```

```bash
cd frontend
npm install
npm run dev
```

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 前端 | Vue 3、TypeScript、Vant、Vite、ECharts |
| 后端 | Spring Boot、Java 17、MyBatis-Plus、JWT、SLF4J、Logback |
| 数据库 | MySQL 8.0 |
| 部署 | Docker Compose、Nginx |

## 项目目录结构

```text
.
├── backend
│   └── src/main
│       ├── java/com/babytracker
│       │   ├── constants
│       │   ├── controller
│       │   ├── dto
│       │   ├── entity
│       │   ├── exception
│       │   ├── mapper
│       │   ├── service
│       │   └── utils
│       └── resources
├── database
├── frontend
│   └── src
└── docker-compose.yml
```

## 环境变量说明

| 变量 | 说明 |
| --- | --- |
| COMPOSE_PROJECT_NAME | Compose 项目名，默认 babytracker |
| SPRING_DATASOURCE_URL | Spring Boot MySQL 地址 |
| SPRING_DATASOURCE_USERNAME | 数据库用户名 |
| SPRING_DATASOURCE_PASSWORD | 数据库密码 |
| JWT_SECRET | JWT 签名密钥 |

## Docker 部署说明

- 前端端口：`18405:80`
- 后端端口：`19405:8080`
- MySQL 数据使用命名卷 `babytracker-db-data`。
- 后端启动时自动检查并创建数据表（`schema.sql`），旧数据卷升级无需手动执行 SQL。
- Nginx 将 `/api` 代理到 `backend:8080`。

## License

MIT
