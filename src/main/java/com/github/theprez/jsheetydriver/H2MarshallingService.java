package com.github.theprez.jsheetydriver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

interface H2MarshallingService {
    public void marshall(Statement _stmt, File _src) throws SQLException, FileNotFoundException, IOException;
}
