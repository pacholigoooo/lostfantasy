package dev.lostfantasy.network;
import dev.lostfantasy.LostFantasy;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.*;
public final class StateMessage implements IMessage {
    public NBTTagCompound data;
    public StateMessage() {}
    public StateMessage(NBTTagCompound data) {this.data=data;}
    @Override public void toBytes(ByteBuf buf) {ByteBufUtils.writeTag(buf,data);}
    @Override public void fromBytes(ByteBuf buf) {data=ByteBufUtils.readTag(buf);}
    public static final class Handler implements IMessageHandler<StateMessage,IMessage> {
        @Override public IMessage onMessage(StateMessage m,MessageContext ctx) {if(m.data!=null)LostFantasy.PROXY.receiveState(m.data);return null;}
    }
}
