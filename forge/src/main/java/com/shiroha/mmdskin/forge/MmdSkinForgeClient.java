package com.shiroha.mmdskin.forge;

import com.shiroha.mmdskin.MmdSkin;
import com.shiroha.mmdskin.MmdSkinClient;
import com.shiroha.mmdskin.forge.config.MmdSkinConfig;
import com.shiroha.mmdskin.forge.maid.MaidRenderEventHandler;
import com.shiroha.mmdskin.forge.register.MmdSkinRegisterClient;
import com.shiroha.mmdskin.renderer.model.MMDModelOpenGL;

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
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = MmdSkin.MOD_ID)
public class MmdSkinForgeClient {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        MmdSkinClient.logger.info("MMD Skin Forge 客户端初始化开始...");
        
        // 初始化配置系统
        MmdSkinConfig.init();
        
        // 初始化客户端
        MmdSkinClient.initClient();
        
        // 注册客户端内容
        MmdSkinRegisterClient.Register();
        
        // 配置 MMD Shader
        MMDModelOpenGL.isMMDShaderEnabled = com.shiroha.mmdskin.config.ConfigManager.isMMDShaderEnabled();
        
        // 注册女仆渲染事件处理器（TouhouLittleMaid 联动）
        MinecraftForge.EVENT_BUS.register(new MaidRenderEventHandler());
        
        MmdSkinClient.logger.info("MMD Skin Forge 客户端初始化成功");
    }
}
