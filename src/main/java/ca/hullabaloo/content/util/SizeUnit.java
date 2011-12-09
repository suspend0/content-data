package ca.hullabaloo.content.util;

public enum SizeUnit {
  BYTE {
    @Override
    public long toBytes(long s) {
      return s;
    }

    @Override
    public long toKiloBytes(long s) {
      return s / 1024;
    }

    @Override
    public long toMegabytes(long s) {
      return s / 1024 / 1024;
    }

    @Override
    public long toGigabytes(long s) {
      return s / 1024 / 1024 / 1024;
    }
  },

  KB {
    @Override
    public long toBytes(long s) {
      return s * 1024;
    }

    @Override
    public long toKiloBytes(long s) {
      return s;
    }

    @Override
    public long toMegabytes(long s) {
      return s / 1024;
    }

    @Override
    public long toGigabytes(long s) {
      return s / 1024 / 1024;
    }
  },

  MB {
    @Override
    public long toBytes(long s) {
      return s * 1024 * 1024;
    }

    @Override
    public long toKiloBytes(long s) {
      return s * 1024;
    }

    @Override
    public long toMegabytes(long s) {
      return s;
    }

    @Override
    public long toGigabytes(long s) {
      return s / 1024;
    }
  },

  GB {
    @Override
    public long toBytes(long s) {
      return s * 1024 * 1024 * 1024;
    }

    @Override
    public long toKiloBytes(long s) {
      return s * 1024 * 1024;
    }

    @Override
    public long toMegabytes(long s) {
      return s * 1024;
    }

    @Override
    public long toGigabytes(long s) {
      return s;
    }
  };

  public abstract long toBytes(long s);

  public abstract long toKiloBytes(long s);

  public abstract long toMegabytes(long s);

  public abstract long toGigabytes(long s);
}
