package com.shiroha.skinlayers3d.renderer.animation;

import com.shiroha.skinlayers3d.renderer.model.MMDModelManager.EntityData;
import com.shiroha.skinlayers3d.renderer.model.MMDModelManager.ModelWithEntityData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;

/**
 * 动画状态管理器
 * 负责根据玩家状态切换动画
 * 
 * 设计原则：单一职责，只负责动画状态的判断和切换
 */
public class AnimationStateManager {
    
    /**
     * 更新玩家动画状态
     */
    public static void updateAnimationState(AbstractClientPlayer player, ModelWithEntityData model) {
        if (model.entityData.playCustomAnim) {
            if (shouldStopCustomAnimation(player)) {
                model.entityData.playCustomAnim = false;
            }
        }
        
        if (!model.entityData.playCustomAnim) {
            updateLayer0Animation(player, model);
            updateLayer1Animation(player, model);
            updateLayer2Animation(player, model);
        }
    }
    
    private static boolean shouldStopCustomAnimation(AbstractClientPlayer player) {
        return player.getHealth() == 0.0f ||
               player.isFallFlying() ||
               player.isSleeping() ||
               player.isSwimming() ||
               player.onClimbable() ||
               player.isSprinting() ||
               player.isVisuallyCrawling() ||
               player.isPassenger() ||
               hasMovement(player);
    }
    
    private static boolean hasMovement(AbstractClientPlayer player) {
        return player.getX() - player.xo != 0.0f || player.getZ() - player.zo != 0.0f;
    }
    
    private static void updateLayer0Animation(AbstractClientPlayer player, ModelWithEntityData model) {
        if (player.getHealth() == 0.0f) {
            changeAnimationOnce(model, EntityData.EntityState.Die, 0);
        } else if (player.isFallFlying()) {
            changeAnimationOnce(model, EntityData.EntityState.ElytraFly, 0);
        } else if (player.isSleeping()) {
            changeAnimationOnce(model, EntityData.EntityState.Sleep, 0);
        } else if (player.isPassenger()) {
            updateRidingAnimation(player, model);
        } else if (player.isSwimming()) {
            changeAnimationOnce(model, EntityData.EntityState.Swim, 0);
        } else if (player.onClimbable()) {
            updateClimbingAnimation(player, model);
        } else if (player.isSprinting() && !player.isShiftKeyDown()) {
            changeAnimationOnce(model, EntityData.EntityState.Sprint, 0);
        } else if (player.isVisuallyCrawling()) {
            updateCrawlingAnimation(player, model);
        } else if (hasMovement(player)) {
            changeAnimationOnce(model, EntityData.EntityState.Walk, 0);
        } else {
            changeAnimationOnce(model, EntityData.EntityState.Idle, 0);
        }
    }
    
    private static void updateRidingAnimation(AbstractClientPlayer player, ModelWithEntityData model) {
        if (player.getVehicle().getType() == EntityType.HORSE && hasMovement(player)) {
            changeAnimationOnce(model, EntityData.EntityState.OnHorse, 0);
        } else {
            changeAnimationOnce(model, EntityData.EntityState.Ride, 0);
        }
    }
    
    private static void updateClimbingAnimation(AbstractClientPlayer player, ModelWithEntityData model) {
        double verticalMovement = player.getY() - player.yo;
        if (verticalMovement > 0) {
            changeAnimationOnce(model, EntityData.EntityState.OnClimbableUp, 0);
        } else if (verticalMovement < 0) {
            changeAnimationOnce(model, EntityData.EntityState.OnClimbableDown, 0);
        } else {
            changeAnimationOnce(model, EntityData.EntityState.OnClimbable, 0);
        }
    }
    
    private static void updateCrawlingAnimation(AbstractClientPlayer player, ModelWithEntityData model) {
        if (hasMovement(player)) {
            changeAnimationOnce(model, EntityData.EntityState.Crawl, 0);
        } else {
            changeAnimationOnce(model, EntityData.EntityState.LieDown, 0);
        }
    }
    
    private static void updateLayer1Animation(AbstractClientPlayer player, ModelWithEntityData model) {
        if (!player.isUsingItem() && !player.swinging || player.isSleeping()) {
            if (model.entityData.stateLayers[1] != EntityData.EntityState.Idle) {
                model.entityData.stateLayers[1] = EntityData.EntityState.Idle;
                model.model.ChangeAnim(0, 1);
            }
        } else {
            updateHandAnimation(player, model);
        }
    }
    
    private static void updateHandAnimation(AbstractClientPlayer player, ModelWithEntityData model) {
        if (player.getUsedItemHand() == InteractionHand.MAIN_HAND && player.isUsingItem()) {
            String itemId = getItemId(player, InteractionHand.MAIN_HAND);
            applyCustomItemAnimation(model, EntityData.EntityState.ItemRight, itemId, "Right", "using", 1);
        } else if (player.swingingArm == InteractionHand.MAIN_HAND && player.swinging) {
            String itemId = getItemId(player, InteractionHand.MAIN_HAND);
            applyCustomItemAnimation(model, EntityData.EntityState.SwingRight, itemId, "Right", "swinging", 1);
        } else if (player.getUsedItemHand() == InteractionHand.OFF_HAND && player.isUsingItem()) {
            String itemId = getItemId(player, InteractionHand.OFF_HAND);
            applyCustomItemAnimation(model, EntityData.EntityState.ItemLeft, itemId, "Left", "using", 1);
        } else if (player.swingingArm == InteractionHand.OFF_HAND && player.swinging) {
            String itemId = getItemId(player, InteractionHand.OFF_HAND);
            applyCustomItemAnimation(model, EntityData.EntityState.SwingLeft, itemId, "Left", "swinging", 1);
        }
    }
    
    private static void updateLayer2Animation(AbstractClientPlayer player, ModelWithEntityData model) {
        if (player.isShiftKeyDown() && !player.isVisuallyCrawling()) {
            changeAnimationOnce(model, EntityData.EntityState.Sneak, 2);
        } else {
            if (model.entityData.stateLayers[2] != EntityData.EntityState.Idle) {
                model.entityData.stateLayers[2] = EntityData.EntityState.Idle;
                model.model.ChangeAnim(0, 2);
            }
        }
    }
    
    private static void changeAnimationOnce(ModelWithEntityData model, EntityData.EntityState targetState, int layer) {
        String property = EntityData.stateProperty.get(targetState);
        if (model.entityData.stateLayers[layer] != targetState) {
            model.entityData.stateLayers[layer] = targetState;
            model.model.ChangeAnim(MMDAnimManager.GetAnimModel(model.model, property), layer);
        }
    }
    
    private static void applyCustomItemAnimation(ModelWithEntityData model, EntityData.EntityState targetState, 
                                                  String itemName, String activeHand, String handState, int layer) {
        long anim = MMDAnimManager.GetAnimModel(model.model, 
            String.format("itemActive_%s_%s_%s", itemName, activeHand, handState));
        
        if (anim != 0) {
            if (model.entityData.stateLayers[layer] != targetState) {
                model.entityData.stateLayers[layer] = targetState;
                model.model.ChangeAnim(anim, layer);
            }
            return;
        }
        
        if (targetState == EntityData.EntityState.ItemRight || targetState == EntityData.EntityState.SwingRight) {
            changeAnimationOnce(model, EntityData.EntityState.SwingRight, layer);
        } else if (targetState == EntityData.EntityState.ItemLeft || targetState == EntityData.EntityState.SwingLeft) {
            changeAnimationOnce(model, EntityData.EntityState.SwingLeft, layer);
        }
    }
    
    private static String getItemId(AbstractClientPlayer player, InteractionHand hand) {
        String descriptionId = player.getItemInHand(hand).getItem().getDescriptionId();
        return descriptionId.substring(descriptionId.indexOf(".") + 1);
    }
}
