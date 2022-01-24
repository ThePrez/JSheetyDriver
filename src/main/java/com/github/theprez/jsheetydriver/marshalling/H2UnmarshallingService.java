package com.github.theprez.jsheetydriver.marshalling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import com.github.theprez.jsheetydriver.SheetyJDBCConnection;

public interface H2UnmarshallingService {
    void unmarshall(File _dest, SheetyJDBCConnection _src) throws FileNotFoundException, IOException, SQLException;
}
