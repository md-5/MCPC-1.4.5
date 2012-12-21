package cpw.mods.fml.common;

import java.util.List;

import mcpc.com.google.common.collect.MapDifference;

import net.minecraft.server.Entity;
import net.minecraft.server.INetworkManager;
import net.minecraft.server.NetHandler;
import net.minecraft.server.Packet;
import net.minecraft.server.Packet131ItemData;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.network.EntitySpawnAdjustmentPacket;
import cpw.mods.fml.common.network.EntitySpawnPacket;
import cpw.mods.fml.common.network.ModMissingPacket;
import cpw.mods.fml.common.registry.ItemData;
import cpw.mods.fml.common.registry.EntityRegistry.EntityRegistration;

public interface IFMLSidedHandler
{
    List<String> getAdditionalBrandingInformation();

    Side getSide();

    void haltGame(String message, Throwable exception);

    void showGuiScreen(Object clientGuiElement);

    Entity spawnEntityIntoClientWorld(EntityRegistration registration, EntitySpawnPacket packet);

    void adjustEntityLocationOnClient(EntitySpawnAdjustmentPacket entitySpawnAdjustmentPacket);

    void beginServerLoading(MinecraftServer server);

    void finishServerLoading();

    MinecraftServer getServer();

    void sendPacket(Packet packet);

    void displayMissingMods(ModMissingPacket modMissingPacket);

    void handleTinyPacket(NetHandler handler, Packet131ItemData mapData);

    void setClientCompatibilityLevel(byte compatibilityLevel);

    byte getClientCompatibilityLevel();

    boolean shouldServerShouldBeKilledQuietly();

    void disconnectIDMismatch(MapDifference<Integer, ItemData> s, NetHandler toKill, INetworkManager mgr);
}
