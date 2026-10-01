package dev.lostfantasy.world;

import dev.lostfantasy.entity.EntityKomachi;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

/** One saved overworld riverside encounter; generation never reads neighbouring chunks. */
public final class KomachiEncounter extends WorldSavedData {
    private static final String KEY="lostfantasy_komachi";
    private BlockPos shore;
    public KomachiEncounter() {super(KEY);}
    public KomachiEncounter(String name) {super(name);}
    public static KomachiEncounter get(World world) {
        KomachiEncounter data=(KomachiEncounter)world.getPerWorldStorage().getOrLoadData(KomachiEncounter.class,KEY);
        if(data==null) {data=new KomachiEncounter();world.getPerWorldStorage().setData(KEY,data);}
        return data;
    }
    public BlockPos shore() {return shore;}
    public static void arrive(World world,BlockPos pos) {
        if(world.isRemote || world.provider.getDimension()!=0)return;
        KomachiEncounter data=get(world);if(data.shore!=null)return;
        if(!world.isAirBlock(pos.up()) || !world.getBlockState(pos.down()).isTopSolid())return;
        EntityKomachi komachi=new EntityKomachi(world);komachi.setLocationAndAngles(pos.getX()+.5,pos.getY(),pos.getZ()+.5,180,0);komachi.anchor(pos);
        if(world.spawnEntity(komachi)) {data.shore=pos.toImmutable();data.markDirty();}
    }
    @Override public void readFromNBT(NBTTagCompound n) {shore=n.hasKey("shore")?BlockPos.fromLong(n.getLong("shore")):null;}
    @Override public NBTTagCompound writeToNBT(NBTTagCompound n) {if(shore!=null)n.setLong("shore",shore.toLong());return n;}
}
