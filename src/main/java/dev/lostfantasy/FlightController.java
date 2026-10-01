package dev.lostfantasy;

import dev.lostfantasy.data.PlayerData;
import net.minecraft.entity.player.EntityPlayerMP;

/** Own only flight permissions actually granted by this mod. */
public final class FlightController {
    private FlightController() {}
    public static void update(EntityPlayerMP player) {
        if(dev.lostfantasy.world.HiganWorld.inside(player)) { dev.lostfantasy.world.HiganRestrictions.ground(player);return; }
        if((player.isCreative() || player.isSpectator()) && !player.capabilities.allowFlying) {
            player.capabilities.allowFlying=true;player.sendPlayerAbilities();
        }
        PlayerData data=PlayerData.get(player);
        if(data.journey.suspendedFlight()) {
            data.journey.suspendedFlight(false);data.dirty=true;
            player.capabilities.allowFlying=true;player.sendPlayerAbilities();
        }
        if(data.hasFinalStage()) {
            if(!player.capabilities.allowFlying) {
                player.capabilities.allowFlying=true;data.flightGranted=true;data.dirty=true;player.sendPlayerAbilities();
            }
        } else if(data.flightGranted) {
            data.flightGranted=false;data.dirty=true;
            if(!player.isCreative()&&!player.isSpectator()) {
                player.capabilities.allowFlying=false;player.capabilities.isFlying=false;player.sendPlayerAbilities();
            }
        }
    }
}
