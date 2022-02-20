package com.github.theprez.jsheetydriver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

class CsvMarshallingService implements H2MarshallingService {

    private static final String s_stringDbType = getDbTypeName(String.class, "");

    private static String getDbTypeName(final Class<? extends Object> _cellType, final Object _cellData) {
        if (Boolean.class.isAssignableFrom(_cellType)) {
            return "BOOLEAN";
        }
        if (Number.class.isAssignableFrom(_cellType)) {
            return "DECFLOAT(100)";
        }
        // if (CharSequence.class.isAssignableFrom(_cellType)) {
        // return "CHARACTER VARYING";
        // }
        try {
            Double.parseDouble("" + _cellData);
            return "DECFLOAT(100)";
        } catch (final Exception e) {
        }
        return "CHARACTER VARYING";
    }

    public CsvMarshallingService() throws SQLException {
    }

    private void addDbColumn(final Statement _stmt, final String _schema, final String _table, final String _name) throws SQLException {
        final String sql = String.format("ALTER TABLE \"%s\".%s ADD COLUMN \"%s\" CHARACTER VARYING", _schema, _table, _name);
        System.out.println(sql);
        dbExecute(_stmt, sql);
    }

    private void createTable(final Statement _stmt, final String _schema, final String _table) throws SQLException {
        final String sql = String.format("CREATE TABLE \"%s\".%s", _schema, _table);
        dbExecute(_stmt, sql);
    }

    private void dbExecute(final Statement _stmt, final String _sql) throws SQLException {
        _stmt.execute(_sql);
    }

    private void insertRow(final Statement _stmt, final String _schema, final String _table, final List<String> _columnNames, final ResultSet rs, final TreeMap<String, String> _columnTypeMap) throws SQLException {
        String parameterMarkers = "";
        final int columnCount = rs.getMetaData().getColumnCount();
        for (int i = 0; i < columnCount && i < _columnNames.size(); ++i) {
            parameterMarkers += "?,";
        }
        parameterMarkers = parameterMarkers.replaceFirst(",$", "");
        final String sql = String.format("INSERT INTO %s.%s VALUES(%s)", _schema, _table, parameterMarkers);
        final PreparedStatement s = _stmt.getConnection().prepareStatement(sql);
        for (int i = 1; i <= _columnNames.size(); ++i) {

            final Object c = rs.getObject(i);
            if (null == c) {
                s.setObject(i, null);
                continue;
            }
            final Class<? extends Object> cellType = c.getClass();
            updateH2ColumnType(_stmt, _schema, _table, _columnNames.get(-1 + i), cellType, c, _columnTypeMap);

            if ("null".equalsIgnoreCase("" + c)) {
                s.setObject(i, null);
            } else {
                s.setString(i, "" + c);
            }
        }
        s.execute();

    }

    @Override
    public void marshall(final Statement _stmt, final File _file) throws SQLException, FileNotFoundException, IOException {

        final String schema = _file.getName().replaceAll("\\..*", "").toUpperCase();// TODO: handle special characters and such
        dbExecute(_stmt, "CREATE SCHEMA IF NOT EXISTS \"" + schema + "\"");

        final PreparedStatement ps = _stmt.getConnection().prepareStatement("select * from CSVREAD('" + _file.getAbsolutePath() + "')");
        // ps.setString(1, _file.getAbsolutePath());
        ps.execute();
        final ResultSet rs = ps.getResultSet();
        final int columnCount = rs.getMetaData().getColumnCount();
        final String sheet = "CSV";
        final List<String> columnNames = new LinkedList<String>();
        createTable(_stmt, schema, sheet);
        for (int i = 1; i <= columnCount; ++i) {
            columnNames.add(rs.getMetaData().getColumnName(i));
            addDbColumn(_stmt, schema, sheet, rs.getMetaData().getColumnName(i));
        }
        final TreeMap<String, String> columnTypeMap = new TreeMap<String, String>();
        while (rs.next()) {
            insertRow(_stmt, schema, sheet, columnNames, rs, columnTypeMap);
        }
    }

    private void updateH2ColumnType(final Statement _stmt, final String _schema, final String _table, final String _column, final Class<? extends Object> _cellType, final Object _cellData, final TreeMap<String, String> _columnTypeMap) throws SQLException {
        final String dbType = getDbTypeName(_cellType, _cellData);
        final String currentType = _columnTypeMap.get(_column);
        if (dbType.equals(currentType) || "null".equalsIgnoreCase("" + _cellData) || null == _cellData) {
            return;
        }
        if (_column.equals("SOCKET_SEND_TIMEOUT")) {
            System.out.println("hmm");
        }
        final String newType = null == currentType ? dbType : s_stringDbType;
        final String sql = String.format("ALTER TaBLE %s.%s ALTER COLUMN \"%s\" %s", _schema, _table, _column, newType);
        System.out.println(sql);
        dbExecute(_stmt, sql);
        _columnTypeMap.put(_column, newType);
    }

}
