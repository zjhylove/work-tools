# Work Tools API 文档

## 核心API

### 1. 转发服务 API
```java
public interface ForwardService {
    /**
     * 启动转发
     * @param entry 转发配置
     */
    void startForward(ForwardEntry entry);
    
    /**
     * 停止转发
     * @param id 转发ID
     */
    void stopForward(String id);
    
    /**
     * 获取活动的转发列表
     * @return 转发列表
     */
    List<ForwardEntry> getActiveForwards();
}
```

### 2. 存储服务 API
```java
public interface StorageService {
    /**
     * 上传文件
     * @param file 本地文件
     * @param key 存储键
     * @return 访问URL
     */
    String uploadFile(File file, String key);
    
    /**
     * 下载文件
     * @param key 存储键
     * @param localPath 本地路径
     */
    void downloadFile(String key, String localPath);
    
    /**
     * 删除文件
     * @param key 存储键
     */
    void deleteFile(String key);
}
```

### 3. 验证服务 API
```java
public interface AuthService {
    /**
     * 生成验证码
     * @param secret 密钥
     * @return 验证码
     */
    String generateCode(String secret);
    
    /**
     * 验证验证码
     * @param secret 密钥
     * @param code 验证码
     * @return 是否有效
     */
    boolean verifyCode(String secret, String code);
}
```

## 事件系统

### 1. 转发事件
```java
public class ForwardEvent extends Event {
    private final ForwardEntry entry;
    private final ForwardStatus status;
    
    // 构造方法
    public ForwardEvent(ForwardEntry entry, ForwardStatus status) {
        this.entry = entry;
        this.status = status;
    }
    
    // getter方法
    public ForwardEntry getEntry() {
        return entry;
    }
    
    public ForwardStatus getStatus() {
        return status;
    }
}
```

### 2. 存储事件
```java
public class StorageEvent extends Event {
    private final String key;
    private final StorageAction action;
    
    // 构造方法
    public StorageEvent(String key, StorageAction action) {
        this.key = key;
        this.action = action;
    }
    
    // getter方法
    public String getKey() {
        return key;
    }
    
    public StorageAction getAction() {
        return action;
    }
}
```

## 工具类

### 1. 通知工具
```java
public class NotificationUtil {
    /**
     * 显示成功通知
     * @param title 标题
     * @param message 消息
     */
    public static void showSuccess(String title, String message);
    
    /**
     * 显示错误通知
     * @param title 标题
     * @param message 消息
     */
    public static void showError(String title, String message);
    
    /**
     * 显示警告通知
     * @param title 标题
     * @param message 消息
     */
    public static void showWarning(String title, String message);
    
    /**
     * 显示信息通知
     * @param title 标题
     * @param message 消息
     */
    public static void showInfo(String title, String message);
}
```

### 2. 文件工具
```java
public class FileUtil {
    /**
     * 读取配置文件
     * @param path 文件路径
     * @return 配置对象
     */
    public static Config readConfig(String path);
    
    /**
     * 保存配置文件
     * @param config 配置对象
     * @param path 文件路径
     */
    public static void saveConfig(Config config, String path);
    
    /**
     * 导出数据
     * @param data 数据对象
     * @param path 导出路径
     */
    public static void exportData(Object data, String path);
    
    /**
     * 导入数据
     * @param path 导入路径
     * @param type 数据类型
     * @return 数据对象
     */
    public static <T> T importData(String path, Class<T> type);
}
```

## 异常类

### 1. 转发异常
```java
public class ForwardException extends RuntimeException {
    public ForwardException(String message) {
        super(message);
    }
    
    public ForwardException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### 2. 存储异常
```java
public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }
    
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

## 配置类

### 1. 应用配置
```java
public class ApplicationConfig {
    private String appName;
    private String version;
    private boolean autoStart;
    private String theme;
    
    // getter和setter方法
}
```

### 2. 存储配置
```java
public class StorageConfig {
    private String provider;
    private String accessKey;
    private String secretKey;
    private String region;
    private String bucket;
    
    // getter和setter方法
} 