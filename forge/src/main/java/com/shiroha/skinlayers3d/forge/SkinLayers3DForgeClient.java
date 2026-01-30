package com.shiroha.skinlayers3d.forge;

import com.shiroha.skinlayers3d.SkinLayers3D;
import com.shiroha.skinlayers3d.SkinLayers3DClient;
import com.shiroha.skinlayers3d.forge.config.SkinLayers3DConfig;
import com.shiroha.skinlayers3d.forge.maid.MaidRenderEventHandler;
import com.shiroha.skinlayers3d.forge.register.SkinLayers3DRegisterClient;
import com.shiroha.skinlayers3d.renderer.model.MMDModelOpenGL;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Forge 客户端初始化
 * 
 * 重构说明：
 * - 初始化统一配置管理器
 * - 使用 ConfigManager 访问配置
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = SkinLayers3D.MOD_ID)
public class SkinLayers3DForgeClient {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        SkinLayers3DClient.logger.info("SkinLayers3D Forge 客户端初始化开始...");
        
        // 初始化配置系统
        SkinLayers3DConfig.init();
        
        // 初始化客户端
        SkinLayers3DClient.initClient();
        
        // 注册客户端内容
        SkinLayers3DRegisterClient.Register();
        
        // 配置 MMD Shader
        MMDModelOpenGL.isMMDShaderEnabled = com.shiroha.skinlayers3d.config.ConfigManager.isMMDShaderEnabled();
        
        // 注册女仆渲染事件处理器（TouhouLittleMaid 联动）
        MinecraftForge.EVENT_BUS.register(new MaidRenderEventHandler());
        
        SkinLayers3DClient.logger.info("SkinLayers3D Forge 客户端初始化成功");
    }
}
