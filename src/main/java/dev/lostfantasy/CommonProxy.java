package dev.lostfantasy;
import net.minecraft.nbt.NBTTagCompound;
import dev.lostfantasy.network.EffectMessage;
public class CommonProxy {
    public void receiveResearch(dev.lostfantasy.network.ResearchMessage message) {}
    public void openResearchCopy() {}
    public boolean restrictsMovement(net.minecraft.entity.player.EntityPlayer player) { return false; }
    public void openBarrierStudy(net.minecraft.util.math.BlockPos pos) {}
    public void openEmeraldStudy(net.minecraft.util.math.BlockPos pos) {}
    public void preInit() {}
    public void init() {}
    public void receiveState(NBTTagCompound data) {}
    public void receiveEffect(EffectMessage message) {}
    public void receiveFortune(dev.lostfantasy.network.FortuneMessage message) {}
    public void openBook() {}
    public void openGuide() {}
}
