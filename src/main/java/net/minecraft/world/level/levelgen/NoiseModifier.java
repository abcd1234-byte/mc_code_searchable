package net.minecraft.world.level.levelgen;

@FunctionalInterface
public interface NoiseModifier {
   NoiseModifier PASSTHROUGH = (p_158629_, p_158630_, p_158631_, p_158632_) -> {
      return p_158629_;
   };

   double modifyNoise(double pNoise, int pX, int pY, int pZ);
}