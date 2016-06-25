package com.mtp.shareviasound;

public class viterbi{

	public static int[] code(int d[]) {
		int[] gen=new int[2*d.length];
		int counter=d.length;
		String s="";
		for (int idx = 0; idx < counter; ++idx){
			s=s+d[idx];
		}
		int j=0;
		int index=0;
		int[] c=new int[d.length];
		while(j<d.length){
			for(int i=0;i<c.length;i++){
				c[i]=d[j+i];
			}
			int[] out1=new int[c.length];
			int[] out2=new int[c.length];
			int[] x=new int[7];
			out1[0]=0^c[0];
			out2[0]=0^c[0];
			x[1]=c[0];
			out1[1]=0^c[0]^c[1];
			out2[1]=0^c[1];
			x[0]=c[1];
			for(int i=2;i<c.length;i++){
				x[2]=x[1];
				x[1]=x[0];
				x[0]=c[i];
				out1[i]=x[2]^x[1]^x[0];
				out2[i]=x[2]^x[0];
			}
			for(int i=0;i<out1.length;i++){
				gen[index]=out1[i];
				index++;
				gen[index]=out2[i];
				index++;
			}
			j=j+c.length;
		}
        return gen;
	}
}

