package dev.lostfantasy.client;
import dev.lostfantasy.*;
import dev.lostfantasy.data.PlayerData;
import dev.lostfantasy.entity.*;
import dev.lostfantasy.network.EffectMessage;
import dev.lostfantasy.network.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
public final class ClientProxy extends CommonProxy {
    public static PlayerData state=new PlayerData();
    @Override public void openResearchCopy() { Minecraft.getMinecraft().displayGuiScreen(new ResearchScreen()); }
    @Override public void receiveResearch(dev.lostfantasy.network.ResearchMessage message) {
        Minecraft mc=Minecraft.getMinecraft();
        mc.addScheduledTask(()->{
            if(mc.world==null || mc.player==null || mc.player.dimension!=message.dimension) return;
            if(message.kind==dev.lostfantasy.network.ResearchMessage.DEMO) { ResearchVisual.receive(message);return; }
            if(!mc.player.isEntityAlive() || mc.player.getDistanceSqToCenter(message.pos)>36) return;
            ResearchScreen current=mc.currentScreen instanceof ResearchScreen?(ResearchScreen)mc.currentScreen:null;
            if(message.kind==dev.lostfantasy.network.ResearchMessage.CLOSE) {
                if(current!=null && current.matches(message))mc.displayGuiScreen(null);
            } else if(message.kind==dev.lostfantasy.network.ResearchMessage.VIEW) mc.displayGuiScreen(new ResearchScreen(message));
            else if(current!=null && current.matches(message))current.receive(message);
        });
    }
    @Override public boolean restrictsMovement(net.minecraft.entity.player.EntityPlayer player) {
        EffectMessage effect = SpellVisuals.poseEffect(player);
        return effect != null && player.isEntityAlive() && effect.spell().restrictsMovement(player.world.getTotalWorldTime()-effect.started);
    }
    @Override public void openBarrierStudy(net.minecraft.util.math.BlockPos pos) {
        Network.action(dev.lostfantasy.network.ActionMessage.SYNC_STATE, 0, 0);
        Minecraft.getMinecraft().displayGuiScreen(new BarrierStudyScreen(pos));
    }
    @Override public void openEmeraldStudy(net.minecraft.util.math.BlockPos pos) {
        Network.action(dev.lostfantasy.network.ActionMessage.SYNC_STATE,0,0);
        Minecraft.getMinecraft().displayGuiScreen(new BarrierStudyScreen(pos,dev.lostfantasy.core.Spell.EMERALD_CITY));
    }
    @Override public void preInit() {
        RenderingRegistry.registerEntityRenderingHandler(EntityKomachi.class,RenderKomachi::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityHouseSpirit.class,RenderHouseSpirit::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityHisoutensoku.class,RenderHisoutensoku::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityCablecar.class,RenderCablecar::new);
        MinecraftForge.EVENT_BUS.register(new HiganAtmosphere());
        RenderingRegistry.registerEntityRenderingHandler(EntityRiverFerry.class,RenderRiverFerry::new);
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
        RenderingRegistry.registerEntityRenderingHandler(EntityPowerOrb.class,RenderPower::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityCompanion.class,RenderEcho::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityOfuda.class,RenderOfuda::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGapRift.class,RenderGapRift::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityEchoArrow.class,net.minecraft.client.renderer.entity.RenderTippedArrow::new);
    }
    @Override public void init() {
        ShaderBridge.registerMaterials();
        SpellVisuals.validateCatalog();
        ((net.minecraft.client.resources.IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(KomachiMesh.INSTANCE);
        ((net.minecraft.client.resources.IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(TexturedMesh.CABLECAR);
        ModItems.MISFORTUNE_RIBBON.setTileEntityItemStackRenderer(RibbonItemRenderer.INSTANCE);
        DevelopmentPreviews.install();
        ((net.minecraft.client.resources.IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(RiverMesh.FERRY);
        ((net.minecraft.client.resources.IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(RiverMesh.LAMP);
        ((net.minecraft.client.resources.IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(HiganWaterSurface.INSTANCE);
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state,world,pos,tint)->0xffffff,ModBlocks.SANZU_WATER);
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state,world,pos,tint)->0xffffff,ModBlocks.CURSED_BLOOD);
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state,world,pos,tint)->0x789348,ModBlocks.POND_LOTUS);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack,tint)->0x789348,ModBlocks.POND_LOTUS);
        ClientEvents.registerKeys();
        SpellVisuals.installArmorPoses();
        ((net.minecraft.client.resources.IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(BlenderTrainModel.INSTANCE);
        ((net.minecraft.client.resources.IReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(BlenderEmeraldModel.INSTANCE);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack,tint)->{
            if(tint!=0)return 0xffffff;
            if(stack.getItem()==ModItems.YOUKAI_POTION)return 0x9b4ccc;
            if(stack.getItem()==ModItems.TEA)return 0x783447;
            if(stack.getItem()==ModItems.PLASMA)return 0xcb2849;
            return 0xa91838;
        },ModItems.YOUKAI_POTION,ModItems.TEA,ModItems.PLASMA,ModItems.DRAGON_BLOOD);
    }
    @Override public void receiveState(NBTTagCompound n) {Minecraft.getMinecraft().addScheduledTask(()->state.applyUpdate(n));}
    @Override public void receiveEffect(EffectMessage m) {Minecraft.getMinecraft().addScheduledTask(()->SpellVisuals.receive(m));}
    @Override public void receiveFortune(dev.lostfantasy.network.FortuneMessage message) {Minecraft.getMinecraft().addScheduledTask(()->FortuneVisual.receive(message));}
    @Override public void openBook() {Minecraft.getMinecraft().displayGuiScreen(new ManuscriptScreen());}
    @Override public void openGuide() {if(!openPatchouli("fantasy_guide"))Minecraft.getMinecraft().displayGuiScreen(new FantasyScreen(3));}
    private boolean openPatchouli(String book) {
        if(!net.minecraftforge.fml.common.Loader.isModLoaded("patchouli"))return false;
        try {
            Object api=Class.forName("vazkii.patchouli.api.PatchouliAPI").getField("instance").get(null);
            Class<?> type=Class.forName("vazkii.patchouli.api.PatchouliAPI$IPatchouliAPI");
            if(Boolean.TRUE.equals(type.getMethod("isStub").invoke(api)))return false;
            type.getMethod("openBookGUI",net.minecraft.util.ResourceLocation.class).invoke(api,new net.minecraft.util.ResourceLocation("lostfantasy",book));return true;
        }catch(ReflectiveOperationException e) {
            org.apache.logging.log4j.LogManager.getLogger("LostFantasy").warn("Patchouli 1.12 API could not open book "+book+"; using built-in reader",e);return false;
        }
    }
    @Mod.EventBusSubscriber(modid=LostFantasy.ID,value=Side.CLIENT)
    public static final class Models {
        @SubscribeEvent public static void register(ModelRegistryEvent e) {
            ModelLoader.setCustomStateMapper(ModBlocks.SANZU_WATER,new net.minecraft.client.renderer.block.statemap.StateMap.Builder().ignore(net.minecraftforge.fluids.BlockFluidBase.LEVEL).build());
            ModelLoader.setCustomStateMapper(ModBlocks.CURSED_BLOOD,new net.minecraft.client.renderer.block.statemap.StateMap.Builder().ignore(net.minecraftforge.fluids.BlockFluidBase.LEVEL).build());
            for(Item item:ModItems.ALL.values())ModelLoader.setCustomModelResourceLocation(item,0,new ModelResourceLocation(item.getRegistryName(),"inventory"));
            for(net.minecraft.block.Block block:ModBlocks.ALL.values()) {
                Item item=Item.getItemFromBlock(block);
                if(block==ModBlocks.RUINED_BOOKCASE) {
                    dev.lostfantasy.block.RuinedBookcase.Damage[] variants = dev.lostfantasy.block.RuinedBookcase.Damage.values();
                    for(int meta=0;meta<variants.length*4;meta++) {
                        dev.lostfantasy.block.RuinedBookcase.Damage damage=variants[meta>>2];
                        ModelLoader.setCustomModelResourceLocation(item,meta,new ModelResourceLocation(LostFantasy.ID+":ruined_bookcase_"+damage.getName(),"inventory"));
                    }
                } else ModelLoader.setCustomModelResourceLocation(item,0,new ModelResourceLocation(block.getRegistryName(),"inventory"));
            }
        }
        @SubscribeEvent public static void textures(net.minecraftforge.client.event.TextureStitchEvent.Pre e) {
            GenunkaiClouds.clear();
            e.getMap().registerSprite(GenunkaiClouds.TEXTURE);
            e.getMap().registerSprite(MeshItemModel.TEXTURE);
            e.getMap().registerSprite(MeshItemModel.SILK);
            e.getMap().registerSprite(new net.minecraft.util.ResourceLocation("lostfantasy:blocks/gensokyo_craft"));
            e.getMap().registerSprite(new net.minecraft.util.ResourceLocation("lostfantasy:blocks/interior_furniture"));
            e.getMap().registerSprite(LilyFieldModel.PETAL);
            e.getMap().registerSprite(LilyFieldModel.STAMEN);
        }
        @SubscribeEvent public static void bake(net.minecraftforge.client.event.ModelBakeEvent e) {
            bakeMesh(e,ModItems.HIGAN_LILY,"spider_lily",false);
            bakeMesh(e,ModItems.MISFORTUNE_RIBBON,"misfortune_ribbon",true,MeshItemModel.SILK);
            for(net.minecraft.block.Block block:new net.minecraft.block.Block[]{ModBlocks.SPIDER_LILY,ModBlocks.FLOATING_LILY}) {
                ModelResourceLocation key=new ModelResourceLocation(block.getRegistryName(),"normal");
                net.minecraft.client.renderer.block.model.IBakedModel base=e.getModelRegistry().getObject(key);
                if(base!=null)e.getModelRegistry().putObject(key,new LilyFieldModel(base,block==ModBlocks.FLOATING_LILY));
                bakeMesh(e,Item.getItemFromBlock(block),"spider_lily",false);
            }
            bakeMesh(e,ModItems.GOHEI,"gohei",true);
            bakeMesh(e,ModItems.HAKUREI_CHARM,"hakurei_charm",true,new net.minecraft.util.ResourceLocation("lostfantasy:blocks/gensokyo_craft"));
            net.minecraft.util.ResourceLocation craft=new net.minecraft.util.ResourceLocation("lostfantasy:blocks/gensokyo_craft");
            net.minecraft.util.EnumFacing[] facings={net.minecraft.util.EnumFacing.NORTH,net.minecraft.util.EnumFacing.EAST,
                    net.minecraft.util.EnumFacing.SOUTH,net.minecraft.util.EnumFacing.WEST};
            net.minecraft.util.ResourceLocation furniture=new net.minecraft.util.ResourceLocation("lostfantasy:blocks/interior_furniture");
            for(net.minecraft.block.Block block:new net.minecraft.block.Block[]{ModBlocks.LIBRARY_LAMP,ModBlocks.HANGING_LAMP,
                    ModBlocks.SAISEN_BOX,ModBlocks.TEA_TABLE,ModBlocks.FLOOR_CUSHION,ModBlocks.DRAWER_CABINET,
                    ModBlocks.KITCHEN_SHELF,ModBlocks.WASHSTAND,ModBlocks.UPHOLSTERED_CHAIR}) {
                String mesh=block.getRegistryName().getPath();
                boolean diffuse=block!=ModBlocks.LIBRARY_LAMP && block!=ModBlocks.HANGING_LAMP;
                for(int turn=0;turn<facings.length;turn++) {
                    ModelResourceLocation key=new ModelResourceLocation(block.getRegistryName(),"facing="+facings[turn].getName());
                    net.minecraft.client.renderer.block.model.IBakedModel base=e.getModelRegistry().getObject(key);
                    if(base!=null)try {e.getModelRegistry().putObject(key,new MeshItemModel(base,mesh,diffuse,furniture,turn));}
                    catch(java.io.IOException ex) {org.apache.logging.log4j.LogManager.getLogger("LostFantasy").error("Cannot bake furniture "+key,ex);}
                }
                bakeMesh(e,Item.getItemFromBlock(block),mesh,diffuse,furniture);
            }
            for(int turn=0;turn<facings.length;turn++) {
                ModelResourceLocation key=new ModelResourceLocation(ModBlocks.REHEARSAL_KEYBOARD.getRegistryName(),"facing="+facings[turn].getName());
                net.minecraft.client.renderer.block.model.IBakedModel base=e.getModelRegistry().getObject(key);
                if(base!=null)try {e.getModelRegistry().putObject(key,new MeshItemModel(base,"rehearsal_keyboard",true,craft,turn));}
                catch(java.io.IOException ex) {org.apache.logging.log4j.LogManager.getLogger("LostFantasy").error("Cannot bake rehearsal keyboard",ex);}
            }
            bakeMesh(e,Item.getItemFromBlock(ModBlocks.REHEARSAL_KEYBOARD),"rehearsal_keyboard",true,craft);
            net.minecraft.util.ResourceLocation culm=new net.minecraft.util.ResourceLocation("lostfantasy:blocks/bamboo_culm");
            for(net.minecraft.block.Block block:new net.minecraft.block.Block[]{ModBlocks.BAMBOO_STEM,ModBlocks.BAMBOO_FOLIAGE}) {
                String mesh=block==ModBlocks.BAMBOO_STEM?"bamboo_stem_":"bamboo_foliage_";
                for(int step=0;step<4;step++)for(int turn=0;turn<facings.length;turn++) {
                    ModelResourceLocation key=new ModelResourceLocation(block.getRegistryName(),
                            "facing="+facings[turn].getName()+",step="+step);
                    net.minecraft.client.renderer.block.model.IBakedModel base=e.getModelRegistry().getObject(key);
                    // The exported culm leans east; other facings rotate around the block centre.
                    if(base!=null)try {e.getModelRegistry().putObject(key,new MeshItemModel(base,mesh+step,true,culm,(turn+3)%4));}
                    catch(java.io.IOException ex) {org.apache.logging.log4j.LogManager.getLogger("LostFantasy").error("Cannot bake bamboo "+key,ex);}
                }
                bakeMesh(e,Item.getItemFromBlock(block),mesh+"0",true,culm);
            }
            bakeMesh(e,ModItems.GAP_FRAGMENT,"gap_fragment",false);
            bakeMesh(e,ModItems.GAP_KEY,"gap_key",false);
        }
        private static void bakeMesh(net.minecraftforge.client.event.ModelBakeEvent e,Item item,String mesh,boolean diffuseLighting) {
            bakeMesh(e,item,mesh,diffuseLighting,MeshItemModel.TEXTURE);
        }
        private static void bakeMesh(net.minecraftforge.client.event.ModelBakeEvent e,Item item,String mesh,boolean diffuseLighting,net.minecraft.util.ResourceLocation texture) {
            ModelResourceLocation key=new ModelResourceLocation(item.getRegistryName(),"inventory");
            net.minecraft.client.renderer.block.model.IBakedModel original=e.getModelRegistry().getObject(key);
            if(original==null)return;
            try {
                MeshItemModel baked=new MeshItemModel(original,mesh,diffuseLighting,texture);
                e.getModelRegistry().putObject(key,item==ModItems.MISFORTUNE_RIBBON?new RibbonItemModel(baked):baked);
            }
            catch(java.io.IOException ex) {org.apache.logging.log4j.LogManager.getLogger("LostFantasy").error("Unable to bake item mesh "+mesh,ex);}
        }
    }
}
