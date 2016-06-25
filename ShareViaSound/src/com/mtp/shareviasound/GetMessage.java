package com.mtp.shareviasound;

/*This class is to get actual message from received bits*/

public final class GetMessage {
	 static int cellId,rssi;
	 static String operator;
	 static String gsm_info;
	 public  GetMessage(String f2){
		String f1=MainActivity.Iframe;
		String c="",r="",o="";
	
		if(f1.equals("")){
			MainActivity.Iframe=f2;
			return;
		}
		else{
			if(f1.substring(0, 4).equals(f2.substring(0, 4))){
				c=f1.substring(4);
				r=f2.substring(4, 10);
				o=f2.substring(10, 14);
				cellId=Integer.parseInt(c,2);
				rssi=Integer.parseInt(r,2);
				if(rssi<32)
					rssi = -113+2*rssi;
				else
					rssi=99;
				if(o.equals("0001"))
					operator="Dolphin";
				else if(o.equals("0010"))
					operator="VODAFONE IN";
				else if(o.equals("0011"))
					operator="Idea";
				else
					operator="Unknown";
				MainActivity.Iframe="";
				gsm_info=operator+"\nCellId : "+Integer.toString(cellId)+"\nRssi : "+ Integer.toString(rssi);
					
			}
			else{
		
				MainActivity.Iframe=f2;
				return;
			}
			
		}
		
		
		
	}

	
	
}
