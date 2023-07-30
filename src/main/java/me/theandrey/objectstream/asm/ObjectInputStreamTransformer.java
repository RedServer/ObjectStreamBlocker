package me.theandrey.objectstream.asm;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nullable;
import me.theandrey.objectstream.Config;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * Заменяет использование {@link ObjectInputStream} заглушкой
 */
public class ObjectInputStreamTransformer implements IClassTransformer {

    static final Logger LOGGER = LogManager.getLogger();
    private final Type findType;
    private final Type utilsType;
    private final Type replaceType;

    public ObjectInputStreamTransformer() {
        findType = Type.getType(ObjectInputStream.class);
        utilsType = ASMHelper.getObjectType("org.apache.commons.lang3.SerializationUtils");
        replaceType = ASMHelper.getObjectType("me.theandrey.objectstream.ObjectInputStreamMock");
    }

    @Override
    public byte[] transform(String name, String transformedName, @Nullable byte[] bytes) {
        if (bytes != null && bytes.length > 0) {
            ClassNode node = ASMHelper.readClass(bytes);

            List<MethodNode> unsafeMethods = scanMethods(node);
            boolean superCheck = checkSuperClass(node);

            if (!unsafeMethods.isEmpty() || superCheck) {
                for (MethodNode method : unsafeMethods) {
                    LOGGER.warn("SECURITY ALERT: Detected usage of ObjectInputStream in '{}#{}'", name, method.name);
                }

                return ASMHelper.writeClass(node, 0);
            }
        }

        return bytes;
    }

    /**
     * Ищет случаи использования в коде методов и производит замену.
     * @return Список методов в которых были найдены случаи использования
     */
    private List<MethodNode> scanMethods(ClassNode node) {
        List<MethodNode> found = new ArrayList<>();

        for (MethodNode method : node.methods) {
            if (Config.excludeMethods.contains(ASMHelper.className(node) + '#' + method.name)) {
                continue;
            }

            ListIterator<AbstractInsnNode> it = method.instructions.iterator();
            boolean foundNew = false; // Найден оператор NEW

            while (it.hasNext()) {
                AbstractInsnNode next = it.next();

                if (next.getOpcode() == Opcodes.NEW) {
                    TypeInsnNode cast = ((TypeInsnNode)next);

                    if (cast.desc.equals(findType.getInternalName())) { // Инициализация типа
                        foundNew = true;
                        cast.desc = replaceType.getInternalName(); // Заглушка

                        if (!found.contains(method)) {
                            found.add(method);
                        }
                    }

                } else if (foundNew && next.getOpcode() == Opcodes.INVOKESPECIAL) { // Вызов конструктора
                    MethodInsnNode invoke = ((MethodInsnNode)next);

                    if ("<init>".equals(invoke.name) && invoke.owner.equals(findType.getInternalName())) {
                        invoke.owner = replaceType.getInternalName(); // Заглушка
                        foundNew = false; // Завершаем замену блока
                    }

                } else if (next.getOpcode() == Opcodes.INVOKESTATIC) {
                    MethodInsnNode invoke = ((MethodInsnNode)next);

                    if ("deserialize".equals(invoke.name) && invoke.owner.equals(utilsType.getInternalName())) {
                        invoke.owner = replaceType.getInternalName(); // Заглушка

                        if (!found.contains(method)) {
                            found.add(method);
                        }
                    }
                }
            }
        }

        return found;
    }

    private boolean checkSuperClass(ClassNode node) {
        String name = ASMHelper.className(node);
        if (!Config.excludeClass.contains(name) && node.superName.equals(findType.getInternalName())) {
            node.superName = replaceType.getInternalName();

            // Замена вызова конструктора родителя
            for (MethodNode method : node.methods) {
                if (method.name.equals("<init>")) {
                    ListIterator<AbstractInsnNode> it = method.instructions.iterator();

                    while (it.hasNext()) {
                        AbstractInsnNode next = it.next();

                        if (next.getOpcode() == Opcodes.INVOKESPECIAL) {
                            MethodInsnNode invoke = ((MethodInsnNode)next);

                            if ("<init>".equals(invoke.name) && invoke.owner.equals(findType.getInternalName())) {
                                invoke.owner = replaceType.getInternalName();
                            }
                        }
                    }
                }
            }

            LOGGER.warn("SECURITY ALERT: Detected extending of ObjectInputStream in '{}'", name);
            return true;
        }

        return false;
    }
}
