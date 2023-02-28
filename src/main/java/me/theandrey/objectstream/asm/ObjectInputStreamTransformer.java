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
    private final Type replaceType;

    public ObjectInputStreamTransformer() {
        findType = Type.getType(ObjectInputStream.class);
        replaceType = ASMHelper.getObjectType("me.theandrey.objectstream.ObjectInputStreamMock");
    }

    @Override
    public byte[] transform(String name, String transformedName, @Nullable byte[] bytes) {
        if (bytes == null || bytes.length == 0 || Config.excludeClass.contains(name)) {
            return bytes;
        }

        ClassNode node = ASMHelper.readClass(bytes);

        List<MethodNode> unsafeMethods = scanMethods(node);

        if (!unsafeMethods.isEmpty()) {
            LOGGER.warn("SECURITY ALERT: Detected usage of ObjectInputStream in class '{}'", name);
            for (MethodNode method : unsafeMethods) {
                LOGGER.warn("Method: {} {}", name, method.name + method.desc);
            }

            return ASMHelper.writeClass(node, 0);
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
                }
            }
        }

        return found;
    }
}
