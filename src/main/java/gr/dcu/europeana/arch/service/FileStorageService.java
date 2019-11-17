package gr.dcu.europeana.arch.service;

import gr.dcu.europeana.arch.config.FileStorageProperties;
import gr.dcu.europeana.arch.exception.MyFileNotFoundException;
import gr.dcu.utils.CompressUtils;
import gr.dcu.utils.FileUtilities;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
@Service
public class FileStorageService {
    
    @Autowired
    private FileStorageProperties fileStorageProperties;
    
    public FileStorageProperties getFileStorageProperties() {
        return fileStorageProperties;
    }

    public void setFileStorageProperties(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    public Resource loadFileAsResource(Path filePath) {
        
        try {

            // Path archiveFilePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
           
            log.info("Load file: {}", filePath.toUri());

            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found at " + filePath);
            }
           
        } catch(MalformedURLException ex) {
           throw new MyFileNotFoundException("File not found " + filePath, ex);
        }
    }
   
    public Path upload(Path destinationFilePath, MultipartFile file) throws IOException {
       
        try {
            byte[] bytes = file.getBytes();
            Files.write(destinationFilePath, bytes);

            //log.info("File stored successfully. File: {} | Path: {}", 
            //        file.getOriginalFilename(), destinationFilePath);
        } catch (IOException ex) {
            throw ex;
        }
       
        return destinationFilePath;
    }
   
    // Extract archive to destination
    
    public void extractArchive(Path archiveFilePath, Path destinationDirPath) throws IOException {
               
        try {
            File file = archiveFilePath.toFile();
            if(file.exists() && file.isFile()) {
                
                // Create directories (if doesn't exist)
                Files.createDirectories(destinationDirPath);

                // Detect file type and uncompress
                String mimeType = FileUtilities.detectMimeType(file);
                switch (mimeType) {
                    case "application/gzip":
                        CompressUtils.unTarGzip(file, destinationDirPath.toFile());
                        break;
                    case "application/zip":
                        CompressUtils.unZip(file, destinationDirPath.toFile());
                        break;
                    default:    
                        log.warn("Unsupported mimeType: " + mimeType);
                        throw new IOException();
                }
             } else {
                 log.error("File does not exist.");
                 throw new IOException ();
             }
        } catch (IOException ex) {
           throw ex;
       }
    }
   
    
    public Path createArchiveFromDirectory(Path dirPath, String filenamePrefix) throws IOException {
       
       Path archiveFilePath;
               
       try {
           
           File enrichDir = dirPath.toFile();
           if(enrichDir.exists() && enrichDir.isDirectory()) {
               
               String outDirPath = dirPath.getParent().toString();
                       
               File[] files = dirPath.toFile().listFiles(); 
               log.info("Create enriched EDM archive from path: {} #Files: {}", dirPath, files.length);
               archiveFilePath = CompressUtils.tarGzip(outDirPath, filenamePrefix, files);
               
           } else {
               log.error("Directory does not exist.");
               throw new IOException ();
           }
       } catch (IOException ex) {
           throw ex;
       }
       
       return archiveFilePath;
    }
   
    // *** Utility functions ***//
   
    /**
     * 
     * @param fileNamePrefix
     * @param extesnion
     * @return 
     */
    public String buildFileNameWithTimestamp(String fileNamePrefix, String extesnion) {
       
        Instant now = Instant.now();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        String formattedInstant= sdf.format(Date.from(now));
        
        return fileNamePrefix + "_" + formattedInstant + extesnion;
        // return formattedInstant + "_" + fileNameSuffix;
                
    }

    /**
     * 
     * @param mappingId
     * @return
     * @throws IOException 
     */
    public Path buildExportFilePath (long mappingId) throws IOException {
        
       Path filePath;
        
        try {
            // Create a unique filename
            String fileNamePrefix = "mapping_" + mappingId;
            String fileName = buildFileNameWithTimestamp(fileNamePrefix, ".xlsx");

            // Create export directory if does not exist (i.e. <storage_home>/<mapping_id>/exports)
            Path storageHomeDir = Paths.get(getFileStorageProperties().getStorageHome()).toAbsolutePath().normalize();
            Path exportDir = Paths.get(storageHomeDir.toString(), String.valueOf(mappingId), "exports");
            Files.createDirectories(exportDir);
            
            // Build filepath
            filePath = Paths.get(exportDir.toString(), fileName);
            // log.info("Export file path: {}", archiveFilePath.toString());
        } catch(IOException ex) {
            throw ex;
        }
        
        return filePath;
    }
    
    /**
     * 
     * @param mappingId
     * @param fileName
     * @return
     * @throws IOException 
     */
    public Path buildUploadFilePath (long mappingId, String fileName) throws IOException {
        
       Path filePath;
        
        try {
            // Create export directory if does not exist (i.e. <storage_home>/<mapping_id>/uploads)
            Path storageHomeDir = Paths.get(getFileStorageProperties().getStorageHome()).toAbsolutePath().normalize();
            Path exportDir = Paths.get(storageHomeDir.toString(), String.valueOf(mappingId), "uploads");
            Files.createDirectories(exportDir);
            
            // Build filepath
            filePath = Paths.get(exportDir.toString(), fileName);
            // log.info("Export file path: {}", archiveFilePath.toString());
        } catch(IOException ex) {
            throw ex;
        }
        
        return filePath;
    }
    
    /**
     * 
     * @param mappingId
     * @param fileName
     * @param requestId
     * @return
     * @throws IOException 
     */
    public Path buildUploadEdmArchiveFilePath (long mappingId, String fileName, long requestId) throws IOException {
        
       Path filePath;
        
        try {
            // Create export directory if does not exist (i.e. <storage_home>/<mapping_id>/uploads)
            Path storageHomeDir = Paths.get(getFileStorageProperties().getStorageHome()).toAbsolutePath().normalize();
            Path exportDir = Paths.get(storageHomeDir.toString(), String.valueOf(mappingId), "enrich", String.valueOf(requestId));
            Files.createDirectories(exportDir);
            
            // Build filepath
            filePath = Paths.get(exportDir.toString(), fileName);
            // log.info("Export file path: {}", archiveFilePath.toString());
        } catch(IOException ex) {
            throw ex;
        }
        
        return filePath;
     }
    
    public Path buildUploadEdmArchiveFilePathNew (long packageId, String fileName) throws IOException {
        
       Path filePath;
        
        try {
            // Create export directory if does not exist (i.e. <storage_home>/<mapping_id>/uploads)
            Path storageHomeDir = Paths.get(getFileStorageProperties().getStorageHome()).toAbsolutePath().normalize();
            Path exportDir = Paths.get(storageHomeDir.toString(), String.valueOf("package_" + packageId));
            Files.createDirectories(exportDir);
            
            // Build filepath
            filePath = Paths.get(exportDir.toString(), fileName);
            // log.info("Export file path: {}", archiveFilePath.toString());
        } catch(IOException ex) {
            throw ex;
        }
        
        return filePath;
     }
    
    public Path buildEdmArchiveExtractionPath (long packageId) throws IOException {
        
       Path edmDirPath;
        
        //try {
            // Create export directory if does not exist (i.e. <storage_home>/<mapping_id>/uploads)
            Path storageHomeDir = Paths.get(getFileStorageProperties().getStorageHome()).toAbsolutePath().normalize();
            Path packageHomePath = Paths.get(storageHomeDir.toString(), String.valueOf("package_" + packageId));
            
            // Build filepath
            edmDirPath = Paths.get(packageHomePath.toString(), "EDM");
            // log.info("Export file path: {}", archiveFilePath.toString());
//        } catch(IOException ex) {
//            throw ex;
//        }
        
        return edmDirPath;
     }
    
    public File loadFile (String filepath) throws IOException {
        
        File file = new File(filepath);

        if(file.exists() && file.isFile()) {
            return file;
        } else {
            log.warn("File does not exist. File: " + file.getAbsolutePath());
        }
        
        return file;
        
     }
    
    public Path buildEdmArchiveExtractDirPath (long requestId, Path filePath) throws IOException {
        Path extractDirPath;
        
        try {
            extractDirPath = Paths.get(filePath.getParent().toString(), String.valueOf(requestId));
            Files.createDirectories(extractDirPath);

        } catch(IOException ex) {
            throw ex;
        }
        
        return extractDirPath;
        
     }
   
}
