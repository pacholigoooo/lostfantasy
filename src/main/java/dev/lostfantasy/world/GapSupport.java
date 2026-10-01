package dev.lostfantasy.world;

import dev.lostfantasy.Balance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/** A collision-only walking surface. No blocks, chunks or flight capabilities are changed. */
public final class GapSupport {
    public static final double HEIGHT=65;
    private GapSupport() {}
    public static AxisAlignedBB surface(AxisAlignedBB query) {
        if(query.minY>=HEIGHT || query.maxY<=HEIGHT-1)return null;
        return new AxisAlignedBB(query.minX-1,HEIGHT-1,query.minZ-1,query.maxX+1,HEIGHT,query.maxZ+1);
    }
    private static boolean applies(Entity entity) {
        return entity!=null && entity.dimension==Balance.dimensionId && !entity.noClip && !entity.hasNoGravity()
            && (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).capabilities.isFlying);
    }
    public static void collisions(GetCollisionBoxesEvent event) {
        if(!applies(event.getEntity()))return;
        AxisAlignedBB support=surface(event.getAabb());
        if(support!=null)event.getCollisionBoxesList().add(support);
    }
    public static void recover(Entity entity) {
        if(!applies(entity) || entity.world.isRemote || !entity.isEntityAlive())return;
        // Keep living entities on the current collision surface after spawning or rejoining.
        if(entity.posY<HEIGHT-.01) {
            entity.setPositionAndUpdate(entity.posX,HEIGHT,entity.posZ);
            entity.motionY=0;entity.fallDistance=0;entity.onGround=true;
        }
        if(entity instanceof EntityPlayerMP && entity.onGround && Math.abs(entity.posY-HEIGHT)<.05) {
            // Vanilla's floating detector scans block states, ignoring Forge collision boxes.
            // Exempt only real contact with this surface, never general airborne movement.
            try {FloatingFlag.FIELD.setBoolean(((EntityPlayerMP)entity).connection,false);}
            catch(IllegalAccessException ex) {throw new IllegalStateException("Cannot recognize gap support",ex);}
        }
    }
    private static final class FloatingFlag {
        static final java.lang.reflect.Field FIELD=ReflectionHelper.findField(NetHandlerPlayServer.class,"floating","field_184344_B");
    }
}
