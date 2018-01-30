/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.teradata.edwdecoded.sql.idtarget;

/**
 *
 * @author glennm
 */
public class Debug {
    
    public static boolean debugEnabled = false;
    
    public static void println(String msg) {
        if (debugEnabled) {
            System.out.println(msg);
        }
    }
    
    public static void println() {
        if (debugEnabled) {
            System.out.println();
        }
    }
    
    public static void print(String msg) {
        if (debugEnabled) {
            System.out.print(msg);
        }
    }
    
}
