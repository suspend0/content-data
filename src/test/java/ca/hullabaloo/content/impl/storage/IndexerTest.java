package ca.hullabaloo.content.impl.storage;

import ca.hullabaloo.content.api.IdSet;
import ca.hullabaloo.content.api.StorageSpi;
import ca.hullabaloo.content.impl.ArrayIdSet;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterators;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IndexerTest {
  @Test
  public void run() {
    Block.Writer<ByteArrayDataOutput> writer = Block.writer(ByteStreams.newDataOutput());
    writer.write(String.class, 1, String.class, "foo", "1");
    writer.write(String.class, 1, String.class, "bar", "1");
    byte[] data = writer.getOutput().toByteArray();

    StorageSpi spi = Mockito.mock(StorageSpi.class);
    Mockito.when(spi.data()).thenReturn(Iterators.singletonIterator(data));

    Indexer indexer = new Indexer(spi);
    Supplier<IdSet<String>> idx = indexer.getIndex(String.class, "foo", Predicates.alwaysTrue());
    IdSet<String> ids = idx.get();

    Assert.assertEquals(ids, ArrayIdSet.of(1));
  }
}
