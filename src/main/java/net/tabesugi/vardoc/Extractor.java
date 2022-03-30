package net.tabesugi.vardoc;

import java.io.*;
import java.nio.file.*;
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

    public static void main(String[] args) throws IOException {
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());

        JavaParser parser = new JavaParser();
        for (String arg1 : args) {
            Path path = Paths.get(arg1);
            ParseResult<CompilationUnit> result = parser.parse(path);
            if (result.isSuccessful()) {
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
        }
    }
}
