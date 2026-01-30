package com.shiroha.skinlayers3d.renderer.render;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

public class SkinLayersRenderFactory<T extends Entity> implements EntityRendererProvider<T> {
    String entityName;

    public SkinLayersRenderFactory(String entityName) {
        this.entityName = entityName;
    }

    @Override
    public EntityRenderer<T> create(Context manager) {
        return new SkinLayers3DRenderer<>(manager, entityName);
    }
}
