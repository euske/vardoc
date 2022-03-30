package net.tabesugi.vardoc;

import java.nio.file.*;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.SourceRoot;

public class Extractor {
    public static void main(String[] args) {
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());

        Path path = Paths.get(args[0]);
        SourceRoot sourceRoot = new SourceRoot(path.getParent());
        CompilationUnit cu = sourceRoot.parse("", path.getFileName().toString());

        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(MethodDeclaration decl, Void arg) {
                Range range = decl.getRange().get();
                System.out.println(range);
                Javadoc javadoc = decl.getJavadoc().get();
                for (JavadocBlockTag tag : javadoc.getBlockTags()) {
                    System.out.println(tag);
                }
            }
        }, null);
    }
}
