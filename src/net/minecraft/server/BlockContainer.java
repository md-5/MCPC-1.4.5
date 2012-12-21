package net.minecraft.server;

public abstract class BlockContainer extends Block
{
    protected BlockContainer(int var1, Material var2)
    {
        super(var1, var2);
        this.isTileEntity = true;
    }

    protected BlockContainer(int var1, int var2, Material var3)
    {
        super(var1, var2, var3);
        this.isTileEntity = true;
    }

    /**
     * Called whenever the block is added into the world. Args: world, x, y, z
     */
    public void onPlace(World world, int var2, int var3, int var4)
    {
        super.onPlace(world, var2, var3, var4);
        world.setTileEntity(var2, var3, var4, this.createTileEntity(world, world.getData(var2, var3, var4))); // Forge
    }

    /**
     * ejects contained items into the world, and notifies neighbours of an update, as appropriate
     */
    public void remove(World var1, int var2, int var3, int var4, int var5, int var6)
    {
        super.remove(var1, var2, var3, var4, var5, var6);
        var1.r(var2, var3, var4);
    }

    /**
     * Returns a new instance of a block's tile entity class. Called on placing the block.
     */
    public abstract TileEntity a(World var1);

    // Forge start
    public TileEntity createNewTileEntity(World world, int metadata)
    {
        return this.a(world);
    }
    // Forge end

    /**
     * Called when the block receives a BlockEvent - see World.addBlockEvent. By default, passes it on to the tile
     * entity at this location. Args: world, x, y, z, blockID, EventID, event parameter
     */
    public void b(World var1, int var2, int var3, int var4, int var5, int var6)
    {
        super.b(var1, var2, var3, var4, var5, var6);
        TileEntity var7 = var1.getTileEntity(var2, var3, var4);

        if (var7 != null)
        {
            var7.b(var5, var6);
        }
    }
}
