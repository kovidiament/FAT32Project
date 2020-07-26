
import java.nio.ByteBuffer;

public class BootInfo {
   private long BPB_BytesPerSec;
    private long BPB_SecPerClus;
    private long BPB_RsvdSecCnt;
    private long BPB_NumFATS;
    private long BPB_FATSz32;
    private long BPB_RootClus;
    private long FirstDataSector;
    private long FirstSectorOfRoot;
    private long byteIndexOfRootStart;
    public BootInfo(ByteBuffer buff)
    {
      Short sh = buff.getShort(11);
      this.BPB_BytesPerSec = Short.toUnsignedLong(sh);
      this.BPB_SecPerClus = Byte.toUnsignedLong(buff.get(13));
      this.BPB_RsvdSecCnt = Short.toUnsignedLong(buff.getShort(14));
      this.BPB_NumFATS = Byte.toUnsignedLong(buff.get(16));
      this.BPB_FATSz32 = Integer.toUnsignedLong(buff.getInt(36));
      this.BPB_RootClus = Integer.toUnsignedLong(buff.getInt(44));
      this.FirstDataSector = BPB_RsvdSecCnt + (BPB_NumFATS * BPB_FATSz32);
      this.FirstSectorOfRoot = ((BPB_RootClus - 2) * BPB_SecPerClus) + FirstDataSector;
      this.byteIndexOfRootStart = FirstSectorOfRoot * BPB_BytesPerSec;
    }

    public String printNicely(){
        String nicely = "\n BPB_BytesPerSec: "+BPB_BytesPerSec+"\n BPB_SecPerClus: "+BPB_SecPerClus+"\n BPB_RsvdSecCnt: "+BPB_RsvdSecCnt+
                "\n BPB_NumFATS: "+BPB_NumFATS+"\n BPB_FATSz32: "+BPB_FATSz32;
        return nicely;
    }

    public long getByteIndexOfRootStart(){
        return this.byteIndexOfRootStart;
    }

    public long getFirstSectorOfRoot(){
        return this.FirstSectorOfRoot;
    }

    public long getFirstDataSector(){
        return this.FirstDataSector;
    }
    public long getBPB_RootClus(){
        return BPB_RootClus;
    }

    public long getBPB_BytesPerSec(){
        return BPB_BytesPerSec;
    }

    public long getBPB_SecPerClus()
    {
        return BPB_SecPerClus;
    }

    public long getBPB_RsvdSecCnt(){
        return BPB_RsvdSecCnt;
    }

    public long getBPB_NumFATS(){
        return BPB_NumFATS;
    }

    public long getBPB_FATSz32(){
        return BPB_FATSz32;
    }
}
