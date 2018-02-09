/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.teradata.edwdecoded.sql.idtarget;

import com.teradata.fnc.DbsInfo;
import com.teradata.edwdecoded.sql.idtarget.targettableparser.ParseException;
import com.teradata.edwdecoded.sql.idtarget.targettableparser.TargetTableParser;
import com.teradata.edwdecoded.sql.idtarget.targettableparser.TokenMgrError;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

/**
 *
 * @author glennm + modifications by beekm
 */
public class Main {

    public static final String version = "1.00.03.01";
    
    /** Exit status code returned if the application succeeds. */
    public static final int APP_SUCCESS = 0;
    /** Exit status code returned if the application succeeds but with warnings. */
    public static final int APP_WARNING = 1;
    /** Exit status code returned if the application fails. */
    public static final int APP_FAILURE = 2;
    /** Status that indicates that the GUI has started, so do not exit. */
    public static final int APP_GUI = -1;

    /**
     * Entry point when run as a standalone program.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Add any other initialisation here.
        Main app = new Main();
        int stat;
        try {
            stat = app.go(args);
        } catch (Throwable t) {
            t.printStackTrace();
            stat = APP_FAILURE;
        }
        if (stat != APP_GUI) {
            System.exit(stat);
        }
    }

    /**
     * UDF Entry Point - return the target object as it appears in the query.
     * 
     * @param sqlText the SQL Text to be analysed.
     * @return the target table or null if none.
     */
    public static String idTargetTableUDF (String sqlText) {
        
        if (sqlText == null) {
            return null;
        }

        Main m = new Main();
        return m.idTarget(sqlText, true);
    }

    

    /**
     * UDF Entry point - return the Fully qualified target object from a query.
     * <p>
     * If the query's target is fully qualified then it is returned as is.
     * Otherwise the target will be qualified with the defaultDb supplied as a
     * parameter.
     * </p>
     * 
     * @param defaultDb The name of the Default Database.
     * @param sqlText the SQL Text to be analysed.
     * @return the target table or null if none.
     */
    public static String idTargetTableUDF (String defaultDb, String sqlText) {
        return idTarget(defaultDb, sqlText, true);
    }

    /**
     * UDF entry point - Identify the base object name from a query.
     * <p>
     * If the target of a query is fully qualified then the database name
     * is stripped out of the query.
     * </p>
     * @param sqlText the SQL text to be analysed.
     * @return The base object (table) name extracted from the query.
     */
    public static String idTargetTableBaseUDF(String sqlText) {
        
        if (sqlText == null) {
            return null;
        }

        Main m = new Main();
        String target = m.idTarget(sqlText, true);
            // If a target was found work out if a database is specified
            // in the target.
        if (target != null) {
                // We have a result, so lets check if it is qualified.
                // If it is, just extract the base table name.
            if (target.contains(".")) {
                String [] components = target.split("\\.");
                target = components[1];
            }
        }
        return target;
    }
    
    /**
     * The main line when run as a standalone program.
     * <p>
     * Calls the idTarget method once for each command line parameter.
     * It is assumed that the command line parameter is the name of a file.
     * </p>
     * @param args the command line arguments.
     * @return the exit status code.
     */
    public int go(String [] args) {
        System.out.println("IdTargetOfSql version: " + version);
        if (args.length == 0) {
            usage();
            return APP_SUCCESS;
        }

        if (args.length == 1 && "-v".equals(args[0])) {
            return APP_SUCCESS;
        }
        
        for (String arg : args) {
            if ("-d".equalsIgnoreCase(arg)) {
                Debug.debugEnabled = false;
            } else if ("+d".equalsIgnoreCase(arg)) {
                Debug.debugEnabled = true;
            } else if ("-ui".equalsIgnoreCase(arg)) {
                MainFrame m = new MainFrame();
                m.setVisible(true);
                return APP_GUI;
            } else {
                idTarget(new File(arg));
            }
        }
        
        return APP_SUCCESS;
    }
    
    
    /**
     * Output usage information.
     */
    private void usage() {
        System.out.println("Usage:");
        System.out.println("    idTargetTable [-d|+d] file1 [[-d|+d] file2 ...]");
        System.out.println("    idTargetTable [-d|+d] -ui");
        System.out.println();
        System.out.println("Where:");
        System.out.println("  file1 is the name of a file containing SQL to be analysed.");
        System.out.println("  -d    turn off debugging.");
        System.out.println("  +d    turn on debugging.");
        System.out.println("  -ui   activate GUI.");
    }

    
    
    /**
     * Identify the target table from the query in the named file.
     * @see Main#idTarget(java.lang.String) 
     * 
     * @param fileName the name of the file containing the query text to be analysed.
     */
    public void idTarget(File fileName) {
        BufferedReader br = null;
        try {
            String inLine;
            StringBuilder sb = new StringBuilder();
            
            br = new BufferedReader(new FileReader(fileName));
            while ((inLine = br.readLine()) != null) {
                sb.append(inLine);
                sb.append('\n');
            }
            String targetTable = idTarget(sb.toString(), false);
            System.out.println("*** Target of query in " + fileName.getName()+ ": " + targetTable);
            System.out.println();

        } catch (FileNotFoundException e) {
            Debug.println("Warning: " + fileName + " could not be openned:");
            Debug.println("         " + e.getMessage());
            Debug.println();
        } catch (IOException e) {
            Debug.println("Warning: error reading: " + fileName);
            Debug.println("         " + e.getMessage());
            Debug.println();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                Debug.println("Warning: error closing: " + fileName);
                Debug.println("         " + e.getMessage());
                Debug.println();
            }
        }
    }
    

    /**
     * Identify the target of the table and allow the specification of UDF mode.
     * @param defaultDb the default database if the query is not fully qualified
     * @param sqlText the SQL to parse.
     * @param udfMode true if running as a UDF.
     * @return the fully qualified target table.
     */
    public static String idTarget(String defaultDb, String sqlText, boolean udfMode) {
        
        if (sqlText == null) {
            return null;
        }

        Main m = new Main();
        String target = m.idTarget(sqlText, udfMode);
            // If a target was found work out if a database is specified
            // in the target.
        if (target != null) {
                // If we have a defaultDb supplied as a parameter, check
                // to see if the discovered target is fully qualified.
                // if it is not fully qualified, create a fully qualified
                // target using the default database name supplied as a
                // parameter.
            if (defaultDb != null && ! target.contains(".")) {
                target = defaultDb.trim() + "." + target;
            }
        }

        return target;
    }
    

    /**
     * IDentify the target table of a query that writes to a table (e.g. INSERT).
     * <p>
     * Recognised queries include:
     * <ul>
     *   <li>INSERT</li>
     *   <li>UPDATE</li>
     *   <li>DELETE</li>
     *   <li>MERGE</li>
     *   <li>all queries may be preceded by an optional set of LOCKING clauses</li>
     *   <li>TODO: Add Create Table XXX AS</li>
     *   <li>TODO: Add USING clause</li>
     * </ul>
     * </p>
     * @param sqlText the sqlText to be analysed.
     * @return the target of the query, or null if note a write query.
     */
    public String idTarget(String sqlText, boolean udfMode) {
        String targetTable = null;
        try {
            Debug.println("Analysing:\n\"" + sqlText + "\"");

            if (sqlText == null) {
                return null;
            }

            
        /* Running just a tokeniser and dumping the tokens */
//        StringReader reader = new StringReader(sqlText);
//        SimpleCharStream scs = new SimpleCharStream(reader);
//        TargetTableParserTokenManager tokenManager = new TargetTableParserTokenManager(scs);
//        int tokenNumber = 1;
//        try {
//            for (Token t = tokenManager.getNextToken(); t.kind != EOF; t = tokenManager.getNextToken()) {
//                Debug.println(String.format("Token %4d (%4d): %s", tokenNumber, t.kind, t.image));
//                tokenNumber++;
//            }
//        } catch (TokenMgrError e) {
//            System.out.println("TokenMgrError: " + e.getMessage());
//        }
        
            TargetTableParser  parser = new TargetTableParser(new StringReader(sqlText));
            try {
                targetTable = parser.idTarget();

            } catch (ParseException e) {
                targetTable = parseError(e, sqlText);
            } catch (TokenMgrError e) {
                targetTable = parseError(e, sqlText);
            }
        } catch (Exception e) {
            Debug.println("Exception caught: " + e);
            if (udfMode) {
                String inp = sqlText;
                if (inp != null && inp.length() > 100) {
                    inp = inp.substring(0, 100);
                }
                DbsInfo.traceWrite("TargetTableUDF: Exception: " + e + ", inp: " + inp);
            }
        }
        

        return targetTable;
    }
    
    /**
     * Format a parse error for output.
     * @param t the exception
     * @param originalSql the sql being parsed that generated the error.
     * @return Returns the original Sql and a comment containing the error text.
     * 
     */
    public String parseError(Throwable t, String originalSql) {
        Debug.println("Error processing sql Text. Using input SqlText.");
        String msg = t.getMessage();
        Debug.println(msg);

        return null;

    }
}
