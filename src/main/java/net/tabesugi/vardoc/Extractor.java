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
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.*;
import com.github.javaparser.javadoc.*;
import com.github.javaparser.javadoc.description.*;
import com.github.javaparser.utils.*;

public class Extractor {

    public enum ValueType {
        NONE,
        PARAM,
        RETURN,
        FIELD,
    }

    private Path basedir = null;
    private JavaParser parser = new JavaParser();

    private static String posToStr(Position pos) {
        // Fix 1-based indices.
        return ((pos.line - Position.FIRST_LINE)+","+
                (pos.column - Position.FIRST_COLUMN));
    }

    private static String formatText(String text) {
        return text.strip().replaceAll("\\s+", " ");
    }

    private Path getRelPath(Path path) {
        if (basedir == null) {
            return path;
        } else {
            return basedir.relativize(path);
        }
    }

    private void show1(ValueType type, Path path, SimpleName name, JavadocDescription desc) {
        Range range = name.getRange().get();
        System.out.println(
            "+"+type.toString()+
            " "+getRelPath(path).toString()+
            " "+posToStr(range.begin)+
            " "+name.toString()+
            " "+formatText(desc.toText()));
    }

    private void doMethodDecl(Path path, MethodDeclaration decl) {
        Optional<Javadoc> javadoc = decl.getJavadoc();
        if (!javadoc.isPresent()) return;
        for (JavadocBlockTag tag : javadoc.get().getBlockTags()) {
            switch (tag.getType()) {
            case PARAM:
                if (tag.getName().isPresent()) {
                    String name = tag.getName().get();
                    for (Parameter param : decl.getParameters()) {
                        if (param.getName().toString().equals(name)) {
                            show1(ValueType.PARAM, path, param.getName(), tag.getContent());
                        }
                    }
                }
                break;
            case RETURN:
                show1(ValueType.RETURN, path, decl.getName(), tag.getContent());
                break;
            }
        }
    }

    private void doFieldDecl(Path path, FieldDeclaration decl) {
        Optional<Javadoc> javadoc = decl.getJavadoc();
        if (!javadoc.isPresent()) return;
        NodeList<VariableDeclarator> vars = decl.getVariables();
        if (vars.size() != 1) return;
        SimpleName name = vars.get(0).getName();
        show1(ValueType.FIELD, path, name, javadoc.get().getDescription());
    }

    public Extractor(Path basedir) {
        this.basedir = basedir;
    }

    public void doFile(Path path) throws IOException {
        ParseResult<CompilationUnit> result = parser.parse(path);
        if (!result.isSuccessful()) {
            System.err.println("Error: "+path);
            return;
        }
        System.err.println("Parsed: "+path);
        CompilationUnit cu = result.getResult().get();
        cu.accept(
            new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(MethodDeclaration decl, Void arg) {
                    doMethodDecl(path, decl);
                }
                @Override
                public void visit(FieldDeclaration decl, Void arg) {
                    doFieldDecl(path, decl);
                }
            }, null);
    }

    public static void main(String[] args) throws IOException {
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());

        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.java");
        for (String arg1 : args) {
            Path path = Paths.get(arg1);
            Extractor extractor = new Extractor(
                Files.isDirectory(path)? path : path.getParent());
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
