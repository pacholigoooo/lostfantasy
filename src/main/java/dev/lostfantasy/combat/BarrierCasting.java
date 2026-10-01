package dev.lostfantasy.combat;

import dev.lostfantasy.LostFantasy;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.UUID;

/** Operation gates do not pin coordinates: environmental movement remains available. */
@Mod.EventBusSubscriber(modid = LostFantasy.ID)
public final class BarrierCasting {
    private static final UUID SPEED_ID = UUID.fromString("c5633624-ec63-476d-a82a-d351779c10bf");
    private static final AttributeModifier SPEED = new AttributeModifier(SPEED_ID, "Fourfold barrier stance", -1, 2).setSaved(false);
    private BarrierCasting() {}

    public static boolean restricted(EntityPlayer player) {
        return player.world.isRemote ? LostFantasy.PROXY.restrictsMovement(player) : SpellManager.restrictsMovement(player);
    }
    public static void movementModifier(EntityPlayer player, boolean enabled) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (enabled && attribute.getModifier(SPEED_ID) == null) attribute.applyModifier(SPEED);
        if (!enabled) attribute.removeModifier(SPEED_ID);
    }
    /** Undo only the stance's speed contribution to vanilla FOV, without changing movement attributes. */
    public static float fieldOfViewCorrection(EntityPlayer player) {
        IAttributeInstance attribute=player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        double walkSpeed=player.capabilities.getWalkSpeed();
        if(attribute.getModifier(SPEED_ID)==null || walkSpeed<=0)return 1;
        double base=attribute.getBaseValue();
        for(AttributeModifier modifier:attribute.getModifiersByOperation(0))base+=modifier.getAmount();
        double speed=base;
        for(AttributeModifier modifier:attribute.getModifiersByOperation(1))speed+=base*modifier.getAmount();
        for(AttributeModifier modifier:attribute.getModifiersByOperation(2))
            if(!modifier.getID().equals(SPEED_ID))speed*=1+modifier.getAmount();
        speed=attribute.getAttribute().clampValue(speed);
        double lockedFactor=(attribute.getAttributeValue()/walkSpeed+1)/2;
        double normalFactor=(speed/walkSpeed+1)/2;
        double ratio=normalFactor/lockedFactor;
        return Float.isFinite((float)normalFactor) && Float.isFinite((float)ratio) && ratio>0?(float)ratio:1;
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void attack(AttackEntityEvent event) {
        if (restricted(event.getEntityPlayer())) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void interact(PlayerInteractEvent event) {
        if (event.isCancelable() && restricted(event.getEntityPlayer())) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void breakBlock(BlockEvent.BreakEvent event) {
        if (restricted(event.getPlayer())) event.setCanceled(true);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void useItem(LivingEntityUseItemEvent.Start event) {
        if (event.getEntityLiving() instanceof EntityPlayer && restricted((EntityPlayer) event.getEntityLiving())) event.setCanceled(true);
    }
    @SubscribeEvent
    public static void jump(LivingEvent.LivingJumpEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity instanceof EntityPlayer && restricted((EntityPlayer) entity)) {
            entity.motionY = 0;
            entity.setSprinting(false);
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void knockback(LivingKnockBackEvent event) {
        if (Combat.suppressesKnockback(event.getEntityLiving())) event.setCanceled(true);
    }
}
