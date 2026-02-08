package com.shiroha.mmdskin.renderer.camera;

import com.shiroha.mmdskin.NativeFunc;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

/**
 * MMD 舞台模式相机控制器（单例）
 * 
 * 管理相机 VMD 动画的播放、暂停、停止，以及 Minecraft 相机接管状态。
 * 通过 Mixin 在相机 setup 时覆盖位置/旋转/FOV。
 */
public class MMDCameraController {
    private static final Logger logger = LogManager.getLogger();
    
    private static final MMDCameraController INSTANCE = new MMDCameraController();
    
    // MMD 单位到 Minecraft 单位的缩放（1 MMD 单位 ≈ 0.08 MC 方块）
    private static final float MMD_TO_MC_SCALE = 0.08f;
    // VMD 30fps
    private static final float VMD_FPS = 30.0f;
    
    // 状态
    private boolean active = false;
    private boolean cinematicMode = false;
    private boolean previousHideGui = false;
    
    // 帧控制
    private float currentFrame = 0.0f;
    private float maxFrame = 0.0f;
    private float playbackSpeed = 1.0f;
    
    // 相机 VMD 句柄（可独立于动作 VMD）
    private long cameraAnimHandle = 0;
    // 动作 VMD 句柄（用于同步帧）
    private long motionAnimHandle = 0;
    
    // 相机数据
    private final MMDCameraData cameraData = new MMDCameraData();
    
    // 玩家位置偏移（相机基于玩家位置）
    private double anchorX, anchorY, anchorZ;
    
    // 计算后的相机世界坐标
    private double cameraX, cameraY, cameraZ;
    private float cameraPitch, cameraYaw, cameraRoll;
    private float cameraFov = 70.0f;
    
    // 时间追踪
    private long lastTickTimeNs = 0;
    
    private MMDCameraController() {}
    
    public static MMDCameraController getInstance() {
        return INSTANCE;
    }
    
    /**
     * 启动舞台模式
     * @param motionAnim 动作 VMD 动画句柄（已设置到模型）
     * @param cameraAnim 相机 VMD 动画句柄（含相机数据），0 表示使用 motionAnim 的内嵌相机
     * @param cinematic 是否影院模式（隐藏 HUD）
     */
    public void startStage(long motionAnim, long cameraAnim, boolean cinematic) {
        NativeFunc nf = NativeFunc.GetInst();
        
        this.motionAnimHandle = motionAnim;
        
        // 确定相机数据来源
        if (cameraAnim != 0 && nf.HasCameraData(cameraAnim)) {
            this.cameraAnimHandle = cameraAnim;
        } else if (motionAnim != 0 && nf.HasCameraData(motionAnim)) {
            this.cameraAnimHandle = motionAnim;
        } else {
            logger.warn("[舞台模式] 没有可用的相机数据");
            return;
        }
        
        this.maxFrame = nf.GetAnimMaxFrame(this.cameraAnimHandle);
        this.currentFrame = 0.0f;
        this.cinematicMode = cinematic;
        this.cameraData.setAnimHandle(this.cameraAnimHandle);
        
        // 记录玩家当前位置作为锚点
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            this.anchorX = mc.player.getX();
            this.anchorY = mc.player.getY();
            this.anchorZ = mc.player.getZ();
        }
        
        // 影院模式：隐藏 HUD
        if (cinematic) {
            this.previousHideGui = mc.options.hideGui;
            mc.options.hideGui = true;
        }
        
        this.lastTickTimeNs = System.nanoTime();
        this.active = true;
        logger.info("[舞台模式] 启动: 相机帧={}, 影院={}", maxFrame, cinematic);
    }
    
    /**
     * 停止舞台模式
     */
    public void stopStage() {
        if (!active) return;
        
        Minecraft mc = Minecraft.getInstance();
        
        // 恢复 HUD
        if (cinematicMode) {
            mc.options.hideGui = previousHideGui;
        }
        
        this.active = false;
        this.cameraAnimHandle = 0;
        this.motionAnimHandle = 0;
        this.currentFrame = 0.0f;
        this.maxFrame = 0.0f;
        
        logger.info("[舞台模式] 已停止");
    }
    
    /**
     * 每帧更新（由 Mixin 在 Camera.setup 中调用）
     * 使用 System.nanoTime 计算真实 delta time
     */
    public void updateCamera() {
        if (!active) return;
        
        // 计算真实 delta time
        long now = System.nanoTime();
        float deltaTime = (now - lastTickTimeNs) / 1_000_000_000.0f;
        lastTickTimeNs = now;
        
        // 限制最大 delta（防止暂停后跳帧）
        deltaTime = Math.min(deltaTime, 0.1f);
        
        // 推进帧
        currentFrame += deltaTime * VMD_FPS * playbackSpeed;
        
        // 播放完毕自动停止
        if (currentFrame >= maxFrame) {
            currentFrame = maxFrame;
            stopStage();
            return;
        }
        
        // 更新相机数据
        cameraData.update(currentFrame);
        
        // MMD 坐标 -> Minecraft 世界坐标
        Vector3f mmdPos = cameraData.getPosition();
        cameraX = anchorX + mmdPos.x * MMD_TO_MC_SCALE;
        cameraY = anchorY + mmdPos.y * MMD_TO_MC_SCALE;
        cameraZ = anchorZ + mmdPos.z * MMD_TO_MC_SCALE;
        
        // 欧拉角转换（弧度 -> 度）
        cameraPitch = (float) Math.toDegrees(cameraData.getPitch());
        cameraYaw = (float) Math.toDegrees(cameraData.getYaw());
        cameraRoll = (float) Math.toDegrees(cameraData.getRoll());
        
        // FOV
        cameraFov = cameraData.getFov();
    }
    
    /**
     * 检查是否按下 ESC 键退出舞台模式（由 Mixin 每帧调用）
     */
    public void checkEscapeKey() {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();
        if (org.lwjgl.glfw.GLFW.glfwGetKey(window, org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
            stopStage();
        }
    }
    
    // ==================== 状态查询 ====================
    
    public boolean isActive() {
        return active;
    }
    
    public boolean isCinematicMode() {
        return cinematicMode;
    }
    
    public float getCurrentFrame() {
        return currentFrame;
    }
    
    public float getMaxFrame() {
        return maxFrame;
    }
    
    public float getProgress() {
        return maxFrame > 0 ? currentFrame / maxFrame : 0.0f;
    }
    
    // ==================== 相机参数（供 Mixin 读取） ====================
    
    public double getCameraX() { return cameraX; }
    public double getCameraY() { return cameraY; }
    public double getCameraZ() { return cameraZ; }
    
    public float getCameraPitch() { return cameraPitch; }
    public float getCameraYaw() { return cameraYaw; }
    public float getCameraRoll() { return cameraRoll; }
    
    public float getCameraFov() { return cameraFov; }
    
    public void setPlaybackSpeed(float speed) {
        this.playbackSpeed = speed;
    }
    
    public float getPlaybackSpeed() {
        return playbackSpeed;
    }
}
