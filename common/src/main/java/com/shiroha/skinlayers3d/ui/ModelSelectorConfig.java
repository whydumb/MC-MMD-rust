package com.shiroha.skinlayers3d.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 模型选择配置管理
 * 保存每个玩家选择的模型
 * 
 * 重构说明：
 * - 使用 ConcurrentHashMap 保证线程安全
 * - 添加异常处理和重试机制
 * - 改进日志记录
 * - 添加配置验证
 */
public class ModelSelectorConfig {
    private static final Logger logger = LogManager.getLogger();
    private static final String CONFIG_FILE = "config/skinlayers3d/model_selector.json";
    private static ModelSelectorConfig instance;
    
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private ConfigData data;
    private long lastSaveTime = 0;
    private static final long SAVE_COOLDOWN = 1000; // 保存冷却时间 1 秒

    private ModelSelectorConfig() {
        load();
    }

    public static synchronized ModelSelectorConfig getInstance() {
        if (instance == null) {
            instance = new ModelSelectorConfig();
        }
        return instance;
    }

    /**
     * 加载配置（带重试机制）
     */
    private void load() {
        Minecraft mc = Minecraft.getInstance();
        File configFile = new File(mc.gameDirectory, CONFIG_FILE);
        
        if (configFile.exists()) {
            int retryCount = 0;
            int maxRetries = 3;
            
            while (retryCount < maxRetries) {
                try (Reader reader = new FileReader(configFile)) {
                    data = gson.fromJson(reader, ConfigData.class);
                    
                    // 验证数据
                    if (data == null || data.playerModels == null) {
                        throw new IOException("配置数据无效");
                    }
                    
                    logger.info("模型选择配置加载成功 ({} 个玩家)", data.playerModels.size());
                    return;
                } catch (Exception e) {
                    retryCount++;
                    logger.warn("加载模型选择配置失败 (尝试 {}/{}): {}", retryCount, maxRetries, e.getMessage());
                    
                    if (retryCount >= maxRetries) {
                        logger.error("配置加载失败，使用默认配置", e);
                        data = new ConfigData();
                        save();
                    }
                }
            }
        } else {
            data = new ConfigData();
            save();
        }
    }

    /**
     * 保存配置（带冷却和异常处理）
     */
    public synchronized void save() {
        // 冷却检查，避免频繁保存
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSaveTime < SAVE_COOLDOWN) {
            return;
        }
        
        Minecraft mc = Minecraft.getInstance();
        File configFile = new File(mc.gameDirectory, CONFIG_FILE);
        
        // 确保目录存在
        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }
        
        int retryCount = 0;
        int maxRetries = 3;
        
        while (retryCount < maxRetries) {
            try (Writer writer = new FileWriter(configFile)) {
                gson.toJson(data, writer);
                lastSaveTime = currentTime;
                logger.debug("模型选择配置保存成功");
                return;
            } catch (Exception e) {
                retryCount++;
                logger.warn("保存模型选择配置失败 (尝试 {}/{}): {}", retryCount, maxRetries, e.getMessage());
                
                if (retryCount >= maxRetries) {
                    logger.error("配置保存失败", e);
                }
            }
        }
    }

    /**
     * 获取当前玩家选择的模型
     */
    public String getSelectedModel() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            return getPlayerModel(mc.player.getName().getString());
        }
        return "默认 (原版渲染)";
    }
    
    /**
     * 获取指定玩家选择的模型
     */
    public String getPlayerModel(String playerName) {
        if (playerName == null || playerName.isEmpty()) {
            return "默认 (原版渲染)";
        }
        return data.playerModels.getOrDefault(playerName, "默认 (原版渲染)");
    }
    
    /**
     * 设置当前玩家选择的模型
     */
    public void setSelectedModel(String modelName) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            setPlayerModel(mc.player.getName().getString(), modelName);
        }
    }
    
    /**
     * 设置指定玩家选择的模型
     */
    public void setPlayerModel(String playerName, String modelName) {
        if (playerName == null || playerName.isEmpty()) {
            logger.warn("尝试为空玩家名设置模型");
            return;
        }
        
        if (modelName == null) {
            modelName = "默认 (原版渲染)";
        }
        
        data.playerModels.put(playerName, modelName);
        save();
        logger.info("玩家 {} 选择模型: {}", playerName, modelName);
    }
    
    /**
     * 移除玩家的模型选择
     */
    public void removePlayerModel(String playerName) {
        if (data.playerModels.remove(playerName) != null) {
            save();
            logger.info("已移除玩家 {} 的模型选择", playerName);
        }
    }
    
    /**
     * 获取所有玩家的模型选择
     */
    public Map<String, String> getAllPlayerModels() {
        return new ConcurrentHashMap<>(data.playerModels);
    }
    
    /**
     * 配置数据类（线程安全）
     */
    private static class ConfigData {
        // 使用 ConcurrentHashMap 保证线程安全
        Map<String, String> playerModels = new ConcurrentHashMap<>();
    }
}
