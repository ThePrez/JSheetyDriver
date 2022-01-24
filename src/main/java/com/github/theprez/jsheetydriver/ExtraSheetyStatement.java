package com.github.theprez.jsheetydriver;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.theprez.jcmdutils.StringUtils;

public class ExtraSheetyStatement {

    private final String m_sql;
    private final Statement m_stmt;
    private final Pattern s_baseCommandPattern = Pattern.compile("^\\s*sheety\\s+(LOAD|LOAD_READONLY|CREATE|LINKDB2|LINKDB2AS|DB2QUERY|CD)\\s+(.*)\\s*$", Pattern.CASE_INSENSITIVE);

    public ExtraSheetyStatement(final Statement _stmt, final String _sql) {
        m_stmt = _stmt;
        m_sql = _sql;
    }

    private boolean chdir(final String _dir) throws SQLException {
        final SheetyJDBCConnection conn = (SheetyJDBCConnection) m_stmt.getConnection();
        final File newDir = new File(_dir);
        if (!newDir.isDirectory()) {
            throw new SQLException("Not a directory");
        }
        conn.chdir(newDir);
        return false;
    }

    private boolean db2query(final String _deets) throws SQLException {

        final Pattern format1 = Pattern.compile("^\\s*(([a-z0-9]+|\\\"[^\\\"]+\\\")\\s*\\.){0,1}\\s*([a-z0-9]+|\\\"[^\\\"]+\\\"){1}\\s+(.*)$", Pattern.CASE_INSENSITIVE);
        final Matcher m = format1.matcher(_deets);
        if (!m.matches()) {
            throw new SQLException("Unable to parse command: " + _deets);
        }
        final JSheetyDriver conn = ((SheetyJDBCConnection) m_stmt.getConnection()).getDriver();
        final String uid = conn.getDb2Uid();
        final char[] pw = conn.getDb2pw();
        final String sys = conn.getDb2System();
        final String schema = m.group(2);
        final String table = m.group(3);
        final String query = m.group(4);
        final Statement s = m_stmt.getConnection().createStatement();
        String sql = String.format("drop TABLE IF EXISTS %s.%s", schema, table);
        s.execute(sql);
        sql = String.format("CREATE LINKED TABLE %s.%s('com.ibm.as400.access.AS400JDBCDriver','jdbc:as400://%s','%s','%s','(%s)') READONLY", schema, table, sys, uid, new String(pw), query);
        s.execute(sql);
        s.close();
        sql = String.format("Select * from %s.%s", schema, table);
        return m_stmt.execute(sql);
    }

    public boolean execute() throws SQLException {
        final Matcher m = s_baseCommandPattern.matcher(m_sql.replace("\n", " ").replace("\r", ""));
        if (!m.matches()) {
            throw new SQLException("Unrecognized request");
        }
        final String command = m.group(1);
        final String deets = m.group(2);
        if ("load".equalsIgnoreCase(command)) {
            return loadFile(deets, true);
        }
        if ("load_readonly".equalsIgnoreCase(command)) {
            return loadFile(deets, false);
        }
        if ("cd".equalsIgnoreCase(command)) {
            return chdir(deets);
        }
        if ("linkdb2".equalsIgnoreCase(command)) {
            return linkdb2(deets);
        }
        if ("linkdb2as".equalsIgnoreCase(command)) {
            return linkdb2as(deets);
        }
        if ("db2query".equalsIgnoreCase(command)) {
            return db2query(deets);
        }
        throw new SQLException("Unrecognized request");
    }

    private boolean linkdb2(final String _deets) throws SQLException {
        // link.execute("CREATE global LINKED TABLE DB2.LIAM('com.ibm.as400.access.AS400JDBCDriver','jdbc:as400://oss73dev/jesseg','linux','linux1','jesseg','qcustcdt') EMIT UPDATES");

        final Pattern p = Pattern.compile("^\\s*([a-z0-9]+|\\\"[^\\\"]+\\\"){1}\\s*\\.\\s*([a-z0-9]+|\\\"[^\\\"]+\\\"){1}$", Pattern.CASE_INSENSITIVE);

        final Matcher m = p.matcher(_deets);

        if (!m.matches()) {
            throw new SQLException("Unable to parse command: " + _deets);
        }
        m_stmt.execute("CREATE SCHEMA IF NOT EXISTS DB2");
        final String sourceSchema = m.group(1);
        final String sourceTable = m.group(2);
        final String targetSchema = "DB2";
        final String targetTable = sourceTable;
        final JSheetyDriver conn = ((SheetyJDBCConnection) m_stmt.getConnection()).getDriver();
        final String uid = conn.getDb2Uid();
        final char[] pw = conn.getDb2pw();
        final String sys = conn.getDb2System();

        final String sql = String.format("CREATE LINKED TABLE %s.%s('com.ibm.as400.access.AS400JDBCDriver','jdbc:as400://%s','%s','%s','%s','%s') EMIT UPDATES", targetSchema, targetTable, sys, uid, new String(pw), sourceSchema, sourceTable);
        final Statement s = m_stmt.getConnection().createStatement();
        s.execute(sql);
        s.close();
        // TODO Auto-generated method stub
        return false;
    }

    private boolean linkdb2as(final String _deets) throws SQLException {
        // link.execute("CREATE global LINKED TABLE DB2.LIAM('com.ibm.as400.access.AS400JDBCDriver','jdbc:as400://oss73dev/jesseg','linux','linux1','jesseg','qcustcdt') EMIT UPDATES");

        final Pattern format1 = Pattern.compile("^\\s*(([a-z0-9]+|\\\"[^\\\"]+\\\")\\s*\\.){0,1}\\s*([a-z0-9]+|\\\"[^\\\"]+\\\"){1}\\s+([a-z0-9]+|\\\"[^\\\"]+\\\")\\){0,1}\\s*\\.\\s*([a-z0-9]+|\\\"[^\\\"]+\\\"){1}$", Pattern.CASE_INSENSITIVE);

        final Matcher m = format1.matcher(_deets);

        if (!m.matches()) {
            throw new SQLException("Unable to parse command: " + _deets);
        }

        final String targetSchema = m.group(2);
        final String targetTable = m.group(3);
        final String sourceSchema = m.group(4);
        final String sourceTable = m.group(5);
        final JSheetyDriver conn = ((SheetyJDBCConnection) m_stmt.getConnection()).getDriver();
        final String uid = conn.getDb2Uid();
        final char[] pw = conn.getDb2pw();
        final String sys = conn.getDb2System();
        final String targetSchemaStringWithDot = StringUtils.isEmpty(targetSchema) ? "" : targetSchema + ".";

        final String sql = String.format("CREATE LINKED TABLE %s%s('com.ibm.as400.access.AS400JDBCDriver','jdbc:as400://%s','%s','%s','%s','%s') EMIT UPDATES", targetSchemaStringWithDot, targetTable, sys, uid, new String(pw), sourceSchema, sourceTable);
        final Statement s = m_stmt.getConnection().createStatement();
        s.execute(sql);
        s.close();

        return false;
    }

    private boolean loadFile(final String _file, final boolean _writable) throws SQLException {
        final SheetyJDBCConnection conn = (SheetyJDBCConnection) m_stmt.getConnection();
        try {
            conn.openFile(m_stmt, _file, _writable);
            return false;
        } catch (final IOException e) {
            throw new SQLException(e);
        }
    }

}
