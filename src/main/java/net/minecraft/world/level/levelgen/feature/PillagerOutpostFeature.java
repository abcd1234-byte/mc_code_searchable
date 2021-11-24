package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

public class PillagerOutpostFeature extends JigsawFeature {
   private static final WeightedRandomList<MobSpawnSettings.SpawnerData> OUTPOST_ENEMIES = WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 1, 1, 1));

   public PillagerOutpostFeature(Codec<JigsawConfiguration> p_66562_) {
      super(p_66562_, 0, true, true);
   }

   public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
      return OUTPOST_ENEMIES;
   }

   protected boolean isFeatureChunk(ChunkGenerator pGenerator, BiomeSource pBiomeSource, long pSeed, WorldgenRandom pRandom, ChunkPos pChunkPos, Biome pBiome, ChunkPos pPotentialPos, JigsawConfiguration pConfig, LevelHeightAccessor pLevel) {
      int i = pChunkPos.x >> 4;
      int j = pChunkPos.z >> 4;
      pRandom.setSeed((long)(i ^ j << 4) ^ pSeed);
      pRandom.nextInt();
      if (pRandom.nextInt(5) != 0) {
         return false;
      } else {
         return !this.isNearVillage(pGenerator, pSeed, pRandom, pChunkPos);
      }
   }

   private boolean isNearVillage(ChunkGenerator pChunkGenerator, long pSeed, WorldgenRandom pRandom, ChunkPos pChunkPos) {
      StructureFeatureConfiguration structurefeatureconfiguration = pChunkGenerator.getSettings().getConfig(StructureFeature.VILLAGE);
      if (structurefeatureconfiguration == null) {
         return false;
      } else {
         int i = pChunkPos.x;
         int j = pChunkPos.z;

         for(int k = i - 10; k <= i + 10; ++k) {
            for(int l = j - 10; l <= j + 10; ++l) {
               ChunkPos chunkpos = StructureFeature.VILLAGE.getPotentialFeatureChunk(structurefeatureconfiguration, pSeed, pRandom, k, l);
               if (k == chunkpos.x && l == chunkpos.z) {
                  return true;
               }
            }
         }

         return false;
      }
   }
}