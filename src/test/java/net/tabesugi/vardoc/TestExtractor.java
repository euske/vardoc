//  TestExtractor.java
//
package net.tabesugi.vardoc;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.net.*;
import java.util.*;
import java.util.stream.*;

import org.junit.Test;
import org.junit.Assert;

public class TestExtractor {

    private Path getPath(String name) throws IOException {
        try {
            return Paths.get(getClass().getClassLoader().getResource(name).toURI());
        } catch (URISyntaxException e) {
            throw new IOException(name);
        }
    }

    @Test
    public void test1() throws IOException {
        Path inputPath = getPath("Foo.java");
        Extractor extractor = new Extractor();
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        extractor.out = new BufferedWriter(new OutputStreamWriter(buf));
        extractor.doFile(inputPath);
        Stream<String> outputLines = buf.toString().lines();
        Path refPath = getPath("Foo.txt");
        Stream<String> refLines = Files.lines(
            refPath, Charset.defaultCharset());
        Assert.assertArrayEquals(outputLines.toArray(), refLines.toArray());
    }
}
