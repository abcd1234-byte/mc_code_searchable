package net.minecraft.core;

public final class QuartPos {
   public static final int BITS = 2;
   public static final int SIZE = 4;
   private static final int SECTION_TO_QUARTS_BITS = 2;

   private QuartPos() {
   }

   public static int fromBlock(int pValue) {
      return pValue >> 2;
   }

   public static int toBlock(int pValue) {
      return pValue << 2;
   }

   public static int fromSection(int pValue) {
      return pValue << 2;
   }

   public static int toSection(int pValue) {
      return pValue >> 2;
   }
}