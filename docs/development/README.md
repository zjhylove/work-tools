# Work Tools 开发文档

## 项目结构
```
src/main/java/com/zjhy/love/worktools/
├── WorkToolsApplication.java # 应用程序入口
├── common/                  # 公共工具类
│   └── util/
│       ├── FileUtil.java    # 文件操作工具
│       ├── NotificationUtil.java # 通知工具
│       └── SystemUtil.java  # 系统工具
├── model/                   # 数据模型
│   ├── ForwardEntry.java   # 转发规则
│   ├── IpForwardConfig.java # 转发配置
│   └── StorageObject.java  # 存储对象
├── service/                 # 业务逻辑
│   ├── AliyunOssService.java # 阿里云存储服务
│   ├── NacosService.java   # Nacos服务
│   └── TencentCosService.java # 腾讯云存储服务
└── view/                    # 视图组件
    ├── AuthView.java       # 验证码视图
    └── BaseView.java       # 基础视图
```

## 核心模块说明

### 1. IP端口转发模块
#### 主要类
- `IpForwardService`: 转发服务实现
- `SshTunnelManager`: SSH隧道管理
- `IpForwardView`: 转发界面

#### 关键接口
```java
public interface ForwardService {
    void startForward(ForwardEntry entry);
    void stopForward(String id);
    List<ForwardEntry> getActiveForwards();
}
```

### 2. Nacos服务发现模块
#### 主要类
- `NacosService`: Nacos服务实现
- `ServiceInstance`: 服务实例
- `NacosView`: 服务列表界面

### 3. 存储管理模块
#### 主要类
- `StorageService`: 存储服务接口
- `AliyunOssService`: 阿里云实现
- `TencentCosService`: 腾讯云实现

### 4. 二次验证模块
#### 主要类
- `TotpService`: TOTP实现
- `AuthAccount`: 账号信息
- `AuthView`: 验证码界面

## 开发规范

### 1. 代码风格
- 使用 Google Java Style
- 类名使用 PascalCase
- 方法名使用 camelCase
- 常量使用 UPPER_SNAKE_CASE

### 2. 提交规范
- feat: 新功能
- fix: 修复问题
- docs: 文档变更
- style: 代码格式
- refactor: 代码重构

### 3. 异常处理
- 使用统一的异常处理机制
- 合理使用自定义异常
- 详细的错误日志记录

### 4. 日志规范
- 使用 Log4j2 记录日志
- 合理的日志级别
- 关键操作必须记录日志

## 构建部署

### 1. 开发环境配置
- JDK 23 安装配置
- Maven 配置
- IDE 配置

### 2. 构建命令
```bash
# 清理构建
mvn clean

# 运行测试
mvn test

# 打包
mvn jfx:package

# 运行
mvn javafx:run
```

### 3. 部署说明
- 支持的操作系统：Windows、macOS、Linux
- 运行环境要求：JRE 23+
- 配置文件说明：
  - `META-INF/services/com.zjhy.love.worktools.plugin.api.WorkToolsPlugin`: 应插件配置
  - `log4j2.xml`: 日志配置 