package ca.hullabaloo.content.api;

import ca.hullabaloo.content.impl.storage.Block;
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
      w.write(10, 1, "a", "eh");
      w.write(10, 1, "b", "bee");
      w.write(10, 1, "c", "sea");
      data.add(out.toByteArray());
    }
    {
      ByteArrayDataOutput out = ByteStreams.newDataOutput();
      Block.Writer w = Block.writer(out);
      w.write(11, 7, "x", "ex");
      w.write(10, 2, "a", "eh!");
      w.write(10, 2, "b", "bee!");
      data.add(out.toByteArray());
    }
    return data.iterator();
  }

  @Test
  public void testCount() {
    Iterator<byte[]> data = simpleData();
    Block.Reader r = Block.reader(data);
    int count = 0;
    while (r.advanceTo(new int[]{10})) {
      count++;
    }

    Assert.assertEquals(count, 5);
  }

  @Test
  public void testRead() {
    Block.Sink sink = mock(Block.Sink.class);
    when(sink.accept(anyInt(), any(String.class), any(String.class)))
        .thenReturn(true, false);

    Iterator<byte[]> data = simpleData();
    Block.Reader r = Block.reader(data);
    int count = r.read(new int[]{10}, sink);

    Assert.assertEquals(count, 2);

    InOrder v = Mockito.inOrder(sink);
    v.verify(sink).accept(1, "a", "eh");
    v.verify(sink).accept(1, "b", "bee");
    Mockito.verifyNoMoreInteractions(sink);
  }
}
