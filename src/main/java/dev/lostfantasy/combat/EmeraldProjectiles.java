package dev.lostfantasy.combat;

import dev.lostfantasy.LostFantasy;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid=LostFantasy.ID)
public final class EmeraldProjectiles {
    private EmeraldProjectiles() {}
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void impact(ProjectileImpactEvent.Arrow event) {
        if(!event.getArrow().world.isRemote && SpellManager.interceptEmeraldArrow(event.getArrow(),event.getRayTraceResult().hitVec))
            event.setCanceled(true);
    }
}
