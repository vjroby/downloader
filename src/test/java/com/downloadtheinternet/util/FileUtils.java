package com.downloadtheinternet.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

public class FileUtils {

    public static void deleteFolder(String path) {
        Path rootPath = Paths.get(path);
        try {
            Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .peek(System.out::println)
                    .forEach(File::delete);
        } catch (NoSuchFileException e) {
            // do nothing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
