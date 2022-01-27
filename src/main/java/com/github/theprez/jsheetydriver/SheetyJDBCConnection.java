package com.github.theprez.jsheetydriver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.h2.jdbc.JdbcConnection;

import com.github.theprez.jsheetydriver.marshalling.CsvMarshallingService;
import com.github.theprez.jsheetydriver.marshalling.CsvUnmarshallingService;
import com.github.theprez.jsheetydriver.marshalling.ExcelMarshallingService;
import com.github.theprez.jsheetydriver.marshalling.ExcelUnmarshallingService;

class SheetyJDBCConnection extends JdbcConnection {

    private final CsvMarshallingService m_csvMarshall = new CsvMarshallingService();
    private final CsvUnmarshallingService m_csvUnmarshall = new CsvUnmarshallingService();
    private File m_currentDir = new File(System.getProperty("user.dir", "."));
    private final JSheetyDriver m_driver;
    private final List<File> m_files = new LinkedList<File>();
    private boolean m_isReadOnly;
    private final ExcelMarshallingService m_marshall = new ExcelMarshallingService();
    private final ExcelUnmarshallingService m_unmarshall = new ExcelUnmarshallingService();
    private final JdbcConnection m_wrapped;
    private final List<File> m_writableFiles = new LinkedList<File>();

    public SheetyJDBCConnection(final JdbcConnection _wrp, final JSheetyDriver _driver) throws SQLException {
        super(_wrp);
        m_wrapped = _wrp;
        m_driver = _driver;
        // this.watcher = CloseWatcher.register(this, this.getSession(), true);
    }

    public void chdir(final File _newDir) {
        m_currentDir = _newDir;
    }

    @Override
    public void close() throws SQLException {
        if (super.getAutoCommit()) {
            commit();
        }
        m_wrapped.close();

    }

    @Override
    public void commit() throws SQLException {
        super.commit();
        writeToFile();
    }

    @Override
    public Statement createStatement() throws SQLException {
        return new SheetyStatement(super.createStatement());
    }

    public JSheetyDriver getDriver() {
        return m_driver;
    }

    private boolean isCsvFile(final File _f) {
        return _f.getName().toLowerCase().endsWith(".csv");
    }

    private boolean isExcelFile(final File _f) {
        return _f.getName().toLowerCase().endsWith(".xlsx");
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return m_isReadOnly || super.isReadOnly();
    }

    public void openFile(final Statement _stmt, final String _file, final boolean _writable) throws FileNotFoundException, SQLException, IOException {

        final File inFile = new File(_file);
        final File file;
        if (inFile.isAbsolute()) {
            file = inFile;
        } else {
            file = new File(m_currentDir, _file);
        }
        if (isExcelFile(file)) {
            m_marshall.marshall(_stmt, file);
        } else if (isCsvFile(file)) {
            m_csvMarshall.marshall(_stmt, file);
        } else {
            throw new SQLException("Unsupported File Type");
        }
        m_files.add(file);
        if (_writable) {
            m_writableFiles.add(file);
        }
    }

    @Override
    public void rollback() throws SQLException {
        super.rollback();
        writeToFile();
    }

    @Override
    public void setReadOnly(final boolean _readOnly) throws SQLException {
        super.setReadOnly(_readOnly);
        m_isReadOnly = _readOnly;
    }

    private void writeToFile() throws SQLException {
        try {
            if (!m_isReadOnly) {
                for (final File f : m_writableFiles) {
                    if (isExcelFile(f)) {
                        m_unmarshall.unmarshall(f, this);
                    } else if (isCsvFile(f)) {
                        m_csvUnmarshall.unmarshall(f, this);
                    }
                }
            }
        } catch (final FileNotFoundException e) {
            throw new SQLException(e);
        } catch (final IOException e) {
            throw new SQLException(e);
        }
    }
}
