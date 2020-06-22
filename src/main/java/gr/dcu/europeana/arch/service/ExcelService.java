package gr.dcu.europeana.arch.service;

import gr.dcu.europeana.arch.domain.entity.SubjectTermEntity;
import gr.dcu.europeana.arch.exception.MyFileNotFoundException;
import gr.dcu.europeana.arch.domain.entity.SpatialTermEntity;
import gr.dcu.europeana.arch.domain.entity.TemporalTermEntity;
import gr.dcu.europeana.arch.service.edm.EdmFileTermExtractionResult;
import gr.dcu.europeana.arch.service.edm.ElementExtractionData;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
@Service
public class ExcelService {
    
    private final FileStorageService fileStorageService;

    private static final String DEFAULT_EXPORT_FILENAME = "mappings.xlsx";

    public ExcelService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * 
     * @param filename
     * @param mappingId
     * @param skipLineCount
     * @param limitCount
     * @return 
     */
    public List<SubjectTermEntity> loadSubjectTermsFromExcel(String filename, long mappingId,
                                                             int skipLineCount, int limitCount) {

        List<SubjectTermEntity> terms = new LinkedList<>();
        
        try {
            
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
                        String language;
                        String aatConceptLabel;
                        String aatUid;

                        // Get Native Term - It is mandatory
                        Cell nativeTermCell = currentRow.getCell(0);
                        if(nativeTermCell == null) {
                            log.info("NULL native term");
                            continue;
                        } else {
                            nativeTermCell.setCellType(CellType.STRING);
                            nativeTerm = nativeTermCell.getStringCellValue();
                        }
                        
                        // Get language - it is mandatory
                        Cell languageCell = currentRow.getCell(1);
                        if(languageCell == null) {
                            log.info("NULL language");
                            continue;
                        } else {
                            // This guarantess that you will read the value as string
                            languageCell.setCellType(CellType.STRING);
                            language = languageCell.getStringCellValue();
                        }
                        

                        // Get AAT Concept label
                        Cell aatConceptLabelCell = currentRow.getCell(2);
                        if(aatConceptLabelCell == null) {
                            aatConceptLabel = "";
                        } else {
                            // This guarantess that you will read the value as string
                            aatConceptLabelCell.setCellType(CellType.STRING);
                            aatConceptLabel = aatConceptLabelCell.getStringCellValue();
                        }
                        
                        // Get AAT uid
                        Cell aatUidCell = currentRow.getCell(3);
                        if(aatUidCell == null) {
                            aatUid = "";
                        } else {
                            // This guarantess that you will read the value as string
                            aatUidCell.setCellType(CellType.STRING);
                            aatUid = aatUidCell.getStringCellValue();
                             
                            /*
                            if (geonameIdCell.getCellTypeEnum() == CellType.STRING) {
                                geonameId = geonameIdCell.getStringCellValue();
                            } else if(geonameIdCell.getCellTypeEnum() == CellType.NUMERIC) {
                                geonameId = String.valueOf(geonameIdCell.getNumericCellValue());
                            } else {
                                geonameId = "Unknown Cell Type";
                            }*/
                        }

                        // Create mapping value
                        SubjectTermEntity subjectTermEntity = new SubjectTermEntity();
                        // spatialTerm.setId((long) -1);
                        subjectTermEntity.setMappingId(mappingId);
                        subjectTermEntity.setNativeTerm(nativeTerm);
                        subjectTermEntity.setLanguage(language);
                        subjectTermEntity.setAatConceptLabel(aatConceptLabel);
                        subjectTermEntity.setAatUid(aatUid);

                        terms.add(subjectTermEntity);
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
        
        return terms;
        
    }
    
    /**
     * 
     * @param filename
     * @param mappingId
     * @param skipLineCount
     * @param limitCount
     * @return 
     */
    public List<SpatialTermEntity> loadSpatialTermsFromExcel(String filename, long mappingId,
                                                             int skipLineCount, int limitCount) {

        List<SpatialTermEntity> terms = new LinkedList<>();
        
        try {
            
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
                        String language;
                        String geonameName;
                        String geonameId;

                        // Get Native Term - It is mandatory
                        Cell nativeTermCell = currentRow.getCell(0);
                        if(nativeTermCell == null) {
                            log.info("NULL native term");
                            continue;
                        } else {
                            nativeTermCell.setCellType(CellType.STRING);
                            nativeTerm = nativeTermCell.getStringCellValue();
                        }
                        
                        // Get language - it is mandatory
                        Cell languageCell = currentRow.getCell(1);
                        if(languageCell == null) {
                            log.info("NULL language");
                            continue;
                        } else {
                            // This guarantess that you will read the value as string
                            languageCell.setCellType(CellType.STRING);
                            language = languageCell.getStringCellValue();
                        }
                        

                        // Get AAT Concept label
                        Cell geonameNameCell = currentRow.getCell(2);
                        if(geonameNameCell == null) {
                            geonameName = "";
                        } else {
                            // This guarantess that you will read the value as string
                            geonameNameCell.setCellType(CellType.STRING);
                            geonameName = geonameNameCell.getStringCellValue();
                        }
                        
                        // Get AAT uid
                        Cell geonameIdCell = currentRow.getCell(3);
                        if(geonameIdCell == null) {
                            geonameId = "";
                        } else {
                            // This guarantess that you will read the value as string
                            geonameIdCell.setCellType(CellType.STRING);
                            geonameId = geonameIdCell.getStringCellValue();
                        }

                        // Create mapping value
                        SpatialTermEntity spatialTermEntity = new SpatialTermEntity();
                        // spatialTerm.setId((long) -1);
                        spatialTermEntity.setMappingId(mappingId);
                        spatialTermEntity.setNativeTerm(nativeTerm);
                        spatialTermEntity.setLanguage(language);
                        spatialTermEntity.setGeonameName(geonameName);
                        spatialTermEntity.setGeonameId(geonameId);

                        terms.add(spatialTermEntity);
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
        
        return terms;
        
    }
    
    public List<TemporalTermEntity> loadTemporalTermsFromExcel(String filename, long mappingId,
                                                               int skipLineCount, int limitCount) {

        List<TemporalTermEntity> terms = new LinkedList<>();
        
        try {
            
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
                        String language;
                        String earchTemporalLabel;
                        String aatUid;

                        // Get Native Term - It is mandatory
                        Cell nativeTermCell = currentRow.getCell(0);
                        if(nativeTermCell == null) {
                            log.info("NULL native term");
                            continue;
                        } else {
                            nativeTermCell.setCellType(CellType.STRING);
                            nativeTerm = nativeTermCell.getStringCellValue();
                        }
                        
                        // Get language - it is mandatory
                        Cell languageCell = currentRow.getCell(1);
                        if(languageCell == null) {
                            log.info("NULL language");
                            continue;
                        } else {
                            // This guarantess that you will read the value as string
                            languageCell.setCellType(CellType.STRING);
                            language = languageCell.getStringCellValue();
                        }
                        

                        // Get AAT Concept label
                        Cell earchTemporalLabelCell = currentRow.getCell(2);
                        if(earchTemporalLabelCell == null) {
                            earchTemporalLabel = "";
                        } else {
                            // This guarantess that you will read the value as string
                            earchTemporalLabelCell.setCellType(CellType.STRING);
                            earchTemporalLabel = earchTemporalLabelCell.getStringCellValue();
                        }
                        
                        // Get AAT uid
                        Cell aatUidCell = currentRow.getCell(3);
                        if(aatUidCell == null) {
                            aatUid = "";
                        } else {
                            // This guarantees that you will read the value as string
                            aatUidCell.setCellType(CellType.STRING);
                            aatUid = aatUidCell.getStringCellValue();
                        }

                        // Create mapping value
                        TemporalTermEntity temporalTermEntity = new TemporalTermEntity();
                        // spatialTerm.setId((long) -1);
                        temporalTermEntity.setMappingId(mappingId);
                        temporalTermEntity.setNativeTerm(nativeTerm);
                        temporalTermEntity.setLanguage(language);
                        temporalTermEntity.setEarchTemporalLabel(earchTemporalLabel);
                        temporalTermEntity.setAatUid(aatUid);

                        terms.add(temporalTermEntity);
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
        
        return terms;
        
    }
    
    /**
     * 
     * @param filePath
     * @param terms
     * @throws IOException 
     */
    public static String exportSubjectTermsToExcel(Path filePath, List<SubjectTermEntity> terms) throws IOException {
        
        try {
           
            log.info("Export thematic terms to excel file...");
            
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

            // 4 columns 
            String[] columns = {"Native Term", "Language", "Aat concept label", "Aat uid"};
            
            // Create cells
            for(int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Create Other rows and cells with employees extractionData
            int rowNum = 1;
            for(SubjectTermEntity term: terms) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0)
                        .setCellValue(term.getNativeTerm());
                row.createCell(1)
                        .setCellValue(term.getLanguage());
                row.createCell(2)
                        .setCellValue(term.getAatConceptLabel());
                
                row.createCell(3)
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

            // Write the output to an excelFile
            FileOutputStream fileOut = new FileOutputStream(filePath.toString());
            workbook.write(fileOut);
            fileOut.close();

            // Closing the workbook
            workbook.close();
            
            // log.info("Export saved at {}", filePath);
            
        } catch (IOException ex) {
            throw ex;
        }
        
        return filePath.toString();
    }
    
    public static String exportSpatialTermsToExcel(Path filePath, List<SpatialTermEntity> terms) throws IOException {
        
        try {
           
            log.info("Export spatial terms to excel file...");
            
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

            // 4 columns 
            String[] columns = {"Native Term", "Language", "Geoname Name", "Geoname ID"};
            
            // Create cells
            for(int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Create Other rows and cells with employees extractionData
            int rowNum = 1;
            for(SpatialTermEntity term: terms) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0)
                        .setCellValue(term.getNativeTerm());
                row.createCell(1)
                        .setCellValue(term.getLanguage());
                row.createCell(2)
                        .setCellValue(term.getGeonameName());
                
                row.createCell(3)
                        .setCellValue(term.getGeonameId());
            }

            // Resize all columns to fit the content size
            for(int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write the output to an excelFile
            FileOutputStream fileOut = new FileOutputStream(filePath.toString());
            workbook.write(fileOut);
            fileOut.close();

            // Closing the workbook
            workbook.close();
            
            // log.info("Export saved at {}", filePath);
            
        } catch (IOException ex) {
            throw ex;
        }
        
        return filePath.toString();
    }
    
    public static String exportTemporalTermsToExcel(Path filePath, List<TemporalTermEntity> terms) throws IOException {
        
        try {
           
            log.info("Export temporal terms to excel file...");
            
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

            // 4 columns 
            String[] columns = {"Native Term", "Language", "Earch Temporal label", "Aat uid"};
            
            // Create cells
            for(int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Create Other rows and cells with employees extractionData
            int rowNum = 1;
            for(TemporalTermEntity term: terms) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0)
                        .setCellValue(term.getNativeTerm());
                row.createCell(1)
                        .setCellValue(term.getLanguage());
                row.createCell(2)
                        .setCellValue(term.getEarchTemporalLabel());
                row.createCell(3)
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

            // Write the output to an excelFile
            FileOutputStream fileOut = new FileOutputStream(filePath.toString());
            workbook.write(fileOut);
            fileOut.close();

            // Closing the workbook
            workbook.close();
            
            // log.info("Export saved at {}", filePath);
            
        } catch (IOException ex) {
            throw ex;
        }
        
        return filePath.toString();
    }
    
    /**
     * 
     * @param filePath
     * @param extractionResult
     * @return
     * @throws IOException 
     */
    public static String exportExtractedAllTerms(Path filePath, List<EdmFileTermExtractionResult> extractionResult) throws IOException {
        
        try {
           
            log.info("Export all terms to excel file started. #Files processed: {}", extractionResult.size());
            
            // Create a Workbook
            Workbook workbook = new XSSFWorkbook();

            // Create a Sheet
            Sheet sheet = workbook.createSheet("Extracted Terms");

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

            // 4 columns 
            String[] columns = {"filename", "element", "value", "xml_lang",  "rdf_resource"};
            
            // Create cells
            for(int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowNum = 1;
            for(EdmFileTermExtractionResult edmFileExtractionResult : extractionResult) {

                for(ElementExtractionData elementExtractionData : edmFileExtractionResult.getExtractionData()) {
                    Row row = sheet.createRow(rowNum++);

                    row.createCell(0)
                        .setCellValue(edmFileExtractionResult.getFilename());
                    row.createCell(1)
                        .setCellValue(elementExtractionData.getElementName());
                    row.createCell(2)
                        .setCellValue(elementExtractionData.getElementValue());
                    row.createCell(3)
                        .setCellValue(elementExtractionData.getXmlLangAttrValue());
                    row.createCell(4)
                        .setCellValue(elementExtractionData.getRdfResourceAttrValue());
                }
                
            }
            
            // Resize all columns to fit the content size
            for(int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write the output to an excelFile
            FileOutputStream fileOut = new FileOutputStream(filePath.toString());
            workbook.write(fileOut);
            fileOut.close();

            // Closing the workbook
            workbook.close();
            
            log.info("Export saved at {}. #Rows: {}", filePath, rowNum);
            
        } catch (IOException ex) {
            throw ex;
        }
        
        return filePath.toString();
    }
    
    public static String exportExtractedThematicTerms(Path filePath, Set<ElementExtractionData> extractionResult) throws IOException {
        
        List<SubjectTermEntity> terms = new LinkedList<>();
        for(ElementExtractionData extractionData : extractionResult) {
            SubjectTermEntity term = new SubjectTermEntity();
            term.setNativeTerm(extractionData.getElementValue());
            term.setLanguage(extractionData.getXmlLangAttrValue());
            term.setAatConceptLabel("");
            term.setAatUid("");
            terms.add(term);
        }
        
        // Sort terms
        terms.sort(Comparator.comparing(SubjectTermEntity::getNativeTerm));

        exportSubjectTermsToExcel(filePath, terms);
        
        return filePath.toString();
    }
    
    public static String exportExtractedSpatialTerms(Path filePath, Set<ElementExtractionData> extractionResult) throws IOException {
        
        List<SpatialTermEntity> terms = new LinkedList<>();
        for(ElementExtractionData extractionData : extractionResult) {
            SpatialTermEntity term = new SpatialTermEntity();
            term.setNativeTerm(extractionData.getElementValue());
            term.setLanguage(extractionData.getXmlLangAttrValue());
            term.setGeonameName("");
            term.setGeonameId("");
            terms.add(term);
        }
        
        // Sort terms
        terms.sort(Comparator.comparing(SpatialTermEntity::getNativeTerm));

        exportSpatialTermsToExcel(filePath, terms);
        
        return filePath.toString();
    }
    
    public static String exportExtractedTemporalTerms(Path filePath, Set<ElementExtractionData> extractionResult) throws IOException {
        
        List<TemporalTermEntity> terms = new LinkedList<>();
        for(ElementExtractionData extractionData : extractionResult) {
            TemporalTermEntity term = new TemporalTermEntity();
            term.setNativeTerm(extractionData.getElementValue());
            term.setLanguage(extractionData.getXmlLangAttrValue());
            term.setEarchTemporalLabel("");
            // term.setAatUid("");
            terms.add(term);
        }
        
        // Sort terms
        terms.sort(Comparator.comparing(TemporalTermEntity::getNativeTerm));

        exportTemporalTermsToExcel(filePath, terms);
        
        return filePath.toString();
    }
    
    
    
}

