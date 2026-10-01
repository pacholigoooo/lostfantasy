package dev.lostfantasy.world;

import dev.lostfantasy.ModBlocks;
import dev.lostfantasy.combat.SpellManager;
import dev.lostfantasy.core.Spell;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.item.FantasyItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;

/** The research page generated on the library desk. */
public final class EmeraldStudy {
    private EmeraldStudy() {}
    public static BlockPos position(LibraryRuinLayout.Site site) {
        int[] offset = LibraryRuinLayout.toWorld(-2, 0, site.turns);
        return new BlockPos(site.centerX()+offset[0], site.baseY+5, site.centerZ()+offset[1]);
    }
    public static void learn(EntityPlayerMP player, int x, int z) {
        if (!LibrarySites.dimension(player.dimension) || SpellManager.active(player) || SpellManager.locked(player)) return;
        WorldServer world = player.getServerWorld();
        LibraryRuinLayout.Site site = LibrarySites.generated(world, player.getPosition());
        if (site == null) return;
        BlockPos pos = position(site);
        if (pos.getX()!=x || pos.getZ()!=z || player.getDistanceSqToCenter(pos)>36 || !world.isBlockLoaded(pos)
                || world.getBlockState(pos).getBlock()!=ModBlocks.EMERALD_STUDY) return;
        RayTraceResult hit = world.rayTraceBlocks(player.getPositionEyes(1), new Vec3d(pos).add(.5,.1,.5), false,true,false);
        if (hit!=null && !pos.equals(hit.getBlockPos())) return;
        PlayerData data = PlayerData.get(player);
        if (!data.canStudy(Spell.EMERALD_CITY)) {
            FantasyItem.message(player,data.research.has(dev.lostfantasy.core.ResearchProgress.COMPLETED)
                    ?"emerald_requirements":"emerald_research");
            return;
        }
        boolean learned = data.learnFromStudy(Spell.EMERALD_CITY);
        player.sendMessage(new TextComponentTranslation("message.lostfantasy."+(learned?"emerald_learned":"already_learned"),
                new TextComponentTranslation(Spell.EMERALD_CITY.translationKey())));
    }
}
