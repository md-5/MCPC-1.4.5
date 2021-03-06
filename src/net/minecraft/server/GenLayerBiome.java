package net.minecraft.server;

public class GenLayerBiome extends GenLayer
{
    /** this sets all the biomes that are allowed to appear in the overworld */
    private BiomeBase[] b;

    public GenLayerBiome(long var1, GenLayer var3, WorldType var4)
    {
        super(var1);
        // Forge start
        //this.b = new BiomeBase[]{BiomeBase.DESERT, BiomeBase.FOREST, BiomeBase.EXTREME_HILLS, BiomeBase.SWAMPLAND, BiomeBase.PLAINS, BiomeBase.TAIGA, BiomeBase.JUNGLE};
        this.b = var4.getBiomesForWorldType();
        // Forge end
        this.a = var3;
      if(var4 == WorldType.NORMAL_1_1) {
         this.b = new BiomeBase[]{BiomeBase.DESERT, BiomeBase.FOREST, BiomeBase.EXTREME_HILLS, BiomeBase.SWAMPLAND, BiomeBase.PLAINS, BiomeBase.TAIGA};
      }
    }

    /**
     * Returns a list of integer values generated by this layer. These may be interpreted as temperatures, rainfall
     * amounts, or biomeList[] indices based on the particular GenLayer subclass.
     */
    public int[] a(int var1, int var2, int var3, int var4)
    {
        int[] var5 = this.a.a(var1, var2, var3, var4);
        int[] var6 = IntCache.a(var3 * var4);

        for (int var7 = 0; var7 < var4; ++var7)
        {
            for (int var8 = 0; var8 < var3; ++var8)
            {
                this.a((long)(var8 + var1), (long)(var7 + var2));
                int var9 = var5[var8 + var7 * var3];

                if (var9 == 0)
                {
                    var6[var8 + var7 * var3] = 0;
                }
                else if (var9 == BiomeBase.MUSHROOM_ISLAND.id)
                {
                    var6[var8 + var7 * var3] = var9;
                }
                else if (var9 == 1)
                {
                    var6[var8 + var7 * var3] = this.b[this.a(this.b.length)].id;
                }
                else
                {
                    int var10 = this.b[this.a(this.b.length)].id;

                    if (var10 == BiomeBase.TAIGA.id)
                    {
                        var6[var8 + var7 * var3] = var10;
                    }
                    else
                    {
                        var6[var8 + var7 * var3] = BiomeBase.ICE_PLAINS.id;
                    }
                }
            }
        }

        return var6;
    }
}
