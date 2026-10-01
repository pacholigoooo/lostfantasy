package dev.lostfantasy.client;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ShaderProgramStateTest {
    private ShaderBridge.Access access;
    @Before public void setup() throws Exception {
        FakeShaders.shaderPackLoaded=true;FakeShaders.isShaderPackInitialized=true;
        FakeShaders.activeProgram=FakeShaders.ProgramNone;FakeShaders.switches=0;
        FakeGL.alpha=1;FakeGL.blend=2;FakeGL.alphaLock=FakeGL.blendLock=false;
        access=new ShaderBridge.Access(FakeShaders.class,FakeGL.class,Alpha.class,Blend.class);
    }

    @Test public void restoringPhysicalValuesMustNotReplaceTheSavedLogicalStates() {
        access.use(FakeShaders.ProgramArmorGlint);
        ShaderBridge.ProgramState saved=access.capture();
        assertEquals(11,FakeGL.alpha);assertEquals(12,FakeGL.blend);
        access.use(FakeShaders.ProgramBasic);
        FakeGL.setAlphaState(new Alpha(40));FakeGL.setBlendState(new Blend(50));
        saved.restoreProgram();
        // RenderState also restores the queried physical state. Under a shader lock
        // these requests update OptiFine's deferred state, not the physical override.
        FakeGL.setAlphaState(new Alpha(11));FakeGL.setBlendState(new Blend(12));
        saved.restoreLocks();
        assertSame(FakeShaders.ProgramArmorGlint,FakeShaders.activeProgram);
        assertEquals(11,FakeGL.alpha);assertEquals(12,FakeGL.blend);
        access.use(FakeShaders.ProgramNone);
        assertEquals(1,FakeGL.alpha);assertEquals(2,FakeGL.blend);
    }

    @Test public void unchangedProgramDoesNotTriggerShaderOrDrawBufferRebinding() {
        ShaderBridge.ProgramState saved=access.capture();
        saved.restoreProgram();saved.restoreLocks();
        assertEquals(0,FakeShaders.switches);
    }

    @Test public void unloadedOrUninitializedShadersAreNotCapturedOrReactivated() {
        FakeShaders.shaderPackLoaded=false;assertNull(access.capture());
        FakeShaders.shaderPackLoaded=true;FakeShaders.isShaderPackInitialized=false;assertNull(access.capture());
        FakeShaders.isShaderPackInitialized=true;
        ShaderBridge.ProgramState saved=access.capture();
        access.use(FakeShaders.ProgramBasic);
        FakeShaders.shaderPackLoaded=false;
        FakeGL.alpha=7;FakeGL.blend=8;
        int switches=FakeShaders.switches;
        saved.restoreProgram();saved.restoreLocks();
        assertEquals(switches,FakeShaders.switches);
        assertEquals(7,FakeGL.alpha);assertEquals(8,FakeGL.blend);
    }

    @Test public void nestedEffectsRestoreEachCallersProgramAndLogicalBlend() {
        access.use(FakeShaders.ProgramArmorGlint);
        ShaderBridge.ProgramState outer=access.capture();
        access.use(FakeShaders.ProgramSpiderEyes);
        FakeGL.setAlphaState(new Alpha(7));FakeGL.setBlendState(new Blend(8));
        ShaderBridge.ProgramState inner=access.capture();
        access.use(FakeShaders.ProgramBasic);
        FakeGL.setAlphaState(new Alpha(70));FakeGL.setBlendState(new Blend(80));
        inner.restoreProgram();inner.restoreLocks();
        assertSame(FakeShaders.ProgramSpiderEyes,FakeShaders.activeProgram);
        access.use(FakeShaders.ProgramNone);
        assertEquals(7,FakeGL.alpha);assertEquals(8,FakeGL.blend);
        outer.restoreProgram();outer.restoreLocks();
        access.use(FakeShaders.ProgramNone);
        assertEquals(1,FakeGL.alpha);assertEquals(2,FakeGL.blend);
    }

    public static final class Alpha {
        int value;
        public Alpha(){}
        Alpha(int value){this.value=value;}
    }
    public static final class Blend {
        int value;
        public Blend(){}
        Blend(int value){this.value=value;}
    }
    // Mirrors G5's lock semantics: getters/setters operate on the saved logical
    // state while a program overrides GL; switching programs unlocks then relocks.
    public static final class FakeGL {
        static int alpha,blend,savedAlpha,savedBlend;
        static boolean alphaLock,blendLock;
        public static void getAlphaState(Alpha out){out.value=alphaLock?savedAlpha:alpha;}
        public static void getBlendState(Blend out){out.value=blendLock?savedBlend:blend;}
        public static void setAlphaState(Alpha in){if(alphaLock)savedAlpha=in.value;else alpha=in.value;}
        public static void setBlendState(Blend in){if(blendLock)savedBlend=in.value;else blend=in.value;}
        static void unlock(){if(alphaLock)alpha=savedAlpha;if(blendLock)blend=savedBlend;alphaLock=blendLock=false;}
        static void lock(Program program){
            if(program.alpha!=null){savedAlpha=alpha;alpha=program.alpha;alphaLock=true;}
            if(program.blend!=null){savedBlend=blend;blend=program.blend;blendLock=true;}
        }
    }
    public static final class Program {
        final Integer alpha,blend;
        Program(Integer alpha,Integer blend){this.alpha=alpha;this.blend=blend;}
    }
    public static final class FakeShaders {
        public static final Program ProgramNone=new Program(null,null),ProgramBasic=new Program(null,null),
            ProgramArmorGlint=new Program(11,12),ProgramSpiderEyes=new Program(null,22);
        public static Program activeProgram;
        public static boolean shaderPackLoaded,isShaderPackInitialized,isRenderingDfb=true,isShadowPass=false;
        static int switches;
        public static void useProgram(Program program){FakeGL.unlock();activeProgram=program;FakeGL.lock(program);switches++;}
    }
}
