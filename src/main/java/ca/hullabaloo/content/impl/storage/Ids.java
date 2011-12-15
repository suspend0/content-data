package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.impl.Id;
import ca.hullabaloo.content.impl.StoredAnnotation;
import ca.hullabaloo.content.util.ImmutableHashInterner;
import ca.hullabaloo.content.util.InternSet;
import com.google.common.base.Function;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Lists;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

import static com.google.common.base.Preconditions.checkNotNull;

public class Ids {
  private volatile BiMap<Class, String> mapping = ImmutableBiMap.of();

  public synchronized void register(Class<?> type) {
    String key = StoredAnnotation.getKey(type);
    mapping = ImmutableBiMap.<Class, String>builder().putAll(mapping).put(type, key).build();
  }

  public String id(Class<?> type, int id) {
    return checkNotNull(mapping.get(type), "not registered; %s", type) + '-' + id;
  }

  public List<Id> id(List<String> ids) {
    return new IdList(ids);
  }

  private final Function<String, Id> F = new Function<String, Id>() {
    @Override
    public Id apply(String input) {
      int idx = input.indexOf('-');
      String key = input.substring(0, idx);
      int id = Integer.valueOf(input.substring(idx + 1));
      Class type = checkNotNull(mapping.inverse().get(key), "key:%s", key);
      return new Id(type, id);
    }
  };

  private class IdList extends AbstractList<Id> implements RandomAccess {
    private final InternSet<String> interner;
    private final List<Id> ids;

    public IdList(List<String> ids) {
      ids = ids instanceof RandomAccess ? ids : Lists.newArrayList(ids);
      this.interner = ImmutableHashInterner.copyOf(ids);
      this.ids = Lists.transform(ids, F);
    }

    @Override
    public Id get(int index) {
      return this.ids.get(index);
    }

    @Override
    public int size() {
      return ids.size();
    }

    @Override
    public boolean contains(Object o) {
      if (o instanceof Id) {
        Id that = (Id) o;
        String str = id(that.type, that.id);
        return interner.intern(str) != null;
      }
      return false;
    }
  }
}
