package dev.lostfantasy.world;

import dev.lostfantasy.Balance;
import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.combat.SpellManager;
import dev.lostfantasy.core.Spell;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.item.FantasyItem;
import dev.lostfantasy.world.gensokyo.GensokyoAtlas;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;

/** The shared original in the Yakumo library, with per-player learning. */
public final class BarrierStudy {
    public static final BlockPos LOCAL = new BlockPos(-25, 4, -10);
    private BarrierStudy() {}
    public static BlockPos position() {
        GensokyoAtlas estate=GensokyoAtlas.YAKUMO;
        return LOCAL.add(estate.x, estate.y, estate.z);
    }
    public static void learn(EntityPlayerMP player, int x, int z) {
        if (player.dimension != Balance.boundaryDimensionId || SpellManager.active(player) || SpellManager.locked(player)) return;
        WorldServer world = player.getServerWorld();
        BlockPos pos = position();
        if (pos.getX() != x || pos.getZ() != z || player.getDistanceSqToCenter(pos) > 36
                || !world.isBlockLoaded(pos) || world.getBlockState(pos).getBlock() != ModBlocks.BARRIER_STUDY) return;
        Vec3d center = new Vec3d(pos).add(.5, .1, .5);
        RayTraceResult hit = world.rayTraceBlocks(player.getPositionEyes(1), center, false, true, false);
        if (hit != null && !pos.equals(hit.getBlockPos())) return;
        PlayerData data = PlayerData.get(player);
        if (!data.qualifies(Spell.FOURFOLD_BARRIER)) { FantasyItem.message(player, "barrier_requirements"); return; }
        boolean learned = data.learn(Spell.FOURFOLD_BARRIER, false);
        player.sendMessage(new TextComponentTranslation("message.lostfantasy."+(learned ? "barrier_learned" : "already_learned"),
                new TextComponentTranslation(Spell.FOURFOLD_BARRIER.translationKey())));
    }
}
