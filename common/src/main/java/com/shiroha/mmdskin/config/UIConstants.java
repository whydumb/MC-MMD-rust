package com.shiroha.mmdskin.config;

/**
 * UI 常量类
 * 集中管理 UI 相关的常量，避免硬编码
 */
public final class UIConstants {
    
    private UIConstants() {
        // 工具类，禁止实例化
    }
    
    // ==================== 默认值 ====================
    /** 默认模型名称（使用原版渲染） */
    public static final String DEFAULT_MODEL_NAME = "默认 (原版渲染)";
    
    // ==================== 轮盘参数 ====================
    /** 轮盘占屏幕比例（大轮盘） */
    public static final float WHEEL_SCREEN_RATIO_LARGE = 0.80f;
    
    /** 轮盘占屏幕比例（中等轮盘） */
    public static final float WHEEL_SCREEN_RATIO_MEDIUM = 0.50f;
    
    /** 轮盘占屏幕比例（小轮盘） */
    public static final float WHEEL_SCREEN_RATIO_SMALL = 0.45f;
    
    /** 内圈占外圈比例 */
    public static final float WHEEL_INNER_RATIO = 0.25f;
    
    /** 配置轮盘内圈比例 */
    public static final float CONFIG_WHEEL_INNER_RATIO = 0.30f;
    
    /** 女仆轮盘内圈比例 */
    public static final float MAID_WHEEL_INNER_RATIO = 0.35f;
    
    /** 悬停检测容差 */
    public static final int HOVER_TOLERANCE = 50;
    
    /** 滚动速度系数 */
    public static final int SCROLL_SPEED = 25;
    
    /** 模型选择滚动速度 */
    public static final int MODEL_SCROLL_SPEED = 30;
    
    // ==================== 颜色常量（玩家配置主题 - 青蓝色） ====================
    /** 分隔线颜色 */
    public static final int COLOR_LINE = 0xFF60A0D0;
    
    /** 分隔线颜色（暗） */
    public static final int COLOR_LINE_DIM = 0xCC60A0D0;
    
    /** 高亮颜色 */
    public static final int COLOR_HIGHLIGHT = 0x60FFFFFF;
    
    /** 中心圆背景 */
    public static final int COLOR_CENTER_BG = 0xE0182030;
    
    /** 中心圆边框 */
    public static final int COLOR_CENTER_BORDER = 0xFF60A0D0;
    
    /** 文字阴影 */
    public static final int COLOR_TEXT_SHADOW = 0xFF000000;
    
    /** 强调色 */
    public static final int COLOR_ACCENT = 0xFF60A0D0;
    
    // ==================== 颜色常量（女仆配置主题 - 粉紫色） ====================
    /** 女仆主题 - 分隔线颜色 */
    public static final int COLOR_MAID_LINE = 0xFFD060A0;
    
    /** 女仆主题 - 分隔线颜色（暗） */
    public static final int COLOR_MAID_LINE_DIM = 0xCCD060A0;
    
    /** 女仆主题 - 中心圆背景 */
    public static final int COLOR_MAID_CENTER_BG = 0xE0301828;
    
    /** 女仆主题 - 中心圆边框 */
    public static final int COLOR_MAID_CENTER_BORDER = 0xFFD060A0;
    
    // ==================== 通用颜色常量 ====================
    /** 主文本颜色 */
    public static final int COLOR_TEXT_PRIMARY = 0xFFFFFF;
    
    /** 次级文本颜色 */
    public static final int COLOR_TEXT_SECONDARY = 0xAAAAAA;
    
    /** 暗淡文本颜色 */
    public static final int COLOR_TEXT_DIM = 0x666666;
    
    /** 面板背景颜色 */
    public static final int COLOR_PANEL_BG = 0x80000000;
    
    /** 条目背景颜色 */
    public static final int COLOR_ITEM_BG = 0x60333333;
    
    /** 条目悬停颜色 */
    public static final int COLOR_ITEM_HOVER = 0x80555555;
    
    // ==================== 来源标记颜色 ====================
    /** 默认来源颜色（蓝色） */
    public static final int COLOR_SOURCE_DEFAULT = 0x88AAFF;
    
    /** 自定义来源颜色（绿色） */
    public static final int COLOR_SOURCE_CUSTOM = 0x88FF88;
    
    /** 模型专属来源颜色（橙色） */
    public static final int COLOR_SOURCE_MODEL = 0xFFAA88;
    
    // ==================== 模型选择颜色 ====================
    /** 卡片背景 */
    public static final int COLOR_CARD_BG = 0x80000000;
    
    /** 卡片选中背景 */
    public static final int COLOR_CARD_SELECTED = 0x80006600;
    
    /** 卡片悬停背景 */
    public static final int COLOR_CARD_HOVER = 0x80333333;
    
    /** 卡片边框 */
    public static final int COLOR_CARD_BORDER = 0xFF555555;
    
    /** 卡片选中边框 */
    public static final int COLOR_CARD_BORDER_SELECTED = 0xFF00AA00;
    
    /** 强调文本颜色（绿色） */
    public static final int COLOR_TEXT_ACCENT = 0x55FF55;
    
    /** PMX 格式颜色 */
    public static final int COLOR_FORMAT_PMX = 0xFF6699FF;
    
    /** PMD 格式颜色 */
    public static final int COLOR_FORMAT_PMD = 0xFFFF9966;
    
    // ==================== 材质控制颜色 ====================
    /** 深蓝黑背景 */
    public static final int COLOR_MATERIAL_BG = 0xE0101820;
    
    /** 材质条目背景 */
    public static final int COLOR_MATERIAL_ITEM_BG = 0x80182028;
    
    /** 材质条目悬停 */
    public static final int COLOR_MATERIAL_ITEM_HOVER = 0x80304050;
    
    /** 边框颜色 */
    public static final int COLOR_MATERIAL_BORDER = 0xFF3A4A5A;
    
    /** 开关开启颜色 */
    public static final int COLOR_TOGGLE_ON = 0xFF40C080;
    
    /** 开关关闭颜色 */
    public static final int COLOR_TOGGLE_OFF = 0xFF505560;
    
    // ==================== 布局常量 ====================
    /** 配置面板宽度 */
    public static final int PANEL_WIDTH = 200;
    
    /** 条目高度 */
    public static final int ITEM_HEIGHT = 36;
    
    /** 条目间距 */
    public static final int ITEM_SPACING = 4;
    
    /** 头部高度 */
    public static final int HEADER_HEIGHT = 50;
    
    /** 底部高度 */
    public static final int FOOTER_HEIGHT = 45;
    
    /** 面板内边距 */
    public static final int PANEL_PADDING = 10;
    
    /** 模型卡片宽度 */
    public static final int MODEL_CARD_WIDTH = 280;
    
    /** 模型卡片高度 */
    public static final int MODEL_CARD_HEIGHT = 50;
    
    /** 卡片间距 */
    public static final int CARD_SPACING = 8;
    
    /** 材质面板宽度 */
    public static final int MATERIAL_PANEL_WIDTH = 320;
    
    /** 开关宽度 */
    public static final int TOGGLE_WIDTH = 50;
    
    /** 开关高度 */
    public static final int TOGGLE_HEIGHT = 22;
}
