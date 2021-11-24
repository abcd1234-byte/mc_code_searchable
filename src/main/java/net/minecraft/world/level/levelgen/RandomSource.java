package net.minecraft.world.level.levelgen;

public interface RandomSource {
   void setSeed(long pSeed);

   int nextInt();

   int nextInt(int pBound);

   long nextLong();

   boolean nextBoolean();

   float nextFloat();

   double nextDouble();

   double nextGaussian();

   default void consumeCount(int pCount) {
      for(int i = 0; i < pCount; ++i) {
         this.nextInt();
      }

   }
}