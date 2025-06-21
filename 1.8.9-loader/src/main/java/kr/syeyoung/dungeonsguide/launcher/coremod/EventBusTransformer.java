/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.launcher.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

// I still need this with classloader injection, because of the cache in ASMEventHandler.
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
