package dev.lostfantasy.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Narrow OptiFine dimension routing and a bounded chunk-builder allocation pool. */
public final class RenderCompatibilityTransformer implements IClassTransformer,Opcodes {
    @Override public byte[] transform(String name,String transformedName,byte[] bytes) {
        if(bytes==null)return null;
        boolean shaders="net.optifine.shaders.Shaders".equals(transformedName);
        boolean chunks="net.minecraft.client.renderer.chunk.ChunkRenderDispatcher".equals(transformedName);
        boolean aliases="net.optifine.shaders.BlockAliases".equals(transformedName);
        if(!shaders&&!chunks&&!aliases)return bytes;
        ClassNode c=new ClassNode(ASM5);new ClassReader(bytes).accept(c,0);int changes=0;
        if(aliases)for(MethodNode m:c.methods)if(m.name.equals("getBlockAliasId") && m.desc.equals("(II)I")) {
            InsnList hook=new InsnList();hook.add(new VarInsnNode(ILOAD,0));
            hook.add(new FieldInsnNode(GETSTATIC,c.name,"blockAliases","[[Lnet/optifine/shaders/BlockAlias;"));
            hook.add(new MethodInsnNode(INVOKESTATIC,"dev/lostfantasy/client/ShaderBridge","material","(I[Ljava/lang/Object;)I",false));
            hook.add(new VarInsnNode(ISTORE,0));m.instructions.insert(hook);changes++;
        }
        for(MethodNode m:c.methods)for(AbstractInsnNode instruction:m.instructions.toArray()) {
            if(shaders && instruction instanceof MethodInsnNode) {
                MethodInsnNode call=(MethodInsnNode)instruction;
                if(!call.desc.equals("()I") || !(call.owner.equals("net/minecraft/world/DimensionType") || call.owner.equals("ayn")))continue;
                if(!(call.name.equals("getId") || call.name.equals("func_186068_a") || call.name.equals("a")))continue;
                InsnList hook=new InsnList();
                hook.add(new FieldInsnNode(GETSTATIC,c.name,"shaderPackDimensions","Ljava/util/List;"));
                hook.add(new FieldInsnNode(GETSTATIC,c.name,"shaderPack","Lnet/optifine/shaders/IShaderPack;"));
                hook.add(new MethodInsnNode(INVOKESTATIC,"dev/lostfantasy/client/ShaderBridge","dimension","(ILjava/util/List;Ljava/lang/Object;)I",false));
                m.instructions.insert(call,hook);changes++;
            } else if(chunks && m.name.equals("<init>")) {
                if(instruction instanceof FieldInsnNode) {
                    FieldInsnNode field=(FieldInsnNode)instruction;
                    if(field.getOpcode()==PUTFIELD && field.desc.equals("I") && (field.name.equals("countRenderBuilders") || field.name.equals("field_188249_c"))) {
                        m.instructions.insertBefore(field,cap(8));changes++;
                    }
                } else if(instruction instanceof MethodInsnNode) {
                    MethodInsnNode call=(MethodInsnNode)instruction;
                    if(call.desc.equals("()I") && ((call.owner.equals("java/lang/Runtime") && call.name.equals("availableProcessors"))
                            || (call.owner.equals("Config") && call.name.equals("getAvailableProcessors")))) {
                        m.instructions.insert(call,cap(4));changes++;
                    }
                }
            }
        }
        if(changes==0)return bytes;
        ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(writer);return writer.toByteArray();
    }
    private static InsnList cap(int limit) {
        InsnList code=new InsnList();code.add(new IntInsnNode(BIPUSH,limit));
        code.add(new MethodInsnNode(INVOKESTATIC,"java/lang/Math","min","(II)I",false));return code;
    }
}
