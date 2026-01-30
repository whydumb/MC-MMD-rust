package com.shiroha.skinlayers3d.forge.config;

import com.shiroha.skinlayers3d.config.ConfigManager;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Forge 配置实现
 * 使用 ForgeConfigSpec 存储配置
 * 
 * 重构说明：
 * - 实现 ConfigManager.IConfigProvider 接口
 * - 统一默认值（modelPoolMaxCount: 20 -> 100）
 * - 添加配置验证和日志记录
 */
public final class SkinLayers3DConfig implements ConfigManager.IConfigProvider {
    private static final Logger logger = LogManager.getLogger();
    private static SkinLayers3DConfig instance;
    
    public static ForgeConfigSpec config;
    public static ForgeConfigSpec.BooleanValue openGLEnableLighting;
    public static ForgeConfigSpec.IntValue modelPoolMaxCount;
    public static ForgeConfigSpec.BooleanValue isMMDShaderEnabled;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("3d-skin");
        
        openGLEnableLighting = builder
            .comment("启用 OpenGL 光照")
            .define("openGLEnableLighting", true);
            
        modelPoolMaxCount = builder
            .comment("模型池最大数量")
            .defineInRange("modelPoolMaxCount", 100, 0, 1000);
            
        isMMDShaderEnabled = builder
            .comment("启用 MMD Shader")
            .define("isMMDShaderEnabled", false);
            
        builder.pop();
        config = builder.build();
        
        logger.info("Forge 配置规格已构建");
    }
    
    public static void init() {
        instance = new SkinLayers3DConfig();
        ConfigManager.init(instance);
        logger.info("Forge 配置系统初始化完成");
    }
    
    @Override
    public boolean isOpenGLLightingEnabled() {
        return openGLEnableLighting.get();
    }
    
    @Override
    public int getModelPoolMaxCount() {
        return modelPoolMaxCount.get();
    }
    
    @Override
    public boolean isMMDShaderEnabled() {
        return isMMDShaderEnabled.get();
    }
}
