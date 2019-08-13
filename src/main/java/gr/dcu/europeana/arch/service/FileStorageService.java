package gr.dcu.europeana.arch.service;

import gr.dcu.europeana.arch.config.FileStorageProperties;
import gr.dcu.europeana.arch.exception.MyFileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
   
//   @Value( "${file.upload-dir}" )
//   String fileUploadDir;
   
   // private Path fileStorageLocation;
   
    // @Autowired
    // public FileStorageService(FileStorageProperties fileStorageProperties) {
//    public FileStorageService() {
//
////        // log.info("----> {}", fileUploadDir);
//        if(fileStorageProperties.getUploadDir() == null) {
//            log.error("NULLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL");
//        } else {
//            log.info("----> {}", fileStorageProperties.getUploadDir());
//        }
//        
//        /*
//        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
//               .toAbsolutePath().normalize();
//       
//        log.info("FileStorage Initialized at: {}", fileStorageLocation.toString());
//*/
//        /*
//        try {
//            Files.createDirectories(this.fileStorageLocation);
//        } catch(Exception ex) {
//            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
//        }*/
//       }

   /*
    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }

    public void setFileStorageLocation(Path fileStorageLocation) {
        this.fileStorageLocation = fileStorageLocation;
    }
    */
    
    public FileStorageProperties getFileStorageProperties() {
        return fileStorageProperties;
    }

    public void setFileStorageProperties(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }
    
   

   /**
    * 
    * @param fileName
    * @return 
    */
   public Resource loadFileAsResource(String fileName) {
       
       try {
           
           Path uploadDirPath = Paths.get(fileStorageProperties.getStorageHome()).toAbsolutePath().normalize();
           Path filePath = Paths.get(uploadDirPath.toString(), fileName);
            
           // Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
           Resource resource = new UrlResource(filePath.toUri());
           
           log.info("Load file: {}", filePath.toUri());
           
           if(resource.exists()) {
               return resource;
           } else {
               throw new MyFileNotFoundException("File not found " + fileName);
            }
           
       } catch(MalformedURLException ex) {
          throw new MyFileNotFoundException("File not found " + fileName, ex);
       }
   }
   
   /**
    * 
    * @param file
    * @return
    * @throws IOException 
    */
   public Path store(MultipartFile file) throws IOException {
       
       Path filePath;
       
       try {
            Path uploadDirPath = Paths.get(fileStorageProperties.getStorageHome()).toAbsolutePath().normalize();
            filePath = Paths.get(uploadDirPath.toString(), file.getOriginalFilename());

            byte[] bytes = file.getBytes();
            Files.write(filePath, bytes);
            
            log.info("File stored successfully. File: {} | Path: {}", 
                    file.getOriginalFilename(), filePath);
       } catch (IOException ex) {
           throw ex;
       }
       
       return filePath;
   }
   
   /*
    public void saveExcelFile(String fileName, Workbook workbook) {
       try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();

            // Write the output to a file
            FileOutputStream fileOut = new FileOutputStream(fileName);
            workbook.write(fileOut);
            fileOut.close();

            // Closing the workbook
            workbook.close();
       } catch(Exception ex) {
           log.error("", ex);
       }
       
   } */

   
}
