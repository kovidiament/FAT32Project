import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.Scanner;

public class Driver {
    public Driver() throws IOException {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            final String msg = "Usage: single arg for path of image file";
            System.err.println(msg);
            throw new IllegalArgumentException(msg);
        }
        byte[] array = Files.readAllBytes(new File(args[0]).toPath());
        ByteBuffer buff = ByteBuffer.wrap(array);
        buff.order(ByteOrder.LITTLE_ENDIAN);
        BootInfo info = new BootInfo(buff);
        LSDriver lsPrinter = new LSDriver(buff, info);
        long StartOfCurrentDir = info.getBPB_RootClus();
        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to the FAT32 Utility. \n");
        while(true){

                String command = sc.next();
                switch (command)
                {
                    case "info":
                        System.out.print(info.printNicely()+"\n");
                        break;
                    case "quit":
                        System.exit(0);
                        break;
                    case "ls":
                    System.out.print(lsPrinter.printAllOfCurrentDir(StartOfCurrentDir)+"\n");
                    break;
                    case "stat":
                        String name = sc.next();
                        String stats = lsPrinter.getStats(StartOfCurrentDir, name);
                        if(stats.equals("not here"))
                        {
                            System.out.println("Whoops! File or directory "+name+" could not be found.\n");
                        }
                        else{
                            System.out.print("Stats for "+name+":\n "+stats+"\n");
                        }
                        break;
                    case "cd":
                        String dirRequest = sc.next();
                        long newDir = lsPrinter.getNewDirStartingCluster(StartOfCurrentDir, dirRequest);//change to cluster
                        if(newDir == -1L)
                        {
                            System.out.println("Whoops! No Directory with the name "+dirRequest+" exists in current working directory\n");

                        }
                        else if(newDir == 0){
                           StartOfCurrentDir = info.getBPB_RootClus();
                        }
                        else {
                            StartOfCurrentDir = newDir;
                        }
                        break;
                    case "size":
                        String fileName = sc.next();
                        int size = lsPrinter.sizeOfFile(StartOfCurrentDir, fileName);
                        if(size == -1){
                            System.out.print("Whoops! no file named "+fileName+" exists in the current directory\n");
                        }
                        else{
                            System.out.print("File or Directory "+fileName+" is of size "+size+"\n");
                        }
                        break;
                    case "read":
                        String fileToRead = sc.next();
                        int position = sc.nextInt();
                        int numBytes = sc.nextInt();
                        String contents = lsPrinter.read(fileToRead, position, numBytes, StartOfCurrentDir);
                        System.out.print(contents+"\n");
                        break;
                    case "volume":
                        String volumeName = lsPrinter.getVolume(info.getBPB_RootClus());
                        System.out.print("Volume name: "+volumeName+"\n");
                        break;
                    default:
                        System.out.println(command+" is not a recognized command.");
                }

        }

    }
//"C:/comporggit/compOrgBackup/OS/fat32.img"
}
