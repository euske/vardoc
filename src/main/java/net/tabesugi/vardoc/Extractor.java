//  Extractor.java
//
package net.tabesugi.vardoc;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.visitor.*;
import com.github.javaparser.javadoc.*;
import com.github.javaparser.javadoc.description.*;
import com.github.javaparser.utils.*;

public class Extractor {

    private JavaParser parser = new JavaParser();

    public void doFile(Path path) throws IOException {
        ParseResult<CompilationUnit> result = parser.parse(path);
        if (!result.isSuccessful()) return;
        CompilationUnit cu = result.getResult().get();
        cu.accept(
            new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(MethodDeclaration decl, Void arg) {
                    Optional<Javadoc> javadoc = decl.getJavadoc();
                    if (!javadoc.isPresent()) return;
                    Range range = decl.getRange().get();
                    for (JavadocBlockTag tag : javadoc.get().getBlockTags()) {
                        show(range, tag);
                    }
                }
                @Override
                public void visit(FieldDeclaration decl, Void arg) {
                    Optional<Javadoc> javadoc = decl.getJavadoc();
                    if (!javadoc.isPresent()) return;
                    Range range = decl.getRange().get();
                    showField(range, javadoc.get().getDescription());
                }
            }, null);
    }

    public void show(Range range, JavadocBlockTag tag) {
        System.out.println(range);
        System.out.println(tag);
    }

    public void showField(Range range, JavadocDescription desc) {
        System.out.println(range);
        System.out.println(desc);
    }

    public static void main(String[] args) throws IOException {
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());

        Extractor extractor = new Extractor();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.java");
        for (String arg1 : args) {
            Path path = Paths.get(arg1);
            Files.walkFileTree(
                path,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(
                        Path file, BasicFileAttributes attrs) {
                        if (matcher.matches(file.getFileName())) {
                            try {
                                extractor.doFile(file);
                            } catch (IOException e) {
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
        }
    }
}
