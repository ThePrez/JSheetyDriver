package com.github.theprez.jsheetydriver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

class ExcelUnmarshallingService implements H2UnmarshallingService {

    private static final int FILE_WRITE_BUFFER_SIZE = 1024 * 1024 * 64;

    private CellType getCellType(final int _sqlType) {
        switch (_sqlType) {
            case Types.BIGINT:
            case Types.DECIMAL:
            case Types.INTEGER:
            case Types.FLOAT:
            case Types.SMALLINT:
            case Types.NUMERIC:
            case Types.TINYINT:
                return CellType.NUMERIC;
            case Types.BOOLEAN:
            case Types.BIT:
                return CellType.BOOLEAN;
            default:
                return CellType.STRING;
        }
    }

    private ResultSet getTableList(final Statement tablesStatementtatement, final String _schema) throws SQLException {
        return tablesStatementtatement.executeQuery("Show tables from \"" + _schema + "\"");
    }

    private void populateSheet(final Connection _src, final SXSSFSheet _sheet, final String _table) throws SQLException {
        final Statement statement = _src.createStatement();
        final ResultSet rs = statement.executeQuery("select * from \"" + _src.getSchema() + "\".\"" + _table + "\"");
        final ResultSetMetaData metadata = rs.getMetaData();
        final int columnCount = metadata.getColumnCount();
        int rowNum = 0;
        final SXSSFRow headerRow = _sheet.createRow(rowNum++);
        for (int i = 1; i <= columnCount; ++i) {
            final SXSSFCell cell = headerRow.createCell(-1 + i, CellType.STRING);
            cell.setCellValue(metadata.getColumnLabel(i));
        }
        while (rs.next()) {
            final SXSSFRow dataRow = _sheet.createRow(rowNum++);
            for (int i = 1; i <= columnCount; ++i) {
                // TODO: handle different column data types
                final CellType cellType = getCellType(metadata.getColumnType(i));
                final SXSSFCell cell = dataRow.createCell(-1 + i, cellType);
                setCellValue(cell, cellType, rs, i);
            }
        }
    }

    private void setCellValue(final SXSSFCell _cell, final CellType _cellType, final ResultSet _rs, final int _column) throws SQLException {
        switch (_cellType) {
            case BOOLEAN:
                _cell.setCellType(CellType.BOOLEAN);
                _cell.setCellValue(_rs.getObject(_column, Boolean.class));
                break;
            case NUMERIC:
                _cell.setCellType(CellType.NUMERIC);
                _cell.setCellValue(_rs.getObject(_column, Double.class));
                break;
            default:
                _cell.setCellValue(_rs.getString(_column));
        }
    }

    @Override
    public void unmarshall(final File _dest, final SheetyJDBCConnection _src) throws FileNotFoundException, IOException, SQLException {
        if (_src.isClosed()) {
            return;
        }
        try (final SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            final Statement tablesStatementtatement = _src.createStatement();
            final ResultSet tablesRs = getTableList(tablesStatementtatement, _src.getSchema());
            boolean isTableFound = false;
            while (tablesRs.next()) {
                isTableFound = true;
                final String table = tablesRs.getString(1);
                System.out.println("Writing Excel sheet: " + table);
                final SXSSFSheet sheet = workbook.createSheet(table);
                populateSheet(_src, sheet, table);
            }
            if (!isTableFound) {
                return;
            }
            try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(_dest), FILE_WRITE_BUFFER_SIZE)) {
                workbook.write(out);
            }
        }
    }

}
