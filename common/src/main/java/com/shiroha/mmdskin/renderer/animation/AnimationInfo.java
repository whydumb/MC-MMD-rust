package com.shiroha.mmdskin.renderer.animation;

import com.shiroha.mmdskin.config.PathConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 动画信息类
 * 用于扫描和存储 VMD 动画文件信息
 * 
 * 扫描目录：
 * - 3d-skin/DefaultAnim/  - 系统预设动画
 * - 3d-skin/CustomAnim/   - 用户自定义动画
 * - 3d-skin/EntityPlayer/模型名/ - 模型专属动画
 */
public class AnimationInfo {
    private static final Logger logger = LogManager.getLogger();
    
    private final String animName;       // 动画名称（不含扩展名）
    private final String displayName;    // 显示名称
    private final String filePath;       // 文件完整路径
    private final String fileName;       // 文件名（含扩展名）
    private final AnimSource source;     // 动画来源
    private final String modelName;      // 模型名称（仅模型专属动画有效）
    private final long fileSize;         // 文件大小

    public enum AnimSource {
        DEFAULT("默认动画"),
        CUSTOM("自定义动画"),
        MODEL("模型专属");
        
        private final String displayName;
        
        AnimSource(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public AnimationInfo(String animName, String displayName, String filePath, 
                         String fileName, AnimSource source, String modelName, long fileSize) {
        this.animName = animName;
        this.displayName = displayName;
        this.filePath = filePath;
        this.fileName = fileName;
        this.source = source;
        this.modelName = modelName;
        this.fileSize = fileSize;
    }

    // Getters
    public String getAnimName() { return animName; }
    public String getDisplayName() { return displayName; }
    public String getFilePath() { return filePath; }
    public String getFileName() { return fileName; }
    public AnimSource getSource() { return source; }
    public String getModelName() { return modelName; }
    public long getFileSize() { return fileSize; }
    
    /**
     * 获取格式化的文件大小
     */
    public String getFormattedSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
    
    /**
     * 获取来源描述
     */
    public String getSourceDescription() {
        if (source == AnimSource.MODEL && modelName != null) {
            return source.getDisplayName() + " (" + modelName + ")";
        }
        return source.getDisplayName();
    }

    /**
     * 扫描所有动画目录
     */
    public static List<AnimationInfo> scanAllAnimations() {
        List<AnimationInfo> animations = new ArrayList<>();
        
        // 1. 扫描默认动画目录
        File defaultDir = PathConstants.getDefaultAnimDir();
        animations.addAll(scanDirectory(defaultDir, AnimSource.DEFAULT, null));
        
        // 2. 扫描自定义动画目录
        File customDir = PathConstants.getCustomAnimDir();
        animations.addAll(scanDirectory(customDir, AnimSource.CUSTOM, null));
        
        // 3. 扫描所有模型目录的动画
        File entityPlayerDir = PathConstants.getEntityPlayerDir();
        if (entityPlayerDir.exists() && entityPlayerDir.isDirectory()) {
            File[] modelDirs = entityPlayerDir.listFiles(File::isDirectory);
            if (modelDirs != null) {
                for (File modelDir : modelDirs) {
                    animations.addAll(scanDirectory(modelDir, AnimSource.MODEL, modelDir.getName()));
                }
            }
        }
        
        // 按来源和名称排序
        animations.sort(Comparator
            .comparing(AnimationInfo::getSource)
            .thenComparing(AnimationInfo::getAnimName, String.CASE_INSENSITIVE_ORDER));
        
        logger.info("共扫描到 {} 个动画文件", animations.size());
        return animations;
    }
    
    /**
     * 只扫描自定义动画目录（用于轮盘）
     */
    public static List<AnimationInfo> scanCustomAnimations() {
        List<AnimationInfo> animations = new ArrayList<>();
        
        // 扫描自定义动画目录
        File customDir = PathConstants.getCustomAnimDir();
        animations.addAll(scanDirectory(customDir, AnimSource.CUSTOM, null));
        
        // 按名称排序
        animations.sort(Comparator.comparing(AnimationInfo::getAnimName, String.CASE_INSENSITIVE_ORDER));
        
        logger.info("共扫描到 {} 个自定义动画", animations.size());
        return animations;
    }
    
    /**
     * 扫描指定模型的所有可用动画
     */
    public static List<AnimationInfo> scanAnimationsForModel(String modelName) {
        List<AnimationInfo> animations = new ArrayList<>();
        
        // 1. 模型专属动画
        if (modelName != null && !modelName.isEmpty()) {
            File modelDir = PathConstants.getModelDir(modelName);
            animations.addAll(scanDirectory(modelDir, AnimSource.MODEL, modelName));
        }
        
        // 2. 自定义动画
        File customDir = PathConstants.getCustomAnimDir();
        animations.addAll(scanDirectory(customDir, AnimSource.CUSTOM, null));
        
        // 3. 默认动画
        File defaultDir = PathConstants.getDefaultAnimDir();
        animations.addAll(scanDirectory(defaultDir, AnimSource.DEFAULT, null));
        
        // 按来源和名称排序
        animations.sort(Comparator
            .comparing(AnimationInfo::getSource)
            .thenComparing(AnimationInfo::getAnimName, String.CASE_INSENSITIVE_ORDER));
        
        return animations;
    }

    /**
     * 扫描单个目录
     */
    private static List<AnimationInfo> scanDirectory(File dir, AnimSource source, String modelName) {
        List<AnimationInfo> animations = new ArrayList<>();
        
        if (!dir.exists() || !dir.isDirectory()) {
            return animations;
        }
        
        FileFilter vmdFilter = file -> 
            file.isFile() && file.getName().toLowerCase().endsWith(".vmd");
        
        File[] vmdFiles = dir.listFiles(vmdFilter);
        if (vmdFiles == null) {
            return animations;
        }
        
        for (File vmdFile : vmdFiles) {
            String fileName = vmdFile.getName();
            String animName = fileName.substring(0, fileName.length() - 4); // 移除 .vmd
            String displayName = formatDisplayName(animName);
            
            animations.add(new AnimationInfo(
                animName,
                displayName,
                vmdFile.getAbsolutePath(),
                fileName,
                source,
                modelName,
                vmdFile.length()
            ));
            
            logger.debug("发现动画: {} [{}]", animName, source.getDisplayName());
        }
        
        return animations;
    }
    
    /**
     * 格式化显示名称
     */
    private static String formatDisplayName(String animName) {
        // 移除常见前缀
        String name = animName
            .replace("itemActive_", "")
            .replace("minecraft.", "");
        
        // 首字母大写
        if (!name.isEmpty()) {
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        
        return name;
    }
    
    /**
     * 根据动画名查找动画信息
     */
    public static AnimationInfo findByAnimName(String animName) {
        List<AnimationInfo> all = scanAllAnimations();
        for (AnimationInfo info : all) {
            if (info.getAnimName().equals(animName)) {
                return info;
            }
        }
        return null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnimationInfo that = (AnimationInfo) o;
        return animName.equals(that.animName) && source == that.source;
    }
    
    @Override
    public int hashCode() {
        return animName.hashCode() * 31 + source.hashCode();
    }
}
