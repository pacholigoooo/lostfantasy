package dev.lostfantasy.network;

import dev.lostfantasy.SpiritCrafting;
import dev.lostfantasy.combat.SpellManager;
import dev.lostfantasy.data.PlayerData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public final class ActionMessage implements IMessage {
    public static final int SYNC_STATE = 0;
    public static final int SELECT_SLOT = 1;
    public static final int EQUIP_SPELL = 2;
    public static final int CAST_SPELL = 3;
    public static final int TOGGLE_RETALIATION = 4;
    public static final int CRAFT_ITEM = 7;
    public static final int LEARN_BARRIER = 8;
    public static final int LEARN_EMERALD = 9;

    public int action;
    public int index;
    public int spellId;

    public ActionMessage() {}

    public ActionMessage(int action, int index, int spellId) {
        this.action = action;
        this.index = index;
        this.spellId = spellId;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeByte(action);
        buffer.writeInt(index);
        buffer.writeInt(spellId);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        action = buffer.readUnsignedByte();
        index = buffer.readInt();
        spellId = buffer.readInt();
    }

    private static boolean validAction(int action) {
        return action >= SYNC_STATE && action <= TOGGLE_RETALIATION
                || action >= CRAFT_ITEM && action <= LEARN_EMERALD;
    }

    public static final class Handler implements IMessageHandler<ActionMessage, IMessage> {
        @Override
        public IMessage onMessage(ActionMessage message, MessageContext context) {
            if (!validAction(message.action)) return null;
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> handle(player, message));
            return null;
        }

        private static void handle(EntityPlayerMP player, ActionMessage message) {
            if (!player.isEntityAlive() || player.isSpectator() || player.connection == null) return;
            if (message.action == SYNC_STATE && !acceptStateRequest(player)) return;

            PlayerData data = PlayerData.get(player);
            switch (message.action) {
                case SYNC_STATE:
                    break;
                case SELECT_SLOT:
                    data.selectSlot(message.index);
                    break;
                case EQUIP_SPELL:
                    data.equipSpell(message.index, message.spellId);
                    break;
                case CAST_SPELL:
                    SpellManager.cast(player);
                    break;
                case TOGGLE_RETALIATION:
                    data.sageRetaliation = !data.sageRetaliation;
                    break;
                case CRAFT_ITEM:
                    SpiritCrafting.craft(player, message.index);
                    break;
                case LEARN_BARRIER:
                    dev.lostfantasy.world.BarrierStudy.learn(player, message.index, message.spellId);
                    break;
                case LEARN_EMERALD:
                    dev.lostfantasy.world.EmeraldStudy.learn(player, message.index, message.spellId);
                    break;
                default:
                    return;
            }
            if(message.action==SYNC_STATE)Network.syncFull(player);else Network.sync(player);
        }

        private static boolean acceptStateRequest(EntityPlayerMP player) {
            // Coalesce read-only refreshes; queued gameplay actions still execute in arrival order.
            NBTTagCompound entityData = player.getEntityData();
            long tickMarker = player.world.getTotalWorldTime() + 1;
            if (entityData.getLong("lfStateRequestTick") == tickMarker) return false;
            entityData.setLong("lfStateRequestTick", tickMarker);
            return true;
        }
    }
}
