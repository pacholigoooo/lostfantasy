package dev.lostfantasy.world;

import com.mojang.authlib.GameProfile;
import dev.lostfantasy.Balance;
import dev.lostfantasy.TestWorld;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import org.junit.Test;
import static org.junit.Assert.*;

public class HiganRestrictionsTest {
    private EntityPlayer player(boolean special) {
        Bootstrap.register();return new EntityPlayer(new TestWorld(),new GameProfile(UUID.randomUUID(),"river")) {
            @Override public boolean isSpectator() {return special;}
            @Override public boolean isCreative() {return special;}
        };
    }
    @Test public void allGameModesLoseFlightAndLevitationInsideButRemainUnchangedOutside() {
        for(boolean special:new boolean[]{false,true}) {
            EntityPlayer p=player(special);p.capabilities.allowFlying=p.capabilities.isFlying=p.noClip=true;
            HiganRestrictions.ground(p);assertTrue(p.capabilities.allowFlying);assertTrue(p.capabilities.isFlying);
            p.dimension=Balance.gensokyoDimensionId;p.setPosition(HiganTerrain.boatX(0),72,HiganTerrain.boatZ(0));p.addPotionEffect(new PotionEffect(MobEffects.LEVITATION,200));
            HiganRestrictions.ground(p);assertFalse(p.capabilities.allowFlying);assertFalse(p.capabilities.isFlying);assertFalse(p.noClip);
            assertFalse(p.isPotionActive(MobEffects.LEVITATION));
        }
    }
    @Test public void travelPermissionIsEntityScopedAndAlwaysClosedAfterReturnOrException() {
        EntityPlayer p=player(false),other=player(false);p.dimension=other.dimension=Balance.gensokyoDimensionId;p.setPosition(HiganTerrain.boatX(0),72,HiganTerrain.boatZ(0));other.setPosition(p.posX,72,p.posZ);
        assertTrue(HiganRestrictions.blocksTeleport(p));assertTrue(HiganRestrictions.blocksTransfer(p,0));
        HiganWorld.authorize(p,()->{
            assertFalse(HiganRestrictions.blocksTeleport(p));assertFalse(HiganRestrictions.blocksTransfer(p,0));
            assertTrue(HiganRestrictions.blocksTeleport(other));
        });
        assertTrue(HiganRestrictions.blocksTeleport(p));
        try {HiganWorld.authorize(p,()->{throw new IllegalStateException("test");});}catch(IllegalStateException expected){}
        assertTrue(HiganRestrictions.blocksTeleport(p));
        // Forge normally adds this override through EventSubscriptionTransformer at launch.
        EnderTeleportEvent pearl=new EnderTeleportEvent(p,1,65,1,5) {
            @Override public boolean isCancelable() {return true;}
        };
        new HiganRestrictions().teleport(pearl);assertTrue(pearl.isCanceled());
        p.dimension=0;assertFalse(HiganRestrictions.blocksTeleport(p));assertFalse(HiganRestrictions.blocksTransfer(p,-1));
        assertFalse(HiganRestrictions.blocksTransfer(p,Balance.gensokyoDimensionId));
    }
}
