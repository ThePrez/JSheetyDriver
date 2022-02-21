package com.github.theprez.jsheetydriver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Logger;

import org.h2.jdbc.JdbcConnection;

import com.github.theprez.jcmdutils.StringUtils;

public class JSheetyDriver implements Driver {
    static {
        try {
            DriverManager.registerDriver(new JSheetyDriver());
        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private char[] m_db2pw = "*CURRENT".toCharArray();
    private String m_db2system = "localhost";
    private String m_db2uid = "*CURRENT";
    private final org.h2.Driver m_driver = new org.h2.Driver();

    @Override
    public boolean acceptsURL(final String url) throws SQLException {
        final boolean isMyURL = url.matches("^(?i)(sheety)($|[;:].*$)");
        return isMyURL;
    }

    @Override
    public Connection connect(final String url, final Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }
        try {
            final Properties p = new Properties();
            for(Object prop :Collections.list(info.propertyNames())) {
                if(prop instanceof CharSequence) {
                    p.put(prop.toString().toUpperCase(), info.get(prop));
                }else {
                    p.put(prop, info.get(prop));
                }
            }
            final String propertiesFromConnectionString = url.contains(";") ? url.replaceFirst("^[^;]*;", "") : "";
            Properties connStringProps = new Properties();
            connStringProps.load(new StringReader(propertiesFromConnectionString.replace(';', '\n')));
            for(Object prop :Collections.list(connStringProps.propertyNames())) {
                if(prop instanceof CharSequence) {
                    p.put(prop.toString().toUpperCase(), connStringProps.get(prop));
                }else {
                    p.put(prop, connStringProps.get(prop));
                }
            }

            Object prop = p.remove("DB2PW");
            if (null != prop) {
                m_db2pw = prop.toString().toCharArray();
            }
            prop = p.remove("DB2UID");
            if (null != prop) {
                m_db2uid = prop.toString();
            }
            prop = p.remove("DB2SYSTEM");
            if (null != prop) {
                m_db2system = prop.toString();
            }

            p.put("MODE", "DB2");
            final boolean isTurboMode = Boolean.parseBoolean(p.getProperty("TURBO"));
            final String h2ConnectionString;

            if (isTurboMode) {
                h2ConnectionString = "jdbc:h2:mem:jesse";
            } else {
                final File tmpFile = File.createTempFile("sheety.", ".db");
                tmpFile.deleteOnExit();
                h2ConnectionString = "jdbc:h2:" + tmpFile.getCanonicalPath();
            }
            final JdbcConnection h2Conn = (JdbcConnection) m_driver.connect(h2ConnectionString, p);
            final SheetyJDBCConnection ret = new SheetyJDBCConnection(h2Conn, this);

            final String filePath = url.replaceFirst("^(?i)sheety[:]{0,1}", "").replaceAll(";.*", "");
            if (StringUtils.isEmpty(filePath)) {
                return ret;
            }
            final File f = new File(filePath);
            if (f.isDirectory()) {
                ret.chdir(f.getCanonicalFile());
                return ret;
            }
            ret.openFile(ret.createStatement(), filePath, true);
            return ret;
        } catch (final FileNotFoundException e) {
            throw new SQLException(e);
        } catch (final IOException e) {
            throw new SQLException(e);
        }
    }

    public char[] getDb2pw() {
        return m_db2pw;
    }

    public String getDb2System() {
        return m_db2system;
    }

    public String getDb2Uid() {
        return m_db2uid;
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getGlobal();
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String _url, final Properties _info) throws SQLException {
        // TODO Auto-generated method stub
        return new DriverPropertyInfo[0];
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

}
