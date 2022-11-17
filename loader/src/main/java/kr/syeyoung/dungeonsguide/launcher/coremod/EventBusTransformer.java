package kr.syeyoung.dungeonsguide.launcher.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class EventBusTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (name.equals("net.minecraftforge.fml.common.eventhandler.EventBus")) {

            String targetMethodName = "register";

            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(basicClass);
            classReader.accept(classNode, 0);

            Iterator<MethodNode> methods = classNode.methods.iterator();
            while(methods.hasNext())
            {
                MethodNode m = methods.next();
                if ((m.name.equals(targetMethodName) && m.desc.equals("(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/reflect/Method;Lnet/minecraftforge/fml/common/ModContainer;)V")))
                {
                    AbstractInsnNode curr = m.instructions.getFirst();
                    while ((curr = curr.getNext()) != m.instructions.getLast()) {
                        if (curr instanceof TypeInsnNode && ((TypeInsnNode) curr).desc.equals("net/minecraftforge/fml/common/eventhandler/ASMEventHandler")) {
                            ((TypeInsnNode) curr).desc = "kr/syeyoung/dungeonsguide/launcher/events/OwnerAwareASMEventHandler";
                        }
                        if (curr instanceof MethodInsnNode && ((MethodInsnNode) curr).owner.equals("net/minecraftforge/fml/common/eventhandler/ASMEventHandler")) {
                            ((MethodInsnNode) curr).owner = "kr/syeyoung/dungeonsguide/launcher/events/OwnerAwareASMEventHandler";
                        }
                    }

                    break;
                }
            }

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }
}
