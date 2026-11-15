package com.lubv.agent;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AgentMain {
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        try {
            System.setProperty("java.awt.headless", "false");
        } catch (Exception ignore) {
        }
        System.out.println("[ComplexAgent] Baslatiliyor...");
        instrumentation.addTransformer(new GameProfileTransformer(), false);
        Thread thread = new Thread(new Runnable() {
            public void run() {
                boolean loaded = false;
                for (int i = 0; i < 30; i++) {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException ie) {
                        return;
                    }
                    try {
                        Class.forName("djz", false, Thread.currentThread().getContextClassLoader());
                        Thread.sleep(3000L);
                        loaded = true;
                        break;
                    } catch (ClassNotFoundException cnfe) {
                        if (i >= 29) continue;
                        System.out.println("[ComplexAgent] MC class bekleniyor... (" + (i + 1) + "s)");
                        continue;
                    } catch (InterruptedException ie) {
                        return;
                    }
                }
                if (!loaded) {
                    System.out.println("[ComplexAgent] MC class 30s icinde yuklenemedi, devam ediliyor.");
                }
                AgentMain.forceNonHeadless();
                AgentMain.forceWindowedMode();
                IpcClient ipcClient = new IpcClient();
                ipcClient.connect();
                HotkeyListener hotkeyListener = new HotkeyListener(ipcClient, instrumentation);
                hotkeyListener.start();
                System.out.println("[ComplexAgent] Hazir. IPC: " + (ipcClient.isConnected() ? "bagli" : "yok"));
            }
        }, "complex-agent-main");
        thread.setDaemon(true);
        thread.start();
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        AgentMain.premain(agentArgs, instrumentation);
    }

    static void forceNonHeadless() {
        try {
            System.setProperty("java.awt.headless", "false");
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            for (Class<?> clazz = ge.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
                try {
                    Field field = clazz.getDeclaredField("headless");
                    field.setAccessible(true);
                    if (field.getType() == Boolean.class) {
                        field.set(ge, Boolean.FALSE);
                        break;
                    }
                    field.setBoolean(ge, false);
                    break;
                } catch (NoSuchFieldException nsfe) {
                    continue;
                }
            }
            try {
                Field field = GraphicsEnvironment.class.getDeclaredField("defaultHeadless");
                field.setAccessible(true);
                if (field.getType() == Boolean.class) {
                    field.set(null, Boolean.FALSE);
                } else {
                    field.setBoolean(null, false);
                }
            } catch (Exception ignore) {
            }
        } catch (Throwable ignore) {
        }
    }

    private static void forceWindowedMode() {
        try {
            Class<?> clazz = Class.forName("djz");
            Object mcInstance = null;
            try {
                Method m = clazz.getDeclaredMethod("C");
                m.setAccessible(true);
                mcInstance = m.invoke(null);
            } catch (Throwable t1) {
                try {
                    Method m = clazz.getDeclaredMethod("getInstance");
                    m.setAccessible(true);
                    mcInstance = m.invoke(null);
                } catch (Throwable t2) {
                    // yok
                }
            }
            if (mcInstance == null) {
                System.out.println("[ComplexAgent] MC instance bulunamadi, windowed mode zorlanamadi.");
                return;
            }
            // 1) fullscreen boolean alanini kapat
            boolean disabled = false;
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getType() != Boolean.TYPE) continue;
                field.setAccessible(true);
                boolean value;
                try {
                    value = field.getBoolean(mcInstance);
                } catch (Throwable t) {
                    continue;
                }
                if (!value) continue;
                field.setBoolean(mcInstance, false);
                System.out.println("[ComplexAgent] Fullscreen devre disi birakildi: " + field.getName());
                disabled = true;
                break;
            }
            if (!disabled) {
                System.out.println("[ComplexAgent] Fullscreen alani bulunamadi veya zaten pencere modu.");
            }
            // 2) Window tipli alandan GLFW handle bulup borderless pencere moduna gec
            try {
                Field windowField = null;
                for (Field field : clazz.getDeclaredFields()) {
                    String typeName = field.getType().getName();
                    if (typeName.contains("Window") || typeName.contains("window")) {
                        windowField = field;
                        break;
                    }
                }
                if (windowField != null) {
                    windowField.setAccessible(true);
                    Object windowObj = windowField.get(mcInstance);
                    if (windowObj != null) {
                        Class<?> windowClass = windowObj.getClass();
                        for (Method method : windowClass.getDeclaredMethods()) {
                            if (!method.getName().contains("ulll")
                                    && !method.getName().contains("setFullscreen")
                                    && !method.getName().contains("funfullscreen")) {
                                continue;
                            }
                            method.setAccessible(true);
                            method.invoke(windowObj, false);
                            System.out.println("[ComplexAgent] Window setFullscreen(false) cagirildi: " + method.getName());
                        }
                        try {
                            Field handleField = null;
                            for (Field field : windowClass.getDeclaredFields()) {
                                if (field.getType() != Long.TYPE) continue;
                                handleField = field;
                                break;
                            }
                            if (handleField != null) {
                                handleField.setAccessible(true);
                                long handle = handleField.getLong(windowObj);
                                if (handle != 0L) {
                                    System.out.println("[ComplexAgent] GLFW window handle: " + handle);
                                    try {
                                        Class<?> glfw = Class.forName("org.lwjgl.glfw.GLFW");
                                        Method m = glfw.getDeclaredMethod("glfwSetWindowMonitor",
                                                Long.TYPE, Long.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
                                        m.setAccessible(true);
                                        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                                        m.invoke(null, handle, 0L, 0, 0, screen.width, screen.height, 0);
                                        System.out.println("[ComplexAgent] GLFW borderless windowed: " + screen.width + "x" + screen.height);
                                    } catch (Throwable t) {
                                        System.out.println("[ComplexAgent] GLFW setWindowed hatasi: " + t.getMessage());
                                    }
                                }
                            }
                        } catch (Throwable t) {
                            System.out.println("[ComplexAgent] Window handle alinamadi: " + t.getMessage());
                        }
                    }
                }
            } catch (Throwable t) {
                System.out.println("[ComplexAgent] Window handling hatasi: " + t.getMessage());
            }
        } catch (Throwable t) {
            System.out.println("[ComplexAgent] forceWindowedMode hatasi: " + String.valueOf(t));
        }
    }
}
