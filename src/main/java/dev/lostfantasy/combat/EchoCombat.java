package dev.lostfantasy.combat;

import dev.lostfantasy.ModItems;
import dev.lostfantasy.Balance;
import dev.lostfantasy.core.EchoAggro;
import dev.lostfantasy.entity.EntityCompanion;
import dev.lostfantasy.entity.EntityEchoArrow;
import dev.lostfantasy.entity.EntityOfuda;
import dev.lostfantasy.network.EffectMessage;
import dev.lostfantasy.network.Network;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraft.world.WorldServer;
import java.util.UUID;

public final class EchoCombat {
    private static final String DAMAGE_TYPE = "lostfantasy.echo";
    private static final EchoAggro AGGRO = new EchoAggro();

    private EchoCombat() {}

    public static boolean isEcho(DamageSource source) {
        return DAMAGE_TYPE.equals(source.damageType);
    }

    public static boolean canAttack(EntityPlayer owner, EntityLivingBase target) {
        if (!Combat.canHarm(owner, target)) return false;
        if (target instanceof EntityPlayer) {
            return AGGRO.active(owner.getUniqueID(), target.getUniqueID(), time(owner));
        }
        return target instanceof IMob || target.isCreatureType(EnumCreatureType.MONSTER, false);
    }

    public static void recordPlayerCombat(EntityPlayer attacker, EntityPlayer victim) {
        if (attacker.world == victim.world && Combat.canHarm(attacker, victim)) {
            AGGRO.record(attacker.getUniqueID(), victim.getUniqueID(), time(attacker));
        }
    }

    public static void recordOwnerAttack(EntityPlayer owner, EntityLivingBase target) {
        if (owner.world == target.world && canAttack(owner, target)) {
            AGGRO.recordAttack(owner.getUniqueID(), target.getUniqueID(), time(owner));
        }
    }

    public static EntityLivingBase ownerAttackTarget(EntityPlayer owner) {
        UUID id = AGGRO.attackTarget(owner.getUniqueID(), time(owner));
        if (id == null) return null;
        Entity target = ((WorldServer) owner.world).getEntityFromUuid(id);
        return target instanceof EntityLivingBase ? (EntityLivingBase) target : null;
    }

    private static long time(EntityPlayer player) {
        return player.getServer().getWorld(0).getTotalWorldTime();
    }

    public static void forget(EntityPlayer player) {
        AGGRO.forget(player.getUniqueID());
    }

    public static void prune(long tick) {
        AGGRO.prune(tick);
    }

    public static void clear() {
        AGGRO.clear();
    }

    public static boolean damage(EntityPlayer owner, EntityLivingBase target, float amount) {
        float damage = amount * Balance.spellDamageMultiplier;
        if (!Float.isFinite(damage) || damage <= 0 || !canAttack(owner, target)) return false;
        int previous = target.hurtResistantTime;
        target.hurtResistantTime = 0;
        try {
            return target.attackEntityFrom(new EntityDamageSource(DAMAGE_TYPE, owner).setMagicDamage(), damage);
        } finally {
            target.hurtResistantTime = Math.max(previous, target.hurtResistantTime);
        }
    }

    public static boolean ranged(EntityCompanion echo) {
        return ranged(echo.getHeldItemMainhand().getItem());
    }

    private static boolean ranged(Item item) {
        return item instanceof ItemBow || item == ModItems.OFUDA || item == ModItems.FURNACE || item == ModItems.YIN_YANG;
    }

    public static int strike(EntityCompanion echo, EntityPlayer owner, EntityLivingBase target) {
        if (!canAttack(owner, target)) return 1;
        ItemStack weapon = echo.getHeldItemMainhand();
        Item item = weapon.getItem();
        if (!ranged(item)) return melee(echo, owner, target, weapon);

        Vec3d from = echo.getPositionEyes(1);
        Vec3d aim = target.getPositionVector().add(0, target.height * .5, 0).subtract(from);
        if (item instanceof ItemBow) {
            EntityEchoArrow arrow = new EntityEchoArrow(echo.world, echo, owner);
            arrow.shoot(aim.x, aim.y + Math.sqrt(aim.x * aim.x + aim.z * aim.z) * .025, aim.z, 2.2f, .3f);
            int power = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, weapon);
            arrow.setDamage(2 + (power > 0 ? power * .5 + .5 : 0));
            arrow.setEchoKnockback(EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, weapon));
            if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, weapon) > 0) arrow.setFire(100);
            echo.world.spawnEntity(arrow);
            return 24;
        }
        if (item == ModItems.OFUDA) {
            echo.world.spawnEntity(new EntityOfuda(echo.world, owner, from, aim, 4, true));
            return 10;
        }
        damage(owner, target, item == ModItems.FURNACE ? 10 : 7);
        Network.CHANNEL.sendToAllAround(
                new EffectMessage(EffectMessage.ORDINARY_BEAM, echo.getEntityId(), echo.dimension, echo.world.getTotalWorldTime(), 8, 0, from, aim, 0),
                new NetworkRegistry.TargetPoint(echo.dimension, echo.posX, echo.posY, echo.posZ, 80));
        return 20;
    }

    private static int melee(EntityCompanion echo, EntityPlayer owner, EntityLivingBase target, ItemStack weapon) {
        float amount = (float) echo.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
        amount += EnchantmentHelper.getModifierForCreature(weapon, target.getCreatureAttribute());
        if (damage(owner, target, amount)) {
            int fire = EnchantmentHelper.getFireAspectModifier(echo);
            int knockback = EnchantmentHelper.getKnockbackModifier(echo);
            if (fire > 0) target.setFire(fire * 4);
            if (knockback > 0) target.knockBack(echo, knockback * .5f, echo.posX - target.posX, echo.posZ - target.posZ);
            EnchantmentHelper.applyThornEnchantments(target, echo);
            EnchantmentHelper.applyArthropodEnchantments(echo, target);
        }
        double speed = echo.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue();
        return Math.max(5, Math.min(80, (int) Math.ceil(20 / Math.max(.25, speed))));
    }
}
