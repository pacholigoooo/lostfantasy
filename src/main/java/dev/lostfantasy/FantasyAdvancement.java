package dev.lostfantasy;

import dev.lostfantasy.core.Spell;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.world.LibrarySites;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

public enum FantasyAdvancement {
    LIBRARY("library"),
    UNFILED_PAGE("unfiled_page"),
    YAKUMO("yakumo"),
    FINAL_STAGE("final_stage"),
    ROYAL_FLARE("royal_flare"),
    ABANDONED_TRAIN("abandoned_train"),
    GUNGNIR("gungnir"),
    FOUR_OF_A_KIND("four_of_a_kind"),
    FOURFOLD_BARRIER("fourfold_barrier"),
    EMERALD_CITY("emerald_city"),
    GAP_CAPTIVE("gap_captive"),
    GAP_ESCAPE("gap_escape");

    public final ResourceLocation id;

    FantasyAdvancement(String path) { id = new ResourceLocation(LostFantasy.ID, path); }

    private Advancement find(EntityPlayerMP player) {
        // Resolve each time so /reload and world advancement overrides remain effective.
        return player.getServerWorld().getAdvancementManager().getAdvancement(id);
    }

    private boolean pending(EntityPlayerMP player) {
        Advancement advancement = find(player);
        return advancement != null && !player.getAdvancements().getProgress(advancement).isDone();
    }

    public void grant(EntityPlayerMP player) {
        if (player.isSpectator()) return;
        Advancement advancement = find(player);
        if (advancement != null) player.getAdvancements().grantCriterion(advancement, "event");
    }

    public static void cast(EntityPlayerMP player, Spell spell) {
        switch (spell) {
            case ROYAL_FLARE: ROYAL_FLARE.grant(player); break;
            case ABANDONED_TRAIN: ABANDONED_TRAIN.grant(player); break;
            case GUNGNIR: GUNGNIR.grant(player); break;
            case FOUR_OF_A_KIND: FOUR_OF_A_KIND.grant(player); break;
            case FOURFOLD_BARRIER: FOURFOLD_BARRIER.grant(player); break;
            case EMERALD_CITY: EMERALD_CITY.grant(player); break;
        }
    }

    static void checkPlayer(EntityPlayerMP player, PlayerData data) {
        if (player.isSpectator()) return;
        if (data.hasFinalStage() && FINAL_STAGE.pending(player)) FINAL_STAGE.grant(player);
        if (LibrarySites.dimension(player.dimension) && LIBRARY.pending(player)
                && LibrarySites.generated(player.getServerWorld(), player.getPosition())!=null) {
            LIBRARY.grant(player);
        }
    }
}
