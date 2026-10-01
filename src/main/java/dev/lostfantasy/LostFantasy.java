package dev.lostfantasy;

import dev.lostfantasy.data.DataProvider;
import dev.lostfantasy.entity.ModEntities;
import dev.lostfantasy.network.Network;
import dev.lostfantasy.world.GapWorld;
import dev.lostfantasy.world.LibraryStructure;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid=LostFantasy.ID,name="失落幻想 · 幻想符卡",version=LostFantasy.VERSION,acceptedMinecraftVersions="[1.12.2]",dependencies="after:patchouli")
public final class LostFantasy {
    public static final String ID="lostfantasy",VERSION="0.5.6";
    @Mod.Instance(ID) public static LostFantasy INSTANCE;
    @SidedProxy(clientSide="dev.lostfantasy.client.ClientProxy",serverSide="dev.lostfantasy.CommonProxy") public static CommonProxy PROXY;
    @Mod.EventHandler public void preInit(FMLPreInitializationEvent e) {
        Balance.load(e.getSuggestedConfigurationFile());dev.lostfantasy.combat.SpellManager.validateCatalog();DataProvider.register();Network.register();
        ModEntities.register();GapWorld.register();dev.lostfantasy.world.HiganWorld.register();dev.lostfantasy.world.gensokyo.GensokyoWorld.register();LibraryStructure.register();MinecraftForge.EVENT_BUS.register(new GameEvents());PROXY.preInit();
    }
    @Mod.EventHandler public void init(FMLInitializationEvent e) {ModItems.initialize();PROXY.init();}
    @Mod.EventHandler public void serverStarting(FMLServerStartingEvent e) {e.registerServerCommand(new FantasyCommand());}
    @Mod.EventHandler public void stopped(FMLServerStoppedEvent e) {dev.lostfantasy.combat.SpellManager.clear();dev.lostfantasy.combat.EchoCombat.clear();dev.lostfantasy.combat.DamageTransactions.clearDeferred();}
}
