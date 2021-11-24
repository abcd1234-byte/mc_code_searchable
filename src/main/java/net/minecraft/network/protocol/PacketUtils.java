package net.minecraft.network.protocol;

import net.minecraft.network.PacketListener;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PacketUtils {
   private static final Logger LOGGER = LogManager.getLogger();

   public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> pPacket, T pProcessor, ServerLevel pLevel) throws RunningOnDifferentThreadException {
      ensureRunningOnSameThread(pPacket, pProcessor, pLevel.getServer());
   }

   public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> pPacket, T pProcessor, BlockableEventLoop<?> pExecutor) throws RunningOnDifferentThreadException {
      if (!pExecutor.isSameThread()) {
         pExecutor.execute(() -> {
            if (pProcessor.getConnection().isConnected()) {
               pPacket.handle(pProcessor);
            } else {
               LOGGER.debug("Ignoring packet due to disconnection: {}", (Object)pPacket);
            }

         });
         throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
      }
   }
}