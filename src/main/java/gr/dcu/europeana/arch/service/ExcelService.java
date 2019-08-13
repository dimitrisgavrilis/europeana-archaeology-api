package gr.dcu.europeana.arch.service;

import gr.dcu.europeana.arch.model.MappingTerm;
import gr.dcu.europeana.arch.exception.MyFileNotFoundException;
import gr.dcu.europeana.arch.model.AatSubject;
import gr.dcu.europeana.arch.repository.AatSubjectRepository;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
@Service
public class ExcelService {
    
    @Autowired
    private ResourceLoader resourceLoader;
    
    @Autowired
    FileStorageService fileStorageService;
    
    @Autowired
    private AatSubjectRepository aatSubjectRepository;
    
    // e.g. http://vocab.getty.edu/page/aat/300387302
    private static final String AAT_URI_PREFIX = "http://vocab.getty.edu/page/aat/";
    
    private static final String AAT_LOD_URI_PREFIX = "http://vocab.getty.edu/aat/";
    
    private static final String DEFAULT_EXPORT_FILENAME = "mappings.xlsx";
    
    // Types of aat subjects
    private static final String AAT_SUBJECT_FACET_OBJECTS            = "objects";
    private static final String AAT_SUBJECT_FACET_STYLES_AND_PERIODS = "styles and periods";
    private static final String AAT_SUBJECT_FACET_AGENTS             = "agents";
    private static final String AAT_SUBJECT_FACET_MATERIALS          = "materials";
    private static final String AAT_SUBJECT_FACET_ACTIVITIES         = "activities";
    private static final String AAT_SUBJECT_FACET_OTHER              = "other";
    
    /**
     * 
     * @param filename 
     */
    public void loadAatSubjectsFromFile(String filename) {

        /*
        File excelFile = new File(getClass().getResource(filename).getFile());
        if(excelFile.exists()) {
            log.info("Exists!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }*/
        
        try {
            
            Resource resource = resourceLoader.getResource("classpath:" + filename);
            File excelFile = resource.getFile();
//            if(excelFile.exists()) {
//                log.info("Exists!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//            }
            
            // log.info("URI: {}" + resource.getURI());
        
            FileInputStream excelFileInputStream = new FileInputStream(excelFile);
            Workbook workbook = new XSSFWorkbook(excelFileInputStream);
            
            List<AatSubject> aatSubjectList;
            String type;
            // Parse Aat Objects facet
            type = AAT_SUBJECT_FACET_OBJECTS;
            aatSubjectList = processAatSheet(workbook, type, 0, 2, -1);
            log.info("Saving {} facet. #Aat Subjects: {}" , type, aatSubjectList.size());
            aatSubjectRepository.saveAll(aatSubjectList);
            
             // Parse Aat Styles and Periods facet
            type = AAT_SUBJECT_FACET_STYLES_AND_PERIODS;
            aatSubjectList = processAatSheet(workbook, type, 1, 2, -1);
             log.info("Saving {} facet. #Aat Subjects: {}" , type, aatSubjectList.size());
            aatSubjectRepository.saveAll(aatSubjectList);
            
            // Parse Aat Agents facet
            type = AAT_SUBJECT_FACET_AGENTS;
            aatSubjectList = processAatSheet(workbook, type, 2, 2, -1);
            log.info("Saving {} facet. #Aat Subjects: {}" , type, aatSubjectList.size());
            aatSubjectRepository.saveAll(aatSubjectList);
            
            // Parse Materials facet
            type = AAT_SUBJECT_FACET_MATERIALS;
            aatSubjectList = processAatSheet(workbook, type, 3, 2, -1);
            log.info("Saving {} facet. #Aat Subjects: {}" , type, aatSubjectList.size());
            aatSubjectRepository.saveAll(aatSubjectList);
            
            // Parse Activities facet
            type = AAT_SUBJECT_FACET_ACTIVITIES;
            aatSubjectList = processAatSheet(workbook, type, 4, 2, -1);
            log.info("Saving {} facet. #Aat Subjects: {}" , type, aatSubjectList.size());
            aatSubjectRepository.saveAll(aatSubjectList);
            
            // Parse other facet
            type = AAT_SUBJECT_FACET_OTHER;
            aatSubjectList = processAatSheet(workbook, type, 5, 2, -1);
            log.info("Saving {} facet. #Aat Subjects: {}" , type, aatSubjectList.size());
            aatSubjectRepository.saveAll(aatSubjectList);
            
            
        } catch(IOException ex) {
            log.error("FileNotFound", ex);
        }
        
    }
    
    /**
     * 
     * @param workbook
     * @param type
     * @param index
     * @param skipLineCount
     * @param limitCount
     * @return 
     */
    public List<AatSubject> processAatSheet(Workbook workbook, String type, 
            int index, int skipLineCount, int limitCount) {
        
        List<AatSubject> aatSubjectList = new LinkedList<>();
        
        // Process sheet
        Sheet datatypeSheet = workbook.getSheetAt(index);
        Iterator<Row> iterator = datatypeSheet.iterator();

        int rowCount = 0;

        // Process each row...
        while (iterator.hasNext()) {

            Row currentRow = iterator.next();

            rowCount++;

            // Skip first rows
            if(rowCount <= skipLineCount) {
                continue;
            }

            String aatUid;
            String label;
            String uri;
            String lodUri;

            Iterator<Cell> cellIterator = currentRow.iterator();
            while (cellIterator.hasNext()) {

                Cell currentCell = cellIterator.next();
                //getCellTypeEnum ill be renamed to getCellType starting from version 4.0
                if (currentCell.getCellTypeEnum() == CellType.STRING) {

                    AatSubject currentAatSubject = new AatSubject();
                    currentAatSubject.setId(-1);

                    // Set name
                    label = currentCell.getStringCellValue();
                    if(label.startsWith("<") && label.endsWith(">")) {
                        continue;
                    }
                    currentAatSubject.setLabel(label);

                    // Set type
                    currentAatSubject.setType(type);

                    // Set uploadDirPath
                    // currentAatSubject.setPath("");

                    // Set uri & aat_id
                    Hyperlink link = currentCell.getHyperlink();
                    if(link != null) {
                        aatUid = link.getAddress().substring(link.getAddress().lastIndexOf("=") + 1);
                        uri = AAT_URI_PREFIX + aatUid;
                        lodUri = AAT_LOD_URI_PREFIX + aatUid; 
                                
                        currentAatSubject.setAatUid(aatUid);
                        currentAatSubject.setUri(uri);
                        currentAatSubject.setLodUri(lodUri);
                        

                    } else {
                        log.warn("Link is missing");
                    }

                    aatSubjectList.add(currentAatSubject);

                    // System.out.println(currentAatSubject.toString());
                }
            }

            // Stop processing
            if(rowCount == skipLineCount + limitCount) {
                break;
            }
        }
        
        return aatSubjectList;
    }
    
    
    /**
     * 
     * @param filename
     * @param mappingId
     * @param skipLineCount
     * @param limitCount
     * @return 
     */
    public List<MappingTerm> loadMappingTermsFromExcel(String filename, long mappingId, 
            int skipLineCount, int limitCount) {

        List<MappingTerm> mappings = new LinkedList<>();
        
        try {
            
            // Resource resource = resourceLoader.getResource("classpath:" + filename);
            // File excelFile = resource.getFile();
            
            File excelFile = new File(filename);
            if(excelFile.exists() && excelFile.isFile()) {
                log.info("URI: {}", excelFile.getAbsolutePath());
                
                // Open and process excel 
                FileInputStream excelFileInputStream = new FileInputStream(excelFile);
                Workbook workbook = new XSSFWorkbook(excelFileInputStream);
                Sheet datatypeSheet = workbook.getSheetAt(0);
                Iterator<Row> iterator = datatypeSheet.iterator();

                int rowCount = 0;

                // Process each row...
                while (iterator.hasNext()) {

                    Row currentRow = iterator.next();

                    rowCount++;

                    // Skip first rows
                    if(rowCount <= skipLineCount) {
                        continue;
                    }

                    if(currentRow != null) {

                        //log.info("Row: {}", currentRow);

                        String nativeTerm;
                        String aatConceptLabel;
                        String aatUid;

                        // Get Native Term - It is mandatory
                        Cell nativeTermCell = currentRow.getCell(0);
                        if(nativeTermCell == null) {
                            log.info("NULL");
                            continue;
                        } 
                        nativeTerm = nativeTermCell.getStringCellValue();

                        // Get AAT Concept label
                        Cell aatConceptLabelCell = currentRow.getCell(1);
                        if(aatConceptLabelCell == null) {
                            aatConceptLabel = "";
                        } else {
                            // This guarantess that you will read the value as string
                            aatConceptLabelCell.setCellType(CellType.STRING);
                            aatConceptLabel = aatConceptLabelCell.getStringCellValue();
                        }
                        
                        // Get AAT uid
                        Cell aatUidCell = currentRow.getCell(2);
                        if(aatUidCell == null) {
                            aatUid = "";
                        } else {
                            // This guarantess that you will read the value as string
                            aatUidCell.setCellType(CellType.STRING);
                            aatUid = aatUidCell.getStringCellValue();
                             
                            /*
                            if (aatUidCell.getCellTypeEnum() == CellType.STRING) {
                                aatUid = aatUidCell.getStringCellValue();
                            } else if(aatUidCell.getCellTypeEnum() == CellType.NUMERIC) {
                                aatUid = String.valueOf(aatUidCell.getNumericCellValue());
                            } else {
                                aatUid = "Unknown Cell Type";
                            }*/
                        }

                        // Create mapping term
                        MappingTerm mappingTerm = new MappingTerm();
                        // mappingTerm.setId((long) -1);
                        mappingTerm.setMappingId(mappingId);
                        mappingTerm.setNativeTerm(nativeTerm);
                        mappingTerm.setAatConceptLabel(aatConceptLabel);
                        mappingTerm.setAatUid(aatUid);

                        mappings.add(mappingTerm);
                    }

                    // Stop processing
                    if(rowCount == skipLineCount + limitCount) {
                        break;
                    }
                }
            } else {
                log.error("File not found " + filename);
                throw new MyFileNotFoundException("File not found " + filename);
            }
           
        } catch(IOException ex) {
            log.error("File not found " + filename);
                throw new MyFileNotFoundException("File not found " + filename);
        }
        
        return mappings;
        
    }
    
    /**
     * 
     * @param filename
     * @param terms
     * @throws IOException 
     */
    public String exportMappingTermsToExcel(String filename, List<MappingTerm> terms) throws IOException {
        
        String filePathStr;
        
        try {
           
            log.info("Export to excel file...");
            
            // Create a Workbook
            Workbook workbook = new XSSFWorkbook();

            // Create a Sheet
            Sheet sheet = workbook.createSheet("Mappings");

            // Create a Font for styling header cells
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            // headerFont.setFontHeightInPoints((short) 14);
            // headerFont.setColor(IndexedColors.RED.getIndex());

            // Create a CellStyle with the font
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            // Create a Row
            Row headerRow = sheet.createRow(0);

            // 3 columns 
            String[] columns = {"Native Term", "Aat concept label", "Aat uid"};
            
            // Create cells
            for(int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Create Other rows and cells with employees data
            int rowNum = 1;
            for(MappingTerm term: terms) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0)
                        .setCellValue(term.getNativeTerm());

                row.createCell(1)
                        .setCellValue(term.getAatConceptLabel());
                
                row.createCell(2)
                        .setCellValue(term.getAatUid());

//                Cell dateOfBirthCell = row.createCell(2);
//                dateOfBirthCell.setCellValue(employee.getDateOfBirth());
//                dateOfBirthCell.setCellStyle(dateCellStyle);
//
//                row.createCell(3)
//                        .setCellValue(employee.getSalary());
            }

            // Resize all columns to fit the content size
            for(int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // log.info("-----> Saving...");
            // Path fileStorageLocation = fileStorageService.getFileStorageLocation();
            // log.info("******************** 1 * {}", fileStorageService.getFileStorageProperties().getUploadDir());
            // log.info("-----> {}", fileStorageLocation.toAbsolutePath());
            
            Path uploadDirPath = Paths.get(fileStorageService.getFileStorageProperties().getStorageHome()).toAbsolutePath().normalize();
            Path filePath = Paths.get(uploadDirPath.toString(), filename);
            filePathStr = filePath.toString();
            log.info("File path: {}", filePath.toString());
            
            // Write the output to a excelFile
            FileOutputStream fileOut = new FileOutputStream(filePath.toString());
            workbook.write(fileOut);
            fileOut.close();

            // Closing the workbook
            workbook.close();
            
            log.info("Export saved at {}", filename);
            
        } catch (IOException ex) {
            throw ex;
        }
        
        return filePathStr;
    }
    
}

