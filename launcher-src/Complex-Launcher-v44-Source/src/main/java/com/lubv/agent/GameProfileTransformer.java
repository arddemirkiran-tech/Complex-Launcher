package com.lubv.agent;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

public class GameProfileTransformer implements ClassFileTransformer {
    private static volatile String pendingName = null;

    public static void setPendingName(String name) {
        pendingName = name;
    }

    public static String getPendingName() {
        return pendingName;
    }

    @Override
    public byte[] transform(ClassLoader classLoader, String className, Class<?> beingDefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className == null) {
            return null;
        }
        if (className.contains("GameProfile")) {
            System.out.println("[ComplexAgent] GameProfile sinifi tespit edildi: " + className);
        }
        if (className.equals("dzz")) {
            System.out.println("[ComplexAgent] dzz (GameRenderer) tespit edildi! Hook ekleniyor...");
            try {
                return hookGameRenderer(classfileBuffer);
            } catch (Throwable throwable) {
                System.out.println("[ComplexAgent] GameRenderer hook hatasi: " + throwable);
                throwable.printStackTrace();
            }
        }
        return null;
    }

    private byte[] hookGameRenderer(byte[] classfileBuffer) {
        ClassReader reader = new ClassReader(classfileBuffer);
        ClassWriter writer = new ClassWriter(reader, 3 /* COMPUTE_MAXS */);
        ClassVisitor visitor = new ClassVisitor(589824 /* ASM5 */, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                if (name.equals("a") && desc.equals("(Ldfm;F)V")) {
                    System.out.println("[ComplexAgent] Render methodu bulundu: " + name + desc);
                    return new MethodVisitor(589824, mv) {
                        @Override
                        public void visitInsn(int opcode) {
                            if (opcode == 177 /* RETURN */ || opcode == 191 /* ATHROW */) {
                                super.visitVarInsn(25 /* ALOAD */, 1);
                                super.visitVarInsn(23 /* FLOAD */, 2);
                                super.visitMethodInsn(184 /* INVOKESTATIC */, "com/lubv/agent/OverlayRenderer",
                                        "render", "(Ljava/lang/Object;F)V", false);
                                System.out.println("[ComplexAgent] Overlay hook inject edildi!");
                            }
                            super.visitInsn(opcode);
                        }
                    };
                }
                return mv;
            }
        };
        reader.accept(visitor, 0);
        return writer.toByteArray();
    }
}
