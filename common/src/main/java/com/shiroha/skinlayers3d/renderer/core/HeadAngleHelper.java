package com.shiroha.skinlayers3d.renderer.core;

import com.shiroha.skinlayers3d.NativeFunc;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * 头部角度工具类 (SRP - 单一职责原则)
 * 
 * 处理头部角度计算和设置，消除多个模型实现类中的重复代码。
 */
public final class HeadAngleHelper {
    
    /** 头部最大俯仰角（度） */
    private static final float MAX_PITCH = 50.0f;
    
    /** 头部最大偏航角（度） */
    private static final float MAX_YAW = 80.0f;
    
    private HeadAngleHelper() {
        // 工具类，禁止实例化
    }
    
    /**
     * 计算并设置头部角度
     * 
     * @param nf NativeFunc 实例
     * @param modelHandle 模型句柄
     * @param entity 实体
     * @param entityYaw 实体身体偏航角
     * @param tickDelta 插值因子
     * @param context 渲染上下文
     */
    public static void updateHeadAngle(NativeFunc nf, long modelHandle,
            LivingEntity entity, float entityYaw, float tickDelta, RenderContext context) {
        
        // 计算头部俯仰角（限制范围）
        float headAngleX = Mth.clamp(entity.getXRot(), -MAX_PITCH, MAX_PITCH);
        
        // 计算头部偏航角（相对于身体）
        float headYaw = Mth.lerp(tickDelta, entity.yHeadRotO, entity.yHeadRot);
        float headAngleY = (entityYaw - headYaw) % 360.0f;
        
        // 归一化到 [-180, 180] 范围
        if (headAngleY < -180.0f) {
            headAngleY += 360.0f;
        } else if (headAngleY > 180.0f) {
            headAngleY -= 360.0f;
        }
        
        // 限制偏航角范围
        headAngleY = Mth.clamp(headAngleY, -MAX_YAW, MAX_YAW);
        
        // 转换为弧度
        float pitchRad = headAngleX * ((float) Math.PI / 180F);
        float yawRad = headAngleY * ((float) Math.PI / 180F);
        
        // 物品栏场景需要镜像偏航角
        if (context.isInventoryScene()) {
            yawRad = -yawRad;
        }
        
        // 设置头部角度
        // 参数: pitch, yaw, roll, isWorldRender
        nf.SetHeadAngle(modelHandle, pitchRad, yawRad, 0.0f, context.isWorldScene());
    }
}
