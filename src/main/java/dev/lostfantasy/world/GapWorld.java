package dev.lostfantasy.world;
import dev.lostfantasy.*;
import dev.lostfantasy.combat.SpellManager;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.item.FantasyItem;
import dev.lostfantasy.network.Network;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.DimensionManager;

public final class GapWorld {
    public static DimensionType TYPE;
    private static final java.util.Set<java.util.UUID> AUTHORIZED_TRANSFERS=new java.util.HashSet<>();
    private GapWorld() {}
    public static void register() {
        if(DimensionManager.isDimensionRegistered(Balance.dimensionId))throw new IllegalStateException("Lost Fantasy dimension ID "+Balance.dimensionId+" is already used; change config/lostfantasy.cfg");
        TYPE=DimensionType.register("lostfantasy_gap","_gap",Balance.dimensionId,GapProvider.class,false);DimensionManager.registerDimension(Balance.dimensionId,TYPE);
    }
    public static void useKey(EntityPlayerMP p) {
        if(HiganWorld.inside(p)) {FantasyItem.message(p,"higan_no_teleport");return;}
        if(!hasKey(p)) {FantasyItem.message(p,"gap_needs_key");return;}
        if(SpellManager.active(p)||SpellManager.locked(p)) {FantasyItem.message(p,"cooldown");return;}
        if(p.getCooldownTracker().hasCooldown(ModItems.GAP_KEY))return;
        Vec3d direction=new Vec3d(-Math.sin(Math.toRadians(p.rotationYaw)),0,Math.cos(Math.toRadians(p.rotationYaw)));
        Vec3d at=p.getPositionVector().add(direction.scale(3));
        AxisAlignedBB bounds=new AxisAlignedBB(at.x-1.2,at.y,at.z-1.2,at.x+1.2,at.y+3.4,at.z+1.2);
        if(!p.world.getCollisionBoxes(p,bounds).isEmpty()) {FantasyItem.message(p,"rift_space");return;}
        if(p.world.getEntitiesWithinAABB(dev.lostfantasy.entity.EntityGapRift.class,p.getEntityBoundingBox().grow(16)).size()>=8) {FantasyItem.message(p,"rift_limit");return;}
        dev.lostfantasy.entity.EntityGapRift rift=new dev.lostfantasy.entity.EntityGapRift(p.world);rift.setLocationAndAngles(at.x,at.y,at.z,p.rotationYaw,0);
        if(p.world.spawnEntity(rift)) {p.getCooldownTracker().setCooldown(ModItems.GAP_KEY,60);FantasyItem.message(p,"rift_open");}
    }
    public static void crossRift(EntityPlayerMP p) {
        if(!p.isEntityAlive()||p.isSpectator()||p.timeUntilPortal>0||SpellManager.active(p)||SpellManager.locked(p))return;
        if(!hasKey(p)) {if(p.ticksExisted%40==0)FantasyItem.message(p,"gap_needs_key");return;}
        p.timeUntilPortal=40;
        if(p.dimension==Balance.dimensionId)exit(p);else enter(p,false);
    }
    private static void enter(EntityPlayerMP p,boolean captive) {
        int source=p.dimension;Vec3d origin=p.getPositionVector();float yaw=p.rotationYaw,pitch=p.rotationPitch;
        if(!transfer(p,Balance.dimensionId,new Vec3d(.5,GapSupport.HEIGHT,.5)))return;
        PlayerData d=PlayerData.get(p);d.hasReturn=true;d.returnDimension=source;d.returnX=origin.x;d.returnY=origin.y;d.returnZ=origin.z;d.returnYaw=yaw;d.returnPitch=pitch;d.trappedInGap|=captive;
        Network.sync(p);FantasyItem.message(p,"gap_enter");
        if(captive)FantasyAdvancement.GAP_CAPTIVE.grant(p);
    }
    public static void exit(EntityPlayerMP p) {
        if(p.dimension!=Balance.dimensionId || SpellManager.locked(p) || SpellManager.active(p))return;
        if(!hasKey(p)) {FantasyItem.message(p,"gap_needs_key");return;}
        PlayerData d=PlayerData.get(p);int destination=d.hasReturn&&d.returnDimension!=Balance.dimensionId&&DimensionManager.isDimensionRegistered(d.returnDimension)?d.returnDimension:0;
        WorldServer world=p.getServer().getWorld(destination);if(world==null) {destination=0;world=p.getServer().getWorld(0);}
        if(world==null)return;
        Vec3d requested=returnPosition(d,destination,world.getSpawnPoint());
        Vec3d safe=safePosition(world,requested);
        if(safe==null) {FantasyItem.message(p,"gap_exit_blocked");return;}
        boolean returning=d.hasReturn&&d.returnDimension==destination;
        float yaw=returning?d.returnYaw:p.rotationYaw,pitch=returning?d.returnPitch:p.rotationPitch;
        if(!transfer(p,destination,safe))return;
        if(d.finishGapReturn())FantasyAdvancement.GAP_ESCAPE.grant(p);
        p.connection.setPlayerLocation(safe.x,safe.y,safe.z,Float.isFinite(yaw)?yaw:0,Float.isFinite(pitch)?pitch:0);Network.sync(p);FantasyItem.message(p,"gap_exit");
    }
    public static void pull(EntityPlayerMP sage,EntityLivingBase target) {
        if(target.dimension==Balance.dimensionId || target.isRiding() || target.isBeingRidden())return;
        if(target instanceof EntityPlayerMP)enter((EntityPlayerMP)target,true);
        else {
            WorldServer dest=sage.getServer().getWorld(Balance.dimensionId);
            if(dest==null)return;
            Entity moved=target.changeDimension(Balance.dimensionId,new GapTeleporter(dest,new Vec3d(7.5,GapSupport.HEIGHT,.5)));
            if(moved!=null&&moved.dimension==Balance.dimensionId)moved.getEntityData().setBoolean("lfGapCaptive",true);
        }
    }
    private static boolean transfer(EntityPlayerMP p,int dimension,Vec3d at) {
        WorldServer world=p.getServer().getWorld(dimension);if(world==null || !p.isEntityAlive())return false;
        SpellManager.cancel(p);
        world.getChunk((int)Math.floor(at.x)>>4,(int)Math.floor(at.z)>>4);
        p.dismountRidingEntity();AUTHORIZED_TRANSFERS.add(p.getUniqueID());
        try {p.getServer().getPlayerList().transferPlayerToDimension(p,dimension,new GapTeleporter(world,at));}
        finally {AUTHORIZED_TRANSFERS.remove(p.getUniqueID());}
        if(p.dimension!=dimension || p.world!=world)return false;
        p.connection.setPlayerLocation(at.x,at.y,at.z,p.rotationYaw,p.rotationPitch);p.motionX=p.motionY=p.motionZ=0;p.fallDistance=0;
        return true;
    }
    public static boolean hasKey(EntityPlayer p) {
        for(net.minecraft.item.ItemStack stack:p.inventory.mainInventory)if(stack.getItem()==ModItems.GAP_KEY)return true;
        return p.getHeldItemOffhand().getItem()==ModItems.GAP_KEY;
    }
    public static boolean transferAuthorized(Entity e) {return AUTHORIZED_TRANSFERS.contains(e.getUniqueID());}
    public static void enforceTrap(EntityPlayerMP p) {
        if(PlayerData.get(p).trappedInGap && p.dimension!=Balance.dimensionId)transfer(p,Balance.dimensionId,new Vec3d(.5,GapSupport.HEIGHT,.5));
    }
    static Vec3d returnPosition(PlayerData data,int dimension,BlockPos spawn) {
        return data.hasReturn && data.returnDimension==dimension
                ?new Vec3d(data.returnX,data.returnY,data.returnZ):new Vec3d(spawn);
    }
    private static Vec3d safePosition(WorldServer world,Vec3d requested) {
        if(!Double.isFinite(requested.x)||!Double.isFinite(requested.y)||!Double.isFinite(requested.z))requested=new Vec3d(world.getSpawnPoint());
        BlockPos base=new BlockPos(world.getWorldBorder().contains(new BlockPos(requested))?requested:new Vec3d(world.getSpawnPoint()));
        if(!world.getWorldBorder().contains(base))return null;
        world.getChunk(base);
        BlockPos safe=findSafePosition(base,b->safeLanding(world,b));
        if(safe!=null)return new Vec3d(safe).add(.5,0,.5);
        BlockPos spawn=world.getSpawnPoint();
        if(!world.getWorldBorder().contains(spawn))return null;
        world.getChunk(spawn);
        safe=findSafePosition(world.getTopSolidOrLiquidBlock(spawn),b->safeLanding(world,b));
        return safe==null?null:new Vec3d(safe).add(.5,0,.5);
    }
    public static boolean safeLanding(World world,BlockPos pos) {
        if(pos.getY()<2 || pos.getY()>world.getHeight()-3 || !world.isBlockLoaded(pos)
                || !world.getWorldBorder().contains(pos))return false;
        IBlockState floor=world.getBlockState(pos.down());
        return floor.isFullCube() && floor.getBlock()!=Blocks.MAGMA && floor.getBlock()!=Blocks.CACTUS
                && world.isAirBlock(pos) && world.isAirBlock(pos.up());
    }
    public static BlockPos findSafePosition(BlockPos base,java.util.function.Predicate<BlockPos> safe) {
        // Each completed inner square already failed. Visit only the next ring, in the
        // original dx/dz/dy order, so the first safe result and fallback stay unchanged.
        BlockPos.MutableBlockPos candidate=new BlockPos.MutableBlockPos();
        for(int radius=0;radius<=8;radius++)for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++) {
            if(Math.max(Math.abs(dx),Math.abs(dz))!=radius)continue;
            for(int dy=-4;dy<=12;dy++) {
                int y=base.getY()+dy;if(y<2||y>253)continue;
                candidate.setPos(base.getX()+dx,y,base.getZ()+dz);
                if(safe.test(candidate))return candidate.toImmutable();
            }
        }
        return null;
    }
    private static final class GapTeleporter extends Teleporter {
        private final Vec3d at;
        GapTeleporter(WorldServer world,Vec3d at) {super(world);this.at=at;}
        @Override public void placeInPortal(Entity e,float yaw) {e.setLocationAndAngles(at.x,at.y,at.z,yaw,e.rotationPitch);e.motionX=e.motionY=e.motionZ=0;e.fallDistance=0;}
        @Override public boolean placeInExistingPortal(Entity e,float yaw) {placeInPortal(e,yaw);return true;}
        @Override public boolean makePortal(Entity e) {return true;}
        @Override public void removeStalePortalLocations(long t) {}
    }
}
