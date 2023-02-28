package me.theandrey.objectstream.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public final class ASMHelper {

    public static ClassNode readClass(byte[] bytes) {
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(node, 0);
        return node;
    }

    public static byte[] writeClass(ClassNode node, int flags) {
        ClassWriter writer = new ClassWriter(flags);
        node.accept(writer);
        return writer.toByteArray();
    }

    public static Type getObjectType(String name) {
        return Type.getType('L' + name.replace('.', '/') + ';');
    }

}
