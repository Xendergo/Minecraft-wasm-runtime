package net.fabricmc.wasmruntime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Modules {
  public static void LoadModule(File path) throws IOException {
    FileInputStream stream = new FileInputStream(path);

    byte[] bytes = stream.readAllBytes();

    stream.close();

    System.out.println(bytes[1]);
  }
}
