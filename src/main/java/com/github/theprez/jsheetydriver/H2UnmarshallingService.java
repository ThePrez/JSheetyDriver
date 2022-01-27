package com.github.theprez.jsheetydriver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

interface H2UnmarshallingService {
    void unmarshall(File _dest, SheetyJDBCConnection _src) throws FileNotFoundException, IOException, SQLException;
}
