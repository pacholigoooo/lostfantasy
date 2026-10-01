package dev.lostfantasy.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Spell {
    ROYAL_FLARE(0, "royal_flare", false, 0xFFFF743F, Growth.Route.MAGIC, 3, -1, 0, -1),
    ABANDONED_TRAIN(1, "abandoned_train", false, 0xFFD1A0FF, Growth.Route.YOUKAI, 3, -1, 0, -1),
    GUNGNIR(2, "gungnir", false, 0xFFFF3457, Growth.Route.VAMPIRE, 3, -1, 0, -1),
    FOUR_OF_A_KIND(3, "four_of_a_kind", false, 0xFFCF437A, Growth.Route.VAMPIRE, 3, 0, 0, 0),
    FOURFOLD_BARRIER(4, "fourfold_barrier", false, 0xFFBA8FF0, Growth.Route.YOUKAI, 3, FourfoldBarrier.PROTECTION_END, FourfoldBarrier.DURATION, -1),
    EMERALD_CITY(5, "emerald_city", false, 0xFF56D9AC, Growth.Route.MAGIC, 3, 0, EmeraldCity.WINDUP, EmeraldCity.WINDUP);

    /** Persistent slot and wire ID. Never renumber/reuse these when changing the catalog order. */
    public final int networkId;
    public final String id;
    public final boolean ultimate;
    public final int color;
    public final Growth.Route route;
    public final int requiredStage;
    // 0 disables the feature; -1 lasts for the cast; positive values are exclusive end ticks.
    private final int protectionTicks, movementTicks, poseTicks;
    private static final List<Spell> CATALOG = Collections.unmodifiableList(Arrays.asList(values()));
    private static final Map<Integer, Spell> BY_NUMBER = new HashMap<>();
    private static final Map<String, Spell> BY_NAME = new HashMap<>();
    static {
        for (Spell spell : CATALOG) {
            if (spell.networkId < 0 || BY_NUMBER.put(spell.networkId, spell) != null
                    || BY_NAME.put(spell.id, spell) != null) throw new IllegalStateException("Duplicate/invalid spell ID");
        }
    }

    Spell(int networkId, String id, boolean ultimate, int color, Growth.Route route, int requiredStage, int protectionTicks, int movementTicks, int poseTicks) {
        this.networkId=networkId; this.id=id; this.ultimate=ultimate; this.color=color;
        this.route=route;this.requiredStage=requiredStage;
        this.protectionTicks=protectionTicks;this.movementTicks=movementTicks;this.poseTicks=poseTicks;
    }
    public String translationKey() { return "spell.lostfantasy."+id; }
    public boolean protectsCaster(long age, int duration) {
        return activeUntil(age,duration,protectionTicks);
    }
    public boolean restrictsMovement(long age) { return age>=0 && age<movementTicks; }
    public boolean hasCastingPose() { return poseTicks!=0; }
    public boolean poseActive(double age,int duration) { return activeUntil(age,duration,poseTicks); }
    private static boolean activeUntil(double age,int duration,int end) {
        return age>=0 && age<duration && (end<0 || age<end);
    }
    public static <T> Map<Spell,T> completeRegistry(Map<Spell,T> registry) {
        for(Spell spell:catalog())if(registry.get(spell)==null)throw new IllegalStateException("Missing spell implementation: "+spell.id);
        return Collections.unmodifiableMap(new java.util.EnumMap<>(registry));
    }
    public static Spell byId(int id) { return BY_NUMBER.get(id); }
    public static Spell byName(String id) { return BY_NAME.get(id); }
    public static List<Spell> catalog() { return CATALOG; }
}
