package com.github.theprez.jsheetydriver;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class JSheetyDataSource implements DataSource {

    private PrintWriter m_logWriter=null;
    private JSheetyDriver m_driver = new JSheetyDriver();
    private final String m_url;
    private Properties m_props;

    public JSheetyDataSource(String _url, Properties _p) {
        this.m_url = _url;
        this.m_props = _p;
    }

    public JSheetyDataSource() {
        this("sheety", new Properties());
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return m_logWriter;
    }

    @Override
    public void setLogWriter(PrintWriter _out) throws SQLException {
        m_logWriter = _out;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getGlobal();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(new UnsupportedOperationException());
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return m_driver.connect(m_url, m_props);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

}
