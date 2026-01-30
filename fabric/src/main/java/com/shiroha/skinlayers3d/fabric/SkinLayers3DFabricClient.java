package com.shiroha.skinlayers3d.fabric;

import com.shiroha.skinlayers3d.SkinLayers3DClient;
import com.shiroha.skinlayers3d.fabric.config.SkinLayers3DConfig;
import com.shiroha.skinlayers3d.fabric.register.SkinLayers3DRegisterClient;
import com.shiroha.skinlayers3d.renderer.model.MMDModelOpenGL;

import net.fabricmc.api.ClientModInitializer;

/**
 * Fabric 客户端初始化
 * 
 * 重构说明：
 * - 初始化统一配置管理器
 * - 使用 ConfigManager 访问配置
 */
public class SkinLayers3DFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SkinLayers3DClient.logger.info("SkinLayers3D Fabric 客户端初始化开始...");
        
        // 初始化配置系统
        SkinLayers3DConfig.init();
        
        // 初始化客户端
        SkinLayers3DClient.initClient();
        
        // 注册客户端内容
        SkinLayers3DRegisterClient.Register();
        
        // 配置 MMD Shader
        MMDModelOpenGL.isMMDShaderEnabled = com.shiroha.skinlayers3d.config.ConfigManager.isMMDShaderEnabled();
        
        SkinLayers3DClient.logger.info("SkinLayers3D Fabric 客户端初始化成功");
    }
}
