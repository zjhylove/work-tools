# Work Tools

一个基于 JavaFX 的开发工具集合，提供多种实用的开发辅助功能。

## 功能特性

### 1. IP端口转发
- 支持 SSH 隧道转发
- 可视化管理转发规则
- 支持规则导入导出

### 2. Nacos服务发现
- 支持服务列表查看
- 实时监控服务实例变化
- 支持服务实例端口转发
- 多命名空间管理

### 3. 对象存储管理
- 支持阿里云 OSS
- 支持腾讯云 COS
- 文件上传下载
- 文件列表管理

### 4. 二次验证工具
- 支持 TOTP 验证码生成
- 验证码自动更新
- 支持多账号管理
- 一键复制验证码

### 5. 通用功能
- 现代化 UI 界面（AtlantaFX）
- 支持系统托盘
- 深色/浅色主题
- 统一的通知系统
- 支持开机自启动

## 技术栈

- Java 23
- JavaFX 17
- AtlantaFX 2.0.1
- ControlsFX 11.2.1
- Hutool 5.8.25
- Log4j 2.20.0
- Jackson
- SSH 隧道
- Nacos SDK
- 阿里云/腾讯云 SDK

## 项目结构
```
src/main/java/com/zjhy/love/worktools/
├── WorkToolsApplication.java # 应用程序入口
├── common/ # 公共工具类
│ └── util/
│ ├── FileUtil.java # 文件操作工具
│ ├── NotificationUtil.java# 通知工具
│ └── SystemUtil.java # 系统工具
├── model/ # 数据模型
│ ├── ForwardEntry.java # 转发规则
│ ├── IpForwardConfig.java # 转发配置
│ └── StorageObject.java # 存储对象
├── service/ # 业务逻辑
│ ├── AliyunOssService.java # 阿里云存储服务
│ ├── NacosService.java # Nacos服务
│ └── TencentCosService.java # 腾讯云存储服务
└── view/ # 视图组件
├── AuthView.java # 验证码视图
└── BaseView.java # 基础视图
```


## 开发环境

- JDK 23
- Maven 3.9+
- IDE 推荐：IntelliJ IDEA

## 构建运行
- bash
- 克隆项目
- git clone https://github.com/zjhylove/work-tools.git
- 进入项目目录
- cd work-tools
- 编译打包
- mvn clean jfx:package
- 运行
- mvn clean javafx:run

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交改动 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

[MIT License](LICENSE)

## 作者

@zjhylove

## 详细文档

- [用户手册](docs/user/README.md) - 详细的功能使用说明和常见问题解答
- [开发文档](docs/development/README.md) - 项目架构、开发规范和构建部署说明
- [API文档](docs/api/README.md) - 核心API接口说明和示例代码
- [更新日志](CHANGELOG.md) - 版本更新历史和功能变更记录