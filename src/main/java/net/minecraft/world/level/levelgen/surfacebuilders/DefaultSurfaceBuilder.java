package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public class DefaultSurfaceBuilder extends SurfaceBuilder<SurfaceBuilderBaseConfiguration> {
   public DefaultSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> p_74788_) {
      super(p_74788_);
   }

   public void apply(Random pRandom, ChunkAccess pChunk, Biome pBiome, int pX, int pZ, int pHeight, double pNoise, BlockState pDefaultBlock, BlockState pDefaultFluid, int pSeaLevel, int pMinSurfaceLevel, long pSeed, SurfaceBuilderBaseConfiguration pConfig) {
      this.apply(pRandom, pChunk, pBiome, pX, pZ, pHeight, pNoise, pDefaultBlock, pDefaultFluid, pConfig.getTopMaterial(), pConfig.getUnderMaterial(), pConfig.getUnderwaterMaterial(), pSeaLevel, pMinSurfaceLevel);
   }

   protected void apply(Random pRandom, ChunkAccess pChunk, Biome pBiome, int pX, int pZ, int pHeight, double pNoise, BlockState pDefaultBlock, BlockState pDefaultFluid, BlockState pTopMaterial, BlockState pUnderMaterial, BlockState pUnderwaterMaterial, int pSeaLevel, int pMinSurfaceLevel) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
      int i = (int)(pNoise / 3.0D + 3.0D + pRandom.nextDouble() * 0.25D);
      if (i == 0) {
         boolean flag = false;

         for(int j = pHeight; j >= pMinSurfaceLevel; --j) {
            blockpos$mutableblockpos.set(pX, j, pZ);
            BlockState blockstate = pChunk.getBlockState(blockpos$mutableblockpos);
            if (blockstate.isAir()) {
               flag = false;
            } else if (blockstate.is(pDefaultBlock.getBlock())) {
               if (!flag) {
                  BlockState blockstate1;
                  if (j >= pSeaLevel) {
                     blockstate1 = Blocks.AIR.defaultBlockState();
                  } else if (j == pSeaLevel - 1) {
                     blockstate1 = pBiome.getTemperature(blockpos$mutableblockpos) < 0.15F ? Blocks.ICE.defaultBlockState() : pDefaultFluid;
                  } else if (j >= pSeaLevel - (7 + i)) {
                     blockstate1 = pDefaultBlock;
                  } else {
                     blockstate1 = pUnderwaterMaterial;
                  }

                  pChunk.setBlockState(blockpos$mutableblockpos, blockstate1, false);
               }

               flag = true;
            }
         }
      } else {
         BlockState blockstate3 = pUnderMaterial;
         int k = -1;

         for(int l = pHeight; l >= pMinSurfaceLevel; --l) {
            blockpos$mutableblockpos.set(pX, l, pZ);
            BlockState blockstate4 = pChunk.getBlockState(blockpos$mutableblockpos);
            if (blockstate4.isAir()) {
               k = -1;
            } else if (blockstate4.is(pDefaultBlock.getBlock())) {
               if (k == -1) {
                  k = i;
                  BlockState blockstate2;
                  if (l >= pSeaLevel + 2) {
                     blockstate2 = pTopMaterial;
                  } else if (l >= pSeaLevel - 1) {
                     blockstate3 = pUnderMaterial;
                     blockstate2 = pTopMaterial;
                  } else if (l >= pSeaLevel - 4) {
                     blockstate3 = pUnderMaterial;
                     blockstate2 = pUnderMaterial;
                  } else if (l >= pSeaLevel - (7 + i)) {
                     blockstate2 = blockstate3;
                  } else {
                     blockstate3 = pDefaultBlock;
                     blockstate2 = pUnderwaterMaterial;
                  }

                  pChunk.setBlockState(blockpos$mutableblockpos, blockstate2, false);
               } else if (k > 0) {
                  --k;
                  pChunk.setBlockState(blockpos$mutableblockpos, blockstate3, false);
                  if (k == 0 && blockstate3.is(Blocks.SAND) && i > 1) {
                     k = pRandom.nextInt(4) + Math.max(0, l - pSeaLevel);
                     blockstate3 = blockstate3.is(Blocks.RED_SAND) ? Blocks.RED_SANDSTONE.defaultBlockState() : Blocks.SANDSTONE.defaultBlockState();
                  }
               }
            }
         }
      }

   }
}