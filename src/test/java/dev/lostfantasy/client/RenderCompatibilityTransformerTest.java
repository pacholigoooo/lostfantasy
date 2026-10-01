package dev.lostfantasy.client;
import dev.lostfantasy.asm.RenderCompatibilityTransformer;
import java.io.*;
import org.junit.Test;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import static org.junit.Assert.*;
public class RenderCompatibilityTransformerTest {
    @Test public void actualChunkDispatcherLimitsBothWorkersAndBuffers() throws Exception {
        String name="net.minecraft.client.renderer.chunk.ChunkRenderDispatcher";
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        try(InputStream in=getClass().getClassLoader().getResourceAsStream(name.replace('.','/')+".class")) {
            byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);
        }
        ClassNode c=new ClassNode();new ClassReader(new RenderCompatibilityTransformer().transform(name,name,out.toByteArray())).accept(c,0);
        int limits=0;
        for(MethodNode m:c.methods)if(m.name.equals("<init>")) {
            new Analyzer<BasicValue>(new BasicVerifier()).analyze(c.name,m);
            for(AbstractInsnNode node:m.instructions.toArray())if(node instanceof MethodInsnNode && ((MethodInsnNode)node).owner.equals("java/lang/Math") && ((MethodInsnNode)node).name.equals("min"))limits++;
        }
        assertEquals(2,limits);
    }
    @Test public void optifineShaderDimensionReadsArePatchedInMappedAndProductionNames() throws Exception {
        for(String owner:new String[]{"ayn","net/minecraft/world/DimensionType"}) {
            ClassWriter w=new ClassWriter(0);w.visit(52,Opcodes.ACC_PUBLIC,"net/optifine/shaders/Shaders",null,"java/lang/Object",null);
            org.objectweb.asm.MethodVisitor m=w.visitMethod(Opcodes.ACC_STATIC,"read","(L"+owner+";)I",null,null);
            m.visitCode();m.visitVarInsn(Opcodes.ALOAD,0);m.visitMethodInsn(Opcodes.INVOKEVIRTUAL,owner,owner.equals("ayn")?"a":"getId","()I",false);m.visitInsn(Opcodes.IRETURN);m.visitMaxs(1,1);m.visitEnd();w.visitEnd();
            ClassNode c=new ClassNode();new ClassReader(new RenderCompatibilityTransformer().transform("net.optifine.shaders.Shaders","net.optifine.shaders.Shaders",w.toByteArray())).accept(c,0);
            int hooks=0;for(MethodNode method:c.methods){new Analyzer<BasicValue>(new BasicVerifier()).analyze(c.name,method);for(AbstractInsnNode n:method.instructions.toArray())if(n instanceof MethodInsnNode && ((MethodInsnNode)n).name.equals("dimension"))hooks++;}
            assertEquals(1,hooks);
        }
    }
}
