package net.minecraft.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class RegistryAccess {
   private static final Logger LOGGER = LogManager.getLogger();
   static final Map<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> REGISTRIES = Util.make(() -> {
      Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> builder = ImmutableMap.builder();
      put(builder, Registry.DIMENSION_TYPE_REGISTRY, DimensionType.DIRECT_CODEC, DimensionType.DIRECT_CODEC);
      put(builder, Registry.BIOME_REGISTRY, Biome.DIRECT_CODEC, Biome.NETWORK_CODEC);
      put(builder, Registry.CONFIGURED_SURFACE_BUILDER_REGISTRY, ConfiguredSurfaceBuilder.DIRECT_CODEC);
      put(builder, Registry.CONFIGURED_CARVER_REGISTRY, ConfiguredWorldCarver.DIRECT_CODEC);
      put(builder, Registry.CONFIGURED_FEATURE_REGISTRY, ConfiguredFeature.DIRECT_CODEC);
      put(builder, Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, ConfiguredStructureFeature.DIRECT_CODEC);
      put(builder, Registry.PROCESSOR_LIST_REGISTRY, StructureProcessorType.DIRECT_CODEC);
      put(builder, Registry.TEMPLATE_POOL_REGISTRY, StructureTemplatePool.DIRECT_CODEC);
      put(builder, Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings.DIRECT_CODEC);
      return builder.build();
   });
   private static final RegistryAccess.RegistryHolder BUILTIN = Util.make(() -> {
      RegistryAccess.RegistryHolder registryaccess$registryholder = new RegistryAccess.RegistryHolder();
      DimensionType.registerBuiltin(registryaccess$registryholder);
      REGISTRIES.keySet().stream().filter((p_175518_) -> {
         return !p_175518_.equals(Registry.DIMENSION_TYPE_REGISTRY);
      }).forEach((p_175511_) -> {
         copyBuiltin(registryaccess$registryholder, p_175511_);
      });
      return registryaccess$registryholder;
   });

   public abstract <E> Optional<WritableRegistry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> pRegistryKey);

   public <E> WritableRegistry<E> ownedRegistryOrThrow(ResourceKey<? extends Registry<? extends E>> pRegistryKey) {
      return this.ownedRegistry(pRegistryKey).orElseThrow(() -> {
         return new IllegalStateException("Missing registry: " + pRegistryKey);
      });
   }

   public <E> Optional<? extends Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> pRegistryKey) {
      Optional<? extends Registry<E>> optional = this.ownedRegistry(pRegistryKey);
      return optional.isPresent() ? optional : (Optional<? extends Registry<E>>)Registry.REGISTRY.getOptional(pRegistryKey.location());
   }

   public <E> Registry<E> registryOrThrow(ResourceKey<? extends Registry<? extends E>> pRegistryKey) {
      return this.registry(pRegistryKey).orElseThrow(() -> {
         return new IllegalStateException("Missing registry: " + pRegistryKey);
      });
   }

   private static <E> void put(Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> pBuilder, ResourceKey<? extends Registry<E>> pRegistryKey, Codec<E> pElementCodec) {
      pBuilder.put(pRegistryKey, new RegistryAccess.RegistryData<>(pRegistryKey, pElementCodec, (Codec<E>)null));
   }

   private static <E> void put(Builder<ResourceKey<? extends Registry<?>>, RegistryAccess.RegistryData<?>> pBuilder, ResourceKey<? extends Registry<E>> pRegistryKey, Codec<E> pElementCodec, Codec<E> pNetworkCodec) {
      pBuilder.put(pRegistryKey, new RegistryAccess.RegistryData<>(pRegistryKey, pElementCodec, pNetworkCodec));
   }

   public static RegistryAccess.RegistryHolder builtin() {
      RegistryAccess.RegistryHolder registryaccess$registryholder = new RegistryAccess.RegistryHolder();
      RegistryReadOps.ResourceAccess.MemoryMap registryreadops$resourceaccess$memorymap = new RegistryReadOps.ResourceAccess.MemoryMap();

      for(RegistryAccess.RegistryData<?> registrydata : REGISTRIES.values()) {
         addBuiltinElements(registryaccess$registryholder, registryreadops$resourceaccess$memorymap, registrydata);
      }

      RegistryReadOps.createAndLoad(JsonOps.INSTANCE, registryreadops$resourceaccess$memorymap, registryaccess$registryholder);
      return registryaccess$registryholder;
   }

   private static <E> void addBuiltinElements(RegistryAccess.RegistryHolder pDestinationRegistryHolder, RegistryReadOps.ResourceAccess.MemoryMap pResourceAccess, RegistryAccess.RegistryData<E> pData) {
      ResourceKey<? extends Registry<E>> resourcekey = pData.key();
      boolean flag = !resourcekey.equals(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY) && !resourcekey.equals(Registry.DIMENSION_TYPE_REGISTRY);
      Registry<E> registry = BUILTIN.registryOrThrow(resourcekey);
      WritableRegistry<E> writableregistry = pDestinationRegistryHolder.ownedRegistryOrThrow(resourcekey);

      for(Entry<ResourceKey<E>, E> entry : registry.entrySet()) {
         ResourceKey<E> resourcekey1 = entry.getKey();
         E e = entry.getValue();
         if (flag) {
            pResourceAccess.add(BUILTIN, resourcekey1, pData.codec(), registry.getId(e), e, registry.lifecycle(e));
         } else {
            writableregistry.registerMapping(registry.getId(e), resourcekey1, e, registry.lifecycle(e));
         }
      }

   }

   private static <R extends Registry<?>> void copyBuiltin(RegistryAccess.RegistryHolder pDestinationRegistryHolder, ResourceKey<R> pRegistryKey) {
      Registry<R> registry = (Registry<R>)BuiltinRegistries.REGISTRY;
      Registry<?> registry1 = registry.getOrThrow(pRegistryKey);
      copy(pDestinationRegistryHolder, registry1);
   }

   private static <E> void copy(RegistryAccess.RegistryHolder pDestinationRegistryHolder, Registry<E> pSourceRegistry) {
      WritableRegistry<E> writableregistry = pDestinationRegistryHolder.ownedRegistryOrThrow(pSourceRegistry.key());

      for(Entry<ResourceKey<E>, E> entry : pSourceRegistry.entrySet()) {
         E e = entry.getValue();
         writableregistry.registerMapping(pSourceRegistry.getId(e), entry.getKey(), e, pSourceRegistry.lifecycle(e));
      }

   }

   public static void load(RegistryAccess pDestinationRegistryAccess, RegistryReadOps<?> pOps) {
      for(RegistryAccess.RegistryData<?> registrydata : REGISTRIES.values()) {
         readRegistry(pOps, pDestinationRegistryAccess, registrydata);
      }

   }

   private static <E> void readRegistry(RegistryReadOps<?> pOps, RegistryAccess pDestinationRegistryAccess, RegistryAccess.RegistryData<E> pData) {
      ResourceKey<? extends Registry<E>> resourcekey = pData.key();
      MappedRegistry<E> mappedregistry = (MappedRegistry)pDestinationRegistryAccess.<E>ownedRegistryOrThrow(resourcekey);
      DataResult<MappedRegistry<E>> dataresult = pOps.decodeElements(mappedregistry, pData.key(), pData.codec());
      dataresult.error().ifPresent((p_175499_) -> {
         throw new JsonParseException("Error loading registry data: " + p_175499_.message());
      });
   }

   static final class RegistryData<E> {
      private final ResourceKey<? extends Registry<E>> key;
      private final Codec<E> codec;
      @Nullable
      private final Codec<E> networkCodec;

      public RegistryData(ResourceKey<? extends Registry<E>> pKey, Codec<E> pCodec, @Nullable Codec<E> pNetworkCodec) {
         this.key = pKey;
         this.codec = pCodec;
         this.networkCodec = pNetworkCodec;
      }

      public ResourceKey<? extends Registry<E>> key() {
         return this.key;
      }

      public Codec<E> codec() {
         return this.codec;
      }

      @Nullable
      public Codec<E> networkCodec() {
         return this.networkCodec;
      }

      public boolean sendToClient() {
         return this.networkCodec != null;
      }
   }

   public static final class RegistryHolder extends RegistryAccess {
      public static final Codec<RegistryAccess.RegistryHolder> NETWORK_CODEC = makeNetworkCodec();
      private final Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> registries;

      private static <E> Codec<RegistryAccess.RegistryHolder> makeNetworkCodec() {
         Codec<ResourceKey<? extends Registry<E>>> codec = ResourceLocation.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::location);
         Codec<MappedRegistry<E>> codec1 = codec.partialDispatch("type", (p_123134_) -> {
            return DataResult.success(p_123134_.key());
         }, (p_123145_) -> {
            return getNetworkCodec(p_123145_).map((p_175531_) -> {
               return MappedRegistry.networkCodec(p_123145_, Lifecycle.experimental(), p_175531_);
            });
         });
         UnboundedMapCodec<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> unboundedmapcodec = Codec.unboundedMap(codec, codec1);
         return captureMap(unboundedmapcodec);
      }

      private static <K extends ResourceKey<? extends Registry<?>>, V extends MappedRegistry<?>> Codec<RegistryAccess.RegistryHolder> captureMap(UnboundedMapCodec<K, V> pUnboundedMapCodec) {
         return pUnboundedMapCodec.xmap(RegistryAccess.RegistryHolder::new, (p_123136_) -> {
            return ((Map<K, V>)p_123136_.registries).entrySet().stream().filter((p_175526_) -> {
               return RegistryAccess.REGISTRIES.get(p_175526_.getKey()).sendToClient();
            }).collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
         });
      }

      private static <E> DataResult<? extends Codec<E>> getNetworkCodec(ResourceKey<? extends Registry<E>> pRegistryKey) {
         return Optional.ofNullable(RegistryAccess.REGISTRIES.get(pRegistryKey)).map((p_123123_) -> {
            return (Codec<E>)p_123123_.networkCodec();
         }).map(DataResult::success).orElseGet(() -> {
            return DataResult.error("Unknown or not serializable registry: " + pRegistryKey);
         });
      }

      public RegistryHolder() {
         this(RegistryAccess.REGISTRIES.keySet().stream().collect(Collectors.toMap(Function.identity(), RegistryAccess.RegistryHolder::createRegistry)));
      }

      private RegistryHolder(Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> p_123117_) {
         this.registries = p_123117_;
      }

      private static <E> MappedRegistry<?> createRegistry(ResourceKey<? extends Registry<?>> p_123141_) {
         return new MappedRegistry(p_123141_, Lifecycle.stable());
      }

      public <E> Optional<WritableRegistry<E>> ownedRegistry(ResourceKey<? extends Registry<? extends E>> pRegistryKey) {
         return Optional.ofNullable(this.registries.get(pRegistryKey)).map((p_175524_) -> {
            return (WritableRegistry<E>)p_175524_;
         });
      }
   }
}
