package com.mtp.shareviasound;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;



public class csvWriter {
	
	
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final String FILE_HEADER = "xaxis,yaxis,maxpeak,maxval";

	
	public byte[] readCSV(String csvFile, int C){
		int i=0;
		byte[] data= new byte[C];
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		
		try{
		br = new BufferedReader(new FileReader(csvFile));
		while ((line = br.readLine()) != null) {
				String[] val = line.split(cvsSplitBy);
				data[i]=Byte.valueOf(val[1]);
				i++;
			
		}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return data;
	}
	
	public short[] readShortCSV(String csvFile, int mark,int C){
		int i=0;
		short[] data= new short[C];
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		int count=0;
		try{
		br = new BufferedReader(new FileReader(csvFile));
		while(count!=mark){
			br.readLine();
			count++;
		}
		while (mark!=C) {
				line = br.readLine();
				String[] val = line.split(cvsSplitBy);
				data[i]=Short.valueOf(val[1]);
				i++;
				mark++;
			
		}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return data;
	}
	
	public  void writeDataFile(String fileName,byte[] ubuf2) {
		FileWriter fileWriter = null;
				
		try {
			fileWriter = new FileWriter(fileName);
			for(int i=0;i<ubuf2.length;i++){
				fileWriter.append(String.valueOf(i));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(ubuf2[i]));
				fileWriter.append(NEW_LINE_SEPARATOR);
			}
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
			
		}
	}
public  void writeDoubleDataFile(String fileName,double[] data) {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(fileName);
			for(int i=0;i<data.length;i++){
				fileWriter.append(String.valueOf(i));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(data[i]));
				fileWriter.append(NEW_LINE_SEPARATOR);
			}
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
			
		}
	}

public  void writeShortDataFile(String fileName,short[] ubuf2,int k) {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(fileName,true);
			for(int i=k;i<k+ubuf2.length;i++){
				fileWriter.append(String.valueOf(i));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(ubuf2[i-k]));
				fileWriter.append(NEW_LINE_SEPARATOR);
			}
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
			
		}
	}
	
	
	public  void writefreq(String fileName,double[] freq,double[] pow) {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(fileName);
			for(int i=0;i<freq.length;i++){
				fileWriter.append(String.valueOf(freq[i]));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(pow[i]));
				fileWriter.append(NEW_LINE_SEPARATOR);
			}
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
		}
	}

	public  void writeColFile(String fileName,double[] data,double[]maxar,double[]maxval) {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(fileName);
			fileWriter.append(FILE_HEADER.toString());
			fileWriter.append(NEW_LINE_SEPARATOR);
			for(int i=0;i<data.length;i++){
				fileWriter.append(String.valueOf(i));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(data[i]));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(maxar[i]));
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(String.valueOf(maxval[i]));
				fileWriter.append(NEW_LINE_SEPARATOR);
			}
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
			
		}
	}
	public static  void writeStringFile(String fileName,String data) {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(fileName,true);
				fileWriter.append(data);
				fileWriter.append(NEW_LINE_SEPARATOR);
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
			
		}
	}
	
	public static void writeGSMFile(String fileName,String op,String cId,String rssi) {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(fileName,true);
				fileWriter.append(op);
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(cId);
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(rssi);
				fileWriter.append(NEW_LINE_SEPARATOR);
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
			
		}
	}
	
	
}