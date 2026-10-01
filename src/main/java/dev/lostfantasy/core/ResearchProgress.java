package dev.lostfantasy.core;

/** Personal evidence for one research file, independent of spells, inventory and ruin loot. */
public final class ResearchProgress {
    public static final int DISCOVERED=1, SAMPLE=2, GROWTH=4, COMPLETED=8;
    private int flags;
    private final Runnable changed;
    public ResearchProgress() { this(() -> {}); }
    public ResearchProgress(Runnable changed) { this.changed=changed; }
    public int flags() { return flags; }
    public boolean has(int evidence) { return (flags&evidence)==evidence; }
    public boolean discover() {
        if(has(DISCOVERED)) return false;
        flags|=DISCOVERED; changed.run();return true;
    }
    public boolean compare(int evidence,int choice) {
        if(!has(DISCOVERED) || !ResearchEvidence.matches(evidence,choice) || has(evidence)) return false;
        flags|=evidence; changed.run();return true;
    }
    public boolean ready() { return has(DISCOVERED|SAMPLE|GROWTH); }
    public boolean complete() {
        if(!ready() || has(COMPLETED)) return false;
        flags|=COMPLETED; changed.run();return true;
    }
    public void restore(int saved) {
        flags=saved>=0 && saved<=15?saved:0;
        if(!has(DISCOVERED)) flags=0;
        if(!ready()) flags&=~COMPLETED;
        changed.run();
    }
}
