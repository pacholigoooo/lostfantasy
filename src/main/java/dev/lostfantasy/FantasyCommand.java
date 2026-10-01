package dev.lostfantasy;
import dev.lostfantasy.core.*;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.network.Network;
import dev.lostfantasy.world.GapWorld;
import dev.lostfantasy.world.ScarletLibrary;
import dev.lostfantasy.world.LibraryRuinLayout;
import dev.lostfantasy.world.gensokyo.GensokyoAtlas;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import java.util.*;
public final class FantasyCommand extends CommandBase {
    @Override public String getName() {return "lostfantasy";}
    @Override public String getUsage(ICommandSender sender) {return "/lostfantasy status | library | yakumo | komachi | return | unlockall（全开） | testkit | sage | archmage | scarletmoon | set <magic|youkai|vampire|power|xp> <value>";}
    @Override public int getRequiredPermissionLevel() {return 0;}
    @Override public void execute(MinecraftServer server,ICommandSender sender,String[] args) throws CommandException {
        EntityPlayerMP p=getCommandSenderAsPlayer(sender);PlayerData d=PlayerData.get(p);
        String action=args.length>0?args[0]:"status";
        if(action.equals("komachi")) {
            if(p.dimension==Balance.gensokyoDimensionId) {
                BlockPos shore=dev.lostfantasy.world.HiganTerrain.landing(dev.lostfantasy.world.HiganTerrain.farSide(p.posX,p.posZ));
                p.sendMessage(new TextComponentString("小町所在的三途河渡口：X "+shore.getX()+"，Y "+shore.getY()+"，Z "+shore.getZ()+"。"));return;
            }
            BlockPos shore=dev.lostfantasy.world.KomachiEncounter.get(server.getWorld(0)).shore();
            p.sendMessage(new TextComponentString(shore==null?"尚未在河岸遇见小町。":"小町所在的主世界河岸：X "+shore.getX()+"，Y "+shore.getY()+"，Z "+shore.getZ()+"。"));return;
        }
        if(action.equals("return")) {if(p.dimension==Balance.dimensionId)GapWorld.useKey(p);return;}
        if(action.equals("yakumo")) {
            GensokyoAtlas estate=GensokyoAtlas.YAKUMO;
            p.sendMessage(new TextComponentString("幻想乡的八云邸院门：X "+estate.x+"，Y "+(estate.y+1)+"，Z "+(estate.z+61)+"。穿过院门进入宅邸。"));
            return;
        }
        if(action.equals("status")) {p.sendMessage(new TextComponentString("灵力 "+String.format(Locale.ROOT,"%.2f",d.spirit())+"/"+d.capacity()+" | P "+d.power()+"/5 | 魔法 "+d.magic()+" 妖怪 "+d.youkai()+" 吸血鬼 "+d.vampire()));return;}
        if(action.equals("library")) {
            LibraryRuinLayout.Site library=ScarletLibrary.site(p.getServerWorld().getSeed());
            p.sendMessage(new TextComponentString("幻想乡红魔馆地下图书馆：X "+library.centerX()+"，Y "+(library.baseY+3)+"，Z "+library.centerZ()+"。从馆内楼梯下行。"));
            return;
        }
        if(!sender.canUseCommand(2,getName()))throw new CommandException("commands.generic.permission");
        if(action.equals("unlockall")||action.equals("全开")) {
            if(args.length!=1)throw new WrongUsageException("/lostfantasy unlockall 或 /lostfantasy 全开");
            d.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,4);d.grantStage(dev.lostfantasy.core.Growth.Route.YOUKAI,4);d.grantStage(dev.lostfantasy.core.Growth.Route.VAMPIRE,4);
            d.setAbsorbedXp(Math.max(d.absorbedXp(),Balance.spiritThresholds[3]));
            d.setSpirit(d.capacity());d.setPower(5);
            d.shield=d.shieldMaximum();d.shieldBrokenTicks=0;
            d.cooldown=d.echoCooldown=0;
            for(Spell spell:Spell.catalog())d.learn(spell);
            d.dirty=true;p.extinguish();
            p.sendMessage(new TextComponentString("职业与种族的三条路线已满级，已学会全部 "+Spell.catalog().size()+" 种符卡。灵力、P和结界已补满，冷却已清除。打开符卡准备，选四张装配。"));
        }else if(action.equals("testkit")) {
            d.setAbsorbedXp(Balance.spiritThresholds[3]);d.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,4);d.grantStage(dev.lostfantasy.core.Growth.Route.YOUKAI,4);d.grantStage(dev.lostfantasy.core.Growth.Route.VAMPIRE,4);d.setPower(5);d.setSpirit(6);
            for(Spell s:Spell.values())d.learn(s);
            for(net.minecraft.item.Item item:ModItems.ALL.values())p.inventory.addItemStackToInventory(new net.minecraft.item.ItemStack(item));
        }else if(action.equals("sage")||action.equals("妖怪贤者")||action.equals("archmage")||action.equals("大魔法使")||action.equals("scarletmoon")||action.equals("鲜红之月")) {
            d.setAbsorbedXp(Math.max(d.absorbedXp(),Balance.spiritThresholds[3]));d.setSpirit(6);d.setPower(5);d.cooldown=0;
            if(action.equals("sage")||action.equals("妖怪贤者")) {
                d.grantStage(dev.lostfantasy.core.Growth.Route.YOUKAI,4);d.learn(Spell.ABANDONED_TRAIN);
                if(!GapWorld.hasKey(p))give(p,ModItems.GAP_KEY);
            }else if(action.equals("archmage")||action.equals("大魔法使")) {
                d.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,4);d.shield=Balance.shieldDurability;d.shieldBrokenTicks=0;d.learn(Spell.ROYAL_FLARE);give(p,ModItems.MANUSCRIPT);
            }else {d.grantStage(dev.lostfantasy.core.Growth.Route.VAMPIRE,4);p.extinguish();d.learn(Spell.GUNGNIR);d.learn(Spell.FOUR_OF_A_KIND);}
            p.sendMessage(new TextComponentString("最终阶段已解锁；双击跳跃进入飞行。"));
        }else if(action.equals("set")&&args.length==3) {
            int value=parseInt(args[2],0,Integer.MAX_VALUE);
            switch(args[1]) {case "magic":d.grantStage(dev.lostfantasy.core.Growth.Route.MAGIC,Rules.clamp(value,0,4));break;case "youkai":d.grantStage(dev.lostfantasy.core.Growth.Route.YOUKAI,Rules.clamp(value,0,4));break;case "vampire":d.grantStage(dev.lostfantasy.core.Growth.Route.VAMPIRE,Rules.clamp(value,0,4));break;case "power":d.setPower(Rules.clamp(value,0,5));break;case "xp":d.setAbsorbedXp(Math.max(d.absorbedXp(),value));break;default:throw new WrongUsageException(getUsage(sender));}
        }else throw new WrongUsageException(getUsage(sender));FlightController.update(p);Network.sync(p);
    }
    private static void give(EntityPlayerMP p,net.minecraft.item.Item item) {net.minecraft.item.ItemStack stack=new net.minecraft.item.ItemStack(item);if(!p.inventory.addItemStackToInventory(stack))p.dropItem(stack,false);}
    @Override public List<String> getTabCompletions(MinecraftServer s,ICommandSender sender,String[] a,BlockPos p) {return a.length==1?getListOfStringsMatchingLastWord(a,"status","library","yakumo","komachi","return","unlockall","全开","testkit","set","sage","archmage","scarletmoon"):a.length==2&&a[0].equals("set")?getListOfStringsMatchingLastWord(a,"magic","youkai","vampire","power","xp"):Collections.emptyList();}
}
