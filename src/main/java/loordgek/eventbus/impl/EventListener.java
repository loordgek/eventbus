package loordgek.eventbus.impl;

import loordgek.eventbus.api.Event;
import loordgek.eventbus.api.EventDummy;
import loordgek.eventbus.api.EventPriority;
import loordgek.eventbus.api.SubscribeEvent;
import loordgek.eventbus.api.Unpack;
import loordgek.eventbus.util.ASMParameterNames;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EventListener implements Opcodes {
    public static final ASMClassLoader LOADER = new ASMClassLoader();
    public static int IDs;
    private static final String HANDLER_DESC = Type.getInternalName(IEventListener.class);
    private static final String HANDLER_FUNC_DESC = Type.getMethodDescriptor(IEventListener.class.getDeclaredMethods()[0]);

    private EventListener() {
    }

    public static EventListenerSuplier createListener(Method method, SubscribeEvent subscribeEvent, boolean isStatic, Class<?> eventListener, Class<? extends Event> eventClass) {
        int invokeEvent = eventClass.isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL;

        String name = getUniqueName(method);
        String desc = name.replace('.', '/');
        String instType = Type.getInternalName(method.getDeclaringClass());
        String eventType = Type.getInternalName(eventClass);
        String priorityType = Type.getInternalName(EventPriority.class);
        ClassWriter cw = new ClassWriter(0);

        int maxStack = 1;

        MethodVisitor mv;

        cw.visit(52, ACC_SUPER | ACC_PUBLIC, desc, null, "java/lang/Object", new String[]{HANDLER_DESC});

        cw.visitSource(".dynamic", null);
        {
            if (!isStatic)
                cw.visitField(ACC_PUBLIC, "instance", "Ljava/lang/Object;", null, null).visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "<init>", isStatic ? "()V" : "(Ljava/lang/Object;)V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            if (!isStatic) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(PUTFIELD, desc, "instance", "Ljava/lang/Object;");
            }
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        if (subscribeEvent.priority() != EventPriority.NORMAL) {
            mv = cw.visitMethod(ACC_PUBLIC, "getPriority", "()Lloordgek/eventbus/api/EventPriority;", null, null);
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC, priorityType, subscribeEvent.priority().name(), "Lloordgek/eventbus/api/EventPriority;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "owner", "()Ljava/lang/Object;", null, null);
            mv.visitCode();
            if (isStatic)
                mv.visitLdcInsn(Type.getType(eventClass));
            else {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, desc, "instance", "Ljava/lang/Object;");
            }
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "invoke", HANDLER_FUNC_DESC, null, null);
            mv.visitCode();

            boolean unpack = subscribeEvent.eventClass() != EventDummy.class;

            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, "loordgek/eventbus/util/TestEvent");
            mv.visitVarInsn(ASTORE, 2);

            if (Event.Cancelable.class.isAssignableFrom(eventClass) && !subscribeEvent.receiveCanceled()) {
                mv.visitVarInsn(ALOAD, 2);
                //mv.visitTypeInsn(CHECKCAST, eventType);
                mv.visitMethodInsn(invokeEvent, eventType, "isCanceled", "()Z", eventClass.isInterface());
                Label l1 = new Label();
                mv.visitJumpInsn(IFEQ, l1);
                mv.visitInsn(RETURN);
                mv.visitLabel(l1);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            }
            mv.visitVarInsn(ALOAD, 2);
            //mv.visitTypeInsn(CHECKCAST, eventType);

            if (!isStatic) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, desc, "instance", "Ljava/lang/Object;");
                mv.visitTypeInsn(CHECKCAST, instType);
                maxStack++;
            }

            if (unpack) {
                List<Method> eventMethods = Arrays.stream(eventClass.getMethods())
                        .filter(method1 -> !(method1.getDeclaringClass() == Object.class))
                        .collect(Collectors.toList());

                Map<String, Method> methodMap = eventMethods.stream()
                        .filter(method1 -> method1.isAnnotationPresent(Unpack.class))
                        .collect(Collectors.toMap(method1 -> method1.getAnnotation(Unpack.class).value(), method1 -> method1, (a, b) -> b));

                for (String parameterNames : ASMParameterNames.getParameterNames(method)) {
                    Method method1 = methodMap.get(parameterNames);
                    if (method1 != null) {

                        mv.visitVarInsn(ALOAD, 2);
                        //mv.visitTypeInsn(CHECKCAST, eventType);
                        mv.visitMethodInsn(invokeEvent, eventType, method1.getName(), Type.getMethodDescriptor(method1), eventClass.isInterface());
                        maxStack++;
                    } else
                        throw new IllegalArgumentException("found a method parameter that can not be mapped to a event method");
                }

                mv.visitMethodInsn(isStatic ? INVOKESTATIC : INVOKEVIRTUAL, instType, method.getName(), Type.getMethodDescriptor(method), false);


                Class<?> returnClass = method.getReturnType();
                if (returnClass != Void.TYPE) {
                    Method returnMethod = null;
                    try {
                        if (subscribeEvent.methodReturnName().isEmpty()) {
                            for (Method method1 : eventMethods) {
                                if (method1.getParameterCount() == 1) {
                                    if (method1.getParameterTypes()[0] == returnClass)
                                        returnMethod = method1;
                                }
                            }
                        } else {
                            returnMethod = eventClass.getMethod(subscribeEvent.methodReturnName(), returnClass);
                        }
                        if (returnMethod == null)
                            throw new IllegalStateException();

                        mv.visitMethodInsn(invokeEvent, eventType, returnMethod.getName(), Type.getMethodDescriptor(returnMethod), eventClass.isInterface());
                    } catch (NoSuchMethodException | IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                mv.visitVarInsn(ALOAD, 2);
                //mv.visitTypeInsn(CHECKCAST, eventType);
                mv.visitMethodInsn(isStatic ? INVOKESTATIC : INVOKEVIRTUAL, instType, method.getName(), Type.getMethodDescriptor(method), false);
                maxStack++;
            }

            mv.visitInsn(RETURN);
            mv.visitMaxs(maxStack, 3);
            mv.visitEnd();

        }

        Class<?> clazz = LOADER.define(name, cw.toByteArray());

        if (isStatic) {
            return new staticEventListener(clazz);
        } else {
            return new eventListener(clazz);
        }
    }

    public static class staticEventListener implements EventListenerSuplier {
        private final Class<?> clazz;

        public staticEventListener(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public IEventListener get(Object target) {
            try {
                return (IEventListener) clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException();
            }
        }
    }

    public static class eventListener implements EventListenerSuplier {
        private final Class<?> clazz;

        public eventListener(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public IEventListener get(Object target) {
            try {
                return (IEventListener) clazz.getConstructor(Object.class).newInstance(target);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException();
            }
        }
    }

    private static String getUniqueName(Method callback) {
        return String.format("%s_%d_%s_%s_%s", EventListener.class.getName(), IDs++,
                callback.getDeclaringClass().getSimpleName(),
                callback.getName(),
                callback.getParameterTypes()[0].getSimpleName());
    }

    private static class ASMClassLoader extends ClassLoader {
        private ASMClassLoader() {
            super(ASMClassLoader.class.getClassLoader());
        }

        public Class<?> define(String name, byte[] data) {
            return defineClass(name, data, 0, data.length);
        }
    }
}