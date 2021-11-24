package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public interface Aquifer {
   int ALWAYS_LAVA_AT_OR_BELOW_Y_INDEX = 9;
   int ALWAYS_USE_SEA_LEVEL_WHEN_ABOVE = 30;

   static Aquifer create(ChunkPos pChunkPos, NormalNoise pBarrierNoise, NormalNoise pWaterLevelNoise, NormalNoise pLavaNoise, NoiseGeneratorSettings pNoiseGeneratorSettings, NoiseSampler pSampler, int pMinY, int pHeight) {
      return new Aquifer.NoiseBasedAquifer(pChunkPos, pBarrierNoise, pWaterLevelNoise, pLavaNoise, pNoiseGeneratorSettings, pSampler, pMinY, pHeight);
   }

   static Aquifer createDisabled(final int pSeaLevel, final BlockState pDefaultFluid) {
      return new Aquifer() {
         public BlockState computeState(BaseStoneSource p_157980_, int p_157981_, int p_157982_, int p_157983_, double p_157984_) {
            if (p_157984_ > 0.0D) {
               return p_157980_.getBaseBlock(p_157981_, p_157982_, p_157983_);
            } else {
               return p_157982_ >= pSeaLevel ? Blocks.AIR.defaultBlockState() : pDefaultFluid;
            }
         }

         public boolean shouldScheduleFluidUpdate() {
            return false;
         }
      };
   }

   BlockState computeState(BaseStoneSource pStoneSource, int pX, int pY, int pZ, double pNoise);

   boolean shouldScheduleFluidUpdate();

   public static class NoiseBasedAquifer implements Aquifer {
      private static final int X_RANGE = 10;
      private static final int Y_RANGE = 9;
      private static final int Z_RANGE = 10;
      private static final int X_SEPARATION = 6;
      private static final int Y_SEPARATION = 3;
      private static final int Z_SEPARATION = 6;
      private static final int X_SPACING = 16;
      private static final int Y_SPACING = 12;
      private static final int Z_SPACING = 16;
      private final NormalNoise barrierNoise;
      private final NormalNoise waterLevelNoise;
      private final NormalNoise lavaNoise;
      private final NoiseGeneratorSettings noiseGeneratorSettings;
      private final Aquifer.NoiseBasedAquifer.AquiferStatus[] aquiferCache;
      private final long[] aquiferLocationCache;
      private boolean shouldScheduleFluidUpdate;
      private final NoiseSampler sampler;
      private final int minGridX;
      private final int minGridY;
      private final int minGridZ;
      private final int gridSizeX;
      private final int gridSizeZ;

      NoiseBasedAquifer(ChunkPos pChunkPos, NormalNoise pBarrierNoise, NormalNoise pWaterLevelNoise, NormalNoise pLavaNoise, NoiseGeneratorSettings pNoiseGeneratorSettings, NoiseSampler pSampler, int pMinY, int pHeight) {
         this.barrierNoise = pBarrierNoise;
         this.waterLevelNoise = pWaterLevelNoise;
         this.lavaNoise = pLavaNoise;
         this.noiseGeneratorSettings = pNoiseGeneratorSettings;
         this.sampler = pSampler;
         this.minGridX = this.gridX(pChunkPos.getMinBlockX()) - 1;
         int i = this.gridX(pChunkPos.getMaxBlockX()) + 1;
         this.gridSizeX = i - this.minGridX + 1;
         this.minGridY = this.gridY(pMinY) - 1;
         int j = this.gridY(pMinY + pHeight) + 1;
         int k = j - this.minGridY + 1;
         this.minGridZ = this.gridZ(pChunkPos.getMinBlockZ()) - 1;
         int l = this.gridZ(pChunkPos.getMaxBlockZ()) + 1;
         this.gridSizeZ = l - this.minGridZ + 1;
         int i1 = this.gridSizeX * k * this.gridSizeZ;
         this.aquiferCache = new Aquifer.NoiseBasedAquifer.AquiferStatus[i1];
         this.aquiferLocationCache = new long[i1];
         Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
      }

      private int getIndex(int pGridX, int pGridY, int pGridZ) {
         int i = pGridX - this.minGridX;
         int j = pGridY - this.minGridY;
         int k = pGridZ - this.minGridZ;
         return (j * this.gridSizeZ + k) * this.gridSizeX + i;
      }

      public BlockState computeState(BaseStoneSource pStoneSource, int pX, int pY, int pZ, double pNoise) {
         if (pNoise <= 0.0D) {
            double d0;
            BlockState blockstate;
            boolean flag;
            if (this.isLavaLevel(pY)) {
               blockstate = Blocks.LAVA.defaultBlockState();
               d0 = 0.0D;
               flag = false;
            } else {
               int i = Math.floorDiv(pX - 5, 16);
               int j = Math.floorDiv(pY + 1, 12);
               int k = Math.floorDiv(pZ - 5, 16);
               int l = Integer.MAX_VALUE;
               int i1 = Integer.MAX_VALUE;
               int j1 = Integer.MAX_VALUE;
               long k1 = 0L;
               long l1 = 0L;
               long i2 = 0L;

               for(int j2 = 0; j2 <= 1; ++j2) {
                  for(int k2 = -1; k2 <= 1; ++k2) {
                     for(int l2 = 0; l2 <= 1; ++l2) {
                        int i3 = i + j2;
                        int j3 = j + k2;
                        int k3 = k + l2;
                        int l3 = this.getIndex(i3, j3, k3);
                        long j4 = this.aquiferLocationCache[l3];
                        long i4;
                        if (j4 != Long.MAX_VALUE) {
                           i4 = j4;
                        } else {
                           WorldgenRandom worldgenrandom = new WorldgenRandom(Mth.getSeed(i3, j3 * 3, k3) + 1L);
                           i4 = BlockPos.asLong(i3 * 16 + worldgenrandom.nextInt(10), j3 * 12 + worldgenrandom.nextInt(9), k3 * 16 + worldgenrandom.nextInt(10));
                           this.aquiferLocationCache[l3] = i4;
                        }

                        int j5 = BlockPos.getX(i4) - pX;
                        int k4 = BlockPos.getY(i4) - pY;
                        int l4 = BlockPos.getZ(i4) - pZ;
                        int i5 = j5 * j5 + k4 * k4 + l4 * l4;
                        if (l >= i5) {
                           i2 = l1;
                           l1 = k1;
                           k1 = i4;
                           j1 = i1;
                           i1 = l;
                           l = i5;
                        } else if (i1 >= i5) {
                           i2 = l1;
                           l1 = i4;
                           j1 = i1;
                           i1 = i5;
                        } else if (j1 >= i5) {
                           i2 = i4;
                           j1 = i5;
                        }
                     }
                  }
               }

               Aquifer.NoiseBasedAquifer.AquiferStatus aquifer$noisebasedaquifer$aquiferstatus = this.getAquiferStatus(k1);
               Aquifer.NoiseBasedAquifer.AquiferStatus aquifer$noisebasedaquifer$aquiferstatus1 = this.getAquiferStatus(l1);
               Aquifer.NoiseBasedAquifer.AquiferStatus aquifer$noisebasedaquifer$aquiferstatus2 = this.getAquiferStatus(i2);
               double d6 = this.similarity(l, i1);
               double d7 = this.similarity(l, j1);
               double d8 = this.similarity(i1, j1);
               flag = d6 > 0.0D;
               if (aquifer$noisebasedaquifer$aquiferstatus.fluidLevel >= pY && aquifer$noisebasedaquifer$aquiferstatus.fluidType.is(Blocks.WATER) && this.isLavaLevel(pY - 1)) {
                  d0 = 1.0D;
               } else if (d6 > -1.0D) {
                  double d9 = 1.0D + (this.barrierNoise.getValue((double)pX, (double)pY, (double)pZ) + 0.05D) / 4.0D;
                  double d10 = this.calculatePressure(pY, d9, aquifer$noisebasedaquifer$aquiferstatus, aquifer$noisebasedaquifer$aquiferstatus1);
                  double d11 = this.calculatePressure(pY, d9, aquifer$noisebasedaquifer$aquiferstatus, aquifer$noisebasedaquifer$aquiferstatus2);
                  double d1 = this.calculatePressure(pY, d9, aquifer$noisebasedaquifer$aquiferstatus1, aquifer$noisebasedaquifer$aquiferstatus2);
                  double d2 = Math.max(0.0D, d6);
                  double d3 = Math.max(0.0D, d7);
                  double d4 = Math.max(0.0D, d8);
                  double d5 = 2.0D * d2 * Math.max(d10, Math.max(d11 * d3, d1 * d4));
                  d0 = Math.max(0.0D, d5);
               } else {
                  d0 = 0.0D;
               }

               blockstate = pY >= aquifer$noisebasedaquifer$aquiferstatus.fluidLevel ? Blocks.AIR.defaultBlockState() : aquifer$noisebasedaquifer$aquiferstatus.fluidType;
            }

            if (pNoise + d0 <= 0.0D) {
               this.shouldScheduleFluidUpdate = flag;
               return blockstate;
            }
         }

         this.shouldScheduleFluidUpdate = false;
         return pStoneSource.getBaseBlock(pX, pY, pZ);
      }

      public boolean shouldScheduleFluidUpdate() {
         return this.shouldScheduleFluidUpdate;
      }

      private boolean isLavaLevel(int pY) {
         return pY - this.noiseGeneratorSettings.noiseSettings().minY() <= 9;
      }

      private double similarity(int pFirstDistance, int pSecondDistance) {
         double d0 = 25.0D;
         return 1.0D - (double)Math.abs(pSecondDistance - pFirstDistance) / 25.0D;
      }

      private double calculatePressure(int pY, double pBarrierNoiseValue, Aquifer.NoiseBasedAquifer.AquiferStatus pFirstAquifer, Aquifer.NoiseBasedAquifer.AquiferStatus pSecondAquifer) {
         if (pY <= pFirstAquifer.fluidLevel && pY <= pSecondAquifer.fluidLevel && pFirstAquifer.fluidType != pSecondAquifer.fluidType) {
            return 1.0D;
         } else {
            int i = Math.abs(pFirstAquifer.fluidLevel - pSecondAquifer.fluidLevel);
            double d0 = 0.5D * (double)(pFirstAquifer.fluidLevel + pSecondAquifer.fluidLevel);
            double d1 = Math.abs(d0 - (double)pY - 0.5D);
            return 0.5D * (double)i * pBarrierNoiseValue - d1;
         }
      }

      private int gridX(int pX) {
         return Math.floorDiv(pX, 16);
      }

      private int gridY(int pY) {
         return Math.floorDiv(pY, 12);
      }

      private int gridZ(int pZ) {
         return Math.floorDiv(pZ, 16);
      }

      private Aquifer.NoiseBasedAquifer.AquiferStatus getAquiferStatus(long pPackedPos) {
         int i = BlockPos.getX(pPackedPos);
         int j = BlockPos.getY(pPackedPos);
         int k = BlockPos.getZ(pPackedPos);
         int l = this.gridX(i);
         int i1 = this.gridY(j);
         int j1 = this.gridZ(k);
         int k1 = this.getIndex(l, i1, j1);
         Aquifer.NoiseBasedAquifer.AquiferStatus aquifer$noisebasedaquifer$aquiferstatus = this.aquiferCache[k1];
         if (aquifer$noisebasedaquifer$aquiferstatus != null) {
            return aquifer$noisebasedaquifer$aquiferstatus;
         } else {
            Aquifer.NoiseBasedAquifer.AquiferStatus aquifer$noisebasedaquifer$aquiferstatus1 = this.computeAquifer(i, j, k);
            this.aquiferCache[k1] = aquifer$noisebasedaquifer$aquiferstatus1;
            return aquifer$noisebasedaquifer$aquiferstatus1;
         }
      }

      private Aquifer.NoiseBasedAquifer.AquiferStatus computeAquifer(int pX, int pY, int pZ) {
         int i = this.noiseGeneratorSettings.seaLevel();
         if (pY > 30) {
            return new Aquifer.NoiseBasedAquifer.AquiferStatus(i, Blocks.WATER.defaultBlockState());
         } else {
            int j = 64;
            int k = -10;
            int l = 40;
            double d0 = this.waterLevelNoise.getValue((double)Math.floorDiv(pX, 64), (double)Math.floorDiv(pY, 40) / 1.4D, (double)Math.floorDiv(pZ, 64)) * 30.0D + -10.0D;
            boolean flag = false;
            if (Math.abs(d0) > 8.0D) {
               d0 *= 4.0D;
            }

            int i1 = Math.floorDiv(pY, 40) * 40 + 20;
            int j1 = i1 + Mth.floor(d0);
            if (i1 == -20) {
               double d1 = this.lavaNoise.getValue((double)Math.floorDiv(pX, 64), (double)Math.floorDiv(pY, 40) / 1.4D, (double)Math.floorDiv(pZ, 64));
               flag = Math.abs(d1) > (double)0.22F;
            }

            return new Aquifer.NoiseBasedAquifer.AquiferStatus(Math.min(56, j1), flag ? Blocks.LAVA.defaultBlockState() : Blocks.WATER.defaultBlockState());
         }
      }

      static final class AquiferStatus {
         final int fluidLevel;
         final BlockState fluidType;

         public AquiferStatus(int pFluidLevel, BlockState pFluidState) {
            this.fluidLevel = pFluidLevel;
            this.fluidType = pFluidState;
         }
      }
   }
}