package sh.roadmap.tavi.weatherapi.tools;


import java.io.IOException;
import java.util.*;

import sh.roadmap.tavi.weatherapi.logging.WeatherLogger;

/**
 * A tool used to read the CSV (Comma Separated Value) files
 */
public class CsvHandler {
	
	private Optional<WeatherLogger> log = Optional.empty();
	private Map<String, Map<String, String>> table = new HashMap<>();
	private String[] columns;
	
	/**
	 * Reads the specified CSV file
	 * @param reader - a FilepathReader, configured to work with a specified .csv file
	 * @see FilepathReader
	 */
	public CsvHandler(FilepathReader reader) {
		String[] columnNames = null;
		for (String line : reader.read()) {
			if (columnNames == null) {
				columnNames = line.split(",");
				columns = Arrays.copyOfRange(columnNames, 1, columnNames.length);
				continue;
			}
			String[] columns = line.split(",");
	    	if (columns.length != columnNames.length) {
	    		log.ifPresent(log -> log.severe(null, new IOException("CSV file corrupted")));
	    	}
	    	
	    	Map<String, String> row = new HashMap<>();
	    	for (int i = 1; i < columns.length; i++) {
	    		row.put(columnNames[i], columns[i]);
	    	}
	    	table.put(columns[0], row);
		}
	}
	
	/**
	 * Get the cell, corresponding to the specified column and row
	 * @param row - The row of the cell
	 * @param column - The column of the cell
	 * @return - The cell (if present) or the row argument from the method (if no such row or column in the file) (String)
	 */
	public String get(String row, String column) {
		if (!table.containsKey(row) || !table.get(row).containsKey(column)) {
			return row;
		}
		return table.get(row).get(column).replaceAll("\\{c\\}", ",");
	}
	
	/**
	 * Return all the columns in the file
	 * @return - A String array with columns
	 */
	public String[] getColumns() {
		return columns;
	}
	
	/**
	 * Get the column of the cell inside the specified row
	 * @param row - Specified row of the cell
	 * @param cell - The cell in this row
	 * @return - The column of the cell inside the specified row, or null (if absent)
	 */
	public String getColumnOf(String row, String cell) {
		if (!table.containsKey(row)) {
			return null;
		}
		for (Map.Entry<String, String> column : table.get(row).entrySet()) {
			if (column.getValue().equals(cell)) {
				return column.getKey();
			}
		}
		return null;
	}
	
	public void setLogger(WeatherLogger logger) {
		log = Optional.of(logger);
	}
	
}
