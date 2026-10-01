package dev.lostfantasy;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public final class Balance {
    private Balance() {}
    public static int[] spiritThresholds={100,350,800,1500};
    public static float spiritPerSecond=.06f, powerDamage=.12f, healthPerPower=40;
    public static int youkaiSecond=200, youkaiThird=1200, vampireSecond=300, vampireThird=1000;
    public static float witchChance=.09961f, lootingBonus=.025f;
    public static float flareDamage=2f, flareRadius=50f, trainDamage=100f, spearDamage=50f;
    public static float spellDamageMultiplier=2f;
    public static float barrierDamage=2f, barrierFinalDamage=12f, barrierPullSpeed=.12f;
    public static float emeraldDamage=10f, emeraldLift=.65f;
    public static int flareTicks=160, chargeTicks=40, spellCooldown=100, cloneTicks=1200;
    public static int dimensionId=734, shieldRepairTicks=600;
    public static float shieldDurability=80f, shieldReduction=.15f;
    public static float magicAttackPerTier=.1f, youkaiAttackPerTier=.12f, vampireAttackPerTier=.12f;
    public static float defensePerTier=.025f, youkaiHealthPerTier=4f, vampireHealthPerTier=4f;
    public static float lifeStealPerTier=.025f, nightBonus=.4f, sunDamage=2f;
    public static boolean pvp=false, trainBreakBlocks=false;
    public static int trainBreakLimit=128;
    public static int gensokyoDimensionId=737;
    public static int kasenDimensionId=738;
    public static int boundaryDimensionId=739;
    public static int oldHellDimensionId=740;
    public static int senkaiDimensionId=741;
    public static int netherworldDimensionId=742;
    public static int bloodPoolDimensionId=743;
    public static boolean higanShaderCompatibility=false;
    public static boolean genunkaiClouds=true;
    public static void load(File file) {
        Configuration c=new Configuration(file); c.load();
        // Reserve one point for every later stage so min never exceeds max.
        for(int i=0;i<4;i++) spiritThresholds[i]=c.getInt("capacity"+(i+3)+"XP","progression",spiritThresholds[i],i==0?1:spiritThresholds[i-1]+1,10000000-(3-i),"Cumulative absorbed XP; capacity never decreases.");
        spiritPerSecond=c.getFloat("spiritPerSecond","progression",spiritPerSecond,.001f,20,"Base regeneration in cells per second.");
        powerDamage=c.getFloat("damagePerPower","combat",powerDamage,0,10,"Normal attacks only; spells have fixed base damage.");
        healthPerPower=c.getFloat("healthPerPower","progression",healthPerPower,1,10000,"ceil(max health / value), capped at 5.");
        witchChance=c.getFloat("witchPotionChance","progression",witchChance,0,1,"Base probability, 0.09961 = 9.961%.");
        lootingBonus=c.getFloat("lootingChancePerLevel","progression",lootingBonus,0,1,"Additional drop probability per Looting level.");
        youkaiSecond=c.getInt("youkaiStage2","progression",youkaiSecond,1,9999999,"Kill strength points.");
        youkaiThird=c.getInt("youkaiStage3","progression",youkaiThird,youkaiSecond+1,10000000,"Cumulative kill strength points.");
        vampireSecond=c.getInt("vampireStage2XP","progression",vampireSecond,1,9999999,"XP absorbed since becoming vampire; consume plasma to advance.");
        vampireThird=c.getInt("vampireStage3XP","progression",vampireThird,vampireSecond+1,10000000,"Cumulative vampire XP; consume plasma.");
        flareRadius=c.getFloat("flareRadius","spells",flareRadius,4,50,"Domain radius in blocks. Bounded for server performance.");
        spellDamageMultiplier=c.getFloat("damageMultiplier","spells",2f,0,20,"Multiplier for all spell hits, including Four of a Kind melee and projectiles. Applied once after base damage; ordinary player attacks are unaffected.");
        flareDamage=c.getFloat("flareDamagePerTick","spells",flareDamage,0,1000,"Damage throughout the spherical domain; once per target per server tick.");
        trainDamage=c.getFloat("trainDamage","spells",trainDamage,0,10000,"One impact per target.");
        trainBreakBlocks=c.getBoolean("trainBreakBlocks","spells",false,"Allow train collision to destroy blocks. Default off; respects Forge break cancellation, spawn protection and unbreakable blocks. No harvest drops.");
        trainBreakLimit=c.getInt("trainBreakLimit","spells",trainBreakLimit,1,512,"Maximum block break attempts per cast per tick. Unloaded chunks are never loaded.");
        spearDamage=c.getFloat("gungnirDamage","spells",spearDamage,0,10000,"One piercing impact per target.");
        barrierDamage=c.getFloat("barrierDamage","spells",barrierDamage,0,1000,"Fourfold Barrier: base damage for each of eight pulses; global multiplier applied once.");
        emeraldDamage=c.getFloat("emeraldDamage","spells",emeraldDamage,0,1000,"Emerald City base damage per rising column, at most three hits per target per cast.");
        emeraldLift=c.getFloat("emeraldLift","spells",emeraldLift,0,1.2f,"Upward launch speed before knockback resistance; bosses are never forced to move.");
        barrierFinalDamage=c.getFloat("barrierFinalDamage","spells",barrierFinalDamage,0,10000,"Fourfold Barrier: one final hit at tick 38.");
        barrierPullSpeed=c.getFloat("barrierPullSpeed","spells",barrierPullSpeed,0,.12f,"Maximum added inward speed; respects collision, knockback resistance and boss immunity.");
        chargeTicks=c.getInt("chargeTicks","spells",chargeTicks,10,200,"Royal Flare charge time.");
        flareTicks=c.getInt("flareActiveTicks","spells",flareTicks,20,600,"Royal Flare duration after charge.");
        cloneTicks=c.getInt("cloneLifetimeTicks","spells",1200,40,12000,"Four of a Kind duration in ticks. Default 1200 = 60 seconds; independent of foreground casting.");
        spellCooldown=c.getInt("cooldownTicks","spells",spellCooldown,10,2400,"Cooldown after a cast ends.");
        dimensionId=c.getInt("dimensionId","world",dimensionId,2,32000,"Must be unused by other mods; restart required.");
        gensokyoDimensionId=c.getInt("gensokyoDimensionId","world",gensokyoDimensionId,2,32000,"Gensokyo dimension. Must be unused; restart required.");
        kasenDimensionId=c.getInt("kasenDimensionId","world",kasenDimensionId,2,32000,"Kasen's hermit world dimension. Must be unused; restart required.");
        boundaryDimensionId=c.getInt("boundaryDimensionId","world",boundaryDimensionId,2,32000,"Yakumo residence dimension. Must be unused; restart required.");
        oldHellDimensionId=c.getInt("oldHellDimensionId","world",oldHellDimensionId,2,32000,"Old Hell dimension. Must be unused; restart required.");
        senkaiDimensionId=c.getInt("senkaiDimensionId","world",senkaiDimensionId,2,32000,"Miko's hermit world dimension. Must be unused; restart required.");
        netherworldDimensionId=c.getInt("netherworldDimensionId","world",netherworldDimensionId,2,32000,"Netherworld dimension. Must be unused; restart required.");
        bloodPoolDimensionId=c.getInt("bloodPoolDimensionId","world",bloodPoolDimensionId,2,32000,"Deep Blood Pool cavern. Must be unused; restart required.");
        higanShaderCompatibility=c.getBoolean("higanShaderCompatibility","client",false,"Let an external shader pack handle Higan water and fog. Disables the custom water overlay, ribbons and fog; animated water textures remain. Restart required.");
        genunkaiClouds=c.getBoolean("genunkaiClouds","client",true,"Show the low and high cloud banks around Genunkai. Restart required.");
        shieldRepairTicks=c.getInt("shieldRepairTicks","combat",shieldRepairTicks,20,12000,"Recovery delay after shield breaks.");
        shieldDurability=c.getFloat("shieldDurability","combat",shieldDurability,1,100000,"Absorbed damage before breaking.");
        shieldReduction=c.getFloat("shieldReduction","combat",shieldReduction,0,1,"Added mitigation; total capped at 100%.");
        defensePerTier=c.getFloat("defensePerTier","combat",defensePerTier,0,1,"Each applicable race/profession tier adds reduction.");
        magicAttackPerTier=c.getFloat("magicAttackPerTier","combat",magicAttackPerTier,0,5,"Non-spell magic attacks.");
        youkaiAttackPerTier=c.getFloat("youkaiAttackPerTier","combat",youkaiAttackPerTier,0,5,"Normal physical attacks.");
        vampireAttackPerTier=c.getFloat("vampireAttackPerTier","combat",vampireAttackPerTier,0,5,"Both normal damage types.");
        youkaiHealthPerTier=c.getFloat("youkaiHealthPerTier","combat",youkaiHealthPerTier,0,100,"Extra maximum health.");
        vampireHealthPerTier=c.getFloat("vampireHealthPerTier","combat",vampireHealthPerTier,0,100,"Extra maximum health.");
        lifeStealPerTier=c.getFloat("lifeStealPerTier","combat",lifeStealPerTier,0,.25f,"Heal a fraction of actual normal attack damage.");
        nightBonus=c.getFloat("nightDamageBonus","combat",nightBonus,0,5,"Extra normal attack damage at night for vampires.");
        sunDamage=c.getFloat("sunDamagePerSecond","combat",sunDamage,0,100,"Vampires below the final stage.");
        pvp=c.getBoolean("spellPvp","combat",false,"Allow targeted hostile player effects, respecting teams and server PvP.");
        if(c.hasChanged()) c.save();
    }
}
