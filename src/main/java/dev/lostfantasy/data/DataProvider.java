package dev.lostfantasy.data;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.*;
import javax.annotation.Nullable;

public final class DataProvider implements ICapabilitySerializable<NBTTagCompound> {
    @CapabilityInject(PlayerData.class) public static final Capability<PlayerData> CAPABILITY=null;
    private final PlayerData data=new PlayerData();
    public static void register() {
        CapabilityManager.INSTANCE.register(PlayerData.class,new Capability.IStorage<PlayerData>() {
            @Override public NBTBase writeNBT(Capability<PlayerData> c,PlayerData d,EnumFacing f) {return d.serializeNBT();}
            @Override public void readNBT(Capability<PlayerData> c,PlayerData d,EnumFacing f,NBTBase n) {if(n instanceof NBTTagCompound)d.deserializeNBT((NBTTagCompound)n);}
        },PlayerData::new);
    }
    @Override public boolean hasCapability(Capability<?> c,@Nullable EnumFacing f) {return c==CAPABILITY;}
    @Override public <T> T getCapability(Capability<T> c,@Nullable EnumFacing f) {return c==CAPABILITY?CAPABILITY.cast(data):null;}
    @Override public NBTTagCompound serializeNBT() {return data.serializeNBT();}
    @Override public void deserializeNBT(NBTTagCompound n) {data.deserializeNBT(n);}
}
