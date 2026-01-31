package com.shiroha.skinlayers3d.renderer.core;

import com.shiroha.skinlayers3d.NativeFunc;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * 眼球追踪工具类 (SRP - 单一职责原则)
 * 
 * 将眼球追踪计算逻辑从多个模型实现类中提取出来，
 * 消除代码重复，集中管理追踪算法。
 */
public final class EyeTrackingHelper {
    
    /** 眼球最大转动角度（弧度，约 28.6 度） */
    private static final float MAX_EYE_ANGLE = 0.5f;
    
    /** 最小水平距离阈值，避免除零 */
    private static final float MIN_HORIZONTAL_DIST = 0.01f;
    
    private EyeTrackingHelper() {
        // 工具类，禁止实例化
    }
    
    /**
     * 更新眼球追踪，使模型眼睛看向摄像头
     * 
     * @param nf NativeFunc 实例
     * @param modelHandle 模型句柄
     * @param entity 实体
     * @param entityYaw 实体偏航角
     * @param tickDelta 插值因子
     */
    public static void updateEyeTracking(NativeFunc nf, long modelHandle, 
            LivingEntity entity, float entityYaw, float tickDelta) {
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameRenderer == null) {
            return;
        }
        
        // 获取摄像头位置
        float camX = (float) mc.gameRenderer.getMainCamera().getPosition().x;
        float camY = (float) mc.gameRenderer.getMainCamera().getPosition().y;
        float camZ = (float) mc.gameRenderer.getMainCamera().getPosition().z;
        
        // 获取实体眼睛位置（插值）
        float eyeX = (float) Mth.lerp(tickDelta, entity.xo, entity.getX());
        float eyeY = (float) (Mth.lerp(tickDelta, entity.yo, entity.getY()) + entity.getEyeHeight());
        float eyeZ = (float) Mth.lerp(tickDelta, entity.zo, entity.getZ());
        
        // 计算从眼睛到摄像头的方向向量
        float dx = camX - eyeX;
        float dy = camY - eyeY;
        float dz = camZ - eyeZ;
        
        // 转换到实体局部坐标系（考虑实体朝向）
        float yawRad = entityYaw * ((float) Math.PI / 180F);
        float cosYaw = Mth.cos(yawRad);
        float sinYaw = Mth.sin(yawRad);
        
        float localX = dx * cosYaw + dz * sinYaw;
        float localY = dy;
        float localZ = -dx * sinYaw + dz * cosYaw;
        
        // 计算水平距离
        float horizontalDist = Mth.sqrt(localX * localX + localZ * localZ);
        
        // 计算眼球角度（弧度）
        float eyeAngleX = 0.0f; // 上下
        float eyeAngleY = 0.0f; // 左右
        
        if (horizontalDist > MIN_HORIZONTAL_DIST) {
            // 上下角度：arctan(dy / horizontalDist)
            eyeAngleX = (float) Math.atan2(-localY, horizontalDist);
            // 左右角度：arctan(localX / localZ)
            eyeAngleY = (float) Math.atan2(localX, localZ);
        }
        
        // 限制角度范围
        eyeAngleX = Mth.clamp(eyeAngleX, -MAX_EYE_ANGLE, MAX_EYE_ANGLE);
        eyeAngleY = Mth.clamp(eyeAngleY, -MAX_EYE_ANGLE, MAX_EYE_ANGLE);
        
        // 启用眼球追踪并设置角度
        nf.SetEyeTrackingEnabled(modelHandle, true);
        nf.SetEyeAngle(modelHandle, eyeAngleX, eyeAngleY);
    }
    
    /**
     * 禁用眼球追踪
     */
    public static void disableEyeTracking(NativeFunc nf, long modelHandle) {
        nf.SetEyeTrackingEnabled(modelHandle, false);
    }
}
