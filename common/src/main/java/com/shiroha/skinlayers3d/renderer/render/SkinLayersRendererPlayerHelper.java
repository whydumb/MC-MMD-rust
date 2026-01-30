package com.shiroha.skinlayers3d.renderer.render;

import com.shiroha.skinlayers3d.renderer.core.IMMDModel;
import com.shiroha.skinlayers3d.renderer.animation.MMDAnimManager;
import com.shiroha.skinlayers3d.renderer.model.MMDModelManager;
import net.minecraft.world.entity.player.Player;

public class SkinLayersRendererPlayerHelper {

    SkinLayersRendererPlayerHelper() {
    }

    public static void ResetPhysics(Player player) {
        // 从配置获取玩家选择的模型
        String playerName = player.getName().getString();
        String selectedModel = com.shiroha.skinlayers3d.ui.ModelSelectorConfig.getInstance().getPlayerModel(playerName);
        
        // 如果是默认渲染，不处理
        if (selectedModel.equals("默认 (原版渲染)") || selectedModel.isEmpty()) {
            return;
        }
        
        MMDModelManager.Model m = MMDModelManager.GetModel(selectedModel, playerName);
        if (m != null) {
            IMMDModel model = m.model;
            ((MMDModelManager.ModelWithEntityData) m).entityData.playCustomAnim = false;
            model.ChangeAnim(MMDAnimManager.GetAnimModel(model, "idle"), 0);
            model.ChangeAnim(0, 1);
            model.ChangeAnim(0, 2);
            model.ResetPhysics();
        }
    }

    public static void CustomAnim(Player player, String id) {
        // 从配置获取玩家选择的模型
        String playerName = player.getName().getString();
        String selectedModel = com.shiroha.skinlayers3d.ui.ModelSelectorConfig.getInstance().getPlayerModel(playerName);
        
        // 如果是默认渲染，不处理
        if (selectedModel.equals("默认 (原版渲染)") || selectedModel.isEmpty()) {
            return;
        }
        
        MMDModelManager.Model m = MMDModelManager.GetModel(selectedModel, playerName);
        if (m != null) {
            MMDModelManager.ModelWithEntityData mwed = (MMDModelManager.ModelWithEntityData) m;
            IMMDModel model = m.model;
            mwed.entityData.playCustomAnim = true;
            // 直接使用文件名作为动画ID，MMDAnimManager 会自动在 CustomAnim 目录查找
            model.ChangeAnim(MMDAnimManager.GetAnimModel(model, id), 0);
            model.ChangeAnim(0, 1);
            model.ChangeAnim(0, 2);
        }
    }
}
