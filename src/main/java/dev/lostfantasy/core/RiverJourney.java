package dev.lostfantasy.core;

import net.minecraft.nbt.NBTTagCompound;

/** Player-owned voyage clock. Only time aboard the player's ferry advances it. */
public final class RiverJourney {
    public static final int DURATION=3000;
    public enum Phase { NONE, SHORE, SAILING }
    private Phase phase=Phase.NONE;
    private boolean fortuneSeparated,fromFarBank,suspendedFlight;
    private int elapsed;
    private final Runnable changed;
    public RiverJourney() {this(()->{});}
    public RiverJourney(Runnable changed) {this.changed=changed;}
    public Phase phase() {return phase;}
    public int elapsed() {return elapsed;}
    public boolean fortuneSeparated() {return fortuneSeparated;}
    public boolean fromFarBank() {return fromFarBank;}
    public boolean suspendedFlight() {return suspendedFlight;}
    public void suspendedFlight(boolean value) {if(suspendedFlight!=value){suspendedFlight=value;changed.run();}}
    public void arrive() {if(phase==Phase.NONE){phase=Phase.SHORE;changed.run();}}
    public boolean separateFortune() {
        if(phase!=Phase.NONE || fortuneSeparated)return false;
        fortuneSeparated=true;changed.run();return true;
    }
    public boolean board(boolean fromFarBank) {
        if(phase!=Phase.SHORE)return false;
        this.fromFarBank=fromFarBank;phase=Phase.SAILING;elapsed=0;changed.run();return true;
    }
    public boolean advance(boolean aboard) {
        if(phase!=Phase.SAILING || !aboard)return false;
        elapsed=Math.min(DURATION,elapsed+1);changed.run();return ready();
    }
    public boolean ready() {return phase==Phase.SAILING && elapsed==DURATION;}
    public boolean finishCrossing() {
        if(!ready())return false;
        phase=Phase.SHORE;elapsed=0;changed.run();return true;
    }
    public void leave() {phase=Phase.NONE;elapsed=0;changed.run();}
    public NBTTagCompound displayState() {
        NBTTagCompound n=new NBTTagCompound();n.setString("phase",phase.name());
        n.setInteger("elapsed",elapsed);n.setBoolean("fortuneSeparated",fortuneSeparated);
        n.setBoolean("fromFarBank",fromFarBank);return n;
    }
    public void applyDisplayState(NBTTagCompound n) {
        if(n.isEmpty())return;
        Phase next;
        try {next=Phase.valueOf(n.getString("phase"));}
        catch(IllegalArgumentException ex) {
            org.apache.logging.log4j.LogManager.getLogger("LostFantasy").warn("Invalid river phase: {}",n.getTag("phase"));
            return;
        }
        phase=next;elapsed=Rules.clamp(n.getInteger("elapsed"),0,DURATION);
        fortuneSeparated=n.getBoolean("fortuneSeparated");fromFarBank=n.getBoolean("fromFarBank");
    }
    public NBTTagCompound save() {
        NBTTagCompound n=displayState();n.setBoolean("suspendedFlight",suspendedFlight);return n;
    }
    public void restore(NBTTagCompound n) {
        phase=Phase.NONE;elapsed=Rules.clamp(n.getInteger("elapsed"),0,DURATION);
        fortuneSeparated=n.getBoolean("fortuneSeparated");fromFarBank=n.getBoolean("fromFarBank");
        applyDisplayState(n);suspendedFlight=n.getBoolean("suspendedFlight");changed.run();
    }
}
