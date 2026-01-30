package com.shiroha.skinlayers3d.fabric.register;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class SkinLayers3DRegisterCommon {
    public static ResourceLocation SKIN_C2S = new ResourceLocation("3d-skin", "network_c2s");
    public static ResourceLocation SKIN_S2C = new ResourceLocation("3d-skin", "network_s2c");

    public static void Register() {
        ServerPlayNetworking.registerGlobalReceiver(SKIN_C2S, (server, player, handler, buf, responseSender) -> {
            FriendlyByteBuf packetbuf = PacketByteBufs.create();
            packetbuf.writeInt(buf.readInt());
            packetbuf.writeUUID(buf.readUUID());
            packetbuf.writeInt(buf.readInt());
            server.execute(() -> {
                for(ServerPlayer serverPlayer : PlayerLookup.all(server)){
                    if(!serverPlayer.equals(player)){
                        ServerPlayNetworking.send(serverPlayer, SkinLayers3DRegisterCommon.SKIN_S2C, packetbuf);
                    }
                }
            });
        });
    }
}
