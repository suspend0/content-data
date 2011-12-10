package ca.hullabaloo.content.api;

import ca.hullabaloo.content.impl.storage.Block;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.*;

public class BlockTest {
  private Iterator<byte[]> simpleData() {
    List<byte[]> data = Lists.newArrayList();
    {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      Block.Writer w = Block.writer(out);
      w.write(String.class, 1, String.class, "a", "eh");
      w.write(String.class, 1, String.class, "b", "bee");
      w.write(String.class, 1, String.class, "c", "sea");
      data.add(out.toByteArray());
    }
    {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      Block.Writer w = Block.writer(out);
      w.write(Integer.class, 7, Integer.class, "x", "ex");
      w.write(String.class, 2, String.class, "a", "eh!");
      w.write(String.class, 2, String.class, "b", "bee!");
      data.add(out.toByteArray());
    }
    return data.iterator();
  }

  @Test
  public void testCount() {
    Iterator<byte[]> data = simpleData();
    Block.Reader r = Block.reader(data);
    int count = 0;
    while (r.advanceTo(ImmutableSet.of((Class) String.class))) {
      count++;
    }

    Assert.assertEquals(count, 5);
  }

  @Test
  public void testRead() {
    Block.Sink sink = mock(Block.Sink.class);
    when(sink.accept(any(Class.class), anyInt(), any(Class.class), any(String.class), any(String.class)))
        .thenReturn(true, false);

    Iterator<byte[]> data = simpleData();
    Block.Reader r = Block.reader(data);
    int count = r.read(ImmutableSet.of((Class) String.class), sink);

    Assert.assertEquals(count, 2);

    InOrder v = Mockito.inOrder(sink);
    v.verify(sink).accept(String.class, 1, String.class, "a", "eh");
    v.verify(sink).accept(String.class, 1, String.class, "b", "bee");
    Mockito.verifyNoMoreInteractions(sink);
  }
}
