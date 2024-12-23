package com.zjhy.love.worktools.common.log;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

@Plugin(
    name = "LogView",
    category = Core.CATEGORY_NAME,
    elementType = Appender.ELEMENT_TYPE,
    printObject = true
)
public class LogViewAppender extends AbstractAppender {

    /**
     * 默认构造函数，用于插件扫描
     */
    public LogViewAppender() {
        super("LogView", null, PatternLayout.createDefaultLayout(), true, null);
    }

    protected LogViewAppender(String name, Filter filter, PatternLayout layout) {
        super(name, filter, layout, true, null);
    }

    @PluginFactory
    public static LogViewAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") PatternLayout layout,
            @PluginElement("Filter") Filter filter) {
        
        if (name == null) {
            LOGGER.error("No name provided for LogViewAppender");
            return null;
        }

        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        return new LogViewAppender(name, filter, layout);
    }

    @Override
    public void append(LogEvent event) {
        String level = event.getLevel().toString();
        String message = event.getMessage().getFormattedMessage();
        LogManager.addLog(level, message);
    }
} 