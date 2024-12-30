package com.zjhy.love.worktools.common.util;

import com.dustinredmond.fxtrayicon.FXTrayIcon;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * 系统工具类
 * 用于提供系统级别的工具方法，包括系统托盘、开机自启动等功能
 *
 * @author zhengjun
 */
public class SystemUtil {
    /**
     * 日志记录器
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemUtil.class);

    /**
     * 系统托盘图标
     */
    private static FXTrayIcon trayIcon;

    /**
     * 主窗口引用
     */
    private static Stage primaryStage;

    /**
     * 是否为Mac系统
     */
    private static final boolean IS_MAC = System.getProperty("os.name").toLowerCase().contains("mac");

    /**
     * 是否为Windows系统
     */
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");

    private SystemUtil() {
    }

    /**
     * 初始化系统托盘
     * 创建系统托盘图标，添加托盘菜单，支持最小化到托盘功能
     *
     * @param stage 主窗口Stage对象
     */
    public static void initSystemTray(Stage stage) {
        primaryStage = stage;

        try {
            // 创建系统托盘图标
            trayIcon = new FXTrayIcon(stage, Objects.requireNonNull(SystemUtil.class.getResource("/images/tools-icon.png")));

            // 设置托盘菜单
            trayIcon.setTrayIconTooltip("Work Tools");

            // 使用 JavaFX 菜单项而不是 AWT 菜单项
            MenuItem showItem = new MenuItem("show");
            showItem.setOnAction(e -> {
                Platform.runLater(() -> {
                    stage.show();
                    stage.setIconified(false);
                    stage.toFront();
                });
            });

            MenuItem exitItem = new MenuItem("exit");
            exitItem.setOnAction(e -> {
                Platform.runLater(() -> {
                    if (trayIcon != null) {
                        trayIcon.hide();
                    }
                    if (stage != null) {
                        stage.close();
                    }
                    Platform.exit();
                    System.exit(0);
                });
            });

            // 设置托盘菜单
            trayIcon.addMenuItems(showItem, exitItem);

            // 显示托盘图标
            trayIcon.show();

            LOGGER.info("系统托盘初始化成功");

        } catch (Exception e) {
            LOGGER.error("初始化系统托盘失败", e);
            NotificationUtil.showError("系统托盘初始化失败", e.getMessage());
        }
    }

    /**
     * 最小化到系统托盘
     * 隐藏主窗口，保持程序在后台运行
     */
    public static void minimizeToTray() {
        if (trayIcon != null && primaryStage != null) {
            Platform.runLater(() -> {
                primaryStage.hide();
                if (IS_WINDOWS) {
                    trayIcon.showMessage(
                            "Work Tools",
                            "程序已最小化到系统托盘"
                    );
                }
            });
        }
    }

    /**
     * 设置开机自启动
     * 根据操作系统类型选择对应的自启动设置方式
     *
     * @param enable true表示启用自启动，false表示禁用自启动
     */
    public static void setAutoStart(boolean enable) {
        if (IS_WINDOWS) {
            setWindowsAutoStart(enable);
        } else if (IS_MAC) {
            setMacAutoStart(enable);
        }
    }

    /**
     * 设置Windows系统开机自启动
     * 通过创建快捷方式到启动目录实现
     *
     * @param enable true表示启用自启动，false表示禁用自启动
     */
    private static void setWindowsAutoStart(boolean enable) {
        String appPath = System.getProperty("user.dir") + File.separator + "Work-Tools.exe";
        String startupPath = System.getProperty("user.home") +
                "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\Work-Tools.lnk";

        try {
            if (enable) {
                Runtime.getRuntime().exec(String.format(
                        "cmd /c mklink \"%s\" \"%s\"", startupPath, appPath
                ));
                NotificationUtil.showSuccess("设置成功", "已添加到开机启动项");
            } else {
                Files.deleteIfExists(Paths.get(startupPath));
                NotificationUtil.showSuccess("设置成功", "已从开机启动项移除");
            }
        } catch (IOException e) {
            LOGGER.error("设置Windows开机启动失败", e);
            NotificationUtil.showError("设置失败", e.getMessage());
        }
    }

    /**
     * 设置Mac系统开机自启动
     * 通过创建LaunchAgent配置文件实现
     *
     * @param enable true表示启用自启动，false表示禁用自启动
     */
    private static void setMacAutoStart(boolean enable) {
        String appPath = System.getProperty("user.dir");
        String plistPath = System.getProperty("user.home") +
                "/Library/LaunchAgents/com.zjhy.worktools.plist";

        try {
            if (enable) {
                String plistContent = String.format("""
                        <?xml version="1.0" encoding="UTF-8"?>
                        <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
                        <plist version="1.0">
                        <dict>
                            <key>Label</key>
                            <string>com.zjhy.worktools</string>
                            <key>ProgramArguments</key>
                            <array>
                                <string>%s/Work-Tools.app/Contents/MacOS/Work-Tools</string>
                            </array>
                            <key>RunAtLoad</key>
                            <true/>
                        </dict>
                        </plist>
                        """, appPath);

                Files.writeString(Paths.get(plistPath), plistContent);
                Runtime.getRuntime().exec(new String[]{"launchctl", "load", plistPath});
                NotificationUtil.showSuccess("设置成功", "已添加到开机启动项");
            } else {
                Runtime.getRuntime().exec(new String[]{"launchctl", "unload", plistPath});
                Files.deleteIfExists(Paths.get(plistPath));
                NotificationUtil.showSuccess("设置成功", "已从开机启动项移除");
            }
        } catch (IOException e) {
            LOGGER.error("设置macOS开机启动失败", e);
            NotificationUtil.showError("设置失败", e.getMessage());
        }
    }

    /**
     * 检查是否已启用开机自启动
     * 根据操作系统类型检查对应的自启动配置
     *
     * @return true表示已启用自启动，false表示未启用自启动
     */
    public static boolean isAutoStartEnabled() {
        if (IS_WINDOWS) {
            String startupPath = System.getProperty("user.home") +
                    "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\Work-Tools.lnk";
            return Files.exists(Paths.get(startupPath));
        } else if (IS_MAC) {
            String plistPath = System.getProperty("user.home") +
                    "/Library/LaunchAgents/com.zjhy.worktools.plist";
            return Files.exists(Paths.get(plistPath));
        }
        return false;
    }
} 