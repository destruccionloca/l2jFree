package net.sf.l2j.gameserver.model.entity.geodata;

import java.io.RandomAccessFile;

/**
 * 
 * This class is handles the loading and unloading of the geodata files.
 * Only 1 file is left open at each time, but many blocks may try to 
 * access that same file or may request to change files.  
 *  
 * @author  Fulminus
*/
public class GeoDataFileReader
{
    private String currentFilename;
    private RandomAccessFile currentFile;
    
    public GeoDataFileReader(String filename)
    {
        currentFilename = filename; 
        startFile(filename);
    }
    
    private void startFile(String filename)
    {
        try 
        {
            currentFile = new RandomAccessFile(filename,"r");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            
            currentFilename = null;
            currentFile = null;
        }   
    }
    
    public void closeFile()
    {
        try
        {
            currentFile.close();
        }
        catch (Exception e)
        {
            ;  // do nothing
        }
        
        currentFile = null;
        currentFilename = null;
    }
    
    public String getName()
    {
        // if the file is ready, return the filename
        if (currentFile != null)
            return currentFilename;
        
        // else, return a null
        return null;
    }
   
    public int getBlockSize(int blockIndex)
    {
        try
        {
            currentFile.seek(8*blockIndex + 4);
            return currentFile.readUnsignedByte() | (currentFile.readUnsignedByte() << 8) | 
                (currentFile.readUnsignedByte() << 16) | (currentFile.readUnsignedByte() << 24);
        }
        catch(Exception e)
        {
            return -1;
        }
    }
    
    // returns the number of bytes read or -1 if failed.
    public int readGeoData(int blockIndex, byte[] b, int blockSize)
    {
        try
        {
            // find the header denoting the beginning of this block
            int blockStart;
            currentFile.seek(8*blockIndex);
            blockStart = currentFile.readUnsignedByte() | (currentFile.readUnsignedByte() << 8) | 
                (currentFile.readUnsignedByte() << 16) | (currentFile.readUnsignedByte() << 24);
            
            // skip as many bytes as necessary to get from the current location (at the header) to the beginning of the block 
            currentFile.seek(blockStart);
            
            // attempt read in all the bytes that fit in the passed length to the passed array and return the number of bytes actually read 
            currentFile.readFully(b,0,blockSize);
            
            // if no exception is thrown, readFully worked and the entire block was read.  Return the size of the block
            return blockSize;
        }
        catch(Exception e)
        {
            return -1;
        }
    }
}