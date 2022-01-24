package com.github.theprez.jsheetydriver.marshalling;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.github.theprez.jsheetydriver.SheetyJDBCConnection;

public class CsvUnmarshallingService implements H2UnmarshallingService {

    private static final int FILE_WRITE_BUFFER_SIZE = 1024 * 1024 * 2;

    private void populateSheet(final Connection _src, final File _dest, final String _schema) throws SQLException {
        final Statement statement = _src.createStatement();
        final ResultSet rs = statement.executeQuery("select * from " + _schema + ".CSV");
        final ResultSetMetaData metadata = rs.getMetaData();
        final int columnCount = metadata.getColumnCount();
        final String fileSep = System.getProperty("line.separator", "\n");
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(_dest), FILE_WRITE_BUFFER_SIZE), "UTF-8"))) {
            for (int i = 1; i <= columnCount; ++i) {
                bw.write('\"');
                bw.write(metadata.getColumnName(i));
                bw.write('\"');
                if (i < columnCount) {
                    bw.write(',');
                }
            }
            bw.write(fileSep);

            while (rs.next()) {
                for (int i = 1; i <= columnCount; ++i) {
                    final Object data = rs.getObject(i);
                    if (null == data) {
                        bw.write("null");
                    } else if (data instanceof Number) {
                        final BigDecimal bd = new BigDecimal(rs.getString(i));
                        bw.write("" + bd.toPlainString());
                    } else {
                        bw.write('\"');
                        bw.write("" + rs.getString(i).trim());
                        bw.write('\"');
                    }
                    if (i < columnCount) {
                        bw.write(',');
                    }
                }
                bw.write(fileSep);
            }
        } catch (final IOException e) {
            throw new SQLException(e);
        }

    }

    @Override
    public void unmarshall(final File _dest, final SheetyJDBCConnection _src) throws FileNotFoundException, IOException, SQLException {
        if (_src.isClosed()) {
            return;
        }
        final String schema = _dest.getName().replaceAll("\\..*", "").toUpperCase();
        populateSheet(_src, _dest, schema);
    }

}
