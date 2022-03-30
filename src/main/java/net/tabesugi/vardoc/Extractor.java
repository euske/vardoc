package net.tabesugi.vardoc;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.SourceRoot;

public class Extractor {

    public static void doFile(Path path) throws IOException {
        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> result = parser.parse(path);
        if (!result.isSuccessful()) return;
        CompilationUnit cu = result.getResult().get();
        cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(MethodDeclaration decl, Void arg) {
                    Optional<Javadoc> javadoc = decl.getJavadoc();
                    if (javadoc.isPresent()) {
                        Range range = decl.getRange().get();
                        System.out.println(range);
                        for (JavadocBlockTag tag : javadoc.get().getBlockTags()) {
                            System.out.println(tag);
                        }
                    }
                }
            }, null);
    }

    public static void main(String[] args) throws IOException {
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());

        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.java");
        for (String arg1 : args) {
            Path path = Paths.get(arg1);
            Files.walkFileTree(
                path,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (matcher.matches(file.getFileName())) {
                            try {
                                doFile(file);
                            } catch (IOException e) {
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
        }
    }
}
