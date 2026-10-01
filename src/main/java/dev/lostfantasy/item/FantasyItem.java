package dev.lostfantasy.item;

import com.google.common.collect.Multimap;
import dev.lostfantasy.Balance;
import dev.lostfantasy.LostFantasy;
import dev.lostfantasy.ModItems;
import dev.lostfantasy.combat.Combat;
import dev.lostfantasy.core.Rules;
import dev.lostfantasy.core.Spell;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.entity.EntityOfuda;
import dev.lostfantasy.network.EffectMessage;
import dev.lostfantasy.network.Network;
import dev.lostfantasy.world.GapWorld;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public final class FantasyItem extends Item {
    public final String kind;

    public FantasyItem(String kind) {
        this.kind = kind;
        setMaxStackSize(drinkable() ? 16 : 1);
        if (kind.equals("gohei")) setMaxDamage(512);
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> modifiers = super.getAttributeModifiers(slot, stack);
        if (kind.equals("gohei") && slot == EntityEquipmentSlot.MAINHAND) {
            modifiers.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Gohei damage", 5, 0));
            modifiers.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, "Gohei speed", -2.4, 0));
        }
        return modifiers;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        if (kind.equals("gohei")) stack.damageItem(1, attacker);
        return true;
    }

    @Override
    public boolean isFull3D() {
        return kind.equals("gohei") || super.isFull3D();
    }

    private boolean drinkable() {
        return kind.equals("youkai_potion") || kind.equals("suspicious_tea")
                || kind.equals("plasma") || kind.equals("dragon_blood");
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return drinkable() ? EnumAction.DRINK : EnumAction.NONE;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return drinkable() ? 32 : 0;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (drinkable()) {
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        if (kind.equals("grimoire_manuscript") && !player.isSneaking()) {
            if (world.isRemote) LostFantasy.PROXY.openBook();
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        if (kind.equals("fantasy_guide")) {
            if (world.isRemote) LostFantasy.PROXY.openGuide();
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        if (kind.equals("research_copy")) {
            if (world.isRemote) LostFantasy.PROXY.openResearchCopy();
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        if (kind.equals("gohei")) return new ActionResult<>(EnumActionResult.PASS, stack);
        if (world.isRemote) return new ActionResult<>(EnumActionResult.SUCCESS, stack);

        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        PlayerData data = PlayerData.get(player);
        boolean consumed = useItem(serverPlayer, data, hand);
        if (consumed && !player.isCreative()) stack.shrink(1);
        data.dirty = true;
        Network.sync(serverPlayer);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    /** Whether this use should consume one item; some actions keep or replace it. */
    private boolean useItem(EntityPlayerMP player, PlayerData data, EnumHand hand) {
        switch (kind) {
            case "beginner_grimoire":
                return learnGrimoire(player, data, 1);
            case "intermediate_grimoire":
                return learnGrimoire(player, data, 2);
            case "advanced_grimoire":
                return learnGrimoire(player, data, 3);
            case "grimoire_manuscript":
                studyManuscript(player, data);
                return false;
            case "gap_fragment":
                useGapFragment(player, data, hand);
                return false;
            case "gap_key":
                GapWorld.useKey(player);
                return false;
            case "gungnir_study":
                return learnVampireSpell(player, data, Spell.GUNGNIR);
            case "four_study":
                return learnVampireSpell(player, data, Spell.FOUR_OF_A_KIND);
            case "ofuda":
            case "yin_yang_orb":
            case "mini_hakkero":
                castMagicAttack(player);
                return false;
            default:
                message(player, "ingredient");
                return false;
        }
    }

    private static boolean learnGrimoire(EntityPlayer player, PlayerData data, int tier) {
        if(!data.growth.canEnter(dev.lostfantasy.core.Growth.Route.MAGIC)) { message(player,"one_route");return false; }
        if (!Rules.canLearnBook(data.magic(), tier, data.capacity())) {
            message(player, "book_requirements", tier, tier * 2);
            return false;
        }
        data.advanceStage(dev.lostfantasy.core.Growth.Route.MAGIC,tier);
        message(player, "promoted");
        return true;
    }

    private static void studyManuscript(EntityPlayer player, PlayerData data) {
        if (data.magic() < 3) {
            message(player, "require_magic");
            return;
        }
        if (data.magic() == 3) {
            data.advanceStage(dev.lostfantasy.core.Growth.Route.MAGIC,4);
            data.shield = Balance.shieldDurability;
            data.shieldBrokenTicks = 0;
        }
        learnSpell(player, data, Spell.ROYAL_FLARE);
    }

    private static void useGapFragment(EntityPlayer player, PlayerData data, EnumHand hand) {
        if (!(data.youkai() >= 3 && data.spirit() + .0001f >= data.capacity())) {
            message(player, "require_youkai");
            return;
        }
        data.setSpirit(0);
        data.advanceStage(dev.lostfantasy.core.Growth.Route.YOUKAI,4);
        data.learn(Spell.ABANDONED_TRAIN);
        player.setHeldItem(hand, new ItemStack(ModItems.GAP_KEY));
        message(player, "learned", new TextComponentTranslation(Spell.ABANDONED_TRAIN.translationKey()));
    }

    private static boolean learnVampireSpell(EntityPlayer player, PlayerData data, Spell spell) {
        if (data.vampire() < 3) {
            message(player, "require_vampire");
            return false;
        }
        return learnSpell(player, data, spell);
    }

    private static boolean learnSpell(EntityPlayer player, PlayerData data, Spell spell) {
        boolean learned = data.learn(spell);
        message(player, learned ? "learned" : "already_learned", new TextComponentTranslation(spell.translationKey()));
        return learned;
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase user) {
        if (world.isRemote || !(user instanceof EntityPlayerMP)) return stack;
        EntityPlayerMP player = (EntityPlayerMP) user;
        PlayerData data = PlayerData.get(player);
        if((kind.equals("youkai_potion") && !data.growth.canEnter(dev.lostfantasy.core.Growth.Route.YOUKAI))
                || (kind.equals("suspicious_tea") && !data.growth.canEnter(dev.lostfantasy.core.Growth.Route.VAMPIRE))) {
            message(player,"one_route");return stack;
        }
        if (!applyDrink(player, data)) {
            message(player, "drink_requirements");
            return stack;
        }
        message(player, "drink_success");
        data.dirty = true;
        Network.sync(player);
        return player.isCreative() ? stack : consumeDrink(player, stack);
    }

    private boolean applyDrink(EntityPlayerMP player, PlayerData data) {
        switch (kind) {
            case "youkai_potion":
                if(!data.growth.canEnter(dev.lostfantasy.core.Growth.Route.YOUKAI))return false;
                if (data.youkai() != 0) return false;
                data.advanceStage(dev.lostfantasy.core.Growth.Route.YOUKAI,1);
                return true;
            case "suspicious_tea":
                if(!data.growth.canEnter(dev.lostfantasy.core.Growth.Route.VAMPIRE))return false;
                if (data.vampire() != 0) return false;
                data.advanceStage(dev.lostfantasy.core.Growth.Route.VAMPIRE,1);
                data.vampireXp(0);
                return true;
            case "plasma":
                if (data.vampire() <= 0) return false;
                if (data.vampire() == 1 && data.vampireXp() >= Balance.vampireSecond) {
                    data.advanceStage(dev.lostfantasy.core.Growth.Route.VAMPIRE,2);
                } else if (data.vampire() == 2 && data.vampireXp() >= Balance.vampireThird) {
                    data.advanceStage(dev.lostfantasy.core.Growth.Route.VAMPIRE,3);
                }
                player.heal(4);
                return true;
            case "dragon_blood":
                if (data.vampire() != 3) return false;
                data.advanceStage(dev.lostfantasy.core.Growth.Route.VAMPIRE,4);
                player.extinguish();
                return true;
            default:
                return false;
        }
    }

    private static ItemStack consumeDrink(EntityPlayerMP player, ItemStack stack) {
        stack.shrink(1);
        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        if (stack.isEmpty()) return bottle;
        if (!player.inventory.addItemStackToInventory(bottle)) player.dropItem(bottle, false);
        return stack;
    }

    private void castMagicAttack(EntityPlayerMP player) {
        if (player.getCooldownTracker().hasCooldown(this)) return;
        if (kind.equals("ofuda")) {
            castOfuda(player);
            return;
        }

        boolean furnace = kind.equals("mini_hakkero");
        float spiritCost = furnace ? .7f : .4f;
        float damage = furnace ? 10 : 7;
        int range = furnace ? 32 : 20;
        int cooldown = furnace ? 20 : 10;
        EntityLivingBase target = Combat.target(player, range);
        if (target == null) {
            message(player, "no_target");
            return;
        }
        if (!PlayerData.get(player).useSpirit(spiritCost)) {
            message(player, "no_spirit");
            return;
        }
        DamageSource source = new EntityDamageSource("lostfantasy.magic", player).setMagicDamage();
        target.attackEntityFrom(source, damage);
        player.getCooldownTracker().setCooldown(this, cooldown);

        Vec3d origin = player.getPositionEyes(1);
        Vec3d direction = target.getPositionVector().add(0, target.height * .5, 0).subtract(origin);
        EffectMessage effect = new EffectMessage(EffectMessage.ORDINARY_BEAM, player.getEntityId(), player.dimension,
                player.world.getTotalWorldTime(), 10, 0, origin, direction, 0);
        NetworkRegistry.TargetPoint audience = new NetworkRegistry.TargetPoint(
                player.dimension, player.posX, player.posY, player.posZ, 80);
        Network.CHANNEL.sendToAllAround(effect, audience);
    }

    private void castOfuda(EntityPlayerMP player) {
        float spiritCost = .2f;
        PlayerData data = PlayerData.get(player);
        if (!data.useSpirit(spiritCost)) {
            message(player, "no_spirit");
            return;
        }
        Vec3d direction = player.getLookVec();
        Vec3d origin = player.getPositionEyes(1).add(direction.scale(.45));
        if (player.world.spawnEntity(new EntityOfuda(player.world, player, origin, direction, 4, false))) {
            player.getCooldownTracker().setCooldown(this, 10);
        } else {
            data.setSpirit(Math.min(data.capacity(), data.spirit() + spiritCost));
        }
    }

    public static void message(EntityPlayer player, String key, Object... arguments) {
        player.sendStatusMessage(new TextComponentTranslation("message.lostfantasy." + key, arguments), true);
    }
}
