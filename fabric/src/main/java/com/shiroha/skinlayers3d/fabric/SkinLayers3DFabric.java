package com.shiroha.skinlayers3d.fabric;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.shiroha.skinlayers3d.fabric.register.SkinLayers3DRegisterCommon;
import net.fabricmc.api.ModInitializer;

public class SkinLayers3DFabric implements ModInitializer {
    public static final Logger logger = LogManager.getLogger();
    @Override
    public void onInitialize() {
        logger.info("SkinLayers3D 初始化开始...");
        SkinLayers3DRegisterCommon.Register();
        logger.info("SkinLayers3D 初始化成功");
    }
}
