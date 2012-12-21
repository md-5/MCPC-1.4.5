package cpw.mods.fml.common.network;

import java.io.IOException;
import java.util.BitSet;
import java.util.Set;
import java.util.logging.Level;

import net.minecraft.server.INetworkManager;
import net.minecraft.server.NBTCompressedStreamTools;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NetHandler;

import mcpc.com.google.common.collect.MapDifference;
import mcpc.com.google.common.io.ByteArrayDataInput;
import mcpc.com.google.common.io.ByteStreams;
import mcpc.com.google.common.primitives.Bytes;
import mcpc.com.google.common.primitives.Ints;
import mcpc.com.google.common.primitives.UnsignedBytes;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.ItemData;
import static cpw.mods.fml.common.network.FMLPacket.Type.MOD_IDMAP;

public class ModIdMapPacket extends FMLPacket {
    private byte[][] partials;

    public ModIdMapPacket()
    {
        super(MOD_IDMAP);
    }

    @Override
    public byte[] generatePacket(Object... data)
    {
        NBTTagList completeList = (NBTTagList) data[0];
        NBTTagCompound wrap = new NBTTagCompound();
        wrap.set("List", completeList);
        try
        {
            return NBTCompressedStreamTools.a(wrap);
        }
        catch (Exception e)
        {
            FMLLog.log(Level.SEVERE, e, "A critical error writing the id map");
            throw new FMLNetworkException(e);
        }
    }

    @Override
    public FMLPacket consumePacket(byte[] data)
    {
        ByteArrayDataInput bdi = ByteStreams.newDataInput(data);
        int chunkIdx = UnsignedBytes.toInt(bdi.readByte());
        int chunkTotal = UnsignedBytes.toInt(bdi.readByte());
        int chunkLength = bdi.readInt();
        if (partials == null)
        {
            partials = new byte[chunkTotal][];
        }
        partials[chunkIdx] = new byte[chunkLength];
        bdi.readFully(partials[chunkIdx]);
        for (int i = 0; i < partials.length; i++)
        {
            if (partials[i] == null)
            {
                return null;
            }
        }
        return this;
    }

    @Override
    public void execute(INetworkManager network, FMLNetworkHandler handler, NetHandler netHandler, String userName)
    {
        byte[] allData = Bytes.concat(partials);
        GameData.initializeServerGate(1);
        try
        {
            NBTTagCompound serverList = NBTCompressedStreamTools.a(allData);
            NBTTagList list = serverList.getList("List");
            Set<ItemData> itemData = GameData.buildWorldItemData(list);
            GameData.validateWorldSave(itemData);
            MapDifference<Integer, ItemData> serverDifference = GameData.gateWorldLoadingForValidation();
            if (serverDifference!=null)
            {
                FMLCommonHandler.instance().disconnectIDMismatch(serverDifference, netHandler, network);

            }
        }
        catch (IOException e)
        {
        }
    }

}
