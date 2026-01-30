package com.shiroha.skinlayers3d;

import com.shiroha.skinlayers3d.renderer.model.MMDModelManager;
import com.shiroha.skinlayers3d.renderer.resource.MMDTextureManager;
import com.shiroha.skinlayers3d.renderer.animation.MMDAnimManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector3f;

public class SkinLayers3DClient {
    public static final Logger logger = LogManager.getLogger();
    public static int usingMMDShader = 0;
    public static boolean reloadProperties = false;
    static String gameDirectory = Minecraft.getInstance().gameDirectory.getAbsolutePath();
    static final int BUFFER = 512;
    static final long TOOBIG = 0x6400000; // Max size of unzipped data, 100MB
    static final int TOOMANY = 1024;      // Max number of files
    //public static String[] debugStr = new String[10];

    public static void initClient() {
        check3DSkinFolder();
        MMDModelManager.Init();
        MMDTextureManager.Init();
        MMDAnimManager.Init();
        
        // 确保 EntityPlayer 目录存在
        ensureEntityPlayerDirectory();
    }
    
    /**
     * 确保 EntityPlayer 目录结构存在
     */
    private static void ensureEntityPlayerDirectory() {
        File entityPlayerDir = new File(gameDirectory, "3d-skin/EntityPlayer");
        if (!entityPlayerDir.exists()) {
            entityPlayerDir.mkdirs();
            logger.info("创建 EntityPlayer 模型目录: " + entityPlayerDir.getAbsolutePath());
        }
    }

    private static String validateFilename(String filename, String intendedDir) throws java.io.IOException {
        File f = new File(filename);
        String canonicalPath = f.getCanonicalPath();

        File iD = new File(intendedDir);
        String canonicalID = iD.getCanonicalPath();

        if (canonicalPath.startsWith(canonicalID)) {
            return canonicalPath;
        } else {
            throw new IllegalStateException("File is outside extraction target directory.");
        }
    }

    public static final void unzip(String filename, String targetDir) throws java.io.IOException {
        FileInputStream fis = new FileInputStream(filename);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;
        int entries = 0;
        long total = 0;
        try {
            while ((entry = zis.getNextEntry()) != null) {
                logger.info("Extracting: " + entry);
                int count;
                byte data[] = new byte[BUFFER];
                // Write the files to the disk, but ensure that the filename is valid,
                // and that the file is not insanely big
                String name = validateFilename(targetDir+entry.getName(), ".");
                File targetFile = new File(name);
                if (entry.isDirectory()) {
                    logger.info("Creating directory " + name);
                    new File(name).mkdir();
                    continue;
                }
                if (!targetFile.getParentFile().exists()){
                    targetFile.getParentFile().mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(name);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
                while (total + BUFFER <= TOOBIG && (count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                    total += count;
                }
                dest.flush();
                dest.close();
                zis.closeEntry();
                entries++;
                if (entries > TOOMANY) {
                    throw new IllegalStateException("Too many files to unzip.");
                }
                if (total + BUFFER > TOOBIG) {
                    throw new IllegalStateException("File being unzipped is too big.");
                }
            }
        } finally {
            zis.close();
        }
    }

    private static void check3DSkinFolder(){
        File skin3DFolder = new File(gameDirectory + "/3d-skin");
        if (!skin3DFolder.exists()){
            logger.info("3d-skin folder not found, try download from github!");
            skin3DFolder.mkdir();
            try{
                FileUtils.copyURLToFile(new URL("https://github.com/Gengorou-C/3d-skin-C/releases/download/requiredFiles/3d-skin.zip"), new File(gameDirectory + "/3d-skin.zip"), 30000, 30000);
            }catch (IOException e){
                logger.info("Download 3d-skin.zip failed!");
            }

            try{
                unzip(gameDirectory + "/3d-skin.zip", gameDirectory + "/3d-skin/");
            }catch (IOException e){
                logger.info("extract 3d-skin.zip failed!");
            }
        }
        return;
    }

    public static String calledFrom(int i){
        StackTraceElement[] steArray = Thread.currentThread().getStackTrace();
        if (steArray.length <= i) {
            return "";
        }
        return steArray[i].getClassName();
    }

    public static Vector3f str2Vec3f(String arg){
        Vector3f vector3f = new Vector3f();
        String[] splittedStr = arg.split(",");
        if (splittedStr.length != 3){
            return new Vector3f(0.0f);
        }
        vector3f.x = Float.valueOf(splittedStr[0]);
        vector3f.y = Float.valueOf(splittedStr[1]);
        vector3f.z = Float.valueOf(splittedStr[2]);
        return vector3f;
    }
    
    public static void drawText(String arg, int x, int y){
        //MinecraftClient MCinstance = MinecraftClient.getInstance();
        PoseStack mat;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        mat = RenderSystem.getModelViewStack();
        mat.pushPose();
        //instance.textRenderer.draw(mat, arg, x, y, -1);
        mat.popPose();
    }
}
