package dev.lostfantasy.client;

import dev.lostfantasy.Balance;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/** Optional OptiFine calls, without linking the dedicated server to its classes. */
public final class ShaderBridge {
    static final net.minecraft.util.ResourceLocation WHITE=new net.minecraft.util.ResourceLocation("lostfantasy","textures/misc/white.png");
    private static final Access ACCESS=access();
    private static int riverBlock=-1,waterBlock=9;
    private ShaderBridge() {}
    private static Access access() {
        try { return new Access(Class.forName("net.optifine.shaders.Shaders",false,ShaderBridge.class.getClassLoader())); }
        catch(ClassNotFoundException absent) { return null; }
        catch(ReflectiveOperationException | LinkageError ex) {
            org.apache.logging.log4j.LogManager.getLogger("LostFantasy").warn("OptiFine render bridge unavailable",ex);return null;
        }
    }
    static ProgramState captureProgram() {return ACCESS==null?null:ACCESS.capture();}
    static boolean shadowPass() {return ACCESS!=null && Boolean.TRUE.equals(ACCESS.read(ACCESS.shadow));}
    static void colored() {
        // ProgramBasic consumes vertex colour, not the last bound entity/atlas texture.
        // Leave post-composite drawing and the shadow program under OptiFine's control.
        if(ACCESS!=null && ACCESS.ready() && Boolean.TRUE.equals(ACCESS.read(ACCESS.deferred)) && !shadowPass())
            ACCESS.use(ACCESS.read(ACCESS.basic));
    }
    static void glint() {
        if(ACCESS!=null && ACCESS.ready() && Boolean.TRUE.equals(ACCESS.read(ACCESS.deferred)) && !shadowPass())ACCESS.use(ACCESS.read(ACCESS.glint));
    }
    static void emissive() {
        if(ACCESS!=null && ACCESS.ready() && Boolean.TRUE.equals(ACCESS.read(ACCESS.deferred)) && !shadowPass())ACCESS.use(ACCESS.read(ACCESS.eyes));
    }
    static void screen() {
        if(ACCESS!=null && ACCESS.ready())ACCESS.use(ACCESS.read(ACCESS.none));
    }
    static void registerMaterials() {
        riverBlock=net.minecraft.block.Block.getIdFromBlock(dev.lostfantasy.ModBlocks.SANZU_WATER);
        waterBlock=net.minecraft.block.Block.getIdFromBlock(net.minecraft.init.Blocks.WATER);
    }
    public static int material(int block,Object[] aliases) {
        if(block<0 || (aliases!=null && block<aliases.length && aliases[block]!=null))return block;
        if(block==riverBlock)return waterBlock;
        // Furniture keeps its actual material and block light. A shader pack may opt in
        // to a dedicated alias, but a lamp's frame is not a full emissive glowstone block.
        return block;
    }

    /** Called only at OptiFine's shader-directory selection sites, never at world transfers. */
    public static int dimension(int original,List<Integer> directories,Object pack) {
        if(original!=Balance.dimensionId
                && original!=Balance.gensokyoDimensionId && original!=Balance.kasenDimensionId && original!=Balance.boundaryDimensionId
                && original!=Balance.oldHellDimensionId && original!=Balance.senkaiDimensionId && original!=Balance.netherworldDimensionId && original!=Balance.bloodPoolDimensionId)return original;
        if(directories.contains(original))return original;
        int preferred=original==Balance.dimensionId && directories.contains(1)?1:0;
        int fallback=directories.contains(preferred)?preferred:original;
        // G5 only scans -128..128. Honour a pack's explicit mod dimension even outside that range.
        if(pack!=null)try {
            Method directory=pack.getClass().getMethod("hasDirectory",String.class);
            if(Boolean.TRUE.equals(directory.invoke(pack,"/shaders/world"+original))) {
                directories.add(original);return original;
            }
        } catch(ReflectiveOperationException | LinkageError ex) {
            org.apache.logging.log4j.LogManager.getLogger("LostFantasy").warn(
                    "Cannot inspect /shaders/world"+original+" in "+pack.getClass().getName()
                            +"; using dimension selector "+fallback,ex);
        }
        return fallback;
    }
    static final class ProgramState {
        private final Access access;
        private final Object program,alpha,blend;
        ProgramState(Access access,Object program,Object alpha,Object blend) {
            this.access=access;this.program=program;this.alpha=alpha;this.blend=blend;
        }
        void restoreProgram() {if(access.ready())access.use(program);}
        void restoreLocks() {
            if(access.ready()) {access.invoke(access.setAlpha,alpha);access.invoke(access.setBlend,blend);}
        }
    }
    static final class Access {
        final Field active,basic,glint,eyes,none,deferred,shadow,loaded,initialized;
        final Method use,getAlpha,getBlend,setAlpha,setBlend;
        final java.lang.reflect.Constructor<?> alphaConstructor,blendConstructor;
        Access(Class<?> shaders) throws ReflectiveOperationException {
            this(shaders,net.minecraft.client.renderer.GlStateManager.class,
                    Class.forName("net.optifine.render.GlAlphaState",false,shaders.getClassLoader()),
                    Class.forName("net.optifine.render.GlBlendState",false,shaders.getClassLoader()));
        }
        Access(Class<?> shaders,Class<?> state,Class<?> alpha,Class<?> blend) throws ReflectiveOperationException {
            active=shaders.getField("activeProgram");basic=shaders.getField("ProgramBasic");
            glint=shaders.getField("ProgramArmorGlint");
            eyes=shaders.getField("ProgramSpiderEyes");none=shaders.getField("ProgramNone");
            deferred=shaders.getField("isRenderingDfb");shadow=shaders.getField("isShadowPass");
            loaded=shaders.getField("shaderPackLoaded");initialized=shaders.getField("isShaderPackInitialized");
            use=shaders.getMethod("useProgram",active.getType());
            getAlpha=state.getMethod("getAlphaState",alpha);setAlpha=state.getMethod("setAlphaState",alpha);
            getBlend=state.getMethod("getBlendState",blend);setBlend=state.getMethod("setBlendState",blend);
            alphaConstructor=alpha.getConstructor();blendConstructor=blend.getConstructor();
        }
        boolean ready() {return Boolean.TRUE.equals(read(loaded)) && Boolean.TRUE.equals(read(initialized));}
        ProgramState capture() {
            if(!ready())return null;
            try {
                Object alpha=alphaConstructor.newInstance(),blend=blendConstructor.newInstance();
                invoke(getAlpha,alpha);invoke(getBlend,blend);
                return new ProgramState(this,read(active),alpha,blend);
            } catch(ReflectiveOperationException ex) {throw new IllegalStateException("OptiFine render snapshot",ex);}
        }
        Object read(Field field) {
            try {return field.get(null);}catch(ReflectiveOperationException ex){throw new IllegalStateException("OptiFine render state",ex);}
        }
        void use(Object program) {
            if(read(active)!=program)invoke(use,program);
        }
        void invoke(Method method,Object value) {
            try {method.invoke(null,value);}catch(ReflectiveOperationException ex){throw new IllegalStateException("OptiFine render state",ex);}
        }
    }
}
