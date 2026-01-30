package com.shiroha.skinlayers3d.forge.register;

import com.shiroha.skinlayers3d.SkinLayers3DClient;
import com.shiroha.skinlayers3d.forge.config.SkinLayers3DConfig;
import com.shiroha.skinlayers3d.forge.network.SkinLayers3DNetworkPack;
import com.shiroha.skinlayers3d.maid.MaidModelNetworkHandler;
import com.shiroha.skinlayers3d.maid.MaidModelSelectorScreen;
import com.shiroha.skinlayers3d.renderer.render.SkinLayersRenderFactory;
import com.shiroha.skinlayers3d.renderer.render.SkinLayersRendererPlayerHelper;
import com.shiroha.skinlayers3d.renderer.model.MMDModelManager;
import com.shiroha.skinlayers3d.ui.ActionWheelNetworkHandler;
import com.shiroha.skinlayers3d.ui.ActionWheelScreen;
import com.mojang.blaze3d.platform.InputConstants;
import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

/**
 * Forge 客户端注册
 * 负责按键绑定、网络通信和实体渲染器注册
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class SkinLayers3DRegisterClient {
    static final Logger logger = LogManager.getLogger();
    
    // 动作轮盘按键 (Alt+Z)
    static KeyMapping keyActionWheel = new KeyMapping("key.skinlayers3d.action_wheel", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.skinlayers3d");
    
    // 模型选择按键 (Alt+H)
    static KeyMapping keyModelSelector = new KeyMapping("key.skinlayers3d.model_selector", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.categories.skinlayers3d");
    
    // 功能按键
    static KeyMapping keyResetPhysics = new KeyMapping("key.skinlayers3d.reset_physics", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.skinlayers3d");
    static KeyMapping keyReloadModels = new KeyMapping("key.skinlayers3d.reload_models", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.skinlayers3d");
    static KeyMapping keyReloadProperties = new KeyMapping("key.skinlayers3d.reload_properties", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.skinlayers3d");
    static KeyMapping keyChangeProgram = new KeyMapping("key.skinlayers3d.change_program", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_0, "key.categories.skinlayers3d");
    
    // 材质可见性控制 (Alt+M)
    static KeyMapping keyMaterialVisibility = new KeyMapping("key.skinlayers3d.material_visibility", KeyConflictContext.IN_GAME, KeyModifier.NONE, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.categories.skinlayers3d");

    public static void Register() {
        Minecraft MCinstance = Minecraft.getInstance();
        RegisterRenderers RR = new RegisterRenderers();
        RegisterKeyMappingsEvent RKE = new RegisterKeyMappingsEvent(MCinstance.options);
        
        // 注册所有按键
        for (KeyMapping i : new KeyMapping[]{keyActionWheel, keyModelSelector, keyReloadModels, keyResetPhysics, keyReloadProperties, keyMaterialVisibility})
            RKE.register(i);
        
        if(SkinLayers3DConfig.isMMDShaderEnabled.get())
            RKE.register(keyChangeProgram);

        // 注册动作轮盘网络发送器
        ActionWheelNetworkHandler.setNetworkSender(animId -> {
            LocalPlayer player = MCinstance.player;
            if (player != null) {
                // 使用字符串传输动画ID，支持中文文件名
                logger.info("发送动作到服务器: " + animId);
                SkinLayers3DRegisterCommon.channel.sendToServer(new SkinLayers3DNetworkPack(1, player.getUUID(), animId));
            }
        });
        
        // 注册模型选择网络发送器
        com.shiroha.skinlayers3d.ui.ModelSelectorNetworkHandler.setNetworkSender(modelName -> {
            LocalPlayer player = MCinstance.player;
            if (player != null) {
                // 使用 opCode 3 表示模型变更
                SkinLayers3DRegisterCommon.channel.sendToServer(new SkinLayers3DNetworkPack(3, player.getUUID(), modelName.hashCode()));
            }
        });
        
        // 注册女仆模型选择网络发送器
        MaidModelNetworkHandler.setNetworkSender((entityId, modelName) -> {
            LocalPlayer player = MCinstance.player;
            if (player != null) {
                // 使用 opCode 4 表示女仆模型变更，data 字段存储 entityId
                SkinLayers3DRegisterCommon.channel.sendToServer(new SkinLayers3DNetworkPack(4, player.getUUID(), entityId, modelName));
            }
        });

        // 注册实体渲染器
        File[] modelDirs = new File(MCinstance.gameDirectory, "3d-skin").listFiles();
        if (modelDirs != null) {
            for (File i : modelDirs) {
                if (!i.getName().startsWith("EntityPlayer") && !i.getName().equals("DefaultAnim") && !i.getName().equals("Shader")) {
                    String mcEntityName = i.getName().replace('.', ':');
                    if (EntityType.byString(mcEntityName).isPresent()){
                        RR.registerEntityRenderer(EntityType.byString(mcEntityName).get(), new SkinLayersRenderFactory<>(mcEntityName));
                        logger.info(mcEntityName + " 实体存在，注册渲染器");
                    }else{
                        logger.warn(mcEntityName + " 实体不存在，跳过渲染注册");
                    }
                }
            }
        }
        logger.info("SkinLayers3D 客户端注册完成");
    }

    /**
     * 按键事件处理
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onKeyPressed(InputEvent.Key event) {
        Minecraft MCinstance = Minecraft.getInstance();
        LocalPlayer localPlayer = MCinstance.player;
        
        if (localPlayer == null) return;

        // 动作轮盘 (Alt+Z)
        if (keyActionWheel.isDown()) {
            long window = MCinstance.getWindow().getWindow();
            boolean altPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS ||
                               GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;
            
            if (altPressed) {
                MCinstance.setScreen(new ActionWheelScreen());
            }
        }
        
        // 模型选择 (Alt+H 玩家模型 / H 女仆模型)
        if (keyModelSelector.isDown()) {
            long window = MCinstance.getWindow().getWindow();
            boolean altPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS ||
                               GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;
            
            if (altPressed) {
                // Alt+H: 打开玩家模型选择界面
                MCinstance.setScreen(new com.shiroha.skinlayers3d.ui.ModelSelectorScreen());
            } else {
                // H: 检测是否看向女仆，打开女仆模型选择界面
                tryOpenMaidModelSelector(MCinstance);
            }
        }

        // 重载模型
        if (keyReloadModels.isDown()) {
            MMDModelManager.ReloadModel();
        }
        
        // 重置物理
        if (keyResetPhysics.isDown()) {
            MMDModelManager.Model m = MMDModelManager.GetModel("EntityPlayer_" + localPlayer.getName().getString());
            if (m != null) {
                SkinLayersRendererPlayerHelper.ResetPhysics(localPlayer);
                SkinLayers3DRegisterCommon.channel.sendToServer(new SkinLayers3DNetworkPack(2, localPlayer.getUUID(), 0));
            }
        }
        
        // 重载属性
        if (keyReloadProperties.isDown()) {
            SkinLayers3DClient.reloadProperties = true;
        }
        
        // MMD着色器切换
        if (keyChangeProgram.isDown() && SkinLayers3DConfig.isMMDShaderEnabled.get()) {
            SkinLayers3DClient.usingMMDShader = 1 - SkinLayers3DClient.usingMMDShader;
            
            if(SkinLayers3DClient.usingMMDShader == 0)
                MCinstance.gui.getChat().addMessage(Component.literal("默认着色器"));
            if(SkinLayers3DClient.usingMMDShader == 1)
                MCinstance.gui.getChat().addMessage(Component.literal("MMD着色器"));
        }
        
        // 材质可见性 (Alt+M 玩家 / M 女仆)
        if (keyMaterialVisibility.isDown()) {
            long window = MCinstance.getWindow().getWindow();
            boolean altPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS ||
                               GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;
            
            if (altPressed) {
                // Alt+M: 打开玩家材质可见性界面
                com.shiroha.skinlayers3d.ui.MaterialVisibilityScreen screen = 
                    com.shiroha.skinlayers3d.ui.MaterialVisibilityScreen.createForPlayer();
                if (screen != null) {
                    MCinstance.setScreen(screen);
                } else {
                    MCinstance.gui.getChat().addMessage(Component.literal("§c未找到玩家模型，请先选择一个MMD模型"));
                }
            } else {
                // M: 检测是否看向女仆，打开女仆材质可见性界面
                tryOpenMaidMaterialVisibility(MCinstance);
            }
        }
    }
    
    /**
     * 尝试打开女仆材质可见性界面
     */
    private static void tryOpenMaidMaterialVisibility(Minecraft mc) {
        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) {
            return;
        }
        
        EntityHitResult entityHit = (EntityHitResult) hitResult;
        Entity target = entityHit.getEntity();
        
        String className = target.getClass().getName();
        if (className.contains("EntityMaid") || className.contains("touhoulittlemaid")) {
            String maidName = target.getName().getString();
            
            com.shiroha.skinlayers3d.ui.MaterialVisibilityScreen screen = 
                com.shiroha.skinlayers3d.ui.MaterialVisibilityScreen.createForMaid(target.getUUID(), maidName);
            if (screen != null) {
                mc.setScreen(screen);
                logger.info("打开女仆材质可见性界面: {} (ID: {})", maidName, target.getId());
            } else {
                mc.gui.getChat().addMessage(Component.literal("§c未找到女仆模型，请先为女仆选择一个MMD模型"));
            }
        }
    }
    
    /**
     * 尝试打开女仆模型选择界面
     * 检测玩家是否正在看向女仆实体
     */
    private static void tryOpenMaidModelSelector(Minecraft mc) {
        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) {
            return;
        }
        
        EntityHitResult entityHit = (EntityHitResult) hitResult;
        Entity target = entityHit.getEntity();
        
        // 检查是否是女仆实体（通过类名判断，避免硬依赖）
        String className = target.getClass().getName();
        if (className.contains("EntityMaid") || className.contains("touhoulittlemaid")) {
            // 获取女仆信息
            String maidName = target.getName().getString();
            
            // 打开女仆模型选择界面
            mc.setScreen(new MaidModelSelectorScreen(
                target.getUUID(),
                target.getId(),
                maidName
            ));
            
            logger.info("打开女仆模型选择界面: {} (ID: {})", maidName, target.getId());
        }
    }
}
