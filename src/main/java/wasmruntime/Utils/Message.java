package wasmruntime.Utils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class Message {
  public static void broadcast(Text msg, MinecraftServer server) {
    server.sendSystemMessage(msg, Util.NIL_UUID);
    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      player.sendSystemMessage(msg, Util.NIL_UUID);
    }
  }
}
