package com.shiroha.skinlayers3d.renderer.animation;

import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 表情预设信息类
 * 用于扫描和存储 VPD 表情文件信息
 * 
 * 扫描目录：
 * - 3d-skin/DefaultMorph/  - 系统预设表情
 * - 3d-skin/CustomMorph/   - 用户自定义表情
 * - 3d-skin/EntityPlayer/模型名/ - 模型专属表情
 */
public class MorphInfo {
    private static final Logger logger = LogManager.getLogger();
    
    private final String morphName;       // 表情名称（不含扩展名）
    private final String displayName;     // 显示名称
    private final String filePath;        // 文件完整路径
    private final String fileName;        // 文件名（含扩展名）
    private final MorphSource source;     // 表情来源
    private final String modelName;       // 模型名称（仅模型专属表情有效）
    private final long fileSize;          // 文件大小

    public enum MorphSource {
        DEFAULT("默认表情"),
        CUSTOM("自定义表情"),
        MODEL("模型专属");
        
        private final String displayName;
        
        MorphSource(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }

    public MorphInfo(String morphName, String displayName, String filePath, 
                     String fileName, MorphSource source, String modelName, long fileSize) {
        this.morphName = morphName;
        this.displayName = displayName;
        this.filePath = filePath;
        this.fileName = fileName;
        this.source = source;
        this.modelName = modelName;
        this.fileSize = fileSize;
    }

    // Getters
    public String getMorphName() { return morphName; }
    public String getDisplayName() { return displayName; }
    public String getFilePath() { return filePath; }
    public String getFileName() { return fileName; }
    public MorphSource getSource() { return source; }
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
        if (source == MorphSource.MODEL && modelName != null) {
            return source.getDisplayName() + " (" + modelName + ")";
        }
        return source.getDisplayName();
    }

    /**
     * 扫描所有表情目录
     */
    public static List<MorphInfo> scanAllMorphs() {
        List<MorphInfo> morphs = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        String gameDir = mc.gameDirectory.getAbsolutePath();
        
        // 1. 扫描默认表情目录
        File defaultDir = new File(gameDir, "3d-skin/DefaultMorph");
        ensureDirectoryExists(defaultDir);
        morphs.addAll(scanDirectory(defaultDir, MorphSource.DEFAULT, null));
        
        // 2. 扫描自定义表情目录
        File customDir = new File(gameDir, "3d-skin/CustomMorph");
        ensureDirectoryExists(customDir);
        morphs.addAll(scanDirectory(customDir, MorphSource.CUSTOM, null));
        
        // 3. 扫描所有模型目录的表情
        File entityPlayerDir = new File(gameDir, "3d-skin/EntityPlayer");
        if (entityPlayerDir.exists() && entityPlayerDir.isDirectory()) {
            File[] modelDirs = entityPlayerDir.listFiles(File::isDirectory);
            if (modelDirs != null) {
                for (File modelDir : modelDirs) {
                    morphs.addAll(scanDirectory(modelDir, MorphSource.MODEL, modelDir.getName()));
                }
            }
        }
        
        // 按来源和名称排序
        morphs.sort(Comparator
            .comparing(MorphInfo::getSource)
            .thenComparing(MorphInfo::getMorphName, String.CASE_INSENSITIVE_ORDER));
        
        logger.info("共扫描到 {} 个表情文件", morphs.size());
        return morphs;
    }
    
    /**
     * 只扫描自定义表情目录（用于轮盘）
     */
    public static List<MorphInfo> scanCustomMorphs() {
        List<MorphInfo> morphs = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        String gameDir = mc.gameDirectory.getAbsolutePath();
        
        // 扫描自定义表情目录
        File customDir = new File(gameDir, "3d-skin/CustomMorph");
        ensureDirectoryExists(customDir);
        morphs.addAll(scanDirectory(customDir, MorphSource.CUSTOM, null));
        
        // 按名称排序
        morphs.sort(Comparator.comparing(MorphInfo::getMorphName, String.CASE_INSENSITIVE_ORDER));
        
        logger.info("共扫描到 {} 个自定义表情", morphs.size());
        return morphs;
    }
    
    /**
     * 扫描指定模型的所有可用表情
     */
    public static List<MorphInfo> scanMorphsForModel(String modelName) {
        List<MorphInfo> morphs = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        String gameDir = mc.gameDirectory.getAbsolutePath();
        
        // 1. 模型专属表情
        if (modelName != null && !modelName.isEmpty()) {
            File modelDir = new File(gameDir, "3d-skin/EntityPlayer/" + modelName);
            morphs.addAll(scanDirectory(modelDir, MorphSource.MODEL, modelName));
        }
        
        // 2. 自定义表情
        File customDir = new File(gameDir, "3d-skin/CustomMorph");
        morphs.addAll(scanDirectory(customDir, MorphSource.CUSTOM, null));
        
        // 3. 默认表情
        File defaultDir = new File(gameDir, "3d-skin/DefaultMorph");
        morphs.addAll(scanDirectory(defaultDir, MorphSource.DEFAULT, null));
        
        // 按来源和名称排序
        morphs.sort(Comparator
            .comparing(MorphInfo::getSource)
            .thenComparing(MorphInfo::getMorphName, String.CASE_INSENSITIVE_ORDER));
        
        return morphs;
    }

    /**
     * 扫描单个目录
     */
    private static List<MorphInfo> scanDirectory(File dir, MorphSource source, String modelName) {
        List<MorphInfo> morphs = new ArrayList<>();
        
        if (!dir.exists() || !dir.isDirectory()) {
            return morphs;
        }
        
        FileFilter vpdFilter = file -> 
            file.isFile() && file.getName().toLowerCase().endsWith(".vpd");
        
        File[] vpdFiles = dir.listFiles(vpdFilter);
        if (vpdFiles == null) {
            return morphs;
        }
        
        for (File vpdFile : vpdFiles) {
            String fileName = vpdFile.getName();
            String morphName = fileName.substring(0, fileName.length() - 4); // 移除 .vpd
            String displayName = formatDisplayName(morphName);
            
            morphs.add(new MorphInfo(
                morphName,
                displayName,
                vpdFile.getAbsolutePath(),
                fileName,
                source,
                modelName,
                vpdFile.length()
            ));
            
            logger.debug("发现表情: {} [{}]", morphName, source.getDisplayName());
        }
        
        return morphs;
    }
    
    /**
     * 确保目录存在
     */
    private static void ensureDirectoryExists(File dir) {
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                logger.info("创建表情目录: {}", dir.getAbsolutePath());
            }
        }
    }
    
    /**
     * 格式化显示名称
     */
    private static String formatDisplayName(String morphName) {
        // 移除常见前缀
        String name = morphName
            .replace("morph_", "")
            .replace("face_", "")
            .replace("expression_", "");
        
        // 首字母大写
        if (!name.isEmpty()) {
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        
        return name;
    }
    
    /**
     * 根据表情名查找表情信息
     */
    public static MorphInfo findByMorphName(String morphName) {
        List<MorphInfo> all = scanAllMorphs();
        for (MorphInfo info : all) {
            if (info.getMorphName().equals(morphName)) {
                return info;
            }
        }
        return null;
    }
    
    /**
     * 获取默认表情目录
     */
    public static String getDefaultMorphDir() {
        Minecraft mc = Minecraft.getInstance();
        return new File(mc.gameDirectory, "3d-skin/DefaultMorph").getAbsolutePath();
    }
    
    /**
     * 获取自定义表情目录
     */
    public static String getCustomMorphDir() {
        Minecraft mc = Minecraft.getInstance();
        return new File(mc.gameDirectory, "3d-skin/CustomMorph").getAbsolutePath();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MorphInfo that = (MorphInfo) o;
        return morphName.equals(that.morphName) && source == that.source;
    }
    
    @Override
    public int hashCode() {
        return morphName.hashCode() * 31 + source.hashCode();
    }
}
