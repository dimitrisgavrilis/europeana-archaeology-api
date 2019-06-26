package gr.dcu.share3d.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

/**
 *
 * @author Vangelis Nomikos
 */
public class FileUtils {
    
     public static String createFileName(String fileNameSuffix) {
       
        Instant now = Instant.now();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        String formattedInstant= sdf.format(Date.from(now));
        
        return formattedInstant + "_" + fileNameSuffix;
                
    }
}
