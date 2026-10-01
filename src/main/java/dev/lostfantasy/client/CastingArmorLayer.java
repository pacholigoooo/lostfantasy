package dev.lostfantasy.client;

import dev.lostfantasy.network.EffectMessage;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.entity.Entity;

/** Vanilla armour must use the same bone rotations as the player's skin. */
final class CastingArmorLayer extends LayerBipedArmor {
    CastingArmorLayer(RenderLivingBase<?> renderer) {
        super(renderer);modelLeggings=new Armor(.5f);modelArmor=new Armor(1);
    }
    private static final class Armor extends ModelBiped {
        Armor(float size) {super(size);}
        @Override public void setRotationAngles(float swing,float amount,float ticks,float yaw,float pitch,float scale,Entity entity) {
            super.setRotationAngles(swing,amount,ticks,yaw,pitch,scale,entity);
            EffectMessage effect=SpellVisuals.poseEffect(entity);
            if(effect!=null)CastingPlayerModel.applyPose(this,entity,effect,SpellVisuals.poseAge(entity,effect));
        }
    }
}
