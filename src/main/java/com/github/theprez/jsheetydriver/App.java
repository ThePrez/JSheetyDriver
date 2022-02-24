package com.github.theprez.jsheetydriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(final String[] args) {
        try {
            System.out.println("Hello World!");
            final JSheetyDriver driver = new JSheetyDriver();
            DriverManager.registerDriver(driver);
            // DriverManager.registerDriver(new AS400JDBCDriver());
            final Properties p = new Properties();
            p.put("DB2SYSTEM", "oss73dev");
            p.put("DB2UID", "linux");
            p.put("DB2PW", "linux1");
            p.put("turbo", "true");
            final Connection conn = DriverManager.getConnection("sheety", p);
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
            s.execute("sheety cd C:/Users/jgorzins/Desktop/filejdbc");
            // s.execute("create table liam2 like \"Sheet1\"");
            // s.execute("create table liam3 as select * from DB2.LIAM");
            //s.execute("sheety linkdb2 jesseg.qcustcdt");
//            s.execute("sheety load data.csv");
            //s.execute("drop table data.csv");
            // s.execute("please load master.xlsx");
            // s.execute("drop table master.juanma");
            // s.execute("create table master.juanma as select * from DB2.QCUSTCDT where CDTDUE=0");
//            s.execute("sheety linkdb2as data.csv jesseg.qcustcdt");
//            s.execute("commit");

//            s.execute("sheety load master.xlsx");
//             s.execute("sheety db2query master.\"Today's Netstat Info\" SELECT * FROM QSYS2.NETSTAT_INFO");
            // s.execute("insert into DB2.QCUSTCDT (select * from master.LIAM5)");

            // s.execute("create table data.csv as select * from DB2.QCUSTCDT");

            // s.execute("delete from liam2");
            // s.execute("insert into liam2 (select * from DB2.liam where CDTDUE = 0)");
            // s.execute("insert into liam2 (select * from DB2.liam)");
            // PreparedStatement stmt = conn.prepareStatement("UPDATE OUT.\"Sheet1\" SET CDTDUE=? WHERE CUSNUM = ?");
            // stmt.setDouble(1, 7.50);
            // stmt.setInt(2, 9997);
            // stmt.execute();
            
//            s.execute("sheety load_readonly myfile.xlsx");
//            s.execute("sheety load myfile.csv");
//            s.execute("create or replace table myfile.csv as (select * from myfile.\"Sheet 1\")");

//            s.execute("sheety load_readonly data.csv");
//            s.execute("sheety load data.xlsx");
//            s.execute("alter table data.csv rename to \"Today's data\"");
            
            s.execute("sheety load_readonly myfile.csv");
            s.execute("sheety load myfile.xlsx");
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
