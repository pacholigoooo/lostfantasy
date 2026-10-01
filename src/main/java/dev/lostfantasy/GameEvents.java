package dev.lostfantasy;

import dev.lostfantasy.combat.Combat;
import dev.lostfantasy.combat.DamageTransactions;
import dev.lostfantasy.combat.EchoCombat;
import dev.lostfantasy.combat.SageRetaliation;
import dev.lostfantasy.combat.SpellManager;
import dev.lostfantasy.core.Rules;
import dev.lostfantasy.data.DataProvider;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.entity.EntityCompanion;
import dev.lostfantasy.entity.EntityPowerOrb;
import dev.lostfantasy.item.FantasyItem;
import dev.lostfantasy.network.Network;
import dev.lostfantasy.world.GapSupport;
import dev.lostfantasy.world.GapWorld;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class GameEvents {
    private static final UUID RACE_HEALTH_MODIFIER = UUID.fromString("cb0fba31-58d7-4ad4-9f51-1ec2f4b69d5c");
    private static final int GAP_TRAVEL_NOTICE_INTERVAL = 40;
    private final Map<EntityPlayer, Long> gapTravelNotices = new WeakHashMap<>();
    private final SageRetaliation retaliation = new SageRetaliation();

    @SubscribeEvent
    public void attach(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(new ResourceLocation(LostFantasy.ID, "player"), new DataProvider());
        }
    }

    @SubscribeEvent
    public void clone(Clone event) {
        PlayerData data = PlayerData.get(event.getEntityPlayer());
        data.deserializeNBT(PlayerData.get(event.getOriginal()).serializeNBT());
        // Death keeps learned spells and progression, but ends captivity and clears P/current spirit.
        if (event.isWasDeath()) data.resetAfterDeath();
        clearTransientState(event.getOriginal());
    }

    @SubscribeEvent
    public void login(PlayerLoggedInEvent event) {
        restorePlayerState(event.player);
    }

    @SubscribeEvent
    public void respawn(PlayerRespawnEvent event) {
        restorePlayerState(event.player);
    }

    private static void restorePlayerState(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) return;
        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        // Rejoining preserves captivity; the death clone has already cleared it before respawn.
        GapWorld.enforceTrap(serverPlayer);
        Network.syncFull(serverPlayer);
    }

    @SubscribeEvent
    public void logout(PlayerLoggedOutEvent event) {
        clearTransientState(event.player);
    }

    @SubscribeEvent
    public void dimension(PlayerChangedDimensionEvent event) {
        clearTransientState(event.player);
        if (event.player instanceof EntityPlayerMP) Network.syncFull((EntityPlayerMP) event.player);
    }

    private void clearTransientState(EntityPlayer player) {
        if (player.world.isRemote) return;
        SpellManager.cancel(player);
        EchoCombat.forget(player);
        retaliation.forget(player);
        DamageTransactions.forget(player);
        gapTravelNotices.remove(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void xp(PlayerPickupXpEvent event) {
        if (!event.isCanceled() && !event.getEntityPlayer().world.isRemote) {
            PlayerData.get(event.getEntityPlayer()).absorb(event.getOrb().xpValue);
        }
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        if (!player.isEntityAlive()) return;
        PlayerData data = PlayerData.get(player);
        FlightController.update(player);
        GapSupport.recover(player);
        recoverSpirit(data);
        tickCooldowns(data);

        if (player.ticksExisted % 20 == 0) {
            GapWorld.enforceTrap(player);
            FantasyAdvancement.checkPlayer(player, data);
            updateRaceHealth(player, data);
            applySunlight(player, data);
        }
        if (data.dirty && player.ticksExisted % 10 == 0) Network.sync(player);
    }

    private static void recoverSpirit(PlayerData data) {
        float previous = data.spirit();
        data.setSpirit(Math.min(data.capacity(), data.spirit() + Balance.spiritPerSecond * (1 + data.magic() * .3f) / 20f));
        if (previous != data.spirit()) data.dirty = true;
    }

    private static void tickCooldowns(PlayerData data) {
        if (data.cooldown > 0 || data.echoCooldown > 0 || data.shieldBrokenTicks > 0) {
            // The client displays these counters from snapshots, including the shield's reconstruction timer.
            data.dirty = true;
        }
        if (data.cooldown > 0) data.cooldown--;
        if (data.echoCooldown > 0) data.echoCooldown--;
        if (data.shieldBrokenTicks > 0 && --data.shieldBrokenTicks == 0) data.shield = Balance.shieldDurability;
    }

    private static void updateRaceHealth(EntityPlayerMP player, PlayerData data) {
        IAttributeInstance health = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        double bonus = data.youkai() * Balance.youkaiHealthPerTier + data.vampire() * Balance.vampireHealthPerTier;
        AttributeModifier existing = health.getModifier(RACE_HEALTH_MODIFIER);
        if (existing != null && Math.abs(existing.getAmount() - bonus) < .0001) return;
        if (existing != null) health.removeModifier(existing);
        if (bonus > 0) health.applyModifier(new AttributeModifier(RACE_HEALTH_MODIFIER, "Lost Fantasy races", bonus, 0).setSaved(false));
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    private static void applySunlight(EntityPlayerMP player, PlayerData data) {
        if (data.vampire() <= 0 || data.vampire() >= 4 || player.dimension == Balance.dimensionId || dev.lostfantasy.world.HiganWorld.inside(player)
                || player.isCreative() || player.isSpectator() || !player.world.isDaytime()) return;
        BlockPos feet = player.getPosition();
        BlockPos eyes = new BlockPos(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        if (player.world.isRainingAt(feet) || !player.world.canSeeSky(eyes)) return;
        player.setFire(2);
        player.attackEntityFrom(DamageSource.ON_FIRE, Balance.sunDamage);
    }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        SpellManager.tick();
        // addScheduledTask runs immediately on the server thread; this explicit boundary follows damage settlement.
        retaliation.finishTick();
        DamageTransactions.expireDeferred();
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null || server.getTickCounter() % 20 != 0) return;
        WorldServer overworld = server.getWorld(0);
        if (overworld != null) EchoCombat.prune(overworld.getTotalWorldTime());
    }

    @SubscribeEvent
    public void worldUnload(WorldEvent.Unload event) {
        if (event.getWorld().isRemote) return;
        SpellManager.unload(event.getWorld());
        retaliation.forgetWorld(event.getWorld());
        DamageTransactions.forgetWorld(event.getWorld());
        gapTravelNotices.keySet().removeIf(player -> player.world == event.getWorld());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void playerMelee(AttackEntityEvent event) {
        if (event.getEntityPlayer().world.isRemote || !(event.getTarget() instanceof EntityLivingBase)) return;
        // Melee intent starts retaliation targeting even if the hit is blocked; projectiles use playerCombat below.
        recordOwnerCombat(event.getEntityPlayer(), (EntityLivingBase) event.getTarget());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void hurt(LivingHurtEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        if (victim.world.isRemote) return;
        DamageSource source = event.getSource();
        Entity attacker = source.getTrueSource();
        if (attacker instanceof EntityPlayer && !Combat.isSpell(source)) {
            EntityPlayer player = (EntityPlayer) attacker;
            if (Combat.hostile(player, victim)) player.setLastAttackedEntity(victim);
            event.setAmount(event.getAmount() * ordinaryAttackMultiplier(player, PlayerData.get(player), source));
        }
    }

    private static float ordinaryAttackMultiplier(EntityPlayer player, PlayerData data, DamageSource source) {
        float bonus = data.power() * Balance.powerDamage + data.vampire() * Balance.vampireAttackPerTier;
        bonus += source.isMagicDamage() ? data.magic() * Balance.magicAttackPerTier : data.youkai() * Balance.youkaiAttackPerTier;
        if (data.vampire() > 0 && !player.world.isDaytime()) bonus += Balance.nightBonus;
        return 1 + bonus;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void damage(LivingDamageEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        if (victim.world.isRemote) return;
        if (victim instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) victim;
            float original = DamageTransactions.consume(event);
            PlayerData data = PlayerData.get(player);
            event.setAmount(mitigatePlayerDamage(player, data, event.getSource(), original, event.getAmount()));
            // A hit can trigger retaliation even when defence absorbs all of its damage.
            retaliation.request(player, data, event.getSource().getTrueSource());
        }
    }

    private static float mitigatePlayerDamage(EntityPlayerMP player, PlayerData data, DamageSource source,
                                             float original, float afterArmor) {
        if (source == DamageSource.OUT_OF_WORLD || source == DamageSource.STARVE) return afterArmor;
        float passiveReduction = (data.magic() + data.youkai() + data.vampire()) * Balance.defensePerTier;
        float remaining;
        if (Float.isFinite(original)) {
            remaining = Rules.remainingDamage(original, afterArmor, passiveReduction);
            remaining = Math.max(0, remaining - absorbWithShield(player, data, original, remaining));
        } else {
            // A custom unpaired event supplies no pre-armour amount. Do not invent one or spend shield on it.
            remaining = Float.isFinite(afterArmor) ? Math.max(0, afterArmor) : 0;
            if (passiveReduction > 0 || (data.magic() >= 4 && data.shieldBrokenTicks == 0 && data.shield > 0))
                DamageTransactions.reportMissingBaseline(player, source);
        }
        // Echoes take only the damage left after passive defence and the magical barrier.
        if (remaining > 0) remaining = Math.max(0, remaining - SpellManager.interceptWithEcho(player, remaining));
        return remaining;
    }

    private static float absorbWithShield(EntityPlayerMP player, PlayerData data, float original, float remaining) {
        if (data.magic() < 4 || data.shieldBrokenTicks > 0 || data.shield <= 0 || !(remaining > 0)) return 0;
        float absorbed = Math.min(data.shield, Math.min(remaining, original * Balance.shieldReduction));
        if (absorbed <= 0) return 0;
        data.shield -= absorbed;
        if (data.shield <= 0) {
            data.shield = 0;
            data.shieldBrokenTicks = Balance.shieldRepairTicks;
            FantasyItem.message(player, "shield_broken");
        }
        data.dirty = true;
        return absorbed;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void playerCombat(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        // Record body hits before armour can absorb them; echoes cannot renew their own PvP targets.
        if (event.getEntityLiving().world.isRemote || event.getAmount() <= 0
                || EchoCombat.isEcho(source)) return;
        Entity attacker = source.getTrueSource();
        if (attacker instanceof EntityPlayer) recordOwnerCombat((EntityPlayer) attacker, event.getEntityLiving());
    }

    private static void recordOwnerCombat(EntityPlayer player, EntityLivingBase target) {
        if (target instanceof EntityPlayer) EchoCombat.recordPlayerCombat(player, (EntityPlayer) target);
        EchoCombat.recordOwnerAttack(player, target);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void castingProtection(LivingAttackEvent event) {
        if (!event.getEntityLiving().world.isRemote && event.getEntityLiving() instanceof EntityPlayer
                && SpellManager.protectsCaster((EntityPlayer) event.getEntityLiving())) event.setCanceled(true);
    }

    @SubscribeEvent
    public void voidDamage(LivingAttackEvent event) {
        // Ordinary void damage is disabled in the gap, but the command-sized kill source still works.
        if (event.getEntityLiving().dimension == Balance.dimensionId && event.getSource() == DamageSource.OUT_OF_WORLD
                && event.getAmount() < Float.MAX_VALUE) event.setCanceled(true);
    }

    @SubscribeEvent
    public void gapCollisions(GetCollisionBoxesEvent event) {
        GapSupport.collisions(event);
    }

    @SubscribeEvent
    public void gapLiving(LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) GapSupport.recover(event.getEntityLiving());
    }

    @SubscribeEvent
    public void gapFall(LivingFallEvent event) {
        if (event.getEntityLiving().dimension == Balance.dimensionId
                && Math.abs(event.getEntityLiving().posY - GapSupport.HEIGHT) < .05) event.setCanceled(true);
    }

    @SubscribeEvent
    public void teleport(EnderTeleportEvent event) {
        if (SpellManager.locked(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public void dimensionTravel(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        if (entity.world.isRemote) return;
        boolean captive = entity instanceof EntityPlayerMP ? PlayerData.get((EntityPlayerMP) entity).trappedInGap
                : entity.getEntityData().getBoolean("lfGapCaptive");
        if (!captive || event.getDimension() == Balance.dimensionId || GapWorld.transferAuthorized(entity)) return;
        event.setCanceled(true);
        if (entity instanceof EntityPlayerMP) notifyGapTravelBlocked((EntityPlayerMP) entity);
    }

    private void notifyGapTravelBlocked(EntityPlayerMP player) {
        long now = player.world.getTotalWorldTime();
        Long previous = gapTravelNotices.get(player);
        // A portal may retry every tick. Explain the first denial immediately, then limit chat repeats.
        if (previous != null && now >= previous && now - previous < GAP_TRAVEL_NOTICE_INTERVAL) return;
        gapTravelNotices.put(player, now);
        player.sendMessage(new TextComponentTranslation("message.lostfantasy.gap_travel_blocked"));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void death(LivingDeathEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        if (victim.world.isRemote) return;
        if (victim instanceof EntityPlayer) clearTransientState((EntityPlayer) victim);
        else retaliation.forget(victim);

        Entity source = event.getSource().getTrueSource();
        if (!(source instanceof EntityPlayerMP) || source == victim || victim instanceof EntityCompanion) return;
        EntityPlayerMP killer = (EntityPlayerMP) source;
        PlayerData data = PlayerData.get(killer);
        dropPower(killer, data, victim);
        advanceYoukai(data, victim);
        dropWitchPotion(killer, victim);
        data.dirty = true;
    }

    private static void dropPower(EntityPlayerMP killer, PlayerData data, EntityLivingBase victim) {
        if (data.power() >= Rules.MAX_POWER) return;
        int power = Rules.powerDrop(victim.getMaxHealth(), Balance.healthPerPower);
        killer.world.spawnEntity(new EntityPowerOrb(killer.world, killer, power, victim.posX, victim.posY + .5, victim.posZ));
    }

    private static void advanceYoukai(PlayerData data, EntityLivingBase victim) {
        if (data.youkai() <= 0 || data.youkai() >= 3) return;
        data.youkaiProgress(Math.min(Integer.MAX_VALUE, data.youkaiProgress() + (long) Math.max(1, victim.getMaxHealth())));
        if (data.youkaiProgress() >= Balance.youkaiThird) data.advanceStage(dev.lostfantasy.core.Growth.Route.YOUKAI,3);
        else if (data.youkaiProgress() >= Balance.youkaiSecond) data.advanceStage(dev.lostfantasy.core.Growth.Route.YOUKAI,2);
    }

    private static void dropWitchPotion(EntityPlayerMP killer, EntityLivingBase victim) {
        if (!(victim instanceof EntityWitch)) return;
        int looting = EnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, killer.getHeldItemMainhand());
        float chance = Math.min(1, Balance.witchChance + looting * Balance.lootingBonus);
        if (killer.getRNG().nextFloat() < chance) victim.entityDropItem(new ItemStack(ModItems.YOUKAI_POTION), 0);
    }
}
