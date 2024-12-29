package com.zjhy.love.worktools.common.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * 用户操作历史记录工具类
 *
 * @author zhengjun
 */
public class HistoryUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryUtil.class);

    private static final String HISTORY_DIR = System.getProperty("user.home") + "/.work-tools";
    private static final String HISTORY_FILE = "history.json";
    private static final Map<String, Object> HISTORY_CACHE = new ConcurrentHashMap<>();

    private HistoryUtil() {
    }

    static {
        // 确保目录存在
        FileUtil.mkdir(HISTORY_DIR);
        // 加载历史记录
        loadHistory();
    }

    /**
     * 保存工具的操作历史
     *
     * @param toolName 工具名称
     * @param params   参数内容
     */
    public static void saveHistory(String toolName, Object params) {
        try {
            HISTORY_CACHE.put(toolName, params);

            // 写入文件
            String historyJson = JSONUtil.toJsonStr(HISTORY_CACHE);
            Path historyPath = Paths.get(HISTORY_DIR, HISTORY_FILE);
            FileUtil.writeUtf8String(historyJson, historyPath.toFile());
        } catch (Exception e) {
            LOGGER.error("保存历史记录失败", e);
            NotificationUtil.showError("保存历史记录失败", e.getMessage());
        }
    }

    /**
     * 获取工具的历史记录
     *
     * @param toolName 工具名称
     * @return 历史参数
     */
    public static <T> T getHistory(String toolName, Class<T> type) {
        return getHistory(toolName,(mapper,json)-> {
            try {
                if (type == String.class) {
                    return (T)json;
                }
                return mapper.readValue(json,type);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <T> T getHistory(String toolName, TypeReference<T> typeReference) {
        return getHistory(toolName,(mapper,json)-> {
            try {
                return mapper.readValue(json,typeReference);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <T> T getHistory(String toolName, BiFunction<ObjectMapper,String, T> readMethod) {
        Object history = HISTORY_CACHE.get(toolName);
        if (history == null) {
            return null;
        }
        if (history instanceof String) {
            return (T)history;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = JSONUtil.toJsonStr(HISTORY_CACHE.get(toolName));
            return readMethod.apply(mapper,json);
        } catch (Exception e) {
            NotificationUtil.showError("读取历史记录失败 ", e.getMessage());
            return null;
        }
    }

    /**
     * 清除指定工具的历史记录
     *
     * @param toolName 工具名称
     */
    public static void clearHistory(String toolName) {
        HISTORY_CACHE.remove(toolName);
        saveHistory(toolName, null);
    }

    /**
     * 加载历史记录
     */
    private static void loadHistory() {
        try {
            Path historyPath = Paths.get(HISTORY_DIR, HISTORY_FILE);
            if (!FileUtil.exist(historyPath.toFile())) {
                return;
            }
            String historyJson = FileUtil.readUtf8String(historyPath.toFile());
            Map<String, Object> history = JSONUtil.toBean(historyJson, HashMap.class);
            HISTORY_CACHE.putAll(history);
        } catch (Exception e) {
            NotificationUtil.showError("加载历史记录失败: ", e.getMessage());
        }
    }
} 