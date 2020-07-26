import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class LSDriver {
    ByteBuffer buff;
    BootInfo info;

    public LSDriver(ByteBuffer buffer, BootInfo boot) {
        this.buff = buffer;
        this.info = boot;
    }

    public String getVolume(long root){
        String toPrint = "";
        ArrayList<Integer> clustersList = getAllClustersOfDir(root);//
        for (int a = 0; a < clustersList.size(); a++)//
        {//
            long cluster = clustersList.get(a).longValue();//
            long firstSector = ((cluster-2)*info.getBPB_SecPerClus()) + info.getFirstDataSector();
            long startOfCluster = firstSector*info.getBPB_BytesPerSec();
            for (long i = startOfCluster; i < (startOfCluster + (info.getBPB_BytesPerSec() * info.getBPB_SecPerClus())); i += 32) {
                Long attrByte = i + 11;
                int index = attrByte.intValue();
                byte attr = buff.get(index);
                Long entryStart = i;
                int nameStart = entryStart.intValue();
                int readOnly = attr & 0x01;
                int isItADir = attr & 0x10;
                int archived = attr & 0x20;
                int isHidden = attr & 0x02;
                int isSystem = attr & 0x04;
                int isVolume = attr & 0x08;
                int longName = readOnly + isHidden + isSystem + isVolume; //if is 15 then this is set.
                //////////////CHECK VALIDITY OF ENTRY///////////////
                int isItDeleted = Byte.toUnsignedInt(buff.get(nameStart));
                if (isHidden != 0 || isSystem != 0 || longName == 15 || isItDeleted == 229 || isItDeleted == 0) { //0xE5 = 229, 0x00 = 0
                    continue;
                }

                if (isVolume != 0) {
                    StringBuilder name = new StringBuilder();
                    for (int x = nameStart; x < nameStart + 8; x++) {
                        byte next = buff.get(x);
                        char c = (char) (next & 0xFF);
                        if (c != ' ') {
                            name.append(c);
                        }
                    }
                    toPrint = name.toString();
                    return toPrint;
                }


            }
        }

        return "";

    }

    public String read(String fileToPrint, int position, int numBytes, long clusterStart){
        if(sizeOfFile(clusterStart, fileToPrint) == -1){
            return "";
        }
        if(sizeOfFile(clusterStart, fileToPrint) < position){
            return "";
        }
        int size = sizeOfFile(clusterStart, fileToPrint);
        long startingClusterOfTheFile = startingClusterOfFile(clusterStart, fileToPrint);
        ArrayList<Integer> clustersList = getAllClustersOfDir(startingClusterOfTheFile);//get clusters of the file.
        StringBuilder toPrint = new StringBuilder();
        long pos = position;
        long whichClusterStartFrom = pos/(info.getBPB_BytesPerSec()*info.getBPB_SecPerClus());
        Long WCSF = whichClusterStartFrom;
        int firstClusterToRead = WCSF.intValue();
        long spot = whichClusterStartFrom*(info.getBPB_BytesPerSec()*info.getBPB_SecPerClus());
        Long S = spot;
        int spotUpTo = S.intValue();
        for (int a = firstClusterToRead; a < clustersList.size(); a++)//
        {//
            long cluster = clustersList.get(a).longValue();//
            long firstSector = ((cluster-2)*info.getBPB_SecPerClus()) + info.getFirstDataSector();
            long startOfCluster = firstSector*info.getBPB_BytesPerSec();
            for (long i = startOfCluster; i < (startOfCluster + (info.getBPB_BytesPerSec() * info.getBPB_SecPerClus())); i ++) {
            Long currentIndex = i;
            int getter = currentIndex.intValue();
             byte next = buff.get(getter);
             if(spotUpTo >= position && spotUpTo < (position + numBytes) && spotUpTo < size){
                 char c = (char) (next & 0xFF);
                 toPrint.append(c);
             }
             spotUpTo++;
            }
        }
        return toPrint.toString();

    }

    long startingClusterOfFile(long start, String fileName){
        ArrayList<Integer> clustersList = getAllClustersOfDir(start);
        String toPrint = "";

        for (int a = 0; a < clustersList.size(); a++) {
            long cluster = clustersList.get(a).longValue();//
            long firstSector = ((cluster-2)*info.getBPB_SecPerClus()) + info.getFirstDataSector();
            long startOfCluster = firstSector*info.getBPB_BytesPerSec();
            for (long i = startOfCluster; i < (startOfCluster + (info.getBPB_BytesPerSec() * info.getBPB_SecPerClus())); i += 32) {

                Long attrByte = i + 11;
                int index = attrByte.intValue();
                byte attr = buff.get(index);
                Long entryStart = i;
                int nameStart = entryStart.intValue();
                int readOnly = attr & 0x01;
                int isItADir = attr & 0x10;
                int archived = attr & 0x20;
                int isHidden = attr & 0x02;
                int isSystem = attr & 0x04;
                int isVolume = attr & 0x08;
                int longName = readOnly + isHidden + isSystem + isVolume; //if is 15 then this is set.
                //////////////CHECK VALIDITY OF ENTRY///////////////
                int isItDeleted = Byte.toUnsignedInt(buff.get(nameStart));
                if (((isHidden != 0 || isSystem != 0)&& longName!= 15) || isItDeleted == 229 || isItDeleted == 0 || isVolume != 0) { //0xE5 = 229, 0x00 = 0
                    continue;
                }
                ///////////DIRECTORY HANDLING://////////
                else if (isItADir != 0) {
                   continue;
                }
                ///////////FILE HANDLING:///////////////////////
                else {
                    StringBuilder name = new StringBuilder();

                    for (int x = nameStart; x < nameStart + 8; x++) {
                        byte next = buff.get(x);
                        char c = (char) (next & 0xFF);
                        if (c != ' ') {
                            name.append(c);
                        }
                    }
                    name.append(".");
                    for (int z = nameStart + 8; z < nameStart + 11; z++) {
                        byte next = buff.get(z);
                        char c = (char) (next & 0xFF);
                        if (c != ' ') {
                            name.append(c);
                        }
                    }
                    if (fileName.equals(name.toString())) {

                        byte[] clusterNum = new byte[4];
                        clusterNum[0] = buff.get(nameStart + 21);
                        clusterNum[1] = buff.get(nameStart + 20);
                        clusterNum[2] = buff.get(nameStart+27);
                        clusterNum[3] = buff.get(nameStart+26);
                        ByteBuffer clusterWrap = ByteBuffer.wrap(clusterNum);
                        int firstCluster = clusterWrap.getInt();
                       long clust = firstCluster;
                       return clust;
                    }
                }
            }


        }
        return -1;
    }

    public ArrayList<Integer> getAllClustersOfDir(long startingCluster) {
        ArrayList<Integer> theClusters = new ArrayList<Integer>();
        Long start = startingCluster;
        int cluster = start.intValue();
        while (cluster != -1)//0x0FFFFFF8
        {
            theClusters.add(cluster);
            cluster = getNextClusterOfDir(cluster);
        }
        return theClusters;
    }

    int getNextClusterOfDir(int clusterStart) {
        long clusterNumber = clusterStart;
        long ThisFATSecNum = info.getBPB_RsvdSecCnt() + (clusterNumber * 4) / info.getBPB_BytesPerSec();
        long ThisFATEntOffset = (clusterNumber * 4)%info.getBPB_BytesPerSec();
        long indexInFAT = (ThisFATSecNum * info.getBPB_BytesPerSec()) + ThisFATEntOffset;
        Long index = indexInFAT;
        int indexInt = index.intValue();
        int nextCluster = buff.getInt(indexInt);
        int mask = 0x0FFFFFFF;
        nextCluster = nextCluster & mask;
        if (nextCluster >= 268435448)//
        {
            return -1;
        }
        return nextCluster;

    }


    public int sizeOfFile(long start, String fileName)
    {
        ArrayList<Integer> clustersList = getAllClustersOfDir(start);

        for (int a = 0; a < clustersList.size(); a++) {
            long cluster = clustersList.get(a).longValue();//
            long firstSector = ((cluster-2)*info.getBPB_SecPerClus()) + info.getFirstDataSector();
            long startOfCluster = firstSector*info.getBPB_BytesPerSec();
            for (long i = startOfCluster; i < (startOfCluster + (info.getBPB_BytesPerSec() * info.getBPB_SecPerClus())); i += 32) {

                Long attrByte = i + 11;
                int index = attrByte.intValue();
                byte attr = buff.get(index);
                Long entryStart = i;
                int nameStart = entryStart.intValue();
                int readOnly = attr & 0x01;
                int isItADir = attr & 0x10;
                int archived = attr & 0x20;
                int isHidden = attr & 0x02;
                int isSystem = attr & 0x04;
                int isVolume = attr & 0x08;
                int longName = readOnly + isHidden + isSystem + isVolume; //if is 15 then this is set.
                //////////////CHECK VALIDITY OF ENTRY///////////////
                int isItDeleted = Byte.toUnsignedInt(buff.get(nameStart));
                if (((isHidden != 0 || isSystem != 0)&& longName!= 15) || isItDeleted == 229 || isItDeleted == 0 || isVolume != 0) { //0xE5 = 229, 0x00 = 0
                    continue;
                }
                ///////////DIRECTORY HANDLING://////////
                else if (isItADir != 0) {
                    StringBuilder name = new StringBuilder();
                    for (int x = nameStart; x < nameStart + 8; x++) {
                        byte next = buff.get(x);
                        char c = (char) (next & 0xFF);
                        if (c != ' ') {
                            name.append(c);
                        }
                    }
                    if (fileName.equals(name.toString())) {
                        int sizeBytes = buff.getInt(nameStart + 28);
                       return sizeBytes;
                    }
                }
                ///////////FILE HANDLING:///////////////////////
                else {
                    StringBuilder name = new StringBuilder();

                    for (int x = nameStart; x < nameStart + 8; x++) {
                        byte next = buff.get(x);
                        char c = (char) (next & 0xFF);
                        if (c != ' ') {
                            name.append(c);
                        }
                    }
                    name.append(".");
                    for (int z = nameStart + 8; z < nameStart + 11; z++) {
                        byte next = buff.get(z);
                        char c = (char) (next & 0xFF);
                        if (c != ' ') {
                            name.append(c);
                        }
                    }
                    if (fileName.equals(name.toString())) {
                        int sizeBytes = buff.getInt(nameStart + 28);
                        return sizeBytes;
                    }
                }
            }


        }
        return -1;

    }

    public String printAllOfCurrentDir(long start) {
        String toPrint = "";
        ArrayList<Integer> clustersList = getAllClustersOfDir(start);//
        for (int a = 0; a < clustersList.size(); a++)//
        {//
            long cluster = clustersList.get(a).longValue();//
            long firstSector = ((cluster-2)*info.getBPB_SecPerClus()) + info.getFirstDataSector();
            long startOfCluster = firstSector*info.getBPB_BytesPerSec();
            for (long i = startOfCluster; i < (startOfCluster + (info.getBPB_BytesPerSec() * info.getBPB_SecPerClus())); i += 32) {
                Long attrByte = i + 11;
                int index = attrByte.intValue();
                byte attr = buff.get(index);
                Long entryStart = i;
                int nameStart = entryStart.intValue();
                int readOnly = attr & 0x01;
                int isItADir = attr & 0x10;
                int archived = attr & 0x20;
                int isHidden = attr & 0x02;
                int isSystem = attr & 0x04;
                int isVolume = attr & 0x08;
                int longName = readOnly + isHidden + isSystem + isVolume; //if is 15 then this is set.
                //////////////CHECK VALIDITY OF ENTRY///////////////
                int isItDeleted = Byte.toUnsignedInt(buff.get(nameStart));
                if (isHidden != 0 || isSystem != 0 || longName == 15 || isItDeleted == 229 || isItDeleted == 0 || isVolume != 0) { //0xE5 = 229, 0x00 = 0
                    continue;
                }

                //////////DIRECTORY HANDLING:///////////////
                else if (isItADir != 0) {
                    StringBuilder name = new StringBuilder();
                    for (int x = nameStart; x < nameStart + 8; x++) {
                        byte next = buff.get(x);
                        char c = (char) (next & 0xFF);
                        if (c != ' ') {
                            name.append(c);
                        }
                    }
                    toPrint = toPrint + "\n" + name.toString();
                }

                //////////FILE HANDLING:///////////////////
                else {
                    StringBuilder name = new StringBuilder();

                    for (int x = nameStart; x < nameStart + 8; x++) {
                        byte next = buff.get(x);
                        char c = (char) (next & 0xFF);
                        if (c != ' ') {
                            name.append(c);
                        }
                    }
                    name.append(".");
                    for (int z = nameStart + 8; z < nameStart + 11; z++) {
                        byte next = buff.get(z);
                        char c = (char) (next & 0xFF);
                        if (c != ' ') {
                            name.append(c);
                        }
                    }
                    toPrint = toPrint + "\n" + name.toString();
                }
            }
        }//

        return toPrint;
    }

    //////////////////////////////////////////////////
    public String getStats(long start, String fileName) {
        ArrayList<Integer> clustersList = getAllClustersOfDir(start);
        String toPrint = "";

        for (int a = 0; a < clustersList.size(); a++) {
            long cluster = clustersList.get(a).longValue();//
            long firstSector = ((cluster-2)*info.getBPB_SecPerClus()) + info.getFirstDataSector();
            long startOfCluster = firstSector*info.getBPB_BytesPerSec();
            for (long i = startOfCluster; i < (startOfCluster + (info.getBPB_BytesPerSec() * info.getBPB_SecPerClus())); i += 32) {

                Long attrByte = i + 11;
                int index = attrByte.intValue();
                byte attr = buff.get(index);
                Long entryStart = i;
                int nameStart = entryStart.intValue();
                int readOnly = attr & 0x01;
                int isItADir = attr & 0x10;
                int archived = attr & 0x20;
                int isHidden = attr & 0x02;
                int isSystem = attr & 0x04;
                int isVolume = attr & 0x08;
                int longName = readOnly + isHidden + isSystem + isVolume; //if is 15 then this is set.
                //////////////CHECK VALIDITY OF ENTRY///////////////
                int isItDeleted = Byte.toUnsignedInt(buff.get(nameStart));
                if (((isHidden != 0 || isSystem != 0)&& longName!= 15) || isItDeleted == 229 || isItDeleted == 0 || isVolume != 0) { //0xE5 = 229, 0x00 = 0
                    continue;
                }
                ///////////DIRECTORY HANDLING://////////
                else if (isItADir != 0) {
                    StringBuilder name = new StringBuilder();
                    for (int x = nameStart; x < nameStart + 8; x++) {
                        byte next = buff.get(x);
                        char c = (char) (next & 0xFF);
                        if (c != ' ') {
                            name.append(c);
                        }
                    }
                    if (fileName.equals(name.toString())) {
                        int sizeBytes = buff.getInt(nameStart + 28);
                        long result = Integer.toUnsignedLong(sizeBytes);
                        toPrint = toPrint + "Size: "+result+"\n";
                        if(readOnly != 0){
                            toPrint = toPrint + " ATTR_READ_ONLY";
                        }
                        if(isItADir != 0){
                            toPrint = toPrint + " ATTR_DIRECTORY";
                        }
                        if(archived != 0){
                            toPrint = toPrint + " ATTR_ARCHIVE";
                        }
                        if(longName == 15){
                            toPrint = toPrint + " ATTR_LONG_NAME";
                        }

                        byte[] clusterNum = new byte[4];
                        clusterNum[0] = buff.get(nameStart + 21);
                        clusterNum[1] = buff.get(nameStart + 20);
                        clusterNum[2] = buff.get(nameStart+27);
                        clusterNum[3] = buff.get(nameStart+26);
                        ByteBuffer clusterWrap = ByteBuffer.wrap(clusterNum);
                        int firstCluster = clusterWrap.getInt();
                        String hex = Integer.toHexString(firstCluster);
                        toPrint = toPrint + "\n Next cluster Number is 0x"+hex +" in big endian";

                        return toPrint;
                    }
                }
                ///////////FILE HANDLING:///////////////////////
                else {
                    StringBuilder name = new StringBuilder();

                    for (int x = nameStart; x < nameStart + 8; x++) {
                        byte next = buff.get(x);
                        char c = (char) (next & 0xFF);
                        if (c != ' ') {
                            name.append(c);
                        }
                    }
                    name.append(".");
                    for (int z = nameStart + 8; z < nameStart + 11; z++) {
                        byte next = buff.get(z);
                        char c = (char) (next & 0xFF);
                        if (c != ' ') {
                            name.append(c);
                        }
                    }
                    if (fileName.equals(name.toString())) {
                        int sizeBytes = buff.getInt(nameStart + 28);
                        long result = Integer.toUnsignedLong(sizeBytes);
                        toPrint = toPrint + "Size: "+result+"\n";
                        if(readOnly != 0){
                            toPrint = toPrint + " ATTR_READ_ONLY";
                        }
                        if(isItADir != 0){
                            toPrint = toPrint + " ATTR_DIRECTORY";
                        }
                        if(archived != 0){
                            toPrint = toPrint + " ATTR_ARCHIVE";
                        }
                        if(longName == 15){
                            toPrint = toPrint + " ATTR_LONG_NAME";
                        }
                        byte[] clusterNum = new byte[4];
                        clusterNum[0] = buff.get(nameStart + 21);
                        clusterNum[1] = buff.get(nameStart + 20);
                        clusterNum[2] = buff.get(nameStart+27);
                        clusterNum[3] = buff.get(nameStart+26);
                        ByteBuffer clusterWrap = ByteBuffer.wrap(clusterNum);
                        int firstCluster = clusterWrap.getInt();
                        String hex = Integer.toHexString(firstCluster);
                        toPrint = toPrint + "\n Next cluster Number is 0x"+hex+" in big endian";

                        return toPrint;
                    }
                }
            }


        }
        return "not here";

    }

    //////////////////CD COMMAND HANDLING////////////////////////

    public long getNewDirStartingCluster(long start, String fileName) {
        ArrayList<Integer> clustersList = getAllClustersOfDir(start);

        for (int a = 0; a < clustersList.size(); a++) {
            long cluster = clustersList.get(a).longValue();//
            long firstSector = ((cluster-2)*info.getBPB_SecPerClus()) + info.getFirstDataSector();
            long startOfCluster = firstSector*info.getBPB_BytesPerSec();
            for (long i = startOfCluster; i < (startOfCluster + (info.getBPB_BytesPerSec() * info.getBPB_SecPerClus())); i += 32) {

                Long attrByte = i + 11;
                int index = attrByte.intValue();
                byte attr = buff.get(index);
                Long entryStart = i;
                int nameStart = entryStart.intValue();
                int readOnly = attr & 0x01;
                int isItADir = attr & 0x10;
                int archived = attr & 0x20;
                int isHidden = attr & 0x02;
                int isSystem = attr & 0x04;
                int isVolume = attr & 0x08;
                int longName = readOnly + isHidden + isSystem + isVolume; //if is 15 then this is set.
                //////////////CHECK VALIDITY OF ENTRY///////////////
                int isItDeleted = Byte.toUnsignedInt(buff.get(nameStart));
                if (((isHidden != 0 || isSystem != 0)&& longName!= 15) || isItDeleted == 229 || isItDeleted == 0 || isVolume != 0) { //0xE5 = 229, 0x00 = 0
                    continue;
                }
                else if(isItADir == 0){
                    continue;
                }
                ///////////DIRECTORY HANDLING://////////
                else {
                    StringBuilder name = new StringBuilder();
                    for (int x = nameStart; x < nameStart + 8; x++) {
                        byte next = buff.get(x);
                        char c = (char) (next & 0xFF);
                        if (c != ' ') {
                            name.append(c);
                        }
                    }
                    if (fileName.equals(name.toString())) {

                        byte[] clusterNum = new byte[4];
                        clusterNum[3] = buff.get(nameStart + 21);
                        clusterNum[2] = buff.get(nameStart + 20);
                        clusterNum[1] = buff.get(nameStart+27);
                        clusterNum[0] = buff.get(nameStart+26);
                        ByteBuffer clusterWrap = ByteBuffer.wrap(clusterNum).order(ByteOrder.LITTLE_ENDIAN);
                        int firstCluster = clusterWrap.getInt();
                        long clusterToLong = Integer.toUnsignedLong(firstCluster);
                        return clusterToLong;


                    }
                }
            }


        }
        return -1;

    }

}


