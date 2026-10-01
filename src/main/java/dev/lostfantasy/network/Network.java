package dev.lostfantasy.network;

import dev.lostfantasy.LostFantasy;
import dev.lostfantasy.data.PlayerData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class Network {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(LostFantasy.ID);

    private Network() {}

    public static void register() {
        CHANNEL.registerMessage(ActionMessage.Handler.class, ActionMessage.class, 0, Side.SERVER);
        CHANNEL.registerMessage(StateMessage.Handler.class, StateMessage.class, 1, Side.CLIENT);
        CHANNEL.registerMessage(EffectMessage.Handler.class, EffectMessage.class, 2, Side.CLIENT);
        CHANNEL.registerMessage(ResearchRequest.Handler.class, ResearchRequest.class, 3, Side.SERVER);
        CHANNEL.registerMessage(ResearchMessage.Handler.class, ResearchMessage.class, 4, Side.CLIENT);
        CHANNEL.registerMessage(FortuneMessage.Handler.class, FortuneMessage.class, 5, Side.CLIENT);
    }

    public static void sync(EntityPlayerMP player) {
        PlayerData data = PlayerData.get(player);
        CHANNEL.sendTo(new StateMessage(data.createUpdate(false)), player);
    }

    public static void syncFull(EntityPlayerMP player) {
        CHANNEL.sendTo(new StateMessage(PlayerData.get(player).createUpdate(true)),player);
    }

    public static void action(int action, int index, int spellId) {
        CHANNEL.sendToServer(new ActionMessage(action, index, spellId));
    }

    public static void research(net.minecraft.util.math.BlockPos pos,int action,int choice) {
        CHANNEL.sendToServer(new ResearchRequest(pos,action,choice));
    }
}
