package dev.lostfantasy.world;

import dev.lostfantasy.*;
import dev.lostfantasy.combat.SpellManager;
import dev.lostfantasy.core.ResearchProgress;
import dev.lostfantasy.core.ResearchCatalog;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.network.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/** Adds interactions to existing furniture. No world blocks, tile entities or loot are written. */
@Mod.EventBusSubscriber(modid=LostFantasy.ID)
public final class LibraryResearch {
    public static final int TABLE=0, CATALOG=1;
    private LibraryResearch() {}

    static int roleAt(LibraryRuinLayout.Site site,BlockPos pos) {
        int[] local=LibraryRuinLayout.toLocal(pos.getX()-site.centerX(),pos.getZ()-site.centerZ(),site.turns);
        int y=pos.getY()-site.baseY;
        if(local[0]==-2 && local[1]==0 && y==5) return TABLE;
        return ResearchCatalog.indexAt(local[0],y,local[1])>=0?CATALOG:-1;
    }
    static int catalogAt(LibraryRuinLayout.Site site,BlockPos pos) {
        int[] local=LibraryRuinLayout.toLocal(pos.getX()-site.centerX(),pos.getZ()-site.centerZ(),site.turns);
        return ResearchCatalog.indexAt(local[0],pos.getY()-site.baseY,local[1]);
    }
    static boolean holds(LibraryRuinLayout.Site site,BlockPos pos,ResearchRequest request) {
        int document=ResearchCatalog.document(site.structureSeed,catalogAt(site,pos));
        int evidence=request.action==ResearchRequest.SAMPLE?ResearchProgress.SAMPLE
                :request.action==ResearchRequest.GROWTH?ResearchProgress.GROWTH:0;
        return evidence!=0 && ResearchCatalog.evidence(document)==evidence && ResearchCatalog.choice(document)==request.choice;
    }
    static boolean intact(World world,LibraryRuinLayout.Site site,BlockPos pos,int role) {
        if(role<0 || role!=roleAt(site,pos) || !world.isBlockLoaded(pos) || !world.isBlockLoaded(pos.down())) return false;
        IBlockState state=world.getBlockState(pos);
        if(role==TABLE) return state.getBlock()==ModBlocks.EMERALD_STUDY
                && world.getBlockState(pos.down())==Blocks.STONE_SLAB.getStateFromMeta(15);
        if(state.getBlock()!=ModBlocks.ARCHIVE_CATALOG) return false;
        int[] local=LibraryRuinLayout.toLocal(pos.getX()-site.centerX(),pos.getZ()-site.centerZ(),site.turns);
        IBlockState support=LibrarySites.planned(world,site,local[0],pos.getY()-site.baseY-1,local[1]);
        return support!=null && world.getBlockState(pos.down()).equals(support.withRotation(LibraryStructure.rotation(site.turns)));
    }
    static boolean permits(int role,int action) {
        return role==TABLE?(action==ResearchRequest.OPEN || action==ResearchRequest.DISCOVER || action>=ResearchRequest.COMPLETE && action<=ResearchRequest.REPLAY)
                : role==CATALOG && (action==ResearchRequest.OPEN || action==ResearchRequest.SAMPLE || action==ResearchRequest.GROWTH);
    }
    @SubscribeEvent public static void interact(PlayerInteractEvent.RightClickBlock event) {
        if(!(event.getEntityPlayer() instanceof EntityPlayerMP) || event.getHand()!=EnumHand.MAIN_HAND || event.getEntityPlayer().isSneaking()) return;
        IBlockState state=event.getWorld().getBlockState(event.getPos());
        // The emerald study page keeps its own screen; its added button opens this investigation.
        if(state.getBlock()!=ModBlocks.ARCHIVE_CATALOG) return;
        if(handle((EntityPlayerMP)event.getEntityPlayer(),new ResearchRequest(event.getPos(),ResearchRequest.OPEN,0))) {
            event.setCanceled(true);event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }
    public static boolean handle(EntityPlayerMP player,ResearchRequest request) {
        if(!request.valid() || !player.isEntityAlive() || player.isSpectator() || player.connection==null
                || !LibrarySites.dimension(player.dimension) || SpellManager.active(player) || SpellManager.locked(player)
                || player.getDistanceSqToCenter(request.pos)>36) return false;
        WorldServer world=player.getServerWorld();
        if(!world.isBlockLoaded(request.pos)) return false;
        LibraryRuinLayout.Site site=LibrarySites.generated(world,request.pos);
        if(site==null) return false;
        int role=roleAt(site,request.pos);
        if(!intact(world,site,request.pos,role) || !permits(role,request.action)) return false;
        if((request.action==ResearchRequest.SAMPLE || request.action==ResearchRequest.GROWTH) && !holds(site,request.pos,request))return false;
        Vec3d eye=player.getPositionEyes(1),point=new Vec3d(request.pos).add(.5,role==TABLE?.15:.5,.5);
        if(!world.isAreaLoaded(new BlockPos(Math.min(eye.x,point.x),Math.min(eye.y,point.y),Math.min(eye.z,point.z)),
                new BlockPos(Math.max(eye.x,point.x),Math.max(eye.y,point.y),Math.max(eye.z,point.z)))) return false;
        RayTraceResult hit=world.rayTraceBlocks(eye,point,false,true,false);
        if(hit!=null && !request.pos.equals(hit.getBlockPos())) return false;
        long now=world.getTotalWorldTime();
        long previous=player.getEntityData().getLong("lfResearchRequest");
        if(previous>0 && now+1>=previous && now+1-previous<4) return true;
        player.getEntityData().setLong("lfResearchRequest",now+1);
        PlayerData data=PlayerData.get(player);
        ResearchProgress progress=data.research;
        int notice=0; boolean close=false;
        switch(request.action) {
            case ResearchRequest.DISCOVER: if(progress.discover()) notice=1; break;
            case ResearchRequest.SAMPLE:
            case ResearchRequest.GROWTH:
                if(!progress.has(ResearchProgress.DISCOVERED)) notice=3;
                else if(!dev.lostfantasy.core.ResearchEvidence.matches(request.action==ResearchRequest.SAMPLE?ResearchProgress.SAMPLE:ResearchProgress.GROWTH,request.choice)) notice=2;
                else if(progress.compare(request.action==ResearchRequest.SAMPLE?ResearchProgress.SAMPLE:ResearchProgress.GROWTH,request.choice)) notice=4;
                break;
            case ResearchRequest.COMPLETE:
                if(!progress.ready()) notice=3;
                else if(progress.complete()) {
                    notice=giveCopy(player); close=true;
                    FantasyAdvancement.UNFILED_PAGE.grant(player);
                    demo(player,request.pos,now);
                    player.sendMessage(new TextComponentTranslation("message.lostfantasy.research_complete"));
                }
                break;
            case ResearchRequest.COPY:
                if(progress.has(ResearchProgress.COMPLETED)) notice=giveCopy(player);
                else notice=3;
                break;
            case ResearchRequest.REPLAY:
                if(progress.has(ResearchProgress.COMPLETED)) { demo(player,request.pos,now);close=true; }
                else notice=3;
                break;
            default: break;
        }
        data.dirty=true;
        Network.sync(player);
        ResearchMessage message=new ResearchMessage();
        message.kind=close?ResearchMessage.CLOSE:request.action==ResearchRequest.OPEN?ResearchMessage.VIEW:ResearchMessage.UPDATE; message.role=role;message.pos=request.pos;
        message.dimension=player.dimension;message.started=now;message.flags=progress.flags();message.notice=notice;
        message.turns=site.turns;message.hasCopy=hasCopy(player);
        if(role==CATALOG) {message.catalog=catalogAt(site,request.pos);message.document=ResearchCatalog.document(site.structureSeed,message.catalog);}
        Network.CHANNEL.sendTo(message,player);
        if(close && notice==6) player.sendMessage(new TextComponentTranslation("message.lostfantasy.research_full"));
        return true;
    }
    public static boolean hasCopy(EntityPlayer player) {
        for(int slot=0;slot<player.inventory.getSizeInventory();slot++)
            if(player.inventory.getStackInSlot(slot).getItem()==ModItems.RESEARCH_COPY) return true;
        return player.inventory.getItemStack().getItem()==ModItems.RESEARCH_COPY;
    }
    static int giveCopy(EntityPlayer player) {
        if(hasCopy(player)) return 7;
        if(player.inventory.getFirstEmptyStack()<0) return 6;
        ItemStack copy=new ItemStack(ModItems.RESEARCH_COPY);
        if(!player.inventory.addItemStackToInventory(copy)) return 6;
        player.inventory.markDirty();player.inventoryContainer.detectAndSendChanges();
        return 5;
    }
    private static void demo(EntityPlayerMP player,BlockPos pos,long now) {
        ResearchMessage message=new ResearchMessage();message.kind=ResearchMessage.DEMO;message.pos=pos;
        message.dimension=player.dimension;message.started=now;
        Network.CHANNEL.sendToAllAround(message,new NetworkRegistry.TargetPoint(player.dimension,pos.getX()+.5,pos.getY(),pos.getZ()+.5,32));
    }
}
