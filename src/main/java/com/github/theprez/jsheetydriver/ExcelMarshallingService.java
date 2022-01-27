package com.github.theprez.jsheetydriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.monitorjbl.xlsx.StreamingReader;

class ExcelMarshallingService implements H2MarshallingService {

    private static final int FILE_READ_BUFFER_SIZE = 1024 * 1024 * 64;

    private static final String s_stringDbType = getDbTypeName(CellType.STRING);

    private static String getDbTypeName(final CellType _type) {
        switch (_type) {
            case BOOLEAN:
                return "BOOLEAN";
            case NUMERIC:
                return "DECFLOAT(100)";
            default:
                return "CHARACTER VARYING";
        }
    }

    private final Map<String, File> m_schemaToFileMap = new LinkedHashMap<String, File>();

    public ExcelMarshallingService() throws SQLException {
    }

    private void addDbColumn(final Statement _stmt, final String _schema, final String _table, final String _name) throws SQLException {
        final String sql = String.format("ALTER TABLE %s.\"%s\" ADD COLUMN \"%s\" CHARACTER VARYING", _schema, _table, _name);
        System.out.println(sql);
        dbExecute(_stmt, sql);
    }

    private void createTable(final Statement _stmt, final String _schema, final String _table) throws SQLException {
        final String sql = String.format("CREATE TABLE %s.\"%s\"", _schema, _table);
        dbExecute(_stmt, sql);
    }

    private void dbExecute(final Statement _stmt, final String _sql) throws SQLException {
        _stmt.execute(_sql);
    }

    private List<Cell> getCellsForRow(final Row r) {
        final LinkedList<Cell> ret = new LinkedList<Cell>();
        for (final Cell c : r) {
            ret.add(c);
        }
        return ret;
    }

    private void insertRow(final Statement _stmt, final String _schema, final String _table, final List<String> _columnNames, final List<Cell> _cells, final TreeMap<String, String> _columnTypeMap) throws SQLException {
        String parameterMarkers = "";
        for (int i = 0; i < _cells.size() && i < _columnNames.size(); ++i) {
            parameterMarkers += "?,";
        }
        parameterMarkers = parameterMarkers.replaceFirst(",$", "");
        final String sql = String.format("INSERT INTO %s.\"%s\" VALUES(%s)", _schema, _table, parameterMarkers);
        final PreparedStatement s = _stmt.getConnection().prepareStatement(sql);
        for (int i = 1; i <= _columnNames.size(); ++i) {
            if (i > _cells.size()) {
                continue;
            }
            final Cell c = _cells.get(-1 + i);
            final CellType cellType = c.getCellType();
            updateH2ColumnType(_stmt, _schema, _table, _columnNames.get(-1 + i), cellType, _columnTypeMap);
            switch (c.getCellType()) {
                case NUMERIC:
                    s.setDouble(i, c.getNumericCellValue());
                    break;
                case BOOLEAN:
                    s.setBoolean(i, c.getBooleanCellValue());
                    break;
                default:
                    s.setString(i, c.getStringCellValue());
            }
        }
        s.execute();

    }

    @Override
    public void marshall(final Statement _stmt, final File _file) throws SQLException, FileNotFoundException, IOException {

        final String schema = _file.getName().replaceAll("\\..*", "").toUpperCase();// TODO: handle special characters and such
        dbExecute(_stmt, "CREATE SCHEMA " + schema);
        if (m_schemaToFileMap.isEmpty()) {
            _stmt.getConnection().setSchema(schema);
        }

        final Workbook workbook;
        try (InputStream is = new FileInputStream(_file)) {
            // workbook = new XSSFWorkbook();
            workbook = StreamingReader.builder().rowCacheSize(100) // number of rows to keep in memory (defaults to 10)
                    .bufferSize(FILE_READ_BUFFER_SIZE) // buffer size to use when reading InputStream to file (defaults to 1024)
                    .open(is); // InputStream or File for XLSX file (required)
        }
        for (final Sheet sheet : workbook) {
            final TreeMap<String, String> columnTypeMap = new TreeMap<String, String>();
            final List<String> columnNames = new LinkedList<String>();
            createTable(_stmt, schema, sheet.getSheetName());
            int row = -1;
            for (final Row r : sheet) {
                row++;
                final List<Cell> cells = getCellsForRow(r);
                if (0 == row) {
                    // reading header row
                    for (final Cell headerCell : cells) {
                        final String columnName = headerCell.getStringCellValue();
                        addDbColumn(_stmt, schema, sheet.getSheetName(), columnName);
                        columnNames.add(columnName);
                    }
                } else {
                    insertRow(_stmt, schema, sheet.getSheetName(), columnNames, cells, columnTypeMap);
                }
            }
        }
    }

    private void updateH2ColumnType(final Statement _stmt, final String _schema, final String _table, final String _column, final CellType _type, final TreeMap<String, String> _columnTypeMap) throws SQLException {
        final String dbType = getDbTypeName(_type);
        final String currentType = _columnTypeMap.get(_column);
        if (dbType.equals(currentType)) {
            return;
        }
        final String newType = null == currentType ? dbType : s_stringDbType;
        final String sql = String.format("ALTER TaBLE %s.\"%s\" ALTER COLUMN \"%s\" %s", _schema, _table, _column, newType);
        System.out.println(sql);
        dbExecute(_stmt, sql);
        _columnTypeMap.put(_column, newType);
    }

}
