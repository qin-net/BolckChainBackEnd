# 🔧 区块链研究组网站 - 后端服务

基于Spring Boot 3.4.3的现代化后端API服务，为区块链研究组网站提供数据支持和业务逻辑处理。

## 🏗️ 技术架构

### 核心技术栈
- **框架**: Spring Boot 3.4.3
- **编程语言**: Java 17
- **数据库**: PostgreSQL 15
- **数据访问**: Spring Data JPA + Hibernate 6.6
- **安全框架**: Spring Security 6.4 + JWT
- **缓存**: Redis
- **文档**: SpringDoc OpenAPI 3
- **构建工具**: Maven 3.9

### 主要依赖
```xml
<!-- 核心框架 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- 数据库 -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<!-- 工具库 -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

## 🚀 快速开始

### 环境要求
- Java 17 或更高版本
- Maven 3.6 或更高版本
- PostgreSQL 15 或更高版本
- Redis 6.0 或更高版本（可选）

### 数据库配置
```bash
# 创建数据库
createdb blockchain_website

# 创建用户
createuser blockchain_user --pwprompt

# 授权
psql -c "GRANT ALL PRIVILEGES ON DATABASE blockchain_website TO blockchain_user;"
```

### 开发环境配置
```bash
# 克隆项目
git clone https://github.com/your-org/blockchain-website-backend.git
cd blockchain-website-backend

# 安装依赖
mvn clean install

# 启动开发服务器
mvn spring-boot:run -Dspring.profiles.active=dev
```

### 生产环境配置
```bash
# 构建项目
mvn clean package -DskipTests

# 运行项目
java -jar target/blockchain-website-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 📁 项目结构

```
src/
├── main/
│   ├── java/com/dlut/blockchain/
│   │   ├── BlockchainWebsiteApplication.java  # 主启动类
│   │   ├── common/                            # 公共组件
│   │   │   ├── dto/                          # 通用DTO
│   │   │   ├── exception/                    # 异常处理
│   │   │   ├── response/                     # 统一响应
│   │   │   └── util/                         # 工具类
│   │   ├── config/                           # 配置类
│   │   │   ├── SecurityConfig.java          # 安全配置
│   │   │   ├── JwtConfig.java               # JWT配置
│   │   │   ├── CorsConfig.java              # 跨域配置
│   │   │   └── SwaggerConfig.java           # API文档配置
│   │   ├── controller/                       # 控制器层
│   │   │   ├── AuthController.java         # 认证接口
│   │   │   ├── UserController.java         # 用户管理
│   │   │   ├── ProjectController.java      # 项目管理
│   │   │   ├── BlogController.java         # 博客管理
│   │   │   ├── MeetingController.java      # 例会管理
│   │   │   └── FileController.java         # 文件管理
│   │   ├── dto/                             # 数据传输对象
│   │   │   ├── auth/                        # 认证相关DTO
│   │   │   ├── user/                        # 用户相关DTO
│   │   │   ├── project/                     # 项目相关DTO
│   │   │   └── blog/                        # 博客相关DTO
│   │   ├── entity/                          # 实体类
│   │   │   ├── User.java                    # 用户实体
│   │   │   ├── Project.java                 # 项目实体
│   │   │   ├── BlogPost.java               # 博客文章实体
│   │   │   ├── Meeting.java                 # 例会实体
│   │   │   └── FileInfo.java               # 文件信息实体
│   │   ├── repository/                      # 数据访问层
│   │   │   ├── UserRepository.java         # 用户数据访问
│   │   │   ├── ProjectRepository.java      # 项目数据访问
│   │   │   ├── BlogRepository.java         # 博客数据访问
│   │   │   └── MeetingRepository.java      # 例会数据访问
│   │   ├── security/                        # 安全相关
│   │   │   ├── JwtAuthenticationFilter.java # JWT认证过滤器
│   │   │   ├── JwtTokenProvider.java       # JWT令牌提供者
│   │   │   ├── UserDetailsServiceImpl.java # 用户详情服务
│   │   │   └── SecurityConstants.java      # 安全常量
│   │   └── service/                         # 业务逻辑层
│   │       ├── AuthService.java            # 认证服务
│   │       ├── UserService.java            # 用户服务
│   │       ├── ProjectService.java         # 项目服务
│   │       ├── BlogService.java            # 博客服务
│   │       └── MeetingService.java         # 例会服务
│   └── resources/
│       ├── application.yml                 # 主配置文件
│       ├── application-dev.yml            # 开发环境配置
│       ├── application-prod.yml           # 生产环境配置
│       ├── application-test.yml           # 测试环境配置
│       └── data.sql                       # 初始化数据
└── test/                                   # 测试代码
    └── java/com/dlut/blockchain/
        ├── controller/                     # 控制器测试
        ├── service/                        # 服务层测试
        └── repository/                     # 数据访问层测试
```

## 🔐 API接口文档

### 认证相关接口
```
POST /api/auth/login          # 用户登录
POST /api/auth/register       # 用户注册
POST /api/auth/refresh        # 刷新令牌
GET  /api/auth/verify         # 验证令牌
POST /api/auth/logout         # 用户登出
```

### 用户管理接口
```
GET    /api/users             # 获取用户列表
GET    /api/users/{id}        # 获取用户详情
POST   /api/users             # 创建用户
PUT    /api/users/{id}        # 更新用户信息
DELETE /api/users/{id}        # 删除用户
GET    /api/users/search      # 搜索用户
```

### 项目管理接口
```
GET    /api/projects          # 获取项目列表
GET    /api/projects/{id}     # 获取项目详情
POST   /api/projects          # 创建项目
PUT    /api/projects/{id}     # 更新项目信息
DELETE /api/projects/{id}     # 删除项目
GET    /api/projects/search    # 搜索项目
```

完整的API文档请访问：http://localhost:8080/api/swagger-ui.html

## 🔧 配置说明

### 数据库配置
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/blockchain_website
    username: postgres
    password: your_password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
```

### JWT配置
```yaml
jwt:
  secret: your_jwt_secret_key_here_should_be_long_enough
  expiration: 86400  # 24小时
  refresh-expiration: 604800  # 7天
```

### 文件上传配置
```yaml
file:
  upload:
    max-file-size: 10MB
    max-request-size: 50MB
    allowed-extensions: jpg,jpeg,png,pdf,doc,docx
    upload-dir: ./uploads
```

## 🧪 测试

### 单元测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=UserControllerTest

# 运行测试并生成报告
mvn test jacoco:report
```

### 集成测试
```bash
# 使用测试配置文件
mvn test -Dspring.profiles.active=test

# 运行API测试
mvn test -Dtest=*ControllerTest
```

### 性能测试
```bash
# 使用JMeter进行压力测试
# 测试脚本位于: src/test/jmeter/
```

## 🚀 部署

### Docker部署
```dockerfile
FROM openjdk:17-jre-slim

COPY target/blockchain-website-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=prod"]
```

### Docker Compose
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_URL=postgresql://db:5432/blockchain_website
    depends_on:
      - db
  
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: blockchain_website
      POSTGRES_USER: blockchain_user
      POSTGRES_PASSWORD: your_password
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

## 📊 监控和日志

### 应用监控
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

### 日志配置
```yaml
logging:
  level:
    com.dlut.blockchain: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 🔒 安全说明

### 已实施的安全措施
1. **JWT认证**: 无状态认证机制
2. **密码加密**: BCrypt密码加密
3. **CORS配置**: 跨域请求控制
4. **输入验证**: 参数校验和SQL注入防护
5. **HTTPS支持**: 生产环境强制HTTPS
6. **速率限制**: API请求频率限制

### 安全建议
1. 定期更新依赖版本
2. 使用环境变量存储敏感配置
3. 启用数据库连接加密
4. 实施API访问日志记录
5. 定期进行安全扫描

## 🐛 常见问题

### Q: 数据库连接失败
A: 检查数据库配置和连接参数，确保PostgreSQL服务正常运行

### Q: JWT令牌验证失败
A: 检查JWT密钥配置和令牌有效期设置

### Q: 跨域请求被阻止
A: 检查CORS配置，确保前端域名在允许列表中

### Q: 文件上传失败
A: 检查文件大小限制和上传目录权限

### Q: MeetingService出现NullPointerException
A: 这是一个已知问题，已在最新版本中修复。解决方案：
1. 更新到最新版本的MeetingService
2. 确保MeetingRepository正确实现分页查询
3. 检查数据库连接和表结构是否正确
4. 查看应用日志获取详细错误信息

**具体修复措施**：
- 在`MeetingService.getAllMeetings()`中添加空值检查
- 实现手动分页回退机制
- 添加数据库会议总数验证
- 增强异常处理和日志记录

**相关代码文件**：
- <mcfile name="MeetingService.java" path="src/main/java/com/dlut/blockchain/service/MeetingService.java"></mcfile>
- <mcfile name="MeetingRepository.java" path="src/main/java/com/dlut/blockchain/repository/MeetingRepository.java"></mcfile>

**修复时间**：2024年12月19日

## 📞 支持

如有问题或建议，请通过以下方式联系我们：
- 📧 邮箱: blockchain@dlut.edu.cn
- 🐛 提交Issue: [GitHub Issues](https://github.com/your-org/blockchain-website-backend/issues)
- 💬 讨论区: [GitHub Discussions](https://github.com/your-org/blockchain-website-backend/discussions)

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](../LICENSE) 文件了解详情

## 🙏 致谢

感谢Spring Boot社区和所有开源项目的贡献者！

---

**最后更新**: 2024年12月19日