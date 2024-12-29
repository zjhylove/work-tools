package com.zjhy.love.worktools.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.function.BiFunction;

/**
 * 文件操作工具类
 */
public class FileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 导出对象到JSON文件
     */
    public static void exportToJson(Object data, String title, Window owner) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JSON文件", "*.json")
        );
        
        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            Files.writeString(file.toPath(), json);
            NotificationUtil.showSuccess("导出成功", "配置保存到: " + file.getName());
        }
    }

    /**
     * 从JSON文件导入对象
     */
    public static <T> T importFromJson(Class<T> type, String title, Window owner) throws Exception {
        return importFromJson(title,owner, ((objectMapper, json) -> {
            try {
                return objectMapper.readValue(json,type);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public static <T> T importFromJson(TypeReference<T> typeReference, String title, Window owner) throws Exception {
        return importFromJson(title,owner, ((objectMapper, json) -> {
            try {
                return objectMapper.readValue(json,typeReference);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    public static <T> T importFromJson(String title, Window owner, BiFunction<ObjectMapper,String,T> readMethod)  throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON文件", "*.json")
        );

        File file = fileChooser.showOpenDialog(owner);
        if (file != null) {
            String json = Files.readString(file.toPath());
            T data = readMethod.apply(MAPPER,json);
            NotificationUtil.showSuccess("导入成功", "配置已从文件加载: " + file.getName());
            return data;
        }
        return null;
    }
} 