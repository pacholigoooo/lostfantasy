package dev.lostfantasy.network;

import dev.lostfantasy.LostFantasy;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.*;

public final class ResearchMessage implements IMessage {
    public static final int VIEW=0, DEMO=1, CLOSE=2, UPDATE=3;
    public int kind,role,dimension,flags,notice,turns;
    public int catalog=-1,document=-1;
    public long started;
    public boolean hasCopy;
    public BlockPos pos=BlockPos.ORIGIN;
    public ResearchMessage() {}
    @Override public void toBytes(ByteBuf b) {
        b.writeByte(kind);b.writeByte(role);b.writeInt(dimension);b.writeLong(pos.toLong());b.writeLong(started);
        b.writeByte(flags);b.writeByte(notice);b.writeByte(turns);b.writeBoolean(hasCopy);
        b.writeByte(catalog);b.writeByte(document);
    }
    @Override public void fromBytes(ByteBuf b) {
        kind=b.readUnsignedByte();role=b.readUnsignedByte();dimension=b.readInt();pos=BlockPos.fromLong(b.readLong());started=b.readLong();
        flags=b.readUnsignedByte();notice=b.readUnsignedByte();turns=b.readUnsignedByte();hasCopy=b.readBoolean();
        catalog=b.readByte();document=b.readByte();
    }
    public boolean valid() {
        return kind>=VIEW && kind<=UPDATE && role>=0 && role<=1 && flags>=0 && flags<=15 && notice>=0 && notice<=7 && turns>=0 && turns<=3
                && (role==0?catalog==-1 && document==-1:catalog>=0 && catalog<dev.lostfantasy.core.ResearchCatalog.COUNT
                && document>=-1 && document<dev.lostfantasy.core.ResearchEvidence.CHOICES*2);
    }
    public static final class Handler implements IMessageHandler<ResearchMessage,IMessage> {
        @Override public IMessage onMessage(ResearchMessage message,MessageContext context) {
            if(message.valid()) LostFantasy.PROXY.receiveResearch(message);
            return null;
        }
    }
}
