package gr.dcu.europeana.arch.service;

import gr.dcu.europeana.arch.model.SubjectTerm;
import gr.dcu.europeana.arch.exception.MyFileNotFoundException;
import gr.dcu.europeana.arch.model.SpatialTerm;
import gr.dcu.europeana.arch.model.TemporalTerm;
import gr.dcu.europeana.arch.repository.AatSubjectRepository;
import gr.dcu.europeana.arch.service.edm.EdmFileTermExtractionResult;
import gr.dcu.europeana.arch.service.edm.ElementExtractionData;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Vangelis Nomikos
 */
@Slf4j
@Service
public class ExcelService {
    
    @Autowired
    FileStorageService fileStorageService;
    
    @Autowired
    private AatSubjectRepository aatSubjectRepository;
    
    private static final String DEFAULT_EXPORT_FILENAME = "mappings.xlsx";
    
    /**
     * 
     * @param filename
     * @param mappingId
     * @param skipLineCount
     * @param limitCount
     * @return 
     */
    public List<SubjectTerm> loadSubjectTermsFromExcel(String filename, long mappingId, 
            int skipLineCount, int limitCount) {

        List<SubjectTerm> terms = new LinkedList<>();
        
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
                        SubjectTerm subjectTerm = new SubjectTerm();
                        // spatialTerm.setId((long) -1);
                        subjectTerm.setMappingId(mappingId);
                        subjectTerm.setNativeTerm(nativeTerm);
                        subjectTerm.setLanguage(language);
                        subjectTerm.setAatConceptLabel(aatConceptLabel);
                        subjectTerm.setAatUid(aatUid);

                        terms.add(subjectTerm);
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
    public List<SpatialTerm> loadSpatialTermsFromExcel(String filename, long mappingId, 
            int skipLineCount, int limitCount) {

        List<SpatialTerm> terms = new LinkedList<>();
        
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
                        SpatialTerm spatialTerm = new SpatialTerm();
                        // spatialTerm.setId((long) -1);
                        spatialTerm.setMappingId(mappingId);
                        spatialTerm.setNativeTerm(nativeTerm);
                        spatialTerm.setLanguage(language);
                        spatialTerm.setGeonameName(geonameName);
                        spatialTerm.setGeonameId(geonameId);

                        terms.add(spatialTerm);
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
    
    public List<TemporalTerm> loadTemporalTermsFromExcel(String filename, long mappingId, 
            int skipLineCount, int limitCount) {

        List<TemporalTerm> terms = new LinkedList<>();
        
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
                        }

                        // Create mapping value
                        TemporalTerm temporalTerm = new TemporalTerm();
                        // spatialTerm.setId((long) -1);
                        temporalTerm.setMappingId(mappingId);
                        temporalTerm.setNativeTerm(nativeTerm);
                        temporalTerm.setLanguage(language);
                        temporalTerm.setAatConceptLabel(aatConceptLabel);
                        temporalTerm.setAatUid(aatUid);

                        terms.add(temporalTerm);
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
    public static String exportSubjectTermsToExcel(Path filePath, List<SubjectTerm> terms) throws IOException {
        
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
            for(SubjectTerm term: terms) {
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
    
    public static String exportSpatialTermsToExcel(Path filePath, List<SpatialTerm> terms) throws IOException {
        
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
            for(SpatialTerm term: terms) {
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
    
    public static String exportTemporalTermsToExcel(Path filePath, List<TemporalTerm> terms) throws IOException {
        
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
            String[] columns = {"Native Term", "Language", "Aat concept label", "Aat uid"};
            
            // Create cells
            for(int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Create Other rows and cells with employees extractionData
            int rowNum = 1;
            for(TemporalTerm term: terms) {
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
        
        List<SubjectTerm> terms = new LinkedList<>();
        for(ElementExtractionData extractionData : extractionResult) {
            SubjectTerm term = new SubjectTerm();
            term.setNativeTerm(extractionData.getElementValue());
            term.setLanguage(extractionData.getXmlLangAttrValue());
            term.setAatConceptLabel("");
            term.setAatUid("");
            terms.add(term);
        }
        
        // Sort terms
        terms.sort(Comparator.comparing(SubjectTerm::getNativeTerm));

        exportSubjectTermsToExcel(filePath, terms);
        
        return filePath.toString();
    }
    
    public static String exportExtractedSpatialTerms(Path filePath, Set<ElementExtractionData> extractionResult) throws IOException {
        
        List<SpatialTerm> terms = new LinkedList<>();
        for(ElementExtractionData extractionData : extractionResult) {
            SpatialTerm term = new SpatialTerm();
            term.setNativeTerm(extractionData.getElementValue());
            term.setLanguage(extractionData.getXmlLangAttrValue());
            term.setGeonameName("");
            term.setGeonameId("");
            terms.add(term);
        }
        
        // Sort terms
        terms.sort(Comparator.comparing(SpatialTerm::getNativeTerm));

        exportSpatialTermsToExcel(filePath, terms);
        
        return filePath.toString();
    }
    
    public static String exportExtractedTemporalTerms(Path filePath, Set<ElementExtractionData> extractionResult) throws IOException {
        
        List<TemporalTerm> terms = new LinkedList<>();
        for(ElementExtractionData extractionData : extractionResult) {
            TemporalTerm term = new TemporalTerm();
            term.setNativeTerm(extractionData.getElementValue());
            term.setLanguage(extractionData.getXmlLangAttrValue());
            term.setAatConceptLabel("");
            term.setAatUid("");
            terms.add(term);
        }
        
        // Sort terms
        terms.sort(Comparator.comparing(TemporalTerm::getNativeTerm));

        exportTemporalTermsToExcel(filePath, terms);
        
        return filePath.toString();
    }
    
    
    
}

