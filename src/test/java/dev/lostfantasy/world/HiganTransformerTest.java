package dev.lostfantasy.world;

import dev.lostfantasy.asm.HiganTransformer;
import java.io.*;
import java.util.*;
import org.junit.Test;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import static org.junit.Assert.*;

public class HiganTransformerTest {
    @Test public void actualMinecraftMethodsAreGuardedAndRemainValidBytecode() throws Exception {
        Map<String,Integer> expected=new LinkedHashMap<>();
        expected.put("net.minecraft.entity.Entity",1);
        expected.put("net.minecraft.entity.EntityLivingBase",3);
        expected.put("net.minecraft.entity.player.EntityPlayer",1);
        expected.put("net.minecraft.entity.player.EntityPlayerMP",1);
        expected.put("net.minecraft.network.NetHandlerPlayServer",1);
        expected.put("net.minecraft.server.management.PlayerList",2);
        for(Map.Entry<String,Integer> e:expected.entrySet()) {
            String path=e.getKey().replace('.','/');byte[] original;
            try(InputStream in=getClass().getClassLoader().getResourceAsStream(path+".class");ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                assertNotNull(path,in);byte[] block=new byte[8192];int n;while((n=in.read(block))!=-1)out.write(block,0,n);original=out.toByteArray();
            }
            ClassNode c=new ClassNode();new ClassReader(new HiganTransformer().transform(e.getKey(),e.getKey(),original)).accept(c,0);
            int hooks=0;
            for(MethodNode method:c.methods) {
                boolean modified=false;
                for(AbstractInsnNode node:method.instructions.toArray())if(node instanceof MethodInsnNode && ((MethodInsnNode)node).owner.equals("dev/lostfantasy/world/HiganRestrictions")) {hooks++;modified=true;}
                if(modified)new Analyzer<BasicValue>(new BasicVerifier()).analyze(c.name,method);
            }
            assertEquals(path,(int)e.getValue(),hooks);
        }
    }
}
