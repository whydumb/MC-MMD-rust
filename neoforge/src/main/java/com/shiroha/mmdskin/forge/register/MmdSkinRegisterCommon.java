package com.shiroha.mmdskin.forge.register;

import com.shiroha.mmdskin.forge.network.MmdSkinNetworkPack;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import net.minecraft.resources.ResourceLocation;

public class MmdSkinRegisterCommon {
    public static SimpleChannel channel;
    static String networkVersion = "1";

    public static void Register() {
        channel = NetworkRegistry.newSimpleChannel(new ResourceLocation("3d-skin", "network_pack"), () -> networkVersion, NetworkRegistry.acceptMissingOr(networkVersion), (version) -> version.equals(networkVersion));
        channel.registerMessage(0, MmdSkinNetworkPack.class, MmdSkinNetworkPack::Pack, MmdSkinNetworkPack::new, MmdSkinNetworkPack::Do);
    }
}
