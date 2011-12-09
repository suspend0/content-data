package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.Loader;
import ca.hullabaloo.content.api.Storage;
import ca.hullabaloo.content.api.StorageSpi;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Interner;
import com.google.common.collect.Table;

import java.util.*;

public class ByIdLoader<T> implements Loader<T> {
  private final StorageSpi storage;
  private final Class<T> type;

  public ByIdLoader(StorageSpi storage, Class<T> resultType) {
    this.storage = storage;
    this.type = resultType;
  }

  @Override
  public List<T> getAll(final int... ids) {
    Arrays.sort(ids);

    final Table<Integer, String, Object> values = HashBasedTable.create();
    final Interner<String> fieldNames = storage.properties(type);
    Block.Reader reader = Block.reader(this.storage.data());
    reader.read(storage.ids(type), new Block.Sink() {
      @Override
      public boolean accept(int id, String name, String value) {
        if (Arrays.binarySearch(ids, id) >= 0 && null != (name = fieldNames.intern(name))) {
          values.put(id, name, value);
        }
        return true;
      }
    });

    return new Result<T>(type, ids, values);
  }

  private static class Result<T> extends AbstractList<T> implements RandomAccess {
    private final Class<T> type;
    private final int[] ids;
    private final Table<Integer, String, Object> values;

    public Result(Class<T> type, int[] ids, Table<Integer, String, Object> values) {
      this.type = type;
      this.ids = ids;
      this.values = values;
    }

    @Override
    public T get(int index) {
      int id = ids[index];
      Map<String, Object> map = values.rowMap().get(id);
      if (map == null) {
        return null;
      }
      map.put(Storage.ID_METHOD_NAME, id);
      return Values.make(type, map);
    }

    @Override
    public int size() {
      return ids.length;
    }
  }
}
