package com.shiroha.skinlayers3d.fabric.register;

import com.shiroha.skinlayers3d.SkinLayers3DClient;
import com.shiroha.skinlayers3d.fabric.network.SkinLayers3DNetworkPack;
import com.shiroha.skinlayers3d.renderer.render.SkinLayersRenderFactory;
import com.shiroha.skinlayers3d.renderer.render.SkinLayersRendererPlayerHelper;
import com.shiroha.skinlayers3d.renderer.model.MMDModelManager;
import com.shiroha.skinlayers3d.ui.ActionWheelNetworkHandler;
import com.shiroha.skinlayers3d.ui.ActionWheelScreen;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;

import java.io.File;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

/**
 * Fabric 客户端注册
 * 负责按键绑定、网络通信和实体渲染器注册
 */
@Environment(EnvType.CLIENT)
public class SkinLayers3DRegisterClient {
    static final Logger logger = LogManager.getLogger();
    
    // 动作轮盘按键 (Alt+Z)
    static KeyMapping keyActionWheel = new KeyMapping("key.skinlayers3d.action_wheel", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.categories.skinlayers3d");
    
    // 模型选择按键 (Alt+H)
    static KeyMapping keyModelSelector = new KeyMapping("key.skinlayers3d.model_selector", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, "key.categories.skinlayers3d");
    
    // 功能按键
    static KeyMapping keyResetPhysics = new KeyMapping("key.skinlayers3d.reset_physics", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.skinlayers3d");
    static KeyMapping keyReloadModels = new KeyMapping("key.skinlayers3d.reload_models", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.skinlayers3d");
    static KeyMapping keyReloadProperties = new KeyMapping("key.skinlayers3d.reload_properties", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.skinlayers3d");
    static KeyMapping keyChangeProgram = new KeyMapping("key.skinlayers3d.change_program", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_0, "key.categories.skinlayers3d");
    
    // 材质可见性控制 (Alt+M)
    static KeyMapping keyMaterialVisibility = new KeyMapping("key.skinlayers3d.material_visibility", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.categories.skinlayers3d");
    
    static KeyMapping[] keyBindings = new KeyMapping[]{keyActionWheel, keyModelSelector, keyReloadModels, keyResetPhysics, keyReloadProperties, keyMaterialVisibility};

    public static void Register() {
        Minecraft MCinstance = Minecraft.getInstance();
        
        // 注册所有按键
        for (KeyMapping i : keyBindings)
            KeyBindingHelper.registerKeyBinding(i);
        
        // 注册动作轮盘网络发送器
        ActionWheelNetworkHandler.setNetworkSender(animId -> {
            LocalPlayer player = MCinstance.player;
            if (player != null) {
                // 使用字符串传输动画ID，支持中文文件名
                logger.info("发送动作到服务器: " + animId);
                SkinLayers3DNetworkPack.sendToServer(1, player.getUUID(), animId);
            }
        });
        
        // 注册模型选择网络发送器
        com.shiroha.skinlayers3d.ui.ModelSelectorNetworkHandler.setNetworkSender(modelName -> {
            LocalPlayer player = MCinstance.player;
            if (player != null) {
                // 使用 opCode 3 表示模型变更
                SkinLayers3DNetworkPack.sendToServer(3, player.getUUID(), modelName.hashCode());
            }
        });
        
        // 动作轮盘按键事件 (Alt+Z)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyActionWheel.consumeClick()) {
                long window = MCinstance.getWindow().getWindow();
                boolean altPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS ||
                                   GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;
                
                if (altPressed) {
                    MCinstance.setScreen(new ActionWheelScreen());
                }
            }
        });
        
        // 模型选择按键事件 (Alt+H)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyModelSelector.consumeClick()) {
                long window = MCinstance.getWindow().getWindow();
                boolean altPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS ||
                                   GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;
                
                if (altPressed) {
                    MCinstance.setScreen(new com.shiroha.skinlayers3d.ui.ModelSelectorScreen());
                }
            }
        });
        
        // 材质可见性按键事件 (Alt+M 玩家 / M 女仆)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyMaterialVisibility.consumeClick()) {
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
        });
        
        // 重载模型
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyReloadModels.consumeClick()) {
                MMDModelManager.ReloadModel();
            }
        });
        
        // 重置物理
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyResetPhysics.consumeClick()) {
                onKeyResetPhysicsDown();
            }
        });
        
        // 重载属性
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyReloadProperties.consumeClick()) {
                SkinLayers3DClient.reloadProperties = true;
            }
        });

        // MMD着色器切换
        if(com.shiroha.skinlayers3d.config.ConfigManager.isMMDShaderEnabled()){
            KeyBindingHelper.registerKeyBinding(keyChangeProgram);
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                while (keyChangeProgram.consumeClick()) {
                    SkinLayers3DClient.usingMMDShader = 1 - SkinLayers3DClient.usingMMDShader;
                    if(SkinLayers3DClient.usingMMDShader == 0)
                        MCinstance.gui.getChat().addMessage(Component.nullToEmpty("默认着色器"));
                    if(SkinLayers3DClient.usingMMDShader == 1)
                        MCinstance.gui.getChat().addMessage(Component.nullToEmpty("MMD着色器"));
                }
            });
        }

        // 注册实体渲染器
        File[] modelDirs = new File(MCinstance.gameDirectory, "3d-skin").listFiles();
        if (modelDirs != null) {
            for (File i : modelDirs) {
                if (!i.getName().startsWith("EntityPlayer") && !i.getName().equals("DefaultAnim") && !i.getName().equals("Shader")) {
                    String mcEntityName = i.getName().replace('.', ':');
                    if (EntityType.byString(mcEntityName).isPresent())
                        EntityRendererRegistry.register(EntityType.byString(mcEntityName).get(), new SkinLayersRenderFactory<>(mcEntityName));
                    else
                        logger.warn(mcEntityName + " 实体不存在，跳过渲染注册");
                }
            }
        }

        // 注册网络接收器
        ClientPlayNetworking.registerGlobalReceiver(SkinLayers3DRegisterCommon.SKIN_S2C, (client, handler, buf, responseSender) -> {
            int opCode = buf.readInt();
            UUID playerUUID = buf.readUUID();
            int arg0 = buf.readInt();
            client.execute(() -> {
                SkinLayers3DNetworkPack.DoInClient(opCode, playerUUID, arg0);
            });
        });
        
        logger.info("SkinLayers3D 客户端注册完成");
    }

    /**
     * 重置物理按键处理
     */
    public static void onKeyResetPhysicsDown() {
        Minecraft MCinstance = Minecraft.getInstance();
        LocalPlayer localPlayer = MCinstance.player;
        SkinLayers3DNetworkPack.sendToServer(2, localPlayer.getUUID(), 0);
        RenderSystem.recordRenderCall(()->{
            SkinLayersRendererPlayerHelper.ResetPhysics(localPlayer);
        });
    }
    
    /**
     * 尝试打开女仆材质可见性界面
     */
    private static void tryOpenMaidMaterialVisibility(Minecraft mc) {
        net.minecraft.world.phys.HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != net.minecraft.world.phys.HitResult.Type.ENTITY) {
            return;
        }
        
        net.minecraft.world.phys.EntityHitResult entityHit = (net.minecraft.world.phys.EntityHitResult) hitResult;
        net.minecraft.world.entity.Entity target = entityHit.getEntity();
        
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
}
