package org.smbaiwsy.report_creator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds the processed messages text to the XLSX file
 * 
 * @author anamattuzzi-stojanovic
 *
 */
public class XLSAppender {
	private static final Logger LOG = LoggerFactory.getLogger(XLSAppender.class);
	private String filePath;
	private String rollPattern;
	private String sheetPattern;
	private long size;

	@Handler
	public void append(Exchange exchange) {
		CamelContext context = exchange.getContext();
		getReportFactorsFromExchange(context);
		try {
			Workbook workbook = getCurrentWorkbook();
			String sheetName = getFormattedDate(sheetPattern);
			Sheet sheet = workbook.getSheet(sheetName);
			if (sheet == null) {
				sheet = workbook.createSheet(sheetName);
			}
			int rows = sheet.getLastRowNum();
			sheet.shiftRows(sheet.getFirstRowNum(), rows, 1);
			Row newRow = sheet.createRow(1);

			Cell dateCell = newRow.createCell(0);
			CellStyle cellStyle = workbook.createCellStyle();
			CreationHelper createHelper = workbook.getCreationHelper();
			cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm:ss"));
			dateCell.setCellValue(Calendar.getInstance().getTime());
			dateCell.setCellStyle(cellStyle);

			Cell textCell = newRow.createCell(1);
			textCell.setCellValue(exchange.getIn().getBody(String.class));
			workbook.write(new FileOutputStream(Paths.get(filePath.concat(".tmp")).toString()));
			workbook.close();
			Files.deleteIfExists(Paths.get(filePath));
			renameFile(Paths.get(filePath.concat(".tmp")), Paths.get(filePath).getFileName().toString());
		} catch (Exception e) {
			e.printStackTrace();
			LOG.debug(e.getMessage());
		}

	}
	/**
	 * reads the report parameters from Exchnage
	 * @param context
	 */
	private void getReportFactorsFromExchange(CamelContext context) {
		try {
			filePath = context.resolvePropertyPlaceholders("{{report.path}}").toString();
			rollPattern = context.resolvePropertyPlaceholders("{{report.roll.pattern}}").toString();
			sheetPattern = context.resolvePropertyPlaceholders("{{report.sheet.pattern}}").toString();
			String sizeString = context.resolvePropertyPlaceholders("{{report.max.size}}").toString();
			size = Long.valueOf(sizeString);

		} catch (Exception e) {
			filePath = "www/report.xlsx";
			rollPattern = "yyyyMMddHHmmss";
			sheetPattern = "dd-MM-yyyy";
			size = 10000L;
		}
	}
	/**
	 * formats the date according to the given pattern
	 * @param pattern the date pattern
	 * @return the date formatted according to the given pattern
	 */
	private String getFormattedDate(String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.format(Calendar.getInstance().getTime());
	}
	/**
	 * renames the file
	 * @param path file name
	 * @param newFileName the file name under which the file will be saved
	 * @throws IOException
	 */
	private void renameFile(Path path, String newFileName) throws IOException {
		Path dir = path.getParent();
		Path fn = path.getFileSystem().getPath(newFileName);
		Path target = (dir == null) ? fn : dir.resolve(fn);
		Files.move(path, target);
	}
	/**
	 * Returns the current workbook
	 * @return the current work book
	 * @throws IOException
	 * @throws EncryptedDocumentException
	 * @throws InvalidFormatException
	 */
	public Workbook getCurrentWorkbook() throws IOException, EncryptedDocumentException, InvalidFormatException {
		File file;
		Path path = Paths.get(filePath);
		if (!Files.exists(path))
			return new XSSFWorkbook();
		if (Files.size(path) > size) {
			String newNameString = path.getFileName().toString().concat(".").concat(getFormattedDate(rollPattern));
			renameFile(path, newNameString);
			return new XSSFWorkbook();
		}
		file = path.toFile();
		LOG.debug(file.exists() + " " + file.getAbsolutePath());
		return WorkbookFactory.create(file);
	}

}
