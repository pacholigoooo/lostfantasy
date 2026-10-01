package dev.lostfantasy.combat;

import dev.lostfantasy.Balance;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.world.GapWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Keeps retaliation transfers outside the damage callback that requested them. */
public final class SageRetaliation {
    private static final String NEXT_RETALIATION = "lfRetaliationAfter";
    private static final int COOLDOWN_TICKS = 200;
    private final Map<UUID, Attempt> pending = new LinkedHashMap<>();

    public void request(EntityPlayerMP sage, PlayerData data, Entity attacker) {
        if (!(attacker instanceof EntityLivingBase)) return;
        EntityLivingBase target = (EntityLivingBase) attacker;
        if (!canRetaliate(sage, data, target)) return;
        long now = sage.world.getTotalWorldTime();
        if (now < sage.getEntityData().getLong(NEXT_RETALIATION)) return;

        // One attempt per cooldown, even if several hits arrive before this tick ends.
        sage.getEntityData().setLong(NEXT_RETALIATION, now + COOLDOWN_TICKS);
        pending.put(sage.getUniqueID(), new Attempt(sage, target));
    }

    public void finishTick() {
        if (pending.isEmpty()) return;
        // Changing dimension fires more lifecycle events. They must not mutate this iteration.
        ArrayList<Attempt> attempts = new ArrayList<>(pending.values());
        pending.clear();
        for (Attempt attempt : attempts) {
            EntityPlayerMP sage = attempt.sage;
            EntityLivingBase attacker = attempt.attacker;
            if (sage.world != attempt.world || attacker.world != attempt.world
                    || attempt.world.getEntityByID(sage.getEntityId()) != sage
                    || attempt.world.getEntityByID(attacker.getEntityId()) != attacker) continue;
            // The hit may have killed either participant, or the key/team/PvP state may have changed.
            if (canRetaliate(sage, PlayerData.get(sage), attacker)) GapWorld.pull(sage, attacker);
        }
    }

    private static boolean canRetaliate(EntityPlayerMP sage, PlayerData data, EntityLivingBase attacker) {
        return data.youkai() >= 4 && data.sageRetaliation && sage.dimension != Balance.dimensionId
                && GapWorld.hasKey(sage) && Combat.hostile(sage, attacker);
    }

    public void forget(Entity participant) {
        UUID id = participant.getUniqueID();
        pending.values().removeIf(attempt -> id.equals(attempt.sage.getUniqueID())
                || id.equals(attempt.attacker.getUniqueID()));
    }

    public void forgetWorld(World world) {
        pending.values().removeIf(attempt -> attempt.world == world);
    }

    private static final class Attempt {
        final EntityPlayerMP sage;
        final EntityLivingBase attacker;
        final World world;

        Attempt(EntityPlayerMP sage, EntityLivingBase attacker) {
            this.sage = sage;
            this.attacker = attacker;
            world = sage.world;
        }
    }
}
