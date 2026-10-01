package dev.lostfantasy.core;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.nbt.NBTTagCompound;

/** Profession and species are independent collections; being human occupies neither. */
public final class Growth {
    public enum Kind { PROFESSION, RACE }
    public enum Route {
        MAGIC(Kind.PROFESSION, "魔法", "", "无职业", "见习魔法师", "普通的魔法使", "魔法使", "大魔法使"),
        YOUKAI(Kind.RACE, "妖怪", "晋升进度", "未化妖", "草根妖怪", "化妖", "大妖怪", "妖怪贤者"),
        VAMPIRE(Kind.RACE, "吸血鬼", "已吸收经验", "非吸血鬼", "小恶魔", "夜行者", "梦魇", "鲜红之月");
        public final Kind kind;
        public final String label, progressLabel;
        private final String[] stages;
        Route(Kind kind, String label, String progressLabel, String... stages) {
            this.kind=kind;this.label=label;this.progressLabel=progressLabel;this.stages=stages;
        }
        public int finalStage() { return stages.length-1; }
        public String stageName(int stage) { return stages[Rules.clamp(stage,0,finalStage())]; }
    }
    private final EnumMap<Route,Integer> professions=new EnumMap<>(Route.class);
    private final EnumMap<Route,Integer> races=new EnumMap<>(Route.class);
    private final EnumMap<Route,Long> progress=new EnumMap<>(Route.class);
    private boolean reincarnated;
    private final Runnable changed;
    public Growth() { this(() -> {}); }
    public Growth(Runnable changed) { this.changed=changed; }
    public boolean reincarnated() { return reincarnated; }
    public boolean hasFinalStage() {
        for(Route route:Route.values()) if(stage(route)>=route.finalStage())return true;
        return false;
    }
    private EnumMap<Route,Integer> routes(Route route) { return route.kind==Kind.PROFESSION?professions:races; }
    public Map<Route,Integer> professions() { return Collections.unmodifiableMap(professions); }
    public Map<Route,Integer> races() { return Collections.unmodifiableMap(races); }
    public int stage(Route route) { return routes(route).getOrDefault(route,0); }
    public long progress(Route route) { return progress.getOrDefault(route,0L); }
    public int count() { return professions.size()+races.size(); }
    public boolean canEnter(Route route) { return stage(route)>0 || reincarnated || count()==0; }
    public boolean advance(Route route,int stage) {
        if(stage<1 || stage>route.finalStage() || stage<=stage(route) || !canEnter(route))return false;
        grant(route,stage);return true;
    }
    /** Explicit administrative grants and NBT restoration bypass route admission. */
    public void grant(Route route,int stage) {
        stage=Rules.clamp(stage,0,route.finalStage());
        if(stage==stage(route) && (stage!=0 || progress(route)==0))return;
        if(stage==0) { routes(route).remove(route);progress.remove(route); }
        else routes(route).put(route,stage);
        changed.run();
    }
    public void progress(Route route,long value) {
        value=Math.max(0,Math.min(Integer.MAX_VALUE,value));
        if(value!=progress(route)) {progress.put(route,value);changed.run();}
    }
    public void reincarnate(boolean preserve) {
        if(!preserve) {professions.clear();races.clear();}
        progress.keySet().removeIf(route->stage(route)==0);
        reincarnated=true;changed.run();
    }
    public NBTTagCompound save() {
        NBTTagCompound tag=new NBTTagCompound(),jobs=new NBTTagCompound(),species=new NBTTagCompound(),xp=new NBTTagCompound();
        for(Route route:Route.values()) {
            (route.kind==Kind.PROFESSION?jobs:species).setInteger(route.name(),stage(route));
            xp.setLong(route.name(),progress(route));
        }
        tag.setTag("professions",jobs);tag.setTag("races",species);tag.setTag("progress",xp);
        tag.setBoolean("reincarnated",reincarnated);return tag;
    }
    public void restore(NBTTagCompound tag) {
        professions.clear();races.clear();progress.clear();
        for(Route route:Route.values()) {
            grant(route,tag.getCompoundTag(route.kind==Kind.PROFESSION?"professions":"races").getInteger(route.name()));
            if(stage(route)>0)progress(route,tag.getCompoundTag("progress").getLong(route.name()));
        }
        reincarnated=tag.getBoolean("reincarnated");changed.run();
    }
}
