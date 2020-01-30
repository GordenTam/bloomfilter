package org.gorden.bloomfilter.core.hash;

public interface HashFunction {

    Hasher newHasher();

    byte[] hashBytes(byte[] var1);

    byte[] hashBytes(byte[] var1, int var2, int var3);
}
