/*
  Derived from twitter/snowflake
  http://www.apache.org/licenses/LICENSE-2.0
*/
package ca.hullabaloo.content.util;

import static com.google.common.base.Preconditions.checkArgument;

public class Sequence {
  // Oct 18 2011, a bit after 1am
  private static final long epoch = 1318900702323L;
  private static final long workerIdBits = 5L;
  private static final long clusterIdBits = 5L;
  private static final long maxWorkerId = ~(-1L << workerIdBits);
  private static final long maxClusterId = ~(-1L << clusterIdBits);
  private static final long sequenceBits = 12L;

  private static final long workerIdShift = sequenceBits;
  private static final long clusterIdShift = sequenceBits + workerIdBits;
  private static final long timestampLeftShift = sequenceBits + workerIdBits + clusterIdBits;
  private static final int sequenceMask = ~(-1 << sequenceBits);

  private final Time time;
  private final int clusterId;
  private final int workerId;
  private int sequence = 0;
  private long lastTimestamp = -1L;

  public Sequence(Time time, int clusterId, int workerId) {
    this.time = time;
    this.clusterId = clusterId;
    this.workerId = workerId;
    // sanity check for workerId
    checkArgument(workerId <= maxWorkerId && workerId >= 0,
        "worker Id can't be greater than %s or less than 0", maxWorkerId);

    checkArgument(clusterId <= maxClusterId && clusterId >= 0,
        "cluster Id can't be greater than %s or less than 0", maxClusterId);
  }

  public synchronized long next() {
    long timestamp = timeGen();

    if (lastTimestamp == timestamp) {
      sequence = (sequence + 1) & sequenceMask;
      if (sequence == 0) {
        timestamp = tilNextMillis(lastTimestamp);
      }
    } else {
      sequence = 0;
    }

    if (timestamp < lastTimestamp) {
      throw new InvalidSystemClock(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
    }

    lastTimestamp = timestamp;
    return ((timestamp - epoch) << timestampLeftShift) |
        (clusterId << clusterIdShift) |
        (workerId << workerIdShift) |
        sequence;
  }

  protected long tilNextMillis(long lastTimestamp) {
    long timestamp = timeGen();
    while (timestamp <= lastTimestamp) {
      timestamp = timeGen();
    }
    return timestamp;
  }

  private long timeGen() {
    return time.now();
  }

  public static class InvalidSystemClock extends RuntimeException {
    public InvalidSystemClock(String message) {
      super(message);
    }
  }
}
