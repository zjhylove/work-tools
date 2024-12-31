package com.zjhy.love.worktools.common;

public interface ShutdownHook {

    /**
     * 关机需要释放的资源
     */
    void shutdown();
}
