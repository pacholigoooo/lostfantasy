package dev.lostfantasy.client;
import dev.lostfantasy.core.CastMotion;
import dev.lostfantasy.core.Spell;
import dev.lostfantasy.core.Facing;
import dev.lostfantasy.network.EffectMessage;
import net.minecraft.client.model.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHandSide;

public final class CastingPlayerModel extends ModelPlayer {
    public static void validateCatalog() {
        java.util.Set<Spell> implemented=java.util.EnumSet.of(Spell.ROYAL_FLARE,Spell.ABANDONED_TRAIN,Spell.GUNGNIR,Spell.FOURFOLD_BARRIER,Spell.EMERALD_CITY);
        for(Spell spell:Spell.catalog())if(spell.hasCastingPose()!=implemented.contains(spell))
            throw new IllegalStateException("Missing/unused casting pose: "+spell.id);
    }
    EffectMessage effect;double age;
    public CastingPlayerModel(boolean slim) {super(0,slim);}
    @Override public void setRotationAngles(float swing,float amount,float ticks,float yaw,float pitch,float scale,Entity entity) {
        super.setRotationAngles(swing,amount,ticks,yaw,pitch,scale,entity);
        if(effect==null || effect.caster!=entity.getEntityId())return;
        applyPose(this,entity,effect,age);
        copyModelAngles(bipedRightArm,bipedRightArmwear);copyModelAngles(bipedLeftArm,bipedLeftArmwear);
        copyModelAngles(bipedRightLeg,bipedRightLegwear);copyModelAngles(bipedLeftLeg,bipedLeftLegwear);
        copyModelAngles(bipedBody,bipedBodyWear);copyModelAngles(bipedHead,bipedHeadwear);
    }
    static void applyPose(ModelBiped model,Entity entity,EffectMessage effect,double age) {
        if(effect.spell()==Spell.ABANDONED_TRAIN) {
            applyTrainPose(model,(EntityLivingBase)entity,effect,age);
            return;
        }
        model.bipedRightArm.rotateAngleY=model.bipedLeftArm.rotateAngleY=0;
        model.bipedRightArm.rotateAngleZ=model.bipedLeftArm.rotateAngleZ=0;
        model.bipedBody.rotateAngleY=0;
        model.bipedRightArm.rotationPointX=-5;model.bipedLeftArm.rotationPointX=5;
        model.bipedRightArm.rotationPointZ=model.bipedLeftArm.rotationPointZ=0;
        if(effect.spell()==Spell.ROYAL_FLARE) {
            float t=(float)CastMotion.charge(age,effect.charge);
            model.bipedRightArm.rotateAngleX=model.bipedLeftArm.rotateAngleX=0;
            model.bipedRightArm.rotateAngleZ=(float)CastMotion.flareSpread(age,effect.charge);
            model.bipedLeftArm.rotateAngleZ=-model.bipedRightArm.rotateAngleZ;
            model.bipedRightLeg.rotateAngleX=model.bipedLeftLeg.rotateAngleX=0;
            model.bipedRightLeg.rotateAngleZ=.23f*t;model.bipedLeftLeg.rotateAngleZ=-.23f*t;
        }else if(effect.spell()==Spell.FOURFOLD_BARRIER) {
            float hold = (float) dev.lostfantasy.core.FourfoldBarrier.pose(age);
            float pulse = age >= dev.lostfantasy.core.FourfoldBarrier.FINISH_HIT && age < dev.lostfantasy.core.FourfoldBarrier.FADE_END ? .12f : 0;
            model.bipedRightArm.rotateAngleX = model.bipedLeftArm.rotateAngleX = (-1.35f-pulse)*hold;
            model.bipedRightArm.rotateAngleY = -.3f*hold;
            model.bipedLeftArm.rotateAngleY = .3f*hold;
            model.bipedRightArm.rotateAngleZ = .16f*hold;
            model.bipedLeftArm.rotateAngleZ = -.16f*hold;
            model.bipedRightLeg.rotateAngleX = model.bipedLeftLeg.rotateAngleX = 0;
        }else if(effect.spell()==Spell.EMERALD_CITY) {
            float lift=(float)(CastMotion.smooth(age/7)*(1-CastMotion.smooth((age-9)/3)));
            model.bipedRightArm.rotateAngleX=-1.85f*lift;
            model.bipedLeftArm.rotateAngleX=-1.2f*lift;
            model.bipedRightArm.rotateAngleZ=.3f*lift;
            model.bipedLeftArm.rotateAngleZ=-.3f*lift;
            model.bipedRightLeg.rotateAngleX=model.bipedLeftLeg.rotateAngleX=0;
        }else if(effect.spell()==Spell.GUNGNIR) {
            boolean left=((EntityLivingBase)entity).getPrimaryHand()==EnumHandSide.LEFT;
            ModelRenderer arm=left?model.bipedLeftArm:model.bipedRightArm;
            arm.rotateAngleX=(float)CastMotion.spearArm(age);
            arm.rotateAngleY=0;arm.rotateAngleZ=(left?-1:1)*.12f*(float)CastMotion.smooth(age/8);
        }
    }

    private static void applyTrainPose(ModelBiped model,EntityLivingBase caster,EffectMessage effect,double age) {
        float point=(float)CastMotion.trainPoint(age);
        float turn=(float)(CastMotion.charge(age,18)*CastMotion.trainRecovery(age));
        float trackYaw=Facing.yaw(effect.dx,effect.dz,caster.renderYawOffset);
        float armYaw=(float)Math.toRadians(Facing.wrap(trackYaw-caster.renderYawOffset));
        model.bipedLeftArm.rotateAngleX+=(-(float)Math.PI*.5f-model.bipedLeftArm.rotateAngleX)*point;
        model.bipedLeftArm.rotateAngleY+=(armYaw-model.bipedLeftArm.rotateAngleY)*point;
        model.bipedLeftArm.rotateAngleZ*=1-point;
        model.bipedRightArm.rotateAngleX+=(.2f-model.bipedRightArm.rotateAngleX)*turn;
        model.bipedRightArm.rotateAngleY*=1-turn;
        model.bipedRightArm.rotateAngleZ+=(.10f-model.bipedRightArm.rotateAngleZ)*turn;
        model.bipedBody.rotateAngleY*=1-turn;
        model.bipedHead.rotateAngleX*=1-turn;
        model.bipedRightArm.rotationPointX+=(-5-model.bipedRightArm.rotationPointX)*turn;
        model.bipedLeftArm.rotationPointX+=(5-model.bipedLeftArm.rotationPointX)*turn;
        model.bipedRightArm.rotationPointZ*=1-turn;
        model.bipedLeftArm.rotationPointZ*=1-turn;
    }
}
