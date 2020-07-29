package gr.dcu.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;


@Slf4j
public class FileUtilities {
    
    /**
     * Read file
     * @param path filepath
     * @return file's content
     * @throws IOException 
     */
    public static String readFile(String path) throws IOException {
        byte[] fileContent = Files.readAllBytes(Paths.get(path));
        return new String(fileContent);
    }
    
    /**
     * List XML files of a specified directory
     * @param dPath
     * @return 
     */
    public static List<String> listFileNames(String dPath) {
       
       List<String> results = new ArrayList<String>();
       
       File dir = new File(dPath);
       File[] listOfFiles = dir.listFiles();
       
       for (File file : listOfFiles) {
           if (file.isFile()) {
               results.add(file.getName());
            }
        }
       
       return results;
    }

    public static List<File> listAllFiles(File file) throws IOException {

        List<File> result = new LinkedList<>();
        try (Stream<Path> walk = Files.walk(file.toPath()))  {

            result = walk
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            // result.forEach(System.out::println);

        } catch(IOException ex) {
            log.error("", ex);
            throw ex;
        }

        return result;
    }

    public static List<String> listAllFilesByExtension(File file, String extension) throws IOException {

        List<String> result = new LinkedList<>();
        try (Stream<Path> walk = Files.walk(file.toPath()))  {
            result = walk
                    .map(Path::toString)
                    .filter(f -> f.endsWith(extension))
                    .collect(Collectors.toList());


            // result.forEach(System.out::println);

        } catch(IOException ex) {
            log.error("", ex);
            throw ex;
        }

        return result;
    }


    
    /**
     * 
     * @param files
     * @return 
     */
    public static Collection filesToFilenames(Collection files) {
        Collection filenames = new java.util.ArrayList(files.size());
        Iterator i = files.iterator();
        while (i.hasNext()) {
            filenames.add(((File)i.next()).getName());
        }
        return filenames;
    }
    
    
    
    
    /**
     * Creates a temporary file using an array of bytes.
     * @param data Array of bytes.
     * @return Temporary file.
     * @throws IOException Throws the exception to be handler by other method.
     */
    public static File createTmp(byte[] data) throws IOException {
            File tmp = null;
            
            try {
                tmp = File.createTempFile("schematron_", ".tmp");
                FileOutputStream fos = new FileOutputStream(tmp);
                fos.write(data);
                fos.flush();
                fos.close();
            } catch(IOException ex) {
                log.error("",ex);
                throw ex;
            }
            
            return tmp;
    }
    
    /**
     * 
     * @param filePath
     * @param data
     * @return
     * @throws IOException 
     */
    public static File createFile(String filePath, byte[] data) throws IOException { 
        File tmp = null;
        try {
            tmp = new File(filePath);
            FileOutputStream fos = new FileOutputStream(tmp);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch(IOException ex) {
            log.error("",ex);
            throw ex;
        }
        
        return tmp;
    }
    
    public static File createFile(String filePath, String data) throws IOException { 
        File tmp = null;
        try {
            tmp = new File(filePath);
            FileOutputStream fos = new FileOutputStream(tmp);
            fos.write(data.getBytes());
            fos.flush();
            fos.close();
        } catch(IOException ex) {
            log.error("",ex);
            throw ex;
        }
        
        return tmp;
    }
    
    /**
     * 
     * @param dir
     * @param filename
     * @return 
     */
    public static List<File> searchDir(File dir, String filename) {
        
        List<File> fileList = new LinkedList<File>();
        
        // Check if you have permission to read this directory
        if(dir.isDirectory()) {    
            if(dir.canRead()) {
                File[] matchingFiles = dir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.equals("info.xml");
                    }
                });
                
                fileList = Arrays.asList(matchingFiles);
                
            } else {
                log.error(dir.getAbsoluteFile() + "Permission denied.");
            }
        }
        
        return fileList;
    }
    
    /**
     * 
     * @param dir
     * @param filename
     * @return 
     */
    public static List<File> searchDirRecursively(File dir, String filename) {
        
        List<File> matchingFiles = new LinkedList<File>();
        
        // Check if you have permission to read this directory
        if(dir.isDirectory()) {    
            if(dir.canRead()) {
                for (File tmpFile : dir.listFiles()) {
                    if(tmpFile.isDirectory()) { // Find child directoy's matching files
                        List<File> childMatchingFiles = searchDir(tmpFile, filename);
                        matchingFiles.addAll(childMatchingFiles);
                    } else {
                        if (filename.equals(tmpFile.getName())) {
                            matchingFiles.add(tmpFile);
                        }
                    }
                }
            } else {
                System.out.println(dir.getAbsoluteFile() + "Permission denied.");
            }
        }
       
        return matchingFiles;
    }
    
    /**
     * 
     * @param file
     * @return
     * @throws IOException 
     */
    public static String detectMimeType(File file) throws IOException {
       String mimeType = "";
       
       BufferedInputStream bis = null;
       
       try {
            bis = new BufferedInputStream(new FileInputStream(file));
            ContentHandler contentHandler = new BodyContentHandler();
            
            AutoDetectParser parser = new AutoDetectParser();
            Detector detector = parser.getDetector();
            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());
            MediaType mediaType = detector.detect(bis, metadata);
            mimeType = mediaType.toString();
       } catch(IOException ex) {
           log.error("", ex);
           throw ex;
       } finally {
           if(bis != null) {
               try {
                   bis.close();
               } catch (IOException ex) {
                   log.error("", ex);
                   throw ex;
               }
           }
       }
       
       return mimeType;
    }
    
    /**
     *
     * @return
     * @throws IOException 
     */
    public static String readFileFromUrl(String sourceUri)throws IOException {
       
        String itemXmlContent = null;
        
        try {
            // Get content
            URL itemUrl = new URL(sourceUri);
            InputStream itemInputStream = itemUrl.openStream();
            itemXmlContent = IOUtils.toString(itemInputStream, "UTF-8");
            itemInputStream.close();
        } catch(IOException ex) {
            throw ex;
        }
        
        return itemXmlContent;
       /*
       BufferedReader br = null;
       StringBuilder sb = new StringBuilder();
       try {
            URL url = new URL(endpoint);
            br = new BufferedReader(new InputStreamReader(url.openStream()));
       
            String line;
            while((line = br.readLine()) != null) {
                sb.append(line);
                // sb.append(System.lineSeparator());
            }
            
       } catch(IOException ex) {
           log.error(ex);
           throw ex;
       } finally {
           if(br != null) {
                br.close();
           }
       }
       
       return sb.toString();
       */
   }
    
    public static String assembleTmpFileName(String filename, String prefix) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date = new Date();
        
        String tmpFilename = prefix + dateFormat.format(date) + "-" + filename;
        
        return tmpFilename;
    }
    
    public static String assembleTmpFilePath(String tmpDir, String filename, String prefix) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date = new Date();
        
        String filePath = tmpDir + "/" + prefix + dateFormat.format(date) + "-" + filename;
        
        return filePath;
    }
    
}
