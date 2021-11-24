package net.minecraft.world.level.levelgen;

import java.util.Random;

public class WorldgenRandom extends Random implements RandomSource {
   private int count;

   public WorldgenRandom() {
   }

   public WorldgenRandom(long pSeed) {
      super(pSeed);
   }

   public int getCount() {
      return this.count;
   }

   public int next(int pBits) {
      ++this.count;
      return super.next(pBits);
   }

   public long setBaseChunkSeed(int pChunkX, int pChunkZ) {
      long i = (long)pChunkX * 341873128712L + (long)pChunkZ * 132897987541L;
      this.setSeed(i);
      return i;
   }

   public long setDecorationSeed(long pLevelSeed, int pMinChunkBlockX, int pMinChunkBlockZ) {
      this.setSeed(pLevelSeed);
      long i = this.nextLong() | 1L;
      long j = this.nextLong() | 1L;
      long k = (long)pMinChunkBlockX * i + (long)pMinChunkBlockZ * j ^ pLevelSeed;
      this.setSeed(k);
      return k;
   }

   public long setFeatureSeed(long pDecorationSeed, int pIndex, int pDecorationStep) {
      long i = pDecorationSeed + (long)pIndex + (long)(10000 * pDecorationStep);
      this.setSeed(i);
      return i;
   }

   public long setLargeFeatureSeed(long pBaseSeed, int pChunkX, int pChunkZ) {
      this.setSeed(pBaseSeed);
      long i = this.nextLong();
      long j = this.nextLong();
      long k = (long)pChunkX * i ^ (long)pChunkZ * j ^ pBaseSeed;
      this.setSeed(k);
      return k;
   }

   public long setBaseStoneSeed(long pLevelSeed, int pX, int pY, int pZ) {
      this.setSeed(pLevelSeed);
      long i = this.nextLong();
      long j = this.nextLong();
      long k = this.nextLong();
      long l = (long)pX * i ^ (long)pY * j ^ (long)pZ * k ^ pLevelSeed;
      this.setSeed(l);
      return l;
   }

   public long setLargeFeatureWithSalt(long pLevelSeed, int pRegionX, int pRegionZ, int pSalt) {
      long i = (long)pRegionX * 341873128712L + (long)pRegionZ * 132897987541L + pLevelSeed + (long)pSalt;
      this.setSeed(i);
      return i;
   }

   public static Random seedSlimeChunk(int pChunkX, int pChunkZ, long pLevelSeed, long pSalt) {
      return new Random(pLevelSeed + (long)(pChunkX * pChunkX * 4987142) + (long)(pChunkX * 5947611) + (long)(pChunkZ * pChunkZ) * 4392871L + (long)(pChunkZ * 389711) ^ pSalt);
   }
}