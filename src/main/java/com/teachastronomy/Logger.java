package com.teachastronomy;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by root on 10/12/16.
 */
public class Logger {
    XSSFWorkbook workbook;
    XSSFSheet sheet;
    int rownum;
    public Logger(){
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Astronomy articles and probabilites");
            rownum=0;
        Row row =  sheet.createRow(rownum);
        Cell titleCell = row.createCell(0);
        Cell p_astCell = row.createCell(1);
        Cell p_nastCell = row.createCell(2);
        Cell decision_cell = row.createCell(3);
        titleCell.setCellValue("Title");
        p_astCell.setCellValue("p_ast");
        p_nastCell.setCellValue("p_nast");
        decision_cell.setCellValue("Decision");
        rownum++;
    }

    public void writeToExcelSheet(String title,double ast_prob,double n_ast_prob,int decision){
        Row row =  sheet.createRow(rownum);
        Cell titleCell = row.createCell(0);
        Cell p_astCell = row.createCell(1);
        Cell p_nastCell = row.createCell(2);
        Cell decisionCell = row.createCell(3);
        titleCell.setCellValue(title);
        p_astCell.setCellValue(ast_prob);
        p_nastCell.setCellValue(n_ast_prob);
        decisionCell.setCellValue(decision);
        rownum++;
    }

    public void writeToExcelSheet(String title, BigDecimal ast_prob, BigDecimal n_ast_prob){
        Row row =  sheet.createRow(rownum);
        Cell titleCell = row.createCell(0);
        Cell p_astCell = row.createCell(1);
        Cell p_nastCell = row.createCell(2);
        titleCell.setCellValue(title);
        String ast_prob_str = ast_prob.toEngineeringString();

        p_astCell.setCellValue(ast_prob.toEngineeringString());
        p_nastCell.setCellValue(n_ast_prob.toEngineeringString());
        rownum++;
    }

    public void saveExcel(){
        try{
            FileOutputStream excelFile = new FileOutputStream("/home/sridhar/Desktop/Articles.xlsx");
            workbook.write(excelFile);
            excelFile.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void writeToLog(String message){
        BufferedWriter bw = null;
        try {
            FileOutputStream fileStream = new FileOutputStream(new File(Constants.logFilePath), true);
            bw = new BufferedWriter(new OutputStreamWriter(fileStream));
            bw.write(message+"\n");

        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        finally {
            try {
                if (bw != null)
                    bw.close();
            }
            catch (Exception e){
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
    }
}
