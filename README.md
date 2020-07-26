FAT 32 File System Utility

This project provides a shell-like utility that is capable of interpreting a FAT32 file system image. The FAT32 specification used is provided as fat32spec.pdf.

The BootInfo class interprets the BIOS Parameter Block, BPB, located in the first sector of the volume, in its reserved region, also called the “boot sector.” The LSDriver class performs all reading and related operations on the image, and is invoked from the Driver class, which contains the main method.

Commands supported by the main method in the Driver, to which a single argument, the location of the file system image is passed, are as follows:

|Command|Description  |
|--|--|
|info |Prints out information about the following fields in both hex and base 10: BPB_BytesPerSec, BPB_SecPerClus, BPB_RsvdSecCnt, BPB_NumFATS, BPB_FATSz32 |
|stat <FILE_NAME/DIR_NAME> |Prints the size of the file or directory name, the attributes of the file or directory name, and the first cluster number of the file or directory name if it is in the present working directory.  Return an error if FILE_NAME/DIR_NAME does not exist. size of a directory will always be zero|
|size <FILE_NAME> | Prints the size of file FILE_NAME in the present working directory. Log an error if FILE_NAME does not exist. |
|cd <DIR_NAME>|Changes the present working directory to DIR_NAME.  Log an error if the directory does not exist.  DIR_NAME may be “.” (here) and “..” (up one directory).  You don't have to handle a path longer than one directory.  |
|ls <DIR_NAME> |Lists the contents of DIR_NAME, including “.” and “..”. |
|read FILE_NAME POSITION NUM_BYTES | Reads from a file named FILE_NAME, starting at POSITION, and prints NUM_BYTES. Return an error when trying to read an unopened file. |
|volume|Prints the volume name of the file system image.  If there is a volume name it will be found in the root directory.  If there is no volume name, print “Error: volume name not found.”|
|quit | Quit the utility|
