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
            // DriverManager.registerDriver(new AS400JDBCDriver());
            final Properties p = new Properties();
            p.put("DB2SYSTEM", targetSystem);
            p.put("DB2UID", userId);
            p.put("DB2PW", password);
            final Connection conn = DriverManager.getConnection("sheety:/users;ha=hey;this=that;", p);
            // Statement crtDb2 =
            // conn.createStatement();
            // crtDb2.execute("CREATE SCHEMA DB2");
            // crtDb2.close();
            // Statement link = conn.createStatement();
            // com.ibm.as400.access.AS400JDBCDriver
            // jdbc:as400://system-name/default-schema

            // link.execute("CREATE global temporary LINKED TABLE DB2.LIAM('com.ibm.as400.access.AS400JDBCDriver','jdbc:as400://oss73dev/jesseg','linux','linux1','jesseg','qcustcdt') EMIT UPDATES");
            // link.close();
            // Statement stmt = conn.createStatement();
            // stmt.execute("CREATE force LINKED TABLE DB2.LIAM('com.ibm.as400.access.AS400JDBCDriver','jdbc:as400://oss73dev/jesseg','linux','linux1','jesseg','qcustcdt2') EMIT UPDATES");
            // conn.commit();
            // stmt.execute("INSERT into DB2.LIAM (select * from \"Sheet1\")");
            // conn.commit();
            // stmt.execute("with myexcel as (select * from \"Sheet1\")" +
            // " " +
            // "insert into db2.liam values (select * from myexcel)");
            // stmt.execute("CREATE TABLE DB2.LIAM as (SELECT * from \"Sheet1\")");
            // conn.close();
            // Connection conn = DriverManager.getConnection("jesse:C:/Users/jgorzins/Desktop/filejdbc/out.xlsx");
            final Statement s = conn.createStatement();
            s.execute("sheety cd " + directory);
            // s.execute("create table liam2 like \"Sheet1\"");
            // s.execute("create table liam3 as select * from DB2.LIAM");
            s.execute("sheety linkdb2 " + qualifiedTable);
            s.execute("sheety load " + filename);
            s.execute("drop table " + filename);
            // s.execute("please load master.xlsx");
            // s.execute("drop table master.juanma");
            // s.execute("create table master.juanma as select * from DB2.QCUSTCDT where CDTDUE=0");
            s.execute("sheety linkdb2as " + filename + " " + qualifiedTable);

            // s.execute("please db2query data.csv SELECT * FROM QSYS2.NETSTAT_INFO");
            // s.execute("insert into DB2.QCUSTCDT (select * from master.LIAM5)");
            // s.execute("create table data.csv as select * from DB2.QCUSTCDT");
            // s.execute("delete from liam2");
            // s.execute("insert into liam2 (select * from DB2.liam where CDTDUE = 0)");
            // s.execute("insert into liam2 (select * from DB2.liam)");
            // PreparedStatement stmt = conn.prepareStatement("UPDATE OUT.\"Sheet1\" SET CDTDUE=? WHERE CUSNUM = ?");
            // stmt.setDouble(1, 7.50);
            // stmt.setInt(2, 9997);
            // stmt.execute();
            conn.close();

            // AS400JDBCDataSource ds = new AS400JDBCDataSource("oss73dev", "jgorzins","dvo1raks");
            // Connection conn = ds.getConnection();
            // ExcelMarshallingService marshall = new ExcelMarshallingService();
            // Connection conn = marshall.marshall(new File("C:\\Users\\jgorzins\\Desktop\\filejdbc\\in.xlsx"));
            // ExcelUnmarshallingService unmarshall = new ExcelUnmarshallingService();
            // unmarshall.unmarshall(new File("C:\\Users\\jgorzins\\Desktop\\filejdbc\\out.xlsx"), conn);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
