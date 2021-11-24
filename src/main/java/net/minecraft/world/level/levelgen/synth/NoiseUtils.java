package net.minecraft.world.level.levelgen.synth;

import net.minecraft.util.Mth;

public class NoiseUtils {
   public static double sampleNoiseAndMapToRange(NormalNoise pNoise, double pX, double pY, double pZ, double pMin, double pMax) {
      double d0 = pNoise.getValue(pX, pY, pZ);
      return Mth.map(d0, -1.0D, 1.0D, pMin, pMax);
   }

   public static double biasTowardsExtreme(double pValue, double pBias) {
      return pValue + Math.sin(Math.PI * pValue) * pBias / Math.PI;
   }
}