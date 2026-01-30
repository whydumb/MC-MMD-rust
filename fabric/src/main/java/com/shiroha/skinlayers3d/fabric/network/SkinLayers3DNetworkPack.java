package com.shiroha.skinlayers3d.fabric.network;

import com.shiroha.skinlayers3d.fabric.register.SkinLayers3DRegisterCommon;
import com.shiroha.skinlayers3d.renderer.render.SkinLayersRendererPlayerHelper;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

/**
 * Fabric 网络包处理
 * 支持动作同步和物理重置
 */
public class SkinLayers3DNetworkPack {
    /**
     * 发送到服务器（整数参数版本，用于向后兼容）
     */
    public static void sendToServer(int opCode, UUID playerUUID, int arg0){
        FriendlyByteBuf buffer = PacketByteBufs.create();
        buffer.writeInt(opCode);
        buffer.writeUUID(playerUUID);
        buffer.writeInt(arg0);
        ClientPlayNetworking.send(SkinLayers3DRegisterCommon.SKIN_C2S, buffer);
    }
    
    /**
     * 发送到服务器（字符串参数版本，用于动画ID）
     */
    public static void sendToServer(int opCode, UUID playerUUID, String animId){
        FriendlyByteBuf buffer = PacketByteBufs.create();
        buffer.writeInt(opCode);
        buffer.writeUUID(playerUUID);
        buffer.writeUtf(animId); // 使用 UTF-8 编码支持中文
        ClientPlayNetworking.send(SkinLayers3DRegisterCommon.SKIN_C2S, buffer);
    }
    
    public static void DoInClient(FriendlyByteBuf buffer){
        int opCode = buffer.readInt();
        UUID playerUUID = buffer.readUUID();
        
        // 根据 opCode 决定读取整数还是字符串
        if (opCode == 1) {
            // 动作执行，读取字符串
            String animId = buffer.readUtf();
            DoInClient(opCode, playerUUID, animId);
        } else {
            // 其他操作，读取整数
            int arg0 = buffer.readInt();
            DoInClient(opCode, playerUUID, arg0);
        }
    }

    /**
     * 客户端处理（整数参数）
     */
    public static void DoInClient(int opCode, UUID playerUUID, int arg0) {
        Minecraft MCinstance = Minecraft.getInstance();
        // 忽略自己发送的消息
        assert MCinstance.player != null;
        if (playerUUID.equals(MCinstance.player.getUUID()))
            return;
            
        switch (opCode) {
            case 2: {
                // 重置物理
                assert MCinstance.level != null;
                Player target = MCinstance.level.getPlayerByUUID(playerUUID);
                if (target != null)
                    SkinLayersRendererPlayerHelper.ResetPhysics(target);
                break;
            }
        }
    }
    
    /**
     * 客户端处理（字符串参数）
     */
    public static void DoInClient(int opCode, UUID playerUUID, String animId) {
        Minecraft MCinstance = Minecraft.getInstance();
        // 忽略自己发送的消息
        assert MCinstance.player != null;
        if (playerUUID.equals(MCinstance.player.getUUID()))
            return;
            
        switch (opCode) {
            case 1: {
                // 执行动画
                assert MCinstance.level != null;
                Player target = MCinstance.level.getPlayerByUUID(playerUUID);
                if (target != null)
                    SkinLayersRendererPlayerHelper.CustomAnim(target, animId);
                break;
            }
        }
    }
}
