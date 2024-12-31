package com.zjhy.love.worktools.common.util;

import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

/**
 * 弹出框工具
 * @author zj
 */
public class DialogUtil {

    /**
     * 创建公共数据弹出
     * @param title 标题
     * @return 数据弹窗
     * @param <T> 类型
     */
    public static<T> Dialog<T> createCommonDataDialog(String title) {
        return createDataDialog(title,null, "surface-card");
    }

    /**
     * 创建弹出框
     * @param title 标题
     * @param headerText 头文字
     * @param styleClass 样式类型
     * @return 数据弹框
     * @param <T> 类型
     */
    public static<T> Dialog<T> createDataDialog(String title,String headerText,String... styleClass) {
        Dialog<T> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStyleClass().addAll(styleClass);
        return dialog;
    }
}
