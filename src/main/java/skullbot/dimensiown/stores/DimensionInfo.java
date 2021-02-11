package skullbot.dimensiown.stores;

import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DimensionInfo implements Comparable<DimensionInfo>
{
  @Nullable
  private final UUID owner;
  private       int  offset  = -1;
  private       int  upgrade = -1;

  private boolean dirty;

  public DimensionInfo( @Nullable UUID owner )
  {
    this.owner = owner;
  }

  public UUID getOwner()
  {
    return this.owner;
  }

  public int getOffset()
  {
    return this.offset;
  }

  public int getUpgrade()
  {
    return this.upgrade;
  }

  public void create( int offset )
  {
    this.offset  = offset;
    this.upgrade = 0;
    this.dirty = true;
  }

  public void setDirty( boolean dirty )
  {
    this.dirty = dirty;
  }

  public boolean isDirty()
  {
    return this.dirty;
  }

  // Serialize

  public ListTag serialize()
  {
    ListTag list = new ListTag();
    list.add( IntTag.of( this.offset ) );
    list.add( IntTag.of( this.upgrade ) );
    return list;
  }

  public void deserialize( ListTag list )
  {
    this.offset  = list.getInt( 0 );
    this.upgrade = list.getInt( 1 );
  }

  // Overrides

  @Override
  public int compareTo( @NotNull DimensionInfo info )
  {
    return Integer.compare( info.offset, this.offset );
  }

  @Override
  public boolean equals( Object obj )
  {
    return obj == this;
  }

  @Override
  public int hashCode()
  {
    return this.owner.hashCode();
  }

  @Override
  public String toString()
  {
    return "Dimension #" + this.offset + " (lv." + this.upgrade + ") owned by " + owner;
  }
}
