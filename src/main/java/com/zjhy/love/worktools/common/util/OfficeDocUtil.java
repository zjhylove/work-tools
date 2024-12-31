package com.zjhy.love.worktools.common.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.spire.doc.Document;
import com.spire.doc.FileFormat;
import freemarker.cache.TemplateLoader;
import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author zhengjun
 */
public abstract class OfficeDocUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfficeDocUtil.class);

    private OfficeDocUtil() {
    }

    /**
     * 将office xml 格式按照模板进行渲染
     *
     * @param ftlTemplateName 模板名称
     * @param renderData      渲染数据
     * @param renderXmlFile   渲染输出xml 文件
     * @throws IOException       异常
     * @throws TemplateException 异常
     */
    public static void openOfficeXmlRender(String ftlTemplateName, Object renderData, String renderXmlFile) throws IOException, TemplateException {
        Configuration configuration = new Configuration();
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.displayName());
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLocale(Locale.CHINESE);
        TemplateLoader templateLoader = new URLTemplateLoader() {
            @Override
            protected URL getURL(String ftlName) {
                return OfficeDocUtil.class.getResource("/templates/" + ftlName);
            }
        };
        configuration.setTemplateLoader(templateLoader);
        Template template = configuration.getTemplate(ftlTemplateName);
        BufferedWriter writer = FileUtil.getWriter(renderXmlFile, StandardCharsets.UTF_8, false);
        Map<String, Object> map = BeanUtil.beanToMap(renderData);
        template.process(map, writer);
        writer.close();
    }

    /**
     * xml 格式转为docx 格式
     *
     * @param xmlFile  xml 文件
     * @param docxFile docx 文件
     */
    public static void openOfficeXml2Docx(String xmlFile, String docxFile) {
        Document document = new Document();
        document.loadFromFile(xmlFile);
        document.saveToFile(docxFile, FileFormat.Docx);
        document.close();
    }

    /**
     * docx 文档中压缩的json 字符串进行格式化
     *
     * @param sourceFile 原文件
     * @param targetFile 目标文件
     * @throws IOException io 异常
     */
    public static void formatDocxJson(String sourceFile, String targetFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(sourceFile); XWPFDocument document = new XWPFDocument(fis)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (isJson(text)) {
                    for (int j = paragraph.getRuns().size() - 1; j >= 0; j--) {
                        paragraph.removeRun(j);
                    }
                    String formattedJson = JSONUtil.toJsonPrettyStr(text);
                    String[] lines = formattedJson.split("\n");
                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i];
                        XWPFRun run = paragraph.createRun();
                        run.setText(line);
                        if (i != lines.length - 1) {
                            run.addBreak();
                        }
                    }
                }
            }
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                document.write(fos);
            }
        }
    }

    private static boolean isJson(String text) {
        // 简单的JSON检测，可以根据需要进行改进
        return text.trim().startsWith("{") && text.trim().endsWith("}");
    }
}
