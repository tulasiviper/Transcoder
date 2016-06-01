/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.comcast.viper.common;

import com.comcast.viper.ebp.analyzer.EBPAnalyzer;
import com.comcast.viper.ebp.analyzer.EBPReader;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author ctsuser1
 */
public class NTPValidator extends Thread {
    
    private EBPAnalyzer ebpa;
    int colCount = 0;
    int fragCount = 0;
    int prevFragCount,colNum = 0;
    static boolean stopValidator = false;
    DefaultTableModel table;
    JTable jt;
    
    public void setstopValidator(boolean stop){
        this.stopValidator = stop;
    }
    public void run(){
      // Set Table Column Names 
        DefaultTableModel table = (DefaultTableModel) ebpa.jTable1.getModel();  
        table.setColumnCount(0);
        table.setRowCount(0);
        table.fireTableDataChanged();
    
        ArrayList<EBPReader> ebprList = new ArrayList<EBPReader>();

        if (ebpa.ebpr1 != null){
            ebprList.add(ebpa.ebpr1);
           // System.out.println("EBPR 1 added");
            table.addColumn(Integer.toString(ebpa.ebpr1.port));     
        }
        if (ebpa.ebpr2 != null){
            ebprList.add(ebpa.ebpr2);
          //  System.out.println("EBPR 2 added");
            table.addColumn(Integer.toString(ebpa.ebpr2.port));     
        }
        if (ebpa.ebpr3 != null){
            ebprList.add(ebpa.ebpr3);
          //  System.out.println("EBPR 3 added");
            table.addColumn(Integer.toString(ebpa.ebpr3.port));             
        }
        if (ebpa.ebpr4 != null){
            ebprList.add(ebpa.ebpr4);
         //   System.out.println("EBPR 4 added");
            table.addColumn(Integer.toString(ebpa.ebpr4.port));                
        }
        if (ebpa.ebpr5 != null){
            ebprList.add(ebpa.ebpr5);
        //    System.out.println("EBPR 5 added");
            table.addColumn(Integer.toString(ebpa.ebpr5.port));                
        }  
        if (ebpa.ebpr6 != null){
            ebprList.add(ebpa.ebpr6);
          //  System.out.println("EBPR 3 added");
            table.addColumn(Integer.toString(ebpa.ebpr6.port));             
        }
        if (ebpa.ebpr7 != null){
            ebprList.add(ebpa.ebpr7);
         //   System.out.println("EBPR 4 added");
            table.addColumn(Integer.toString(ebpa.ebpr7.port));                
        }
        if (ebpa.ebpr8 != null){
            ebprList.add(ebpa.ebpr8);
        //    System.out.println("EBPR 5 added");
            table.addColumn(Integer.toString(ebpa.ebpr8.port));                
        }          
        System.out.println("EBPR LIST SIZE : " + ebprList.size());
        jt = new JTable(table) ;
        int currRow,preRow = 0;
        int fragSize;
        Object[] data = new Object[ebprList.size()];
        Arrays.fill(data, null);
        //table.setBackground(Color.GREEN);
        for(;;){
            if (stopValidator){
                System.out.println("EXITING NTP VALIDATOR");    
                break;
            }
            // update NTP
            // Get the , minimum row size
           // get common minimumarray size
            currRow = ebprList.get(0).fragNTP.size();
            for (int l = 0 ; l < ebprList.size();l++ ){
                fragSize = ebprList.get(l).fragNTP.size();
                if (l==0){
                    currRow = fragSize;                
                }else if (currRow > fragSize){
                //    System.out.println("CURR ROW = " + currRow + " ebpr port = " + ebprList.get(0).port );
                    currRow = fragSize;
                }
            }
            if (currRow==0){
                preRow = 0;
            }
              
            if (preRow < currRow){
                preRow = currRow; 
              /*  table.addRow(new Object[]{null});
                // print the values in the table
                for (int i = 0 ; i < ebprList.size();i++ ){
                    colNum = getColumnByName(table, ebprList.get(i).port);
                    table.setValueAt(ebprList.get(i).fragNTP.get(currRow-1),currRow-1, colNum); 
                }  */
                for (int i = 0 ; i < ebprList.size();i++ ){
                    colNum = getColumnByName(table, ebprList.get(i).port);
                    data[colNum]  = ebprList.get(i).fragNTP.get(currRow-1);
                }
                table.addRow(data);
            } 
            // delay checks 
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ex) {
                Logger.getLogger(NTPValidator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    private int getColumnByName(DefaultTableModel table, int name) {
        //System.out.println("COL NAME PASSED = " + name + "\n TAB COL COUNT = " +table.getColumnCount());
        for (int i = 0; i < table.getColumnCount(); ++i){
            if (Integer.parseInt(table.getColumnName(i)) == name){
          //      System.out.println("COL NAME IN TABLE = " + table.getColumnName(i) + "Column INDEX = " + i);
                return i;
            }else{
            //    System.out.println("COL NAME Found = " + table.getColumnName(i) + " does not match to the name " + name);
            }
        }
        return 0;
    }   
}
