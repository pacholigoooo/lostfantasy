package dev.lostfantasy;

import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.item.FantasyItem;
import java.util.function.Supplier;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class SpiritCrafting {
    // Indices are the existing UI/network recipe IDs. Resolve items after registration.
    private static final Recipe[] RECIPES = {
        new Recipe(() -> ModItems.OFUDA, 1, () -> new ItemStack[]{
            new ItemStack(Items.PAPER, 4), new ItemStack(Items.REDSTONE, 2)
        }),
        new Recipe(() -> ModItems.GOHEI, 2, () -> new ItemStack[]{
            new ItemStack(Items.STICK, 2), new ItemStack(Items.PAPER, 4)
        }),
        new Recipe(() -> ModItems.YIN_YANG, 3, () -> new ItemStack[]{
            new ItemStack(Items.QUARTZ, 4), new ItemStack(Items.ENDER_PEARL)
        }),
        new Recipe(() -> ModItems.FURNACE, 4, () -> new ItemStack[]{
            new ItemStack(Items.IRON_INGOT, 4), new ItemStack(Items.BLAZE_POWDER, 2),
            new ItemStack(Items.REDSTONE, 4)
        })
    };

    private SpiritCrafting() {}

    public static int count() { return RECIPES.length; }

    private static Recipe recipe(int recipeId) {
        return recipeId >= 0 && recipeId < RECIPES.length ? RECIPES[recipeId] : null;
    }

    public static Item result(int recipeId) {
        Recipe recipe = recipe(recipeId);
        return recipe == null ? null : recipe.output.get();
    }

    public static ItemStack[] ingredients(int recipeId) {
        Recipe recipe = recipe(recipeId);
        return recipe == null ? new ItemStack[0] : recipe.ingredients.get();
    }

    public static float spiritCost(int recipeId) {
        Recipe recipe = recipe(recipeId);
        if (recipe == null) throw new IllegalArgumentException("Unknown spirit recipe: " + recipeId);
        return recipe.spiritCost;
    }

    public static boolean has(EntityPlayerMP player, ItemStack required) {
        int available = 0;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (ItemStack.areItemsEqual(stack, required)) available += stack.getCount();
        }
        return available >= required.getCount();
    }

    public static void consume(EntityPlayerMP player, ItemStack required) {
        int remaining = required.getCount();
        for (ItemStack stack : player.inventory.mainInventory) {
            if (!ItemStack.areItemsEqual(stack, required)) continue;
            int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            remaining -= taken;
            if (remaining == 0) return;
        }
    }

    public static void craft(EntityPlayerMP player, int recipeId) {
        Recipe recipe = recipe(recipeId);
        if (recipe == null) return;

        ItemStack[] ingredients = recipe.ingredients.get();
        for (ItemStack ingredient : ingredients) {
            if (!has(player, ingredient)) {
                FantasyItem.message(player, "craft_materials");
                return;
            }
        }
        if (!PlayerData.get(player).useSpirit(recipe.spiritCost)) {
            FantasyItem.message(player, "no_spirit");
            return;
        }

        for (ItemStack ingredient : ingredients) consume(player, ingredient);
        ItemStack output = new ItemStack(recipe.output.get());
        if (!player.inventory.addItemStackToInventory(output)) player.dropItem(output, false);
        player.inventory.markDirty();
        FantasyItem.message(player, "crafted");
    }

    private static final class Recipe {
        final Supplier<Item> output;
        final float spiritCost;
        final Supplier<ItemStack[]> ingredients;

        Recipe(Supplier<Item> output, float spiritCost, Supplier<ItemStack[]> ingredients) {
            this.output = output;
            this.spiritCost = spiritCost;
            this.ingredients = ingredients;
        }
    }
}
