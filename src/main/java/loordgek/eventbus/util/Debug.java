package loordgek.eventbus.util;


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class Debug {
    public static byte[] generatebytefile(Consumer<ClassVisitor> consumer) {
        Path targetPath = Paths.get("D:\\");
        String clName = "TestClass", srcName = clName + ".jasm", binName = clName + ".class";
        Path srcFile = targetPath.resolve(srcName), binFile = targetPath.resolve(binName);
        ClassWriter c = new ClassWriter(0);

        try (PrintWriter sourceWriter = new PrintWriter(Files.newBufferedWriter(srcFile))) {
            TraceClassVisitor classWriter = new TraceClassVisitor(c, new Textifier(), sourceWriter);
            consumer.accept(classWriter);
            classWriter.visitSource(srcName, null);
            classWriter.visitEnd(); // writes the buffered text
            byte[] bytes = c.toByteArray();
            Files.write(binFile, bytes);
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
