package com.mtp.shareviasound;

public class CRC_CheckAt_Receiver {

	public void doCRCcheck(String s,String NewFile){
		int l=s.length();
	   	boolean[] gPoly={true,false,false,false,false,false,true,true,true};
		boolean[] msg=new boolean[l];

		int n=l;
		int i1=0;
		
		for(int i=0;i<l;i++){
			if(s.charAt(i)=='1')
				msg[i1]=true;
			else
				msg[i1]=false;
			i1++;
		}
		int j;
		for(int i=0;i<n-8;i++){
			while(msg[i]==false && i<n-8){
				i++;
			}
			if(i>=n-8) break;
			j=i;
			boolean[] divi=new boolean[9];
			for(int k=0;k<9;k++){
				divi[k]=msg[j];
				j++;
			}
			call_xor(divi,gPoly);
			j=i;
			for(int k=0;k<9;k++){
				msg[j]=d[k];
				j++;
			}
		}
		boolean rem=false;
		for(int i=l-8;i<l;i++){
			rem=rem|msg[i];
		}	
		String r="";
		if(rem==false){
			for(int m=0;m<l-8;m++)
				r+=s.charAt(m);
			new GetMessage(r);			
			
		}
	}
	
	boolean[] d=new boolean[9];
	void call_xor(boolean a[],boolean b[]){
		for(int i=0;i<9;i++)
			d[i]=a[i]^b[i];
	}	
}
