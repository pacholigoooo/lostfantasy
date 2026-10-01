package dev.lostfantasy.network;

import dev.lostfantasy.LostFantasy;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.fml.common.network.simpleimpl.*;

/** A committed separation, sent to the user and nearby players. Never grants player state. */
public final class FortuneMessage implements IMessage {
    public UUID playerId=new UUID(0,0);
    public UUID ribbonId=new UUID(0,0);
    public int dimension,handSide;
    public long started;
    public FortuneMessage() {}
    public FortuneMessage(EntityPlayerMP player,EnumHand hand,UUID ribbonId) {
        this.ribbonId=ribbonId;
        playerId=player.getUniqueID();dimension=player.dimension;started=player.world.getTotalWorldTime();
        boolean right=player.getPrimaryHand()==EnumHandSide.RIGHT;
        handSide=(hand==EnumHand.MAIN_HAND?right:!right)?1:-1;
    }
    public boolean valid(){return (handSide==1 || handSide==-1) && started>=0;}
    @Override public void toBytes(ByteBuf b) {
        b.writeLong(playerId.getMostSignificantBits());b.writeLong(playerId.getLeastSignificantBits());
        b.writeLong(ribbonId.getMostSignificantBits());b.writeLong(ribbonId.getLeastSignificantBits());
        b.writeInt(dimension);b.writeLong(started);b.writeByte(handSide);
    }
    @Override public void fromBytes(ByteBuf b) {
        playerId=new UUID(b.readLong(),b.readLong());ribbonId=new UUID(b.readLong(),b.readLong());dimension=b.readInt();started=b.readLong();handSide=b.readByte();
    }
    public static final class Handler implements IMessageHandler<FortuneMessage,IMessage> {
        @Override public IMessage onMessage(FortuneMessage message,MessageContext context) {
            if(message.valid())LostFantasy.PROXY.receiveFortune(message);
            return null;
        }
    }
}
