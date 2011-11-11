package ca.hullabaloo.content.util;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;

/**
 * Things I use which were deprecated by guava-libraries
 */
public class Guava {

  /**
   * Deletes all the files within a directory. Does not delete the
   * directory itself.
   *
   * <p>If the file argument is a symbolic link or there is a symbolic
   * link in the path leading to the directory, this method will do
   * nothing. Symbolic links within the directory are not followed.
   *
   * @param directory the directory to delete the contents of
   * @throws IllegalArgumentException if the argument is not a directory
   * @throws java.io.IOException if an I/O error occurs
   */
  public static void deleteDirectoryContents(File directory)
      throws IOException {
    Preconditions.checkArgument(directory.isDirectory(),
        "Not a directory: %s", directory);
    // Symbolic links will have different canonical and absolute paths
    if (!directory.getCanonicalPath().equals(directory.getAbsolutePath())) {
      return;
    }
    File[] files = directory.listFiles();
    if (files == null) {
      throw new IOException("Error listing files for " + directory);
    }
    for (File file : files) {
      deleteRecursively(file);
    }
  }

  /**
   * Deletes a file or directory and all contents recursively.
   *
   * <p>If the file argument is a symbolic link the link will be deleted
   * but not the target of the link. If the argument is a directory,
   * symbolic links within the directory will not be followed.
   *
   * @param file the file to delete
   * @throws IOException if an I/O error occurs
   */
  public static void deleteRecursively(File file) throws IOException {
    if (file.isDirectory()) {
      deleteDirectoryContents(file);
    }
    if (!file.delete()) {
      throw new IOException("Failed to delete " + file);
    }
  }}
