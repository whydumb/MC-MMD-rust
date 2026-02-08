package com.shiroha.mmdskin.ui.stage;

import com.shiroha.mmdskin.NativeFunc;
import com.shiroha.mmdskin.config.PathConstants;
import com.shiroha.mmdskin.config.StageConfig;
import com.shiroha.mmdskin.renderer.camera.MMDCameraController;
import com.shiroha.mmdskin.renderer.model.MMDModelManager;
import com.shiroha.mmdskin.ui.config.ModelSelectorConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 舞台模式选择界面
 * 左侧：动作 VMD 列表
 * 右侧：相机 VMD 列表
 * 底部：影院模式开关 + 开始/取消按钮
 */
public class StageSelectScreen extends Screen {
    private static final Logger logger = LogManager.getLogger();
    
    // 布局常量
    private static final int PANEL_MARGIN = 8;
    private static final int HEADER_HEIGHT = 30;
    private static final int FOOTER_HEIGHT = 56;
    private static final int ITEM_HEIGHT = 16;
    private static final int ITEM_SPACING = 1;
    private static final int GAP = 8;
    
    // 配色
    private static final int COLOR_BG = 0xD0101418;
    private static final int COLOR_PANEL_BG = 0xC0181C22;
    private static final int COLOR_BORDER = 0xFF2A3A4A;
    private static final int COLOR_ACCENT = 0xFF60A0D0;
    private static final int COLOR_TEXT = 0xFFDDDDDD;
    private static final int COLOR_TEXT_DIM = 0xFF888888;
    private static final int COLOR_ITEM_HOVER = 0x30FFFFFF;
    private static final int COLOR_ITEM_SELECTED = 0x4060A0D0;
    private static final int COLOR_CAMERA_TAG = 0xFF90D060;
    private static final int COLOR_BTN_START = 0xFF40A060;
    private static final int COLOR_BTN_CANCEL = 0xFF666666;
    private static final int COLOR_TOGGLE_ON = 0xFF60A0D0;
    private static final int COLOR_TOGGLE_OFF = 0xFF444444;
    
    // VMD 文件列表
    private final List<VmdEntry> motionList = new ArrayList<>();
    private final List<VmdEntry> cameraList = new ArrayList<>();
    
    // 选择状态
    private int selectedMotion = -1;
    private int selectedCamera = -1;
    private boolean cinematicMode;
    
    // 滚动
    private int motionScrollOffset = 0;
    private int cameraScrollOffset = 0;
    
    // 悬停
    private int hoveredMotion = -1;
    private int hoveredCamera = -1;
    private boolean hoverStart = false;
    private boolean hoverCancel = false;
    private boolean hoverToggle = false;
    
    // 布局缓存
    private int leftPanelX, leftPanelW;
    private int rightPanelX, rightPanelW;
    private int panelY, panelH;
    private int listTop, listBottom;
    
    public StageSelectScreen() {
        super(Component.literal("舞台模式"));
        StageConfig config = StageConfig.getInstance();
        this.cinematicMode = config.cinematicMode;
        scanVmdFiles();
        restoreSelection(config);
    }
    
    private void scanVmdFiles() {
        motionList.clear();
        cameraList.clear();
        
        NativeFunc nf = NativeFunc.GetInst();
        
        // 扫描 CustomAnim 目录
        scanDirectory(PathConstants.getCustomAnimDir(), nf, "CustomAnim");
        // 扫描 StageAnim 目录
        scanDirectory(PathConstants.getStageAnimDir(), nf, "StageAnim");
    }
    
    private void scanDirectory(File dir, NativeFunc nf, String source) {
        if (!dir.exists() || !dir.isDirectory()) return;
        
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(PathConstants.VMD_EXTENSION));
        if (files == null) return;
        
        for (File file : files) {
            String name = file.getName();
            String path = file.getAbsolutePath();
            
            // 尝试加载检测相机数据
            long tempAnim = nf.LoadAnimation(0, path);
            boolean hasCamera = false;
            if (tempAnim != 0) {
                hasCamera = nf.HasCameraData(tempAnim);
                nf.DeleteAnimation(tempAnim);
            }
            
            VmdEntry entry = new VmdEntry(name, path, source, hasCamera);
            
            // 动作列表包含所有 VMD
            motionList.add(entry);
            
            // 相机列表只包含含相机数据的 VMD
            if (hasCamera) {
                cameraList.add(entry);
            }
        }
    }
    
    private void restoreSelection(StageConfig config) {
        for (int i = 0; i < motionList.size(); i++) {
            if (motionList.get(i).path.equals(config.lastMotionVmd)) {
                selectedMotion = i;
                break;
            }
        }
        for (int i = 0; i < cameraList.size(); i++) {
            if (cameraList.get(i).path.equals(config.lastCameraVmd)) {
                selectedCamera = i;
                break;
            }
        }
    }
    
    @Override
    protected void init() {
        super.init();
        int totalW = this.width - PANEL_MARGIN * 3;
        leftPanelW = totalW / 2;
        rightPanelW = totalW - leftPanelW;
        leftPanelX = PANEL_MARGIN;
        rightPanelX = leftPanelX + leftPanelW + PANEL_MARGIN;
        panelY = HEADER_HEIGHT + PANEL_MARGIN;
        panelH = this.height - panelY - FOOTER_HEIGHT - PANEL_MARGIN;
        listTop = panelY + 22;
        listBottom = panelY + panelH - 2;
    }
    
    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // 背景
        g.fill(0, 0, this.width, this.height, COLOR_BG);
        
        // 标题
        g.drawCenteredString(this.font, "🎬 舞台模式", this.width / 2, 10, COLOR_ACCENT);
        
        // 左侧面板 — 动作 VMD
        renderPanel(g, leftPanelX, panelY, leftPanelW, panelH, "动作 VMD (" + motionList.size() + ")");
        hoveredMotion = -1;
        renderVmdList(g, motionList, leftPanelX, selectedMotion, motionScrollOffset, mouseX, mouseY, true);
        
        // 右侧面板 — 相机 VMD
        renderPanel(g, rightPanelX, panelY, rightPanelW, panelH, "相机 VMD (" + cameraList.size() + ")");
        hoveredCamera = -1;
        renderVmdList(g, cameraList, rightPanelX, selectedCamera, cameraScrollOffset, mouseX, mouseY, false);
        
        // 底部控件
        renderFooter(g, mouseX, mouseY);
        
        super.render(g, mouseX, mouseY, partialTick);
    }
    
    private void renderPanel(GuiGraphics g, int x, int y, int w, int h, String title) {
        // 面板背景
        g.fill(x, y, x + w, y + h, COLOR_PANEL_BG);
        // 边框
        g.fill(x, y, x + w, y + 1, COLOR_BORDER);
        g.fill(x, y + h - 1, x + w, y + h, COLOR_BORDER);
        g.fill(x, y, x + 1, y + h, COLOR_BORDER);
        g.fill(x + w - 1, y, x + w, y + h, COLOR_BORDER);
        // 标题
        g.drawString(this.font, title, x + 6, y + 7, COLOR_ACCENT, false);
        // 分隔线
        g.fill(x + 2, y + 20, x + w - 2, y + 21, COLOR_BORDER);
    }
    
    private void renderVmdList(GuiGraphics g, List<VmdEntry> list, int panelX, int selected, int scrollOffset, int mouseX, int mouseY, boolean isMotion) {
        for (int i = 0; i < list.size(); i++) {
            int itemY = listTop + i * (ITEM_HEIGHT + ITEM_SPACING) - scrollOffset;
            if (itemY + ITEM_HEIGHT < listTop || itemY > listBottom) continue;
            
            int itemW = (isMotion ? leftPanelW : rightPanelW) - 4;
            int itemX = panelX + 2;
            
            boolean hovered = mouseX >= itemX && mouseX < itemX + itemW && mouseY >= itemY && mouseY < itemY + ITEM_HEIGHT;
            boolean sel = (i == selected);
            
            if (hovered) {
                if (isMotion) hoveredMotion = i; else hoveredCamera = i;
            }
            
            // 背景
            if (sel) {
                g.fill(itemX, itemY, itemX + itemW, itemY + ITEM_HEIGHT, COLOR_ITEM_SELECTED);
            } else if (hovered) {
                g.fill(itemX, itemY, itemX + itemW, itemY + ITEM_HEIGHT, COLOR_ITEM_HOVER);
            }
            
            // 文件名
            VmdEntry entry = list.get(i);
            String displayName = entry.name;
            if (displayName.length() > 20) {
                displayName = displayName.substring(0, 18) + "..";
            }
            g.drawString(this.font, displayName, itemX + 4, itemY + 4, sel ? COLOR_ACCENT : COLOR_TEXT, false);
            
            // 相机标记
            if (entry.hasCamera) {
                g.drawString(this.font, "📷", itemX + itemW - 14, itemY + 4, COLOR_CAMERA_TAG, false);
            }
        }
    }
    
    private void renderFooter(GuiGraphics g, int mouseX, int mouseY) {
        int footerY = this.height - FOOTER_HEIGHT;
        
        // 影院模式开关
        int toggleX = PANEL_MARGIN + 4;
        int toggleY = footerY + 8;
        int toggleW = 30;
        int toggleH = 14;
        
        hoverToggle = mouseX >= toggleX && mouseX < toggleX + toggleW + 80 && mouseY >= toggleY && mouseY < toggleY + toggleH;
        
        g.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH,
                cinematicMode ? COLOR_TOGGLE_ON : COLOR_TOGGLE_OFF);
        int knobX = cinematicMode ? toggleX + toggleW - 12 : toggleX + 2;
        g.fill(knobX, toggleY + 2, knobX + 10, toggleY + toggleH - 2, 0xFFFFFFFF);
        g.drawString(this.font, "影院模式", toggleX + toggleW + 6, toggleY + 3, COLOR_TEXT, false);
        
        // 按钮
        int btnW = 70;
        int btnH = 20;
        int btnY = footerY + 4;
        
        // 取消按钮
        int cancelX = this.width - PANEL_MARGIN - btnW;
        hoverCancel = mouseX >= cancelX && mouseX < cancelX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        g.fill(cancelX, btnY, cancelX + btnW, btnY + btnH, hoverCancel ? 0xFF888888 : COLOR_BTN_CANCEL);
        g.drawCenteredString(this.font, "取消", cancelX + btnW / 2, btnY + 6, COLOR_TEXT);
        
        // 开始按钮
        int startX = cancelX - btnW - 8;
        boolean canStart = selectedMotion >= 0 && selectedCamera >= 0;
        hoverStart = canStart && mouseX >= startX && mouseX < startX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
        int startColor = canStart ? (hoverStart ? 0xFF50C070 : COLOR_BTN_START) : 0xFF333333;
        g.fill(startX, btnY, startX + btnW, btnY + btnH, startColor);
        g.drawCenteredString(this.font, "▶ 开始", startX + btnW / 2, btnY + 6, canStart ? 0xFFFFFFFF : COLOR_TEXT_DIM);
        
        // 选择提示
        String hint = "";
        if (selectedMotion >= 0) hint += "动作: " + motionList.get(selectedMotion).name;
        if (selectedCamera >= 0) hint += "  相机: " + cameraList.get(selectedCamera).name;
        if (!hint.isEmpty()) {
            g.drawString(this.font, hint, PANEL_MARGIN + 4, footerY + 30, COLOR_TEXT_DIM, false);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // 动作列表点击
            if (hoveredMotion >= 0) {
                selectedMotion = hoveredMotion;
                // 如果选中的动作 VMD 自带相机，自动选中
                VmdEntry entry = motionList.get(selectedMotion);
                if (entry.hasCamera && selectedCamera < 0) {
                    for (int i = 0; i < cameraList.size(); i++) {
                        if (cameraList.get(i).path.equals(entry.path)) {
                            selectedCamera = i;
                            break;
                        }
                    }
                }
                return true;
            }
            // 相机列表点击
            if (hoveredCamera >= 0) {
                selectedCamera = hoveredCamera;
                return true;
            }
            // 影院模式开关
            if (hoverToggle) {
                cinematicMode = !cinematicMode;
                return true;
            }
            // 开始按钮
            if (hoverStart) {
                startStage();
                return true;
            }
            // 取消按钮
            if (hoverCancel) {
                this.onClose();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int scrollAmount = (int) (-delta * (ITEM_HEIGHT + ITEM_SPACING) * 3);
        
        if (mouseX < rightPanelX) {
            // 左侧面板滚动
            int maxScroll = Math.max(0, motionList.size() * (ITEM_HEIGHT + ITEM_SPACING) - (listBottom - listTop));
            motionScrollOffset = Math.max(0, Math.min(maxScroll, motionScrollOffset + scrollAmount));
        } else {
            // 右侧面板滚动
            int maxScroll = Math.max(0, cameraList.size() * (ITEM_HEIGHT + ITEM_SPACING) - (listBottom - listTop));
            cameraScrollOffset = Math.max(0, Math.min(maxScroll, cameraScrollOffset + scrollAmount));
        }
        return true;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    private void startStage() {
        if (selectedMotion < 0 || selectedCamera < 0) return;
        
        VmdEntry motionEntry = motionList.get(selectedMotion);
        VmdEntry cameraEntry = cameraList.get(selectedCamera);
        
        NativeFunc nf = NativeFunc.GetInst();
        Minecraft mc = Minecraft.getInstance();
        
        // 保存配置
        StageConfig config = StageConfig.getInstance();
        config.lastMotionVmd = motionEntry.path;
        config.lastCameraVmd = cameraEntry.path;
        config.cinematicMode = cinematicMode;
        config.save();
        
        // 加载动作和相机动画
        long motionAnim = nf.LoadAnimation(0, motionEntry.path);
        long cameraAnim = motionEntry.path.equals(cameraEntry.path) ? motionAnim : nf.LoadAnimation(0, cameraEntry.path);
        
        if (motionAnim == 0) {
            logger.error("[舞台模式] 动作 VMD 加载失败: {}", motionEntry.path);
            return;
        }
        
        // 给当前玩家模型设置动作动画
        if (mc.player != null) {
            String playerName = mc.player.getName().getString();
            String modelName = ModelSelectorConfig.getInstance().getSelectedModel();
            if (modelName != null && !modelName.isEmpty()) {
                MMDModelManager.Model modelData = MMDModelManager.GetModel(modelName, playerName);
                if (modelData != null) {
                    nf.TransitionLayerTo(modelData.model.GetModelLong(), 0, motionAnim, 0.3f);
                }
            }
        }
        
        // 启动相机控制器
        MMDCameraController.getInstance().startStage(motionAnim, cameraAnim, cinematicMode);
        
        // 关闭界面
        this.onClose();
        
        logger.info("[舞台模式] 开始: 动作={}, 相机={}, 影院={}", motionEntry.name, cameraEntry.name, cinematicMode);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    /**
     * VMD 文件条目
     */
    private static class VmdEntry {
        final String name;
        final String path;
        final String source;
        final boolean hasCamera;
        
        VmdEntry(String name, String path, String source, boolean hasCamera) {
            this.name = name;
            this.path = path;
            this.source = source;
            this.hasCamera = hasCamera;
        }
    }
}
