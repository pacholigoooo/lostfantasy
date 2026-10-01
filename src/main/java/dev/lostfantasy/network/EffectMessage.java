package dev.lostfantasy.network;
import dev.lostfantasy.LostFantasy;
import dev.lostfantasy.core.Spell;
import dev.lostfantasy.core.EmeraldCity;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.*;
public final class EffectMessage implements IMessage {
    // Spell IDs are nonnegative. Keep ordinary beams and stop messages outside that namespace.
    public static final int STOP = -1, ORDINARY_BEAM = -2;
    public int kind,caster,duration,charge,dimension;
    public long started;
    public double x,y,z,dx,dy,dz;
    public float radius,castYaw;
    public java.util.List<EmeraldCity.Column> columns=java.util.Collections.emptyList();
    public EffectMessage() {}
    public Spell spell() { return Spell.byId(kind); }
    public boolean hasCastingPose() {
        Spell spell = spell();
        return spell != null && spell.hasCastingPose();
    }
    public boolean poseActive(double age) { return spell()!=null && spell().poseActive(age,duration); }
    public boolean retainedAt(int currentDimension, long now) {
        long age=now-started;
        return dimension==currentDimension && duration>0 && age>=-40 && age<duration;
    }
    public EffectMessage(int kind,int caster,int dimension,long started,int duration,int charge,Vec3d origin,Vec3d direction,float radius) {
        this.kind=kind;this.caster=caster;this.dimension=dimension;this.started=started;this.duration=duration;this.charge=charge;
        x=origin.x;y=origin.y;z=origin.z;dx=direction.x;dy=direction.y;dz=direction.z;this.radius=radius;
    }
    @Override public void toBytes(ByteBuf b) {
        b.writeInt(kind);b.writeInt(caster);b.writeInt(dimension);b.writeLong(started);b.writeInt(duration);b.writeInt(charge);
        b.writeDouble(x);b.writeDouble(y);b.writeDouble(z);b.writeDouble(dx);b.writeDouble(dy);b.writeDouble(dz);b.writeFloat(radius);b.writeFloat(castYaw);
        if(spell()==Spell.EMERALD_CITY) {
            if(columns.size()>EmeraldCity.MAX_COLUMNS) throw new IllegalArgumentException("Too many emerald columns");
            b.writeByte(columns.size());
            for(EmeraldCity.Column column:columns) { b.writeDouble(column.x);b.writeDouble(column.y);b.writeDouble(column.z);b.writeDouble(column.height);b.writeByte(column.round); }
        }
    }
    @Override public void fromBytes(ByteBuf b) {
        kind=b.readInt();caster=b.readInt();dimension=b.readInt();started=b.readLong();duration=b.readInt();charge=b.readInt();
        x=b.readDouble();y=b.readDouble();z=b.readDouble();dx=b.readDouble();dy=b.readDouble();dz=b.readDouble();radius=b.readFloat();castYaw=b.readFloat();
        columns=java.util.Collections.emptyList();
        if(spell()==Spell.EMERALD_CITY) {
            int count=b.readUnsignedByte(); if(count>EmeraldCity.MAX_COLUMNS) throw new IllegalArgumentException("Too many emerald columns");
            java.util.List<EmeraldCity.Column> decoded=new java.util.ArrayList<>(count);
            for(int i=0;i<count;i++) decoded.add(new EmeraldCity.Column(b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readUnsignedByte()));
            columns=java.util.Collections.unmodifiableList(decoded);
        }
    }
    public static final class Handler implements IMessageHandler<EffectMessage,IMessage> {
        @Override public IMessage onMessage(EffectMessage m,MessageContext ctx) {LostFantasy.PROXY.receiveEffect(m);return null;}
    }
}
