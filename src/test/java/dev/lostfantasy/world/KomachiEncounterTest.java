package dev.lostfantasy.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.Test;
import static org.junit.Assert.*;

public class KomachiEncounterTest {
    @Test public void storesTheSameShoreAcrossReloadsIncludingNegativeCoordinates() {
        KomachiEncounter a=new KomachiEncounter();assertNull(a.shore());
        NBTTagCompound n=new NBTTagCompound();BlockPos shore=new BlockPos(-1025,67,347);n.setLong("shore",shore.toLong());a.readFromNBT(n);
        KomachiEncounter b=new KomachiEncounter();b.readFromNBT(a.writeToNBT(new NBTTagCompound()));assertEquals(shore,b.shore());
        b.readFromNBT(new NBTTagCompound());assertNull(b.shore());
    }
}
