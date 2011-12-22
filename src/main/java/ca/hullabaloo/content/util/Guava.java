package ca.hullabaloo.content.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Maps;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Things I use which were deprecated or not implemented by guava-libraries
 */
public class Guava {

  /**
   * Deletes all the files within a directory. Does not delete the
   * directory itself.
   * <p/>
   * <p>If the file argument is a symbolic link or there is a symbolic
   * link in the path leading to the directory, this method will do
   * nothing. Symbolic links within the directory are not followed.
   *
   * @param directory the directory to delete the contents of
   * @throws IllegalArgumentException if the argument is not a directory
   * @throws java.io.IOException      if an I/O error occurs
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
   * <p/>
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
  }

  public static <T> Supplier<T> supplier(final Future<T> future) {
    return new Supplier<T>() {
      @Override
      public T get() {
        try {
          return future.get();
        } catch (InterruptedException e) {
          throw Throwables.propagate(e);
        } catch (ExecutionException e) {
          throw Throwables.propagate(e);
        }
      }
    };
  }

  /**
   * The result is the length of the shorter of the two iterable.
   */
  public static <A, B> Iterable<Map.Entry<A, B>> zip(final Iterable<A> a, final Iterable<B> b) {
    return new Iterable<Map.Entry<A, B>>() {
      @Override
      public Iterator<Map.Entry<A, B>> iterator() {
        return zip(a.iterator(), b.iterator());
      }
    };
  }

  /**
   * The result is the length of the shorter of the two iterators.
   */
  public static <A, B> Iterator<Map.Entry<A, B>> zip(final Iterator<A> a, final Iterator<B> b) {
    return new AbstractIterator<Map.Entry<A, B>>() {
      @Override
      protected Map.Entry<A, B> computeNext() {
        if (a.hasNext() && b.hasNext()) {
          return Maps.immutableEntry(a.next(), b.next());
        }
        return endOfData();
      }
    };
  }
}
