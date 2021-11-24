package net.minecraft.client.searchtree;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface MutableSearchTree<T> extends SearchTree<T> {
   void add(T pObject);

   void clear();

   void refresh();
}