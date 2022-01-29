package wasmruntime.Utils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Util;

public class Message {
  private Message() {}
  
  public static void broadcast(MinecraftServer server, Text msg) {
    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      player.sendSystemMessage(msg, Util.NIL_UUID);
    }
  }

  public static void PrettyBroadcast(MinecraftServer server, Object[] items) {
    MutableText out = new LiteralText("");

    for (Object value : items) {
      LiteralText v = new LiteralText(value.toString());
      if (value instanceof Integer) {
        v.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xc800ff)));
      } else if (value instanceof Long) {
        v.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xff0000)));
      } else if (value instanceof Float) {
        v.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x00fbff)));
      } else if (value instanceof Double) {
        v.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x00ff11)));
      } else if (value instanceof String) {
        v.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xfff200)));
      }

      out.append(v);
    }

    broadcast(server, out);
  }
}
