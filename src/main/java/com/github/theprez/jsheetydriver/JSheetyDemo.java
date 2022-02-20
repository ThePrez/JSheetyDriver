package com.github.theprez.jsheetydriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

/**
 * Hello world!
 *
 */
public class JSheetyDemo {

    public static void main(final String[] args) {
        if (args.length == 0) {
            System.err.println("Wrong number of args ... -h for help!");
            System.exit(1);
        }
        if (args[0].equals("-h")) {
            System.out.println("usage: JSheetyDemo targetSystem userId password schema table directory filename");
            System.exit(0);
        }
        if (args.length < 7) {
            System.err.println("Wrong number of args ... -h for help!");
            System.exit(1);
        }
        String targetSystem = args[0];
        String userId = args[1];
        String password = args[2];
        String schema = args[3];
        String table = args[4];
        String directory = args[5];
        String filename = args[6];
        String qualifiedTable = schema + '.' + table;

        try {
            System.out.println("Hello World!");
            final JSheetyDriver driver = new JSheetyDriver();
            DriverManager.registerDriver(driver);
            final Properties p = new Properties();
            p.put("DB2SYSTEM", targetSystem);
            p.put("DB2UID", userId);
            p.put("DB2PW", password);
            final Connection conn = DriverManager.getConnection("sheety:", p);
            final Statement s = conn.createStatement();
            s.execute("sheety cd " + directory);
            s.execute("sheety load " + filename);
            s.execute("drop table " + filename);
            s.execute("sheety linkdb2 " + qualifiedTable);
            s.execute("create table " + filename + "  as (select * from db2.test0902)");
            s.execute("Insert into " + filename + " (select * from db2." + table + ")");

            conn.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
