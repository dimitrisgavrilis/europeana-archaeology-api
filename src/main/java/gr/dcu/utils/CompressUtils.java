package gr.dcu.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;


/**
 *
 * @author Vangelis
 */
@Slf4j
public class CompressUtils {
    
    private static final int BUFFER_SIZE = 4096;
    
    /**
     * 
     * @param outDirPath
     * @param filenamePrefix
     * @param files
     * @return 
     */
    public static Path tarGzip (String outDirPath, String filenamePrefix, File[] files) {
        
        boolean tarGZipped = false;
        
        Path archivePath = Paths.get(outDirPath, filenamePrefix + ".tar.gz");
        // String outFilePath = outDirPath + "/" + filenamePrefix + ".tar.gz";
        
        TarArchiveOutputStream tarGzipOutput = null;
        try {
            tarGzipOutput = new TarArchiveOutputStream(new GZIPOutputStream (
                    new BufferedOutputStream (new FileOutputStream(archivePath.toString()))));
            
            // Add files to tar
            for(File file : files) {
                TarArchiveEntry entry = new TarArchiveEntry(file.getName());
                entry.setSize(FileUtils.sizeOf(file));
                tarGzipOutput.putArchiveEntry(entry);
                tarGzipOutput.write(FileUtils.readFileToByteArray(file));
                tarGzipOutput.closeArchiveEntry();
            }
            
            tarGZipped = true;
            
        } catch (IOException ex) {
            log.error("Error creating outputstream.", ex);
        } finally {
            if (tarGzipOutput != null) {
                try {
                    tarGzipOutput.close();
                } catch (IOException ex) {
                    log.error("Error closing outputstream.", ex);
                }
            }
        }
        
        return archivePath;
    }
    
    /**
     * 
     * @param tarFile
     * @param dest
     * @return
     * @throws SecurityException
     * @throws IOException 
     */
    public static long unTarGzip(File tarFile, File dest) throws SecurityException, IOException {
        
        long fileEntries = 0;
        TarArchiveInputStream tarInStream = null;
        
        try {
            // Create destination folder if it does not exist
            if(!dest.exists()) {
                log.debug("Create directory: {}", dest.getAbsolutePath());
                dest.mkdirs();
            }
        
            tarInStream = new TarArchiveInputStream(
                    new GzipCompressorInputStream(
                            new BufferedInputStream(new FileInputStream(tarFile))
                    )
            );

            TarArchiveEntry tarEntry = tarInStream.getNextTarEntry();
            while(tarEntry != null) {
                File destPath = new File(dest, tarEntry.getName());

                if(tarEntry.isDirectory()) {
                    destPath.mkdirs();
                } else {
                    destPath.createNewFile();
                    
                    BufferedOutputStream buffedOutputStream = new BufferedOutputStream(new FileOutputStream(destPath));
                    byte[] bytesIn = new byte[BUFFER_SIZE];
                    
                    int length = 0;
                    while((length = tarInStream.read(bytesIn)) != -1) {
                        buffedOutputStream.write(bytesIn, 0, length);
                    }

                    buffedOutputStream.close();

                    fileEntries++;
                }

                tarEntry = tarInStream.getNextTarEntry();

            }
        } catch(SecurityException ex) {
            log.error("Cannot create directory. Permisssion denied.", ex);
            throw ex;
        } catch(IOException ex) {
            log.error("Untar failed. Internal error.", ex);
            throw ex;
        }
       
        
        return fileEntries;
    }
    
    public static long unZip(File zipFile, File dest) throws SecurityException, IOException {
         
        long fileEntries = 0;
         
        ZipInputStream zipInStream = null;
        try {
            // Create destination folder if it does not exist
            if(!dest.exists()) {
                log.debug("Create directory: {}", dest.getAbsolutePath());
                dest.mkdirs();
            }
            
            zipInStream = new ZipInputStream(new FileInputStream(zipFile));

            ZipEntry zipEntry = zipInStream.getNextEntry();
            while(zipEntry != null) {
                
                File destEntry = new File(dest, zipEntry.getName());
                
                // If the entry is a directory, make the directory
                if(zipEntry.isDirectory()) {
                    destEntry.mkdirs();
                } else {
                    BufferedOutputStream buffedOutputStream = new BufferedOutputStream(new FileOutputStream(destEntry));
                    byte[] bytesIn = new byte[BUFFER_SIZE];
                    int length = 0;
                    while((length = zipInStream.read(bytesIn)) != -1) {
                        buffedOutputStream.write(bytesIn, 0, length);
                    }
                    buffedOutputStream.close();
                    
                    fileEntries++;
                }
                
                zipInStream.closeEntry();
                zipEntry = zipInStream.getNextEntry();
            }
            
            zipInStream.close();
            
        } catch(IOException ex) {
            log.error("Unzip failed. Internal error.", ex);
            throw ex;
        } 
         
        return fileEntries;
         
     }
    
}
