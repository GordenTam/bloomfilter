package org.gorden.bloomfilter.core.hash;

public interface Hasher {

    byte[] hashBytes(byte[] var1);

    byte[] hashBytes(byte[] var1, int var2, int var3);
}
