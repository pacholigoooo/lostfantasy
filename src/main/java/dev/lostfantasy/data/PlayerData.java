package dev.lostfantasy.data;

import dev.lostfantasy.Balance;
import dev.lostfantasy.core.Rules;
import dev.lostfantasy.core.Spell;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.INBTSerializable;

public final class PlayerData implements INBTSerializable<NBTTagCompound> {
    public final dev.lostfantasy.core.ResearchProgress research = new dev.lostfantasy.core.ResearchProgress(this::progressChanged);
    private long absorbedXp;
    public final dev.lostfantasy.core.Growth growth=new dev.lostfantasy.core.Growth(this::progressChanged);
    public final dev.lostfantasy.core.RiverJourney journey=new dev.lostfantasy.core.RiverJourney(this::runtimeChanged);
    private float spirit = 2;
    public float shield = Balance.shieldDurability;
    private int power, selected;
    private final Set<Spell> learned = EnumSet.noneOf(Spell.class);
    public int cooldown, echoCooldown, shieldBrokenTicks;
    private int[] slots = {-1, -1, -1, -1};
    public boolean sageRetaliation = true;
    public boolean dirty = true;
    private boolean progressionDirty = true;
    public boolean hasReturn, trappedInGap, flightGranted;
    public int returnDimension;
    public double returnX, returnY, returnZ;
    public float returnYaw, returnPitch;

    private void runtimeChanged() { dirty=true; }
    private void progressChanged() { dirty=true;progressionDirty=true; }
    public float spirit() { return spirit; }
    public int power() { return power; }
    public long absorbedXp() { return absorbedXp; }
    public int selectedSlot() { return selected; }
    public int[] preparedSlots() { return slots.clone(); }
    public int spellInSlot(int slot) { return slot>=0 && slot<slots.length?slots[slot]:-1; }
    public void setSpirit(float value) {
        if(!Float.isFinite(value))return;
        value=Rules.clamp(value,0,capacity());
        if(spirit!=value) {spirit=value;runtimeChanged();}
    }
    public void setPower(int value) {
        value=Rules.clamp(value,0,5);
        if(power!=value) {power=value;runtimeChanged();}
    }
    public void setAbsorbedXp(long value) {
        value=Math.max(0,Math.min(Integer.MAX_VALUE,value));
        if(absorbedXp!=value) {absorbedXp=value;spirit=Math.min(spirit,capacity());progressChanged();}
    }
    public boolean advanceStage(dev.lostfantasy.core.Growth.Route route,int stage) { return growth.advance(route,stage); }
    public void grantStage(dev.lostfantasy.core.Growth.Route route,int stage) { growth.grant(route,stage); }

    public int magic() { return growth.stage(dev.lostfantasy.core.Growth.Route.MAGIC); }

    public int youkai() { return growth.stage(dev.lostfantasy.core.Growth.Route.YOUKAI); }

    public int vampire() { return growth.stage(dev.lostfantasy.core.Growth.Route.VAMPIRE); }

    public long vampireXp() { return growth.progress(dev.lostfantasy.core.Growth.Route.VAMPIRE); }
    public void vampireXp(long value) { growth.progress(dev.lostfantasy.core.Growth.Route.VAMPIRE,value);dirty=true; }

    public long youkaiProgress() { return growth.progress(dev.lostfantasy.core.Growth.Route.YOUKAI); }
    public void youkaiProgress(long value) { growth.progress(dev.lostfantasy.core.Growth.Route.YOUKAI,value);dirty=true; }

    private int synchronizedCapacity = -1;
    private float synchronizedShieldMax = -1;

    public int cooldownFor(Spell spell) {
        return spell == Spell.FOUR_OF_A_KIND ? Math.max(cooldown, echoCooldown) : cooldown;
    }

    public boolean hasFinalStage() {
        return growth.hasFinalStage();
    }

    public int capacity() {
        return synchronizedCapacity >= 2
                ? synchronizedCapacity : Rules.spiritCapacity(absorbedXp, Balance.spiritThresholds);
    }

    public float shieldMaximum() {
        return synchronizedShieldMax > 0 ? synchronizedShieldMax : Balance.shieldDurability;
    }

    public void resetAfterDeath() {
        journey.leave();
        power = 0;
        spirit = 0;
        cooldown = 0;
        echoCooldown = 0;
        trappedInGap = false;
        hasReturn = false;
        dirty = true;
    }

    public boolean finishGapReturn() {
        boolean escapedCaptivity = trappedInGap;
        trappedInGap = false;
        hasReturn = false;
        dirty = true;
        return escapedCaptivity;
    }

    public boolean knows(Spell spell) {
        return spell != null && learned.contains(spell);
    }

    public boolean qualifies(Spell spell) {
        if (spell == null) return false;
        return growth.stage(spell.route) >= spell.requiredStage;
    }

    public boolean learn(Spell spell) {
        return learn(spell, true);
    }

    /** Study requirements are separate from casting eligibility and administrative grants. */
    public boolean canStudy(Spell spell) {
        return qualifies(spell) && (spell!=Spell.EMERALD_CITY
                || research.has(dev.lostfantasy.core.ResearchProgress.COMPLETED));
    }

    public boolean learnFromStudy(Spell spell) {
        return canStudy(spell) && learn(spell,false);
    }

    public boolean learn(Spell spell, boolean autoPrepare) {
        if (spell == null || knows(spell)) return false;
        learned.add(spell);
        progressChanged();
        for (int slotIndex = 0; autoPrepare && slotIndex < slots.length; slotIndex++) {
            if (slots[slotIndex] != -1) continue;
            equipSpell(slotIndex, spell.networkId);
            break;
        }
        dirty = true;
        return true;
    }

    public void selectSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.length) return;
        selected = slotIndex;
        dirty = true;
    }

    public boolean equipSpell(int slotIndex, int spellId) {
        if (slotIndex < 0 || slotIndex >= slots.length) return false;
        if (spellId != -1 && Spell.byId(spellId) == null) return false;

        int[] equipped = slots.clone();
        equipped[slotIndex] = spellId;
        if (!validLoadout(equipped)) return false;
        slots = equipped;
        progressChanged();
        dirty = true;
        return true;
    }

    public boolean validLoadout(int[] equipped) {
        return Rules.validLoadout(equipped, id -> knows(Spell.byId(id)), id -> Spell.byId(id).ultimate);
    }

    public int preparedUltimateCount() {
        int count = 0;
        for (int id : slots) {
            Spell spell = Spell.byId(id);
            if (spell != null && spell.ultimate) count++;
        }
        return count;
    }

    public Spell selectedSpell() {
        return Spell.byId(slots[Rules.clamp(selected, 0, 3)]);
    }

    public boolean useSpirit(float amount) {
        if (amount < 0 || !Float.isFinite(amount) || spirit + .0001f < amount) return false;
        spirit = Math.max(0, spirit - amount);
        dirty = true;
        return true;
    }

    public void absorb(int xp) {
        if (xp <= 0) return;
        absorbedXp = Math.min(Integer.MAX_VALUE, absorbedXp + xp);
        if (vampire() > 0) vampireXp(Math.min(Integer.MAX_VALUE, vampireXp() + xp));
        progressChanged();
    }

    public static PlayerData get(EntityPlayer player) {
        PlayerData data = player.getCapability(DataProvider.CAPABILITY, null);
        if (data == null) throw new IllegalStateException("Lost Fantasy player capability was not attached");
        return data;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("growth",growth.save());
        tag.setTag("riverJourney",journey.save());
        tag.setInteger("unfiledPage", research.flags());
        tag.setLong("xp", absorbedXp);
        tag.setFloat("spirit", spirit);
        tag.setFloat("shield", shield);
        tag.setInteger("power", power);
        NBTTagList learnedSpells = new NBTTagList();
        for (Spell spell : Spell.catalog()) {
            if (knows(spell)) learnedSpells.appendTag(new NBTTagString(spell.id));
        }
        tag.setTag("learnedSpells", learnedSpells);
        tag.setInteger("selected", selected);
        tag.setIntArray("slots", slots.clone());
        tag.setInteger("cooldown", cooldown);
        tag.setInteger("shieldBrokenTicks", shieldBrokenTicks);
        tag.setInteger("echoCooldown", echoCooldown);
        tag.setBoolean("sageRetaliation", sageRetaliation);
        tag.setBoolean("hasReturn", hasReturn);
        tag.setBoolean("trappedInGap", trappedInGap);
        tag.setBoolean("flightGranted", flightGranted);
        tag.setInteger("returnDimension", returnDimension);
        tag.setDouble("returnX", returnX);
        tag.setDouble("returnY", returnY);
        tag.setDouble("returnZ", returnZ);
        tag.setFloat("returnYaw", returnYaw);
        tag.setFloat("returnPitch", returnPitch);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        growth.restore(tag.getCompoundTag("growth"));
        journey.restore(tag.getCompoundTag("riverJourney"));
        research.restore(tag.getInteger("unfiledPage"));
        synchronizedCapacity=-1;synchronizedShieldMax=-1;
        absorbedXp = readProgress(tag, "xp");
        spirit = Rules.clamp(tag.getFloat("spirit"), 0, capacity());
        shield = Rules.clamp(tag.getFloat("shield"), 0, shieldMaximum());
        power = Rules.clamp(tag.getInteger("power"), 0, 5);
        restoreSpells(tag,"player NBT");
        selected = Rules.clamp(tag.getInteger("selected"), 0, 3);
        cooldown = Rules.clamp(tag.getInteger("cooldown"), 0, 2400);
        shieldBrokenTicks = Rules.clamp(tag.getInteger("shieldBrokenTicks"), 0, 12000);
        echoCooldown = Rules.clamp(tag.getInteger("echoCooldown"), 0, 2400);
        sageRetaliation = !tag.hasKey("sageRetaliation") || tag.getBoolean("sageRetaliation");
        hasReturn = tag.getBoolean("hasReturn");
        trappedInGap = tag.getBoolean("trappedInGap");
        flightGranted = tag.getBoolean("flightGranted");
        returnDimension = tag.getInteger("returnDimension");
        returnX = tag.getDouble("returnX");
        returnY = tag.getDouble("returnY");
        returnZ = tag.getDouble("returnZ");
        returnYaw = tag.getFloat("returnYaw");
        returnPitch = tag.getFloat("returnPitch");
        progressChanged();
    }

    public NBTTagCompound createUpdate(boolean full) {
        NBTTagCompound message=new NBTTagCompound(),runtime=new NBTTagCompound();
        runtime.setFloat("spirit",spirit);runtime.setFloat("shield",shield);runtime.setInteger("power",power);
        runtime.setInteger("selected",selected);runtime.setInteger("cooldown",cooldown);
        runtime.setInteger("echoCooldown",echoCooldown);runtime.setInteger("shieldBrokenTicks",shieldBrokenTicks);
        runtime.setBoolean("sageRetaliation",sageRetaliation);runtime.setBoolean("trappedInGap",trappedInGap);
        runtime.setInteger("displayCapacity",capacity());runtime.setFloat("displayShieldMax",shieldMaximum());
        runtime.setTag("journey",journey.displayState());
        message.setTag("runtime",runtime);
        if(full || progressionDirty) {
            NBTTagCompound progress=new NBTTagCompound();
            progress.setTag("growth",growth.save());progress.setInteger("unfiledPage",research.flags());
            progress.setLong("xp",absorbedXp);progress.setIntArray("slots",slots.clone());
            NBTTagList names=new NBTTagList();
            for(Spell spell:Spell.catalog())if(knows(spell))names.appendTag(new NBTTagString(spell.id));
            progress.setTag("learnedSpells",names);message.setTag("progress",progress);
        }
        dirty=false;progressionDirty=false;return message;
    }

    /** A runtime update must never reset progress that it did not carry. */
    public void applyUpdate(NBTTagCompound message) {
        if(message.hasKey("progress",10)) {
            NBTTagCompound progress=message.getCompoundTag("progress");
            growth.restore(progress.getCompoundTag("growth"));research.restore(progress.getInteger("unfiledPage"));
            absorbedXp=readProgress(progress,"xp");restoreSpells(progress,"client progress update");
        }
        NBTTagCompound runtime=message.getCompoundTag("runtime");
        synchronizedCapacity=Rules.clamp(runtime.getInteger("displayCapacity"),2,6);
        synchronizedShieldMax=Rules.clamp(runtime.getFloat("displayShieldMax"),1,100000);
        setSpirit(runtime.getFloat("spirit"));setPower(runtime.getInteger("power"));
        shield=Rules.clamp(runtime.getFloat("shield"),0,shieldMaximum());
        selected=Rules.clamp(runtime.getInteger("selected"),0,Rules.LOADOUT_SIZE-1);
        cooldown=Rules.clamp(runtime.getInteger("cooldown"),0,2400);
        echoCooldown=Rules.clamp(runtime.getInteger("echoCooldown"),0,2400);
        shieldBrokenTicks=Rules.clamp(runtime.getInteger("shieldBrokenTicks"),0,12000);
        sageRetaliation=runtime.getBoolean("sageRetaliation");trappedInGap=runtime.getBoolean("trappedInGap");
        journey.applyDisplayState(runtime.getCompoundTag("journey"));
        dirty=false;progressionDirty=false;
    }

    private void restoreSpells(NBTTagCompound tag,String source) {
        learned.clear();
        NBTTagList names=tag.getTagList("learnedSpells",8);
        java.util.List<String> unknown=null;
        for(int i=0;i<names.tagCount();i++) {
            String name=names.getStringTagAt(i);Spell spell=Spell.byName(name);
            if(spell!=null)learned.add(spell);
            else {
                if(unknown==null)unknown=new java.util.ArrayList<>();
                unknown.add(name);
            }
        }
        if(unknown!=null)org.apache.logging.log4j.LogManager.getLogger("LostFantasy").warn(
                "Ignored unknown learned spell names from {}: {}",source,unknown);
        if(tag.hasKey("learnedSpells") && (!tag.hasKey("learnedSpells",9)
                || ((NBTTagList)tag.getTag("learnedSpells")).tagCount()!=names.tagCount()))
            org.apache.logging.log4j.LogManager.getLogger("LostFantasy").warn(
                    "Invalid learnedSpells tag from {} (expected a string list): {}",source,tag.getTag("learnedSpells"));
        int[] equipped=tag.getIntArray("slots");
        if(validLoadout(equipped))slots=equipped.clone();
        else {
            slots=new int[]{-1,-1,-1,-1};
            if(tag.hasKey("slots"))org.apache.logging.log4j.LogManager.getLogger("LostFantasy").warn(
                    "Cleared invalid spell slots from {}: {}; learned={}. Expected four slots, known learned IDs or -1, no duplicate cards and at most one ultimate",
                    source,tag.getTag("slots"),learned);
        }
    }

    private static long readProgress(NBTTagCompound tag, String key) {
        return Math.min(Integer.MAX_VALUE, Math.max(0, tag.getLong(key)));
    }
}
