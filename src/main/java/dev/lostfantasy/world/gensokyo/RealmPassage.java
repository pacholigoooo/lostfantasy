package dev.lostfantasy.world.gensokyo;

import dev.lostfantasy.world.GapWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraft.world.World;

/** Safe walking transitions shared by Gensokyo's small otherworlds. */
final class RealmPassage {
    private RealmPassage() {}
    static boolean transfer(EntityPlayerMP player,int dimension,BlockPos request,float yaw) {
        WorldServer target=player.getServer().getWorld(dimension);if(target==null)return false;
        BlockPos safe=findLanding(target,request);if(safe==null)return false;
        return transfer(player,target,safe,yaw);
    }
    static BlockPos findLanding(World world,BlockPos request) {
        if(!world.getWorldBorder().contains(request))return null;
        world.getChunk(request);
        if(GapWorld.safeLanding(world,request))return request;
        // Include the neighbouring chunks only when the exact landing has become obstructed.
        for(int cx=(request.getX()-8)>>4;cx<=(request.getX()+8)>>4;cx++)
            for(int cz=(request.getZ()-8)>>4;cz<=(request.getZ()+8)>>4;cz++) {
                BlockPos sample=new BlockPos(Math.max(request.getX()-8,Math.min(request.getX()+8,(cx<<4)+8)),
                        request.getY(),Math.max(request.getZ()-8,Math.min(request.getZ()+8,(cz<<4)+8)));
                if(world.getWorldBorder().contains(sample))world.getChunk(sample);
            }
        return GapWorld.findSafePosition(request,p->GapWorld.safeLanding(world,p));
    }
    static boolean transfer(EntityPlayerMP player,WorldServer target,BlockPos safe,float yaw) {
        int dimension=target.provider.getDimension();
        if(player.dimension==dimension)return false;
        player.changeDimension(dimension,new WalkTeleporter(target,safe,yaw));
        // Forge travel cancellation must not move the player inside the original world.
        if(player.world!=target || player.dimension!=dimension)return false;
        player.connection.setPlayerLocation(safe.getX()+.5,safe.getY(),safe.getZ()+.5,yaw,0);
        player.motionX=player.motionY=player.motionZ=0;player.fallDistance=0;player.timeUntilPortal=80;
        return true;
    }
    private static final class WalkTeleporter extends Teleporter {
        private final BlockPos at;private final float yaw;
        WalkTeleporter(WorldServer world,BlockPos at,float yaw) {super(world);this.at=at;this.yaw=yaw;}
        @Override public void placeInPortal(Entity e,float ignored) {e.setLocationAndAngles(at.getX()+.5,at.getY(),at.getZ()+.5,yaw,0);}
        @Override public boolean placeInExistingPortal(Entity e,float ignored) {placeInPortal(e,ignored);return true;}
        @Override public boolean makePortal(Entity e) {return true;}
        @Override public void removeStalePortalLocations(long time) {}
    }
}
