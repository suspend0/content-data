package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.Loader;
import ca.hullabaloo.content.api.Storage;
import ca.hullabaloo.content.api.StorageSpi;
import ca.hullabaloo.content.util.ImmutableHashInterner;
import ca.hullabaloo.content.util.InternSet;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;

public class ByIdLoader<T> implements Loader<T> {
  private final StorageSpi storage;
  private final Class<T> type;
  private final StorageTypes types;

  public ByIdLoader(Class<T> resultType, StorageSpi storage, StorageTypes types) {
    this.type = resultType;
    this.storage = storage;
    this.types = types;
  }

  @Override
  public List<T> getAll(final String... stringIds) {
    final InternSet<String> ids = ImmutableHashInterner.copyOf(stringIds);
    final Table<String, String, Object> values = HashBasedTable.create();
    final ImmutableMap<String, Class> fieldNames = types.properties(type);
    final InternSet<String> interner = ImmutableHashInterner.copyOf(fieldNames.keySet());
    Block.Reader reader = Block.reader(this.storage.data());
    reader.read(ImmutableSet.copyOf(fieldNames.values()), new Block.Sink() {
      @Override
      public boolean accept(Class whole, int id, Class fraction, String name, String value) {
        String fullId = types.id(whole, id);
        if (null != (fullId = ids.intern(fullId)) && fraction.equals(fieldNames.get(name))) {
          name = interner.intern(name);
          values.put(fullId, name, value);
        }
        return true;
      }

    });

    return new Result<T>(type, stringIds, values);
  }

  private static class Result<T> extends AbstractList<T> {
    private final Class<T> type;
    private final String[] ids;
    private final Table<String, String, Object> values;

    public Result(Class<T> type, String[] ids, Table<String, String, Object> values) {
      this.type = type;
      this.ids = ids;
      this.values = values;
    }

    @Override
    public T get(int index) {
      String id = ids[index];
      Map<String, Object> properties = values.rowMap().get(id);
      if (properties == null) {
        return null;
      }
      properties.put(Storage.ID_METHOD_NAME, id);
      return Values.make(type, properties);
    }

    @Override
    public int size() {
      return ids.length;
    }
  }
}
