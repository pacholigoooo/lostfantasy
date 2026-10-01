package dev.lostfantasy.network;

import dev.lostfantasy.world.LibraryResearch;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public final class ResearchRequest implements IMessage {
    public static final int OPEN=0, DISCOVER=1, SAMPLE=2, GROWTH=3, COMPLETE=4, COPY=5, REPLAY=6;
    public BlockPos pos=BlockPos.ORIGIN;
    public int action,choice;
    public ResearchRequest() {}
    public ResearchRequest(BlockPos pos,int action,int choice) { this.pos=pos.toImmutable();this.action=action;this.choice=choice; }
    public boolean valid() { return action>=OPEN && action<=REPLAY && choice>=0 && choice<dev.lostfantasy.core.ResearchEvidence.CHOICES; }
    @Override public void toBytes(ByteBuf b) { b.writeLong(pos.toLong());b.writeByte(action);b.writeByte(choice); }
    @Override public void fromBytes(ByteBuf b) { pos=BlockPos.fromLong(b.readLong());action=b.readUnsignedByte();choice=b.readUnsignedByte(); }
    public static final class Handler implements IMessageHandler<ResearchRequest,IMessage> {
        @Override public IMessage onMessage(ResearchRequest message,MessageContext context) {
            if(!message.valid()) return null;
            EntityPlayerMP player=context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(()->LibraryResearch.handle(player,message));
            return null;
        }
    }
}
