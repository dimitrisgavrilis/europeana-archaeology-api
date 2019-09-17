package gr.dcu.europeana.arch.service;

import gr.dcu.europeana.arch.model.MappingTerm;
import gr.dcu.europeana.arch.exception.MyFileNotFoundException;
import gr.dcu.europeana.arch.repository.AatSubjectRepository;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
    public List<MappingTerm> loadMappingTermsFromExcel(String filename, long mappingId, 
            int skipLineCount, int limitCount) {

        List<MappingTerm> mappings = new LinkedList<>();
        
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
     * @param filePath
     * @param terms
     * @throws IOException 
     */
    public String exportMappingTermsToExcel(Path filePath, List<MappingTerm> terms) throws IOException {
        
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
    
}

