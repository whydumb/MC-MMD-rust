package com.shiroha.skinlayers3d.renderer.core;

import org.joml.Vector3f;

/**
 * 渲染参数数据类
 * 用于传递玩家渲染所需的参数
 */
public class RenderParams {
    public float bodyYaw;
    public float bodyPitch;
    public Vector3f translation;
    
    public RenderParams() {
        this.bodyYaw = 0.0f;
        this.bodyPitch = 0.0f;
        this.translation = new Vector3f(0.0f);
    }
}
