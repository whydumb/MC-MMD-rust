package com.shiroha.skinlayers3d.forge.register;

import com.shiroha.skinlayers3d.forge.network.SkinLayers3DNetworkPack;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import net.minecraft.resources.ResourceLocation;

public class SkinLayers3DRegisterCommon {
    public static SimpleChannel channel;
    static String networkVersion = "1";

    public static void Register() {
        channel = NetworkRegistry.newSimpleChannel(new ResourceLocation("3d-skin", "network_pack"), () -> networkVersion, NetworkRegistry.acceptMissingOr(networkVersion), (version) -> version.equals(networkVersion));
        channel.registerMessage(0, SkinLayers3DNetworkPack.class, SkinLayers3DNetworkPack::Pack, SkinLayers3DNetworkPack::new, SkinLayers3DNetworkPack::Do);
    }
}
