package com.shiroha.skinlayers3d.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 统一配置管理器
 * 提供跨平台的配置访问接口，解耦配置实现
 * 
 * 设计原则：
 * - 依赖倒置：依赖抽象接口而非具体实现
 * - 单一职责：只负责配置的读取和管理
 */
public class ConfigManager {
    private static final Logger logger = LogManager.getLogger();
    private static IConfigProvider provider;
    
    /**
     * 初始化配置管理器
     * @param configProvider 平台特定的配置提供者
     */
    public static void init(IConfigProvider configProvider) {
        provider = configProvider;
        logger.info("配置管理器初始化完成，使用提供者: " + configProvider.getClass().getSimpleName());
    }
    
    /**
     * 获取 OpenGL 光照启用状态
     */
    public static boolean isOpenGLLightingEnabled() {
        return provider != null ? provider.isOpenGLLightingEnabled() : true;
    }
    
    /**
     * 获取模型池最大数量
     */
    public static int getModelPoolMaxCount() {
        return provider != null ? provider.getModelPoolMaxCount() : 100;
    }
    
    /**
     * 获取 MMD Shader 启用状态
     */
    public static boolean isMMDShaderEnabled() {
        return provider != null ? provider.isMMDShaderEnabled() : false;
    }
    
    /**
     * 配置提供者接口
     * 各平台实现此接口以提供配置值
     */
    public interface IConfigProvider {
        boolean isOpenGLLightingEnabled();
        int getModelPoolMaxCount();
        boolean isMMDShaderEnabled();
    }
}
