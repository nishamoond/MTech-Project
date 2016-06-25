package com.mtp.shareviasound;
public class CRC_At_Sender {

	public static int[] genCRC(int a[]) {
		int m,n;
		m=a.length;
		n=m+8;
		boolean[]  b=new boolean[n];
		boolean[]  g={true,false,false,false,false,false,true,true,true};
		boolean[]  dv=new boolean[9];
		
		for(int i=0;i<m;i++){
			if(a[i]==1)
				b[i]=true;
   		  	else
   		  		b[i]=false;
   	  	}
		
   	  	for(int i=m;i<n;i++)
   	  		b[i]=false;
   	  	
   	 
   	  	int t=0;
   	  	for(int i=0;i<m;i++){
   	  		while(b[i]==false && i<m){
   	  			i++;
   	  		}
   	  		if(i>=m) break;
   	  		t=i;
   	  		boolean[] divi=new boolean[9];
   	  		for(int k=0;k<=8;k++){
   	  			divi[k]=b[t];
   	  			t++;
   	  		}

   	  		for(int k=0;k<9;k++){
   	  			dv[k]=divi[k]^g[k];
   	  		}
   	  		t=i;
   	  		for(int k=0;k<=8;k++){
   	  			b[t]=dv[k];
   	  			t++;
   	  		}
   	  	}
   	  	
   	  	int[] msg=new int[a.length+8];
   	  	for(int i=0;i<m;i++)
   	  		msg[i]=a[i];
   		  
   	  	for(int i=m;i<n;i++){
   	  		if(b[i]==true)
   	  			msg[i]=1;
   	  		else
   	  			msg[i]=0;
   	  	}
   	  	int[] genViterbi=new int[2*msg.length];
   	  	genViterbi=viterbi.code(msg);
      
   	  	return genViterbi;
	}
}
