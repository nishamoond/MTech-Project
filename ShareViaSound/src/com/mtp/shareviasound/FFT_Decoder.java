package com.mtp.shareviasound;
import java.util.*;
public class FFT_Decoder {
	String s="";
	csvWriter fr=new csvWriter();
	double amp;
	
public void csma(short[] orgData,int C){
		int l=orgData.length;
		short[] data=new short[C];
		double[] lp=new double[C];
		double[] hp=new double[C];
		double[] all=new double[C];
		System.arraycopy(orgData, 0, data, 0, C);	
		Filter filter = new Filter(19000,44100, Filter.PassType.Lowpass,1);
	    for (int i = 0; i < orgData.length; i++)
	    {
	        filter.Update(data[i]);
	        all[i]=filter.getValue();
	       
	    }
		
		 Filter filter2 = new Filter(15000,44100, Filter.PassType.Highpass,1);
		    for (int i = 0; i < orgData.length; i++)
		    {
		        filter2.Update((float)all[i]);
		        hp[i]=filter2.getValue();
		       
		    }
		    

			 Filter filter3 = new Filter(15000,44100, Filter.PassType.Lowpass,1);
			    for (int i = 0; i < orgData.length; i++)
			    {
			        filter3.Update((float)all[i]);
			        lp[i]=filter3.getValue();
			       
			    }
			    double low=0,high=0,avglow=0,avghigh=0;
			    for (int i = 0; i < orgData.length; i++){
			    	low+=lp[i]*lp[i];
			    	high+=hp[i]*hp[i];
			    	
			    }
			    avglow=low/l;
			    avghigh=high/l;
			    csvWriter wr= new csvWriter();
			    wr.writeStringFile("/sdcard/csma_avg_5m.csv", Double.toString(avghigh/avglow));
			    
}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
public void Channel_Sensing_FFT(short[]orgData,double[] Ddata){
		int l=Ddata.length;
		double[] real=new double[l];
		double[] imag=new double[l];
		double[] bins=new double[l/2];
		double[] pow1=new double[l/2];
		double[] pow2=new double[l/2];
		double[] freq=new double[l/2];
		for(int k=0;k<l;k++){
			real[k]=orgData[k];
			imag[k]=0;
		}
		fft(real, imag);
		
		for(int k=0;k<l/2;k++){  //8192-3000, 4096-1600
			bins[k]=k*44100/l;
			pow1[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
			freq[k]=k*44100/l;
		}
		
		for(int k=0;k<l;k++){
			real[k]=Ddata[k];
			imag[k]=0;
		}
		fft(real, imag);
		
		for(int k=0;k<l/2;k++){  //8192-3000, 4096-1600
			bins[k]=k*44100/l;
			pow2[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
		}
		double low=0,high=0;
		for(int k=0;k<l/2;k++){
			if(k<11146)
				low+=pow1[k];
			else
				high+=pow1[k];
		}
		low=low/11145;
		high=high/5238;
		csvWriter c=new csvWriter();
		c.writeColFile("/sdcard/nexus_trans.csv", freq, pow1, pow2);
}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public double decodeSS(short[] startSymbol,int l){
		int indLimit;
		if(l==16384)indLimit=5500;
		else if(l==8192)indLimit=3000;
		else if(l==4096)indLimit=1600;
		else if(l==2048)indLimit=760;
		else indLimit=400;
		
		double[] real=new double[l];
		double[] imag=new double[l];
		double[] bins=new double[l/2];
		double[] pow=new double[l/2];
		for(int k=0;k<l;k++){
			real[k]=startSymbol[k];
			imag[k]=0;
		}
		fft(real, imag);
	
		double max=real[0],ind=0;
		for(int k=indLimit;k<l/2;k++){  //8192-3000, 4096-1600
			bins[k]=k*44100/l;
			pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
			
			if(max<pow[k]){
				max=pow[k];
				ind=k;
			}
		}
		return ind;
}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void decodeFSK(short[] dataOnly,int samples,int databits,int datasize,int Encoding,String NewFile){	
		//f1=17,f2=18.5
		int v=0;
		int shift=0;
		int l=datasize;
		int f1Ind;
		int f2Ind;
		int f18;

//16384
		if(l==16384){
			f1Ind=6316;
			 f2Ind=6873;
			 f18=6688;
			 shift=10000;
		}
		
//8192
		if(l==8192){
		f1Ind=3158;
		 f2Ind=3436;
		 f18=3344;
		 shift=3072;
		}
		
//4096		
		else if(l==4096){
			f1Ind=1579;
			f2Ind=1718;
			f18=1672;
			shift=2560;
		}
//2048	
		else if(l==2048){
			 f1Ind=790;		
			 f2Ind=859;       
			 f18=836;			
			 shift=1536;
		}
//1024
		else{
			f1Ind=395;
			f2Ind=429;
			f18=418;
			shift=600;
		}
				
		
		double f1=0,f2=0;
		double[] real=new double[l];
		double[] imag=new double[l];
		double[] bins=new double[l/2];
		double[] pow=new double[l/2];
		double P,Pr=1000.0;
		int[] b=new int[databits-1];
		
		int ndata=0;
	for(int j=0;j<samples;j++){
		 ndata*=l;
		for(int i=0;i<databits-1;i++){
			for(int k=0;k<l;k++){
				real[k]=dataOnly[(i*l+k+v)+ndata];
				imag[k]=0;
			}
			fft(real, imag);
			
			double max=real[0],ind=f1Ind-4;
			int k;
			for( k=f1Ind-4;k<f1Ind+5;k++){
				bins[k]=k*44100/l;
				pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
				if(max<pow[k]){
					max=pow[k];
					ind=k;
				}
			}
			for( k=f2Ind-4;k<f2Ind+5;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
			for( k=f18-1;k<f18+2;k++){
				bins[k]=k*44100/l;
				pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
		double f=ind*44100/l;			
	if((i==0) && ((17950<f && f<18050))){
			v=v+shift;
			i--;
		}
	else if(16900<f && f<17100){
	 	if(( Math.abs(pow[f1Ind]-pow[f2Ind]) < (pow[f2Ind]-f2) ) )
			b[i]=1;
		else
			b[i]=0;
	}
	else if(18400<f && f<18600){
		if( Math.abs(pow[f1Ind]-pow[f2Ind]) < (pow[f1Ind]-f1) )
			b[i]=0;
		else
			b[i]=1;
	}
	else b[i]=0;
	f1=pow[f1Ind];
	f2=pow[f2Ind];
	}
	ndata=(j+1)*(databits-1);
		if(Encoding==0)
			hammingDecoder(b);
		else if(Encoding==1)
			viterbiDecoder(b);
		else//No Encoding
			for(int i=0	;i<databits-1;i++)
				s+=b[i];
	}
 csvWriter str=new csvWriter();
 str.writeStringFile(NewFile+".csv", s);//	/viterbi-FSK/final-22050-8192-4096	
}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void decodeNoiseASK(short[] dataOnly,int samples,int databits,double noiseLevel){	
		
		
		int l=4096;
		int v=0;
		int f18=418;
		double[] real=new double[l];
		double[] imag=new double[l];
		double[] bins=new double[l/2];
		double[] pow=new double[l/2];
		
		int[] b=new int[databits-1];
		int ndata=0;
		for(int k=0;k<l;k++){
			real[k]=dataOnly[(0*l+k+v)+ndata];
			imag[k]=0;
		}
		fft(real, imag);
		
		double max=real[0],ind=0;
		for(int k=1200;k<l/2;k++){
			bins[k]=k*44100/l;
			pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
			if(max<pow[k]){
				max=pow[k];
				ind=k;
		}
	}
		
		System.out.print(max+" , "+ ind+" , ");
		double f=ind*44100/l;
		System.out.println(f);
	if((17950<f && f<18050)|| (pow[f18]>100)){
				v=v+2560;	
		}
	double powerLevel;
	for(int j=0;j<samples;j++){	
		ndata=j*l*(databits-1);
		for(int i=0;i<databits-1;i++){
		powerLevel=0;
		for(int k=0;k<l;k++){
			powerLevel+=dataOnly[(i*l+k+v)+ndata];
		}
			if(powerLevel>noiseLevel*l/200.5){
				b[i]=1;
			}
			else	b[i]=0;
			System.out.println(powerLevel);
			System.out.println(b[i]);
		}
		viterbiDecoder(b);
	}
	System.out.println(s);
	csvWriter str=new csvWriter();
	str.writeStringFile("./viterbi-ASK/RASK-4bit-4KSS-2048.csv", s);		
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
public void decodeASK(byte[] dataOnly,int samples,int databits){	
	int l=8192;
	double[] real=new double[l];
	double[] imag=new double[l];
	double[] bins=new double[l/2];
	double[] pow=new double[l/2];
	double P,Pr=1000.0;
	int[] b=new int[databits];
	int ndata=0;
for(int j=0;j<samples;j++){
	double m=100;
	 ndata*=l;
	for(int i=0;i<databits;i++){
		for(int k=0;k<l;k++){
			real[k]=dataOnly[(i*l+k)+ndata];
			imag[k]=0;
		}
		fft(real, imag);
		
		double max=real[0],ind=0;
		for(int k=2500;k<l/2;k++){
			bins[k]=k*44100/l;
			pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
			//pow[k]=20*Math.log10(P/Pr);
			
			if(max<pow[k]){
				max=pow[k];
				ind=k;
		}
	}
		
	System.out.print(max+" , "+ ind+" , ");
	double f=ind*44100/l;
	System.out.println(f);
	

	
if((16900<f && f<17100) && (max>m))b[i]=1;
else if((i==0) && (17990<f && f<18010) && (max>100))b[i]=2;
else b[i]=0;


	m=(m+max)/2;
}
	
	int[] bits=new int[databits-1];   //change according to encoding scheme
	if(b[0]==2 && (b[1]==1 || b[1]==0)){
		for(int i=1,k=0;i<databits && k<databits-1;i++,k++){
			bits[k]=b[i];
		}
		ndata=(j+1)*(databits-1)+1;
	}
	else{
		for(int i=0;i<databits-1;i++){
			bits[i]=b[i];
		}
		ndata=(j+1)*(databits-1);
	}
	//hammingDecoder(bits);
	
	viterbiDecoder(bits);
	
	/*for(int i=0;i<databits-1;i++){
		s+=bits[i];
	}*/
	
}
csvWriter str=new csvWriter();
str.writeStringFile("./viterbi-ASK/new-8192-8192-8bit/RASK-8bit-8KSS-8192.csv", s);		
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void decode4FSK(double[] dataOnly,int samples,int databits,int datasize,int Encoding,String NewFile){	
		int l=datasize;
		int v=0;
		int f1Ind;
		int f2Ind;
		int f3Ind;
		int f0Ind;
		int f18;
		int shift=0;
		
//2048		
		if(l==2048){
			 f1Ind=790;//17.011
			 f2Ind=813;//17.506
			 f3Ind=859; //18.497     ////For 4096 samples--17KHz=1579 and 18.5KHz=1718  18KHz=1672
			 f0Ind=766;//16.494
			 f18=836;//18.001						///For 2048 samples--17KHz=790 and 18.5KHz=859 and 18KHz=836
									//For 1024 samples ---17=395 and 18.475=429 and 18=418
			 shift=1536;
		}
//4096
		else if(l==4096){
			 f1Ind=1579;
			 f2Ind=1626;
			 f3Ind=1718; //18.497
			 f0Ind=1532;//16.494
			 f18=1672;
			 shift=2560;
		}
	
//8192
		else{
			f1Ind=3158;//17.000
			f2Ind=3251;//17.501
			f3Ind=3436; //18.497
			f0Ind=3065;//16.494
			f18=3344;//18.001
			shift=3072;
		}	
				
		double f0=0,f1=0,f2=0,f3=0;
			 
			 
		double[] real=new double[l];
		double[] imag=new double[l];
		double[] bins=new double[l/2];
		double[] pow=new double[l/2];
		double P,Pr=1000.0;
		int[] b=new int[2*(databits-1)];
		int ndata=0;
	for(int j=0;j<samples;j++){
		 ndata*=l;
		for(int i=0,n=0;i<databits-1 && n<2*(databits-1);i++,n+=2){
			for(int k=0;k<l;k++){
				real[k]=dataOnly[(i*l+k+v)+ndata];
				imag[k]=0;
			}
			fft(real, imag);
			
			double max=real[0],ind=0;
			int k;
				for( k=f0Ind-4;k<f0Ind+5;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f1Ind-4;k<f1Ind+5;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f2Ind-4;k<f2Ind+5;k++){
						bins[k]=k*44100/l;
						pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
						if(max<pow[k]){
							max=pow[k];
							ind=k;
						}
					}
					for( k=f3Ind-4;k<f3Ind+5;k++){
						bins[k]=k*44100/l;
						pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
						if(max<pow[k]){
							max=pow[k];
							ind=k;
						}
					}
					for( k=f18-1;k<f18+2;k++){
						bins[k]=k*44100/l;
						pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
						if(max<pow[k]){
							max=pow[k];
							ind=k;
						}
					}
		
		double f=ind*44100/l;
		if((i==0) && ((17950<f && f<18050))){
			v=v+shift;
			i--;n-=2;
			continue;
		}
		
		else if(16400<f && f<16600){
			b[n]=0;b[n+1]=0;
			
		}
		
		else if(16900<f && f<17100){
			b[n]=0;b[n+1]=1;
			
		}
		else if(17400<f && f<17600){
			b[n]=1;b[n+1]=1;
			
		}
		else if(18400<f && f<18600){
			b[n]=1;b[n+1]=0;
			
		} 
		else {b[n]=0;b[n+1]=0;}
		
		double inc=0,gap=0;
		int flg1=b[n],flg2=b[n+1];
	
//Decide bits according to energy levels of symbols		
		
		if(pow[f0Ind]-f0>inc){
			flg1=0;flg2=0;
			inc=pow[f0Ind]-f0;
			gap=Math.abs(pow[f0Ind]-max);
		}
		if(pow[f1Ind]-f1>inc){
			flg1=0;flg2=1;
			inc=pow[f1Ind]-f1;
			gap=Math.abs(pow[f1Ind]-max);
		}
		if(pow[f2Ind]-f2>inc){
			flg1=1;flg2=1;
			inc=pow[f2Ind]-f2;
			gap=Math.abs(pow[f2Ind]-max);
		}
		if(pow[f3Ind]-f3>inc){
			flg1=1;flg2=0;
			inc=pow[f3Ind]-f3;
			gap=Math.abs(pow[f3Ind]-max);
		}
		if(gap<inc){	
			
			b[n]=flg1;
			b[n+1]=flg2;
		}
	
		f0=pow[f0Ind];
		f1=pow[f1Ind];
		f2=pow[f2Ind];
		f3=pow[f3Ind];
		
	}
	ndata=(j+1)*4;
		int ham[] =new int [7];
		if(Encoding==0){
			for(int h=0;h<b.length-1;h+=7){
				System.arraycopy(b, h, ham, 0, 7);
				hammingDecoder(ham);
			}
		}
		else if(Encoding==1)
			viterbiDecoder(b);
		else//No Encoding
			for(int i=0	;i<databits-1;i++)
				s+=b[i];
	}
	csvWriter str=new csvWriter();
	str.writeStringFile(NewFile+".csv", s);		
	}
	 
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////


public int decode8FSK(short[] dataOnly,int samples,int databits,int datasize,int Encoding,String NewFile){	
	
	
	int l=datasize;
	int v=0;
	int f1Ind;
	int f2Ind;
	int f3Ind;
	int f4Ind;
	int f5Ind;
	int f6Ind;
	int f7Ind;
	int f0Ind;
	int f18;
	int shift=0;
	int sh=0;
	
//2048		
	if(l==2048){
		 f0Ind=766;//16.494 
		 f1Ind=801;//17.250
		 f2Ind=824;//17.750
		 f3Ind=859; //18.497     
		 f4Ind=848;//18.250
		 f5Ind=755;//16250
		 f6Ind=813;//17506
		 f7Ind=790;//17011
				 
		 f18=836;//18.001												
		 shift=300;
	}
//4096
	else {//if(l==4096){
		 f0Ind=1532;//16.494 
		 f1Ind=1602;//17.250
		 f2Ind=1649;//17.750
		 f3Ind=1718; //18.497     
		 f4Ind=1695;//18.250
		 f5Ind=1509;//16250
		 f6Ind=1626;//17506
		 f7Ind=1580;//17011
		
		
		 f18=1672;
		 shift=2560;
	}

//8192
	/*else{
		 f0Ind=;//16.494 
		 f1Ind=;//17.250
		 f2Ind=;//17.750
		 f3Ind=; //18.497     
		 f4Ind=;//18.250
		 f5Ind=;//16250
		 f6Ind=;//17506
		 f7Ind=;//17011
		
		 f18=3344;//18.001
		 shift=3072;
	}	*/
			
	double f0=0,f1=0,f2=0,f3=0,f4=0,f5=0,f6=0,f7=0;
		 
		 
	double[] real=new double[l];
	double[] imag=new double[l];
	double[] bins=new double[l/2];
	double[] pow=new double[l/2];
	double P,Pr=1000.0;
	int[] b=new int[3*(databits-1)];
	//int[] gen=new int[databits];
	int ndata=0;
for(int j=0;j<samples;j++){
	 ndata*=l;
	for(int i=0,n=0;i<databits-1 && n<3*(databits-1);i++,n+=3){
		for(int k=0;k<l;k++){
			real[k]=dataOnly[(i*l+k+v)+ndata];
			imag[k]=0;
		}
		fft(real, imag);
		
		double max=real[0],ind=0;
			int k;
			for( k=f0Ind-4;k<f0Ind+5;k++){
				bins[k]=k*44100/l;
				pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
				if(max<pow[k]){
					max=pow[k];
					ind=k;
				}
			}
			for( k=f1Ind-4;k<f1Ind+5;k++){
				bins[k]=k*44100/l;
				pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
				if(max<pow[k]){
					max=pow[k];
					ind=k;
				}
			}
			for( k=f2Ind-4;k<f2Ind+5;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f3Ind-4;k<f3Ind+5;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f4Ind-4;k<f4Ind+5;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f5Ind-4;k<f5Ind+5;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f6Ind-4;k<f6Ind+5;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f7Ind-4;k<f7Ind+5;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f18-1;k<f18+2;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
	double f=ind*44100/l;
	
	if((i==0) && ((17950<f && f<18050))){
		v=v+shift;
		i--;n-=3;
		sh++;
		continue;
	}
	
	
	
	else if(16400<f && f<16600){
		b[n]=0;b[n+1]=0;b[n+2]=0;
		
	}
	
	else if(17150<f && f<17350){
		b[n]=0;b[n+1]=0;b[n+2]=1;
		
	}
	else if(17650<f && f<17850){
		b[n]=0;b[n+1]=1;b[n+2]=0;
		
	}
	else if(18400<f && f<18600){
		b[n]=0;b[n+1]=1;b[n+2]=1;
		
	} 
	else if(18150<f && f<18350){
		b[n]=1;b[n+1]=0;b[n+2]=0;
		
	}
	else if(16150<f && f<16350){
		b[n]=1;b[n+1]=0;b[n+2]=1;
		
	}
	else if(17400<f && f<17600){
		b[n]=1;b[n+1]=1;b[n+2]=0;
		
	}
	else if(16900<f && f<17100){
		b[n]=1;b[n+1]=1;b[n+2]=1;
		
	}
	
	else {b[n]=0;b[n+1]=0;b[n+2]=0;}
	
	double inc=0,gap=0;
	int flg1=b[n],flg2=b[n+1],flg3=b[n+2];
	
//Decide bits according to energy levels of symbols		

	if(pow[f0Ind]-f0>inc){
		flg1=0;flg2=0;flg3=0;
		inc=pow[f0Ind]-f0;
		gap=Math.abs(pow[f0Ind]-max);
	}
	if(pow[f1Ind]-f1>inc){
		flg1=0;flg2=0;flg3=1;
		inc=pow[f1Ind]-f1;
		gap=Math.abs(pow[f1Ind]-max);
	}
	if(pow[f2Ind]-f2>inc){
		flg1=0;flg2=1;flg3=0;
		inc=pow[f2Ind]-f2;
		gap=Math.abs(pow[f2Ind]-max);
	}
	if(pow[f3Ind]-f3>inc){
		flg1=0;flg2=1;flg3=1;
		inc=pow[f3Ind]-f3;
		gap=Math.abs(pow[f3Ind]-max);
	}
	if(pow[f4Ind]-f4>inc){
		flg1=1;flg2=0;flg3=0;
		inc=pow[f4Ind]-f3;
		gap=Math.abs(pow[f4Ind]-max);
	}
	if(pow[f5Ind]-f5>inc){
		flg1=1;flg2=0;flg3=1;
		inc=pow[f5Ind]-f5;
		gap=Math.abs(pow[f5Ind]-max);
	}
	if(pow[f6Ind]-f6>inc){
		flg1=1;flg2=1;flg3=0;
		inc=pow[f6Ind]-f6;
		gap=Math.abs(pow[f6Ind]-max);
	}
	if(pow[f7Ind]-f7>inc){
		flg1=1;flg2=1;flg3=1;
		inc=pow[f7Ind]-f7;
		gap=Math.abs(pow[f7Ind]-max);
	}
	if(gap<inc){	
		
		b[n]=flg1;
		b[n+1]=flg2;
		b[n+2]=flg3;
	}
	
	
	
	
	f0=pow[f0Ind];
	f1=pow[f1Ind];
	f2=pow[f2Ind];
	f3=pow[f3Ind];
	f4=pow[f4Ind];
	f5=pow[f5Ind];
	f6=pow[f6Ind];
	f7=pow[f7Ind];
	
}
	
	ndata=(j+1)*4;
	
	int ham[] =new int [56];
	if(Encoding==0){
			hammingDecoder(b);
	}
	
	else if(Encoding==1){
		
		System.arraycopy(b, 0, ham, 0, ham.length);
			viterbiDecoder(ham);
		
	}
	else//No Encoding
		for(int i=0	;i<databits-1;i++)
			s+=b[i];
}
CRC_CheckAt_Receiver crc=new CRC_CheckAt_Receiver();
crc.doCRCcheck(s, NewFile);
return sh;
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public void decode16FSK(short[] dataOnly,int samples,int databits,int datasize,int Encoding,String NewFile){	
	
	
	int l=datasize;
	int v=0;
	int f1Ind;
	int f2Ind;
	int f3Ind;
	int f4Ind;
	int f5Ind;
	int f6Ind;
	int f7Ind;
	int f0Ind,f8Ind,f9Ind,f10Ind,f11Ind,f12Ind,f13Ind,f14Ind,f15Ind;
	
	int f18;
	int shift=0;
	
	
	
	
//2048		
//	if(l==2048){
		 f0Ind= 785;//16903
		 f1Ind=790;//17011
		 f2Ind=794;//17097
		 f3Ind=799; //17205
		 f4Ind=803;//17291
		 f5Ind=808;//17398
		 f6Ind=813;//17506
		 f7Ind=817;//17592
		 f8Ind=822;//17700
		 f9Ind=827;//17807
		 f10Ind=831;//17894
		 f11Ind=841;//18109
		 f12Ind=845;//18195
		 f13Ind=850;//18303
		 f14Ind=855;//18410
		 f15Ind=859;//18497
				 
		 f18=836;//18.001												
		 shift=1536;
	//}
//4096
/*	else if(l==4096){
		 f0Ind=;//16.494 
		 f1Ind=;//17.250
		 f2Ind=;//17.750
		 f3Ind=; //18.497     
		 f4Ind=;//18.250
		 f5Ind=;//16250
		 f6Ind=;//17506
		 f7Ind=;//17011
		
		
		 f18=1672;
		 shift=2560;
	}

//8192
	else{
		 f0Ind=;//16.494 
		 f1Ind=;//17.250
		 f2Ind=;//17.750
		 f3Ind=; //18.497     
		 f4Ind=;//18.250
		 f5Ind=;//16250
		 f6Ind=;//17506
		 f7Ind=;//17011
		
		 f18=3344;//18.001
		 shift=3072;
	}	*/
			
	double f0=0,f1=0,f2=0,f3=0,f4=0,f5=0,f6=0,f7=0,f8=0,f9=0,f10=0,f11=0,f12=0,f13=0,f14=0,f15=0;
		 
		 
	double[] real=new double[l];
	double[] imag=new double[l];
	double[] bins=new double[l/2];
	double[] pow=new double[l/2];
	double P,Pr=1000.0;
	int[] b=new int[4*(databits-1)];
	//int[] gen=new int[databits];
	int ndata=0;
for(int j=0;j<samples;j++){
	 ndata*=l;
	for(int i=0,n=0;i<databits-1 && n<4*(databits-1);i++,n+=4){
		for(int k=0;k<l;k++){
			real[k]=dataOnly[(i*l+k+v)+ndata];
			imag[k]=0;
		}
		fft(real, imag);
		
		double max=real[0],ind=0;

			int k;
			for( k=f0Ind-2;k<f0Ind+3;k++){
				bins[k]=k*44100/l;
				pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
				if(max<pow[k]){
					max=pow[k];
					ind=k;
				}
			}
			for( k=f1Ind-2;k<f1Ind+3;k++){
				bins[k]=k*44100/l;
				pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
				if(max<pow[k]){
					max=pow[k];
					ind=k;
				}
			}
				for( k=f2Ind-2;k<f2Ind+3;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f3Ind-2;k<f3Ind+3;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f4Ind-2;k<f4Ind+3;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f5Ind-2;k<f5Ind+3;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f6Ind-2;k<f6Ind+3;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f7Ind-2;k<f7Ind+3;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}

				
				for( k=f8Ind-2;k<f8Ind+3;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f9Ind-2;k<f9Ind+3;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f10Ind-2;k<f10Ind+3;k++){
						bins[k]=k*44100/l;
						pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
						if(max<pow[k]){
							max=pow[k];
							ind=k;
						}
					}
					for( k=f11Ind-2;k<f11Ind+3;k++){
						bins[k]=k*44100/l;
						pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
						if(max<pow[k]){
							max=pow[k];
							ind=k;
						}
					}
					for( k=f12Ind-2;k<f12Ind+3;k++){
						bins[k]=k*44100/l;
						pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
						if(max<pow[k]){
							max=pow[k];
							ind=k;
						}
					}
					for( k=f13Ind-2;k<f13Ind+3;k++){
						bins[k]=k*44100/l;
						pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
						if(max<pow[k]){
							max=pow[k];
							ind=k;
						}
					}
					for( k=f14Ind-2;k<f14Ind+3;k++){
						bins[k]=k*44100/l;
						pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
						if(max<pow[k]){
							max=pow[k];
							ind=k;
						}
					}
					for( k=f15Ind-2;k<f15Ind+3;k++){
						bins[k]=k*44100/l;
						pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
						if(max<pow[k]){
							max=pow[k];
							ind=k;
						}
					}
					for( k=f18-1;k<f18+2;k++){
						bins[k]=k*44100/l;
						pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
						if(max<pow[k]){
							max=pow[k];
							ind=k;
						}
					}
	
	double f=ind*44100/l;
	if((i==0) && ((17950<f && f<18050)|| (pow[f18]>100))){// || 
		v=v+shift;
		i--;n-=4;
		continue;
	}
	
	
	
	else if(16850<f && f<16950){
		b[n]=0;b[n+1]=0;b[n+2]=0;b[n+3]=0;
		
	}
	
	else if(16950<f && f<17050){
		b[n]=0;b[n+1]=0;b[n+2]=0;b[n+3]=1;
		
	}
	else if(17050<f && f<17150){
		b[n]=0;b[n+1]=0;b[n+2]=1;b[n+3]=0;
		
	}
	else if(17150<f && f<17250){
		b[n]=0;b[n+1]=0;b[n+2]=1;b[n+3]=1;
		
	} 
	else if(17250<f && f<17350){
		b[n]=0;b[n+1]=1;b[n+2]=0;b[n+3]=0;
		
	}
	else if(17350<f && f<17450){
		b[n]=0;b[n+1]=1;b[n+2]=0;b[n+3]=1;
		
	}
	else if(17450<f && f<17550){
		b[n]=0;b[n+1]=1;b[n+2]=1;b[n+3]=0;
		
	}
	else if(17550<f && f<17650){
		b[n]=0;b[n+1]=1;b[n+2]=1;b[n+3]=1;
		
	}
	else if(17650<f && f<17750){
		b[n]=1;b[n+1]=0;b[n+2]=0;b[n+3]=0;
		
	}
	else if(17750<f && f<17850){
		b[n]=1;b[n+1]=0;b[n+2]=0;b[n+3]=1;
		
	}
	else if(17850<f && f<17950){
		b[n]=1;b[n+1]=0;b[n+2]=1;b[n+3]=0;
		
	}
	else if(18050<f && f<18150){
		b[n]=1;b[n+1]=0;b[n+2]=1;b[n+3]=1;
		
	} 
	else if(18150<f && f<18250){
		b[n]=1;b[n+1]=1;b[n+2]=0;b[n+3]=0;
		
	}
	else if(18250<f && f<18350){
		b[n]=1;b[n+1]=1;b[n+2]=0;b[n+3]=1;
		
	}
	else if(18350<f && f<18450){
		b[n]=1;b[n+1]=1;b[n+2]=1;b[n+3]=0;
		
	}
	else if(18450<f && f<18550){
		b[n]=1;b[n+1]=1;b[n+2]=1;b[n+3]=1;
		
	}
	
	
	else {b[n]=0;b[n+1]=0;b[n+2]=0;b[n+3]=0;}
	
	double inc=0,gap=0;
	int flg1=b[n],flg2=b[n+1],flg3=b[n+2],flg4=b[n+3];
	
	//Decide bits according to energy levels of symbols		
	
	if(pow[f0Ind]-f0>inc){
		flg1=0;flg2=0;flg3=0;flg4=0;
		inc=pow[f0Ind]-f0;
		gap=Math.abs(pow[f0Ind]-max);
	}
	if(pow[f1Ind]-f1>inc){
		flg1=0;flg2=0;flg3=0;flg4=1;
		inc=pow[f1Ind]-f1;
		gap=Math.abs(pow[f1Ind]-max);
	}
	if(pow[f2Ind]-f2>inc){
		flg1=0;flg2=0;flg3=1;flg4=0;
		inc=pow[f2Ind]-f2;
		gap=Math.abs(pow[f2Ind]-max);
	}
	if(pow[f3Ind]-f3>inc){
		flg1=0;flg2=0;flg3=1;flg4=1;
		inc=pow[f3Ind]-f3;
		gap=Math.abs(pow[f3Ind]-max);
	}
	if(pow[f4Ind]-f4>inc){
		flg1=0;flg2=1;flg3=0;flg4=0;
		inc=pow[f4Ind]-f3;
		gap=Math.abs(pow[f4Ind]-max);
	}
	if(pow[f5Ind]-f5>inc){
		flg1=0;flg2=1;flg3=0;flg4=1;
		inc=pow[f5Ind]-f5;
		gap=Math.abs(pow[f5Ind]-max);
	}
	if(pow[f6Ind]-f6>inc){
		flg1=0;flg2=1;flg3=1;flg4=0;
		inc=pow[f6Ind]-f6;
		gap=Math.abs(pow[f6Ind]-max);
	}
	if(pow[f7Ind]-f7>inc){
		flg1=0;flg2=1;flg3=1;flg4=1;
		inc=pow[f7Ind]-f7;
		gap=Math.abs(pow[f7Ind]-max);
	}
	if(pow[f8Ind]-f8>inc){
		flg1=1;flg2=0;flg3=0;flg4=0;
		inc=pow[f8Ind]-f8;
		gap=Math.abs(pow[f8Ind]-max);
	}
	if(pow[f9Ind]-f9>inc){
		flg1=1;flg2=0;flg3=0;flg4=1;
		inc=pow[f9Ind]-f9;
		gap=Math.abs(pow[f9Ind]-max);
	}
	if(pow[f10Ind]-f10>inc){
		flg1=1;flg2=0;flg3=1;flg4=0;
		inc=pow[f10Ind]-f10;
		gap=Math.abs(pow[f10Ind]-max);
	}
	if(pow[f11Ind]-f11>inc){
		flg1=1;flg2=0;flg3=1;flg4=1;
		inc=pow[f11Ind]-f11;
		gap=Math.abs(pow[f11Ind]-max);
	}
	if(pow[f12Ind]-f12>inc){
		flg1=1;flg2=1;flg3=0;flg4=0;
		inc=pow[f12Ind]-f12;
		gap=Math.abs(pow[f12Ind]-max);
	}
	if(pow[f13Ind]-f13>inc){
		flg1=1;flg2=1;flg3=0;flg4=1;
		inc=pow[f13Ind]-f13;
		gap=Math.abs(pow[f13Ind]-max);
	}
	if(pow[f14Ind]-f14>inc){
		flg1=1;flg2=1;flg3=1;flg4=0;
		inc=pow[f14Ind]-f14;
		gap=Math.abs(pow[f14Ind]-max);
	}
	if(pow[f15Ind]-f15>inc){
		flg1=1;flg2=1;flg3=1;flg4=1;
		inc=pow[f15Ind]-f15;
		gap=Math.abs(pow[f15Ind]-max);
	}
	if(gap<inc){	
		
		b[n]=flg1;
		b[n+1]=flg2;
		b[n+2]=flg3;
		b[n+3]=flg4;
	}
	
	/////////////////////////
	f0=pow[f0Ind];
	f1=pow[f1Ind];
	f2=pow[f2Ind];
	f3=pow[f3Ind];
	f4=pow[f4Ind];
	f5=pow[f5Ind];
	f6=pow[f6Ind];
	f7=pow[f7Ind];
	f8=pow[f8Ind];
	f9=pow[f9Ind];
	f10=pow[f10Ind];
	f11=pow[f11Ind];
	f12=pow[f12Ind];
	f13=pow[f13Ind];
	f14=pow[f14Ind];
	f15=pow[f15Ind];
	//////////////////////

	
}
	ndata=(j+1)*4;

	if(Encoding==0){
			hammingDecoder(b);
	}
	
	else if(Encoding==1){
			viterbiDecoder(b);
		
	}
	else//No Encoding
		for(int i=0	;i<databits-1;i++)
			s+=b[i];
}
csvWriter str=new csvWriter();
str.writeStringFile(NewFile+".csv", s);		
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////


public void Nodecode8Freq(short[] dataOnly,int samples,int databits,int datasize,int Encoding,String NewFile){	
	
	
	int l=datasize;
	int v=0;
	int f1Ind;
	int f2Ind;
	int f3Ind;
	int f4Ind;
	int f5Ind;
	int f6Ind;
	int f7Ind;
	int f0Ind;
	int f18;
	int shift=0;
	

	
	f0Ind=790;//17011;//16.494 
	 f1Ind=794;//17097
	 f2Ind=799;//17.205
	 f3Ind=803;//17291    
	 f4Ind=808;//17.398
	 f5Ind=813;//17506
	 f6Ind=817; //17592
	 f7Ind=822;//17700
			 
	 f18=836;//18.001												
	 shift=1536;
	
			
	double f0=0,f1=0,f2=0,f3=0,f4=0,f5=0,f6=0,f7=0;
		 
		 
	double[] real=new double[l];
	double[] imag=new double[l];
	double[] bins=new double[l/2];
	double[] pow=new double[l/2];
	double P,Pr=1000.0;
	int[] b=new int[(databits-1)];
	int ndata=0;
for(int j=0;j<samples;j++){
	 ndata*=l;
	for(int i=0,n=0;i<databits-1 && n<(databits-1);i++,n+=1){
		for(int k=0;k<l;k++){
			real[k]=dataOnly[(i*l+k+v)+ndata];
			imag[k]=0;
		}
		fft(real, imag);
		
		double max=real[0],ind=0;
	
			int k;
			for( k=f0Ind-2;k<f0Ind+3;k++){
				bins[k]=k*44100/l;
				pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
				if(max<pow[k]){
					max=pow[k];
					ind=k;
				}
			}
			for( k=f1Ind-2;k<f1Ind+3;k++){
				bins[k]=k*44100/l;
				pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
				if(max<pow[k]){
					max=pow[k];
					ind=k;
				}
			}
			for( k=f2Ind-2;k<f2Ind+3;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f3Ind-2;k<f3Ind+3;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f4Ind-2;k<f4Ind+3;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f5Ind-2;k<f5Ind+3;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f6Ind-2;k<f6Ind+3;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f7Ind-2;k<f7Ind+3;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
				for( k=f18-1;k<f18+2;k++){
					bins[k]=k*44100/l;
					pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
					if(max<pow[k]){
						max=pow[k];
						ind=k;
					}
				}
		
	double f=ind*44100/l;
	System.out.println(f);
	if((i==0) && ((17950<f && f<18050)|| (pow[f18]>100))){// || 
		v=v+shift;
		i--;n-=1;
		continue;
	}
	
	
	
	else if(16950<f && f<17050){
		b[n]=1;
		
	}
	
	else if(17150<f && f<17250){
		b[n]=2;
		
	}
	else if(17350<f && f<17450){
		b[n]=3;
		
	}
	else if(17550<f && f<17650){
		b[n]=4;
		
	} 
	else if(17750<f && f<17850){
		b[n]=5;
		
	}
	else if(17950<f && f<18050){
		b[n]=6;
		
	}
	else if(18150<f && f<18250){
		b[n]=7;
		
	}
	else if(18350<f && f<18450){
		b[n]=8;
		
	}
	
	else {b[n]=9;}
	
	double inc=0,gap=0;
	int flg1=b[n],flg2=b[n],flg3=b[n];
	//////////////////////////////////
	
	
	if(pow[f0Ind]-f0>inc){
		flg1=1;flg2=0;flg3=0;
		inc=pow[f0Ind]-f0;
		gap=Math.abs(pow[f0Ind]-max);
	}
	if(pow[f1Ind]-f1>inc){
		flg1=2;flg2=0;flg3=1;
		inc=pow[f1Ind]-f1;
		gap=Math.abs(pow[f1Ind]-max);
	}
	if(pow[f2Ind]-f2>inc){
		flg1=3;flg2=1;flg3=0;
		inc=pow[f2Ind]-f2;
		gap=Math.abs(pow[f2Ind]-max);
	}
	if(pow[f3Ind]-f3>inc){
		flg1=4;flg2=1;flg3=1;
		inc=pow[f3Ind]-f3;
		gap=Math.abs(pow[f3Ind]-max);
	}
	if(pow[f4Ind]-f4>inc){
		flg1=5;flg2=0;flg3=0;
		inc=pow[f4Ind]-f3;
		gap=Math.abs(pow[f4Ind]-max);
	}
	if(pow[f5Ind]-f5>inc){
		flg1=6;flg2=0;flg3=1;
		inc=pow[f5Ind]-f5;
		gap=Math.abs(pow[f5Ind]-max);
	}
	if(pow[f6Ind]-f6>inc){
		flg1=7;flg2=1;flg3=0;
		inc=pow[f6Ind]-f6;
		gap=Math.abs(pow[f6Ind]-max);
	}
	if(pow[f7Ind]-f7>inc){
		flg1=8;flg2=1;flg3=1;
		inc=pow[f7Ind]-f7;
		gap=Math.abs(pow[f7Ind]-max);
	}
	if(gap<inc){	
		
		b[n]=flg1;
		//b[n+1]=flg2;
		//b[n+2]=flg3;
	}
	
	
	
	/////////////////////////
	f0=pow[f0Ind];
	f1=pow[f1Ind];
	f2=pow[f2Ind];
	f3=pow[f3Ind];
	f4=pow[f4Ind];
	f5=pow[f5Ind];
	f6=pow[f6Ind];
	f7=pow[f7Ind];
	//////////////////////
	
}
	
	ndata=(j+1)*4;
	int ham[] =new int [24];
	if(Encoding==0){
			hammingDecoder(b);
	}
	
	else if(Encoding==1){
		System.arraycopy(b, 0, ham, 0, ham.length);
		viterbiDecoder(ham);
		
	}
	else//No Encoding
		for(int i=0	;i<b.length;i++)
			s+=b[i];
}
csvWriter str=new csvWriter();
str.writeStringFile(NewFile+".csv", s);		
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
public void noDecodeFSK(short[] dataOnly,int samples,int databits){	
		
		int l=2048;
		double[] real=new double[l];
		double[] imag=new double[l];
		double[] bins=new double[l/2];
		double[] pow=new double[l/2];
		double P,Pr=1000.0;
		int[] b=new int[2*databits];
		int ndata=0;
	for(int j=0;j<samples;j++){
		 ndata*=l;
		for(int i=0,n=0;i<databits && n<2*databits;i++,n+=2){
			for(int k=0;k<l;k++){
				real[k]=dataOnly[(i*l+k)+ndata];
				imag[k]=0;
			}
			fft(real, imag);
			
			double max=real[0],ind=0;
			for(int k=600;k<l/2;k++){
				bins[k]=k*44100/l;
				pow[k]=Math.sqrt((real[k]*real[k])+(imag[k]*imag[k]));
				if(max<pow[k]){
					max=pow[k];
					ind=k;
			}
		}
		System.out.print(max+" , "+ ind+" , ");
		double f=ind*44100/l;
		System.out.println(f);
	
		if((i==0)&&(17950<f && f<18050)){b[n]=2;b[n+1]=2;}
		else if(16400<f && f<16600){b[n]=0;b[n+1]=0;}
		else if(16900<f && f<17100){b[n]=0;b[n+1]=1;}
		else if(17400<f && f<17600){b[n]=1;b[n+1]=1;}
		else if(18400<f && f<18600){b[n]=1;b[n+1]=0;} 
		else {b[n]=0;b[n+1]=0;}
		
	}
		int[] bits=new int[8];   //change according to encoding scheme
		if(b[0]==2 && (b[2]==1 || b[2]==0)){     //////////////////////////change loops also
			for(int i=2;i<10;i++){
				bits[i-2]=b[i];
			}
			ndata=(j+1)*4+1;
		}
		else{
			for(int i=0;i<8;i++){
				bits[i]=b[i];
			}
			ndata=(j+1)*4;
		}
		
		for(int i=0;i<8;i++){
			s+=bits[i];
		}
		
		//hammingDecoder(bits);
		
		//viterbiDecoder(bits);

	}

	csvWriter str=new csvWriter();
	str.writeStringFile("./noEncoding/R4FSK-8bit-16KSS-4096.csv", s);		
}
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void hammingDecoder(int[] bits){
		
		int[] p=new int[3];
		int[] data=new int[4];
		p[0]=bits[1];
		p[1]=bits[2];
		p[2]=bits[4];
		data[0]=bits[0];
		data[1]=bits[3];
		data[2]=bits[5];
		data[3]=bits[6];
		int P1=data[0]^data[2]^data[3];
		int P2=data[0]^data[1]^data[3];
		int P3=data[0]^data[1]^data[2];
		if(P1==p[0] && P2==p[1] && P3==p[2]){
			//System.out.println("No Error! \n Data is:");
		}
		else if(P1!=p[0] && P2!=p[1] && P3!=p[2]){
			//System.out.println("Error in bit 0 \n After correction:");
			if(data[0]==1)
				data[0]=0;
			else
				data[0]=1;
			
		}
		else if(P1!=p[0] && P2!=p[1] && P3==p[2]){
			//System.out.println("Error in bit 3 \n After correction:");
			if(data[3]==1)
				data[3]=0;
			else
				data[3]=1;		
			
		}
		else if(P1!=p[0] && P2==p[1] && P3!=p[2]){
			//System.out.println("Error in bit 2 \n After correction:");
			if(data[2]==1)
				data[2]=0;
			else
				data[2]=1;
			
			
		}
		else if(P1==p[0] && P2!=p[1] && P3!=p[2]){
			//System.out.println("Error in bit 1 \n After correction:");
			if(data[1]==1)
				data[1]=0;
			else
				data[1]=1;
		}
		else
			System.out.println("Error detected, can't be corrected");
		
		for(int k=0;k<data.length;k++){
			s=s+data[k];
		}
		
	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
public void viterbiDecoder(int[] gen){
	
		int current=0;
		int[] d=new int[gen.length/2];
		List<Integer> arrl= new ArrayList<Integer>();
		arrl.add(0);
		int[] arr={0,0,0,0};
		int[] s0={0,0,1,1};
		int[] s1={2,2,3,3};
		int[] o0={00,11,10,01};
		int[] o1={11,00,01,10};
		int[][] trace=new int[4][d.length];
		
		int[] states={0,1,2,3};
		for(int n=0;n<gen.length;n=n+2)
		{
			int[] comp={arr[0],arr[1],arr[2],arr[3]};
			int match=gen[n];
			match=match*10+gen[n+1];
			int[] flag={0,0,0,0};
			int size=arrl.size();
			
			for(int i=0;i<size;i++){
				current=arrl.get(0);
				arrl.remove(0);
			int x=o0[current]^match;
			int y=o1[current]^match;
			if(x==10) x=1;
			if(x==11) x=2;
			if(y==10) y=1;
			if(y==11) y=2;
			if(flag[s0[current]]==1) {if(comp[current]+x<arr[s0[current]])
			{arr[s0[current]]=comp[current]+x;
			trace[s0[current]][n/2]=current;
			}}
			else
			{	
				flag[s0[current]]=1;
				arr[s0[current]]=comp[current]+x;
				trace[s0[current]][n/2]=current;
				arrl.add(s0[current]);
			}
			if(flag[s1[current]]==1) {if(comp[current]+y<arr[s1[current]])
			{arr[s1[current]]=comp[current]+y;
			trace[s1[current]][n/2]=current;
			}
			}
			else
			{	
				flag[s1[current]]=1;
				arr[s1[current]]=comp[current]+y;
				trace[s1[current]][n/2]=current;
				arrl.add(s1[current]);
			}
			}
		}
		int min=arr[0];int minpos=0;
		int min1=arr[0];int minpos1=0;
		for(int i=0;i<arr.length;i++){
			if(arr[i]<min) {
				min=arr[i];
				minpos=i;
				}
			}
		for(int i=0;i<arr.length;i++){
			if(arr[i]<=min1) {
				min1=arr[i];
				minpos1=i;
				}
			}
		for(int i=gen.length/2-1;i>=0;i--)
		{
			if(s0[trace[minpos][i]]==minpos) {
				d[i]=0;
			}
			else{
				d[i]=1;
			}
				
			
			minpos=trace[minpos][i];
		}
		
		for(int k=0;k<d.length;k++){
			s=s+d[k];
		}
	}
	
	
	
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	
	public void fft(double[] real, double[] imag){
		if (real.length != imag.length)
			throw new IllegalArgumentException("Mismatched lengths");
		int N= real.length;
		int levels = 31-Integer.numberOfLeadingZeros(N);
		if (1 << levels != N)
			throw new IllegalArgumentException("Length is not a power of 2");
		double[] cosValues = new double[N/2];
		double[] sinValues = new double[N/2];
		for (int i=0;i<N/2;i++){
			cosValues[i]=Math.cos(2*Math.PI*i/N);
			sinValues[i]=Math.sin(2*Math.PI*i/N);
		}
		
		for(int i=0;i<N;i++){
			int j = Integer.reverse(i) >>> (32-levels);
			if(j>i){
				double tmp=real[i];
				real[i]=real[j];
				real[j]=tmp;
				tmp=imag[i];
				imag[i]=imag[j];
				imag[j]=tmp;
				
			}
		}
		
		for(int size = 2; size<=N; size*=2){
			int halfsize=size/2;
			int step= N/size;
			for(int i=0;i<N;i+=size){
				for(int j=i, k=0;j<i+halfsize;j++,k+=step){
					double tmp1=real[j+halfsize]*cosValues[k]+imag[j+halfsize]*sinValues[k];
					double tmp2=-real[j+halfsize]*sinValues[k]+imag[j+halfsize]*cosValues[k];
					real[j+halfsize]=real[j]-tmp1;
					imag[j+halfsize]=imag[j]-tmp2;
					real[j]+=tmp1;
					imag[j]+=tmp2;
				}
			}
			if(size==N)break;
		}
		
	}
	
	
	
}

