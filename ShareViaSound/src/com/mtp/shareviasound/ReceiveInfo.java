package com.mtp.shareviasound;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class ReceiveInfo {
	private static final int RECORDER_SAMPLERATE = 44100;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private AudioRecord mRecorder = null;
	public static boolean isRecording = true;
	int C =2*44100;
	private Thread listener = null;
	private Thread processor = null;
	BlockingQueue<Short> items;
	csvWriter wt=new csvWriter();
	int read=0;
	int BUF_SIZE = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

	
	private void processAudio(){
		int fl=1;
		while(fl==1){
			try{
				fl=0;
		    	getData();
		    	
		    }catch(Exception e){
		    	fl=1;
		    }
		 }
	}
	   
	   
	   
	private void writeToBuffer(short[] in,int re) {
		try{  
			for(int i = 0; i < BUF_SIZE; i++)
					 items.put(in[i]);
		}catch(Exception e){}
	}
	   

	public void listen(){
		final short[] in = new short[BUF_SIZE];
		mRecorder = new AudioRecord(
            MediaRecorder.AudioSource.MIC,
            44100,
            AudioFormat.CHANNEL_IN_MONO, 
            AudioFormat.ENCODING_PCM_16BIT, 
            2*BUF_SIZE
            );

		mRecorder.startRecording();
    
		while(isRecording){
			try{    
				mRecorder.read(in,0,BUF_SIZE); // read from device
				writeToBuffer(in,read);
			}catch(Exception t){
				isRecording= false;
			}
		}
	}
		
	public void startRecording() {
		isRecording=true;
		items = new LinkedBlockingQueue<Short>();
		listener = new Thread(
			new Runnable(){
				public void run(){
					listen();
					return;   		
				}
		    });
		listener.start();
		
		
		processor = new Thread(
			new Runnable(){
				public void run(){
					processAudio();
					return;
		        }
		    });
		processor.start();
	}

	public void stopRecording(){
		isRecording = false;
		listener=null;
		processor=null;
		mRecorder = null;
	}
		
		
	private void getData(){
			int pr=14700/4;
			int SSsize=4096,datasize=2048;
			int Encoding=1;//0-hamming,1-viterbi,2-No Encoding
			int samples=1;
			int databits;
			if(Encoding==0)
				databits=29;
			else if(Encoding==1)
				databits=20;
			else
				databits=17;
			
			
			String NewFile="R-"+"LL-"+String.valueOf(SSsize)+"-"+String.valueOf(datasize)+"-"+String.valueOf(samples*(databits))+"bits";
			short[] data;
			int peakInd=0;
			short[] Fildata=new short[C];
			double[] Ddata=new double[C+pr];
			data = new short[C+pr];
			int arr=pr;
			int kya=databits;
			int i=arr;
			int extra=0;
			int ex_pos=0;
			short[] dataOnly=new short[C];
			int lenx=Ddata.length;
			int leny=pr;
			double[] check1=new double[leny];
			double[] check2=new double[leny];
			
			int[] store={1,1,1,0,0,0,1,0,0,1,0};  
			double rate=44100;
			int vol=100,freq=16000;
			int[] prp=new int[store.length];
			
			System.arraycopy(store, 0, prp, 0, store.length);
			int bitlen1=pr/store.length;
			double[] buf1=new double[pr];
			int c1=0;int j1=0;
			while(c1<prp.length){
				if(prp[c1]==0)
					vol=0;
				else
					vol=100;
				c1++;
				for(int k=0;k<bitlen1;k++){
					double angle = ((k/(rate))*freq*2.0*Math.PI);
					if(k<bitlen1/2)
						buf1[j1]=(double)(Math.sin(angle)*vol*2*k/bitlen1);
					else	
						buf1[j1]=(short)(Math.sin(angle)*vol*2*(bitlen1-k)/bitlen1);
					j1++;
				}
			}	 

			System.arraycopy(buf1, 0, check2, 0, leny);
			
			
			
			while (true) {
				
				short[] tdata = new short[C-extra];
				try{ 
					
					for(int j=0;j<C-extra;j++){
						if(isRecording)
							tdata[j]=items.take();
						else	
							return;
					}
				}catch(Exception e){}
	
				System.arraycopy(tdata, C-extra-pr, data, 0, pr);
				System.arraycopy(dataOnly, ex_pos, data, pr, extra);
				System.arraycopy(tdata, 0, data, pr+extra, C-extra);
				System.arraycopy(data, pr, Fildata, 0, C);
			  	 
//High pass filter at 15KHz				
				Filter filter = new Filter(15000,44100, Filter.PassType.Highpass,1);
				for (int k = 0; k < C; k++){
					filter.Update(Fildata[k]);
					Ddata[arr+k]=filter.getValue();
				}
					       			
//Correlation and peak detection
				int flag=0;
				double max2=0;
				double res=0;
				double[] col = new double[lenx-leny+1];
				double sum=0,sumx=0,sumy=0;
				int lpind1=0,lpind2=0;
				
				System.arraycopy(Ddata, i, check1, 0, leny);
				
				for(int j=0;j<check1.length;j++){
					sum+=check1[j]*check2[j];
					sumx+=Math.pow(check1[j],2);
					sumy+=Math.pow(check2[j],2);
				}
				
				if(Math.sqrt((sumx)*(sumy))==0)
					res=0.01;
				else
					res=sum/Math.sqrt((sumx)*(sumy));
				if(res>max2)
					max2=res;
				
				col[0]=res;
				peakInd=i;
				int a=1,jump;
				if(col[0]>0.2)
					jump=1;
				else jump=60;
				
				i=i+jump;
				for(;i<(lenx-leny+1);i+=jump){
					res=0;
					sum=0;
					for(int j=0;j<pr;j++){
						sum+=Ddata[i+j]*check2[j];
					}	
					for(int k=1;k<=jump;k++){
						sumx=sumx-Math.pow(Ddata[i-k],2)+Math.pow(Ddata[i+leny-k],2);
					}
					if(Math.sqrt((sumx)*(sumy))==0)
						res=0;
					else
						res=sum/Math.sqrt((sumx)*(sumy));
					if(res==0)col[a]=col[a-1];
					else col[a]=res;
					if(Double.isNaN(col[a]))
						col[a]=col[a-1];
					
					lpind1=i;
					if(col[a]>max2) {
						max2=col[a];
						lpind2=i;
					}
					if(max2>0.2)
						jump=1;
						
					if(max2>0.35 && (lpind1-lpind2)>3675){
						peakInd=lpind2;
						flag=1;
						break;
					}
					a++;
				}
				if(flag!=1 && max2>=0.5){
					flag=1;
					peakInd=lpind2;
				}
				int sh=0;
//FFT Decoding 
				if(flag==1){
					flag=0;
					FFT_Decoder d=new FFT_Decoder();
					int l=Ddata.length-peakInd-pr;
					short[] startSymbol=new short[SSsize];
					int strPos=(int)peakInd+pr;
					if(l>=SSsize){
						System.arraycopy(data, strPos, startSymbol, 0, SSsize);
						double ind=d.decodeSS(startSymbol,SSsize);
						double f=ind*44100/SSsize;
						if(f<18010 && f>17995){
							int srcPos=(int)peakInd+pr+SSsize;
							l=kya*datasize;
							if(data.length-srcPos>=l){
								System.arraycopy(data, srcPos, dataOnly, 0, data.length-srcPos);
								//call decoder
								
								try{
									sh=d.decode8FSK(dataOnly,samples,databits,datasize,Encoding,NewFile);
								}catch(ArrayIndexOutOfBoundsException e){
								}catch(Exception e){}
								
								ex_pos=sh*1536+(kya-1)*datasize;
								extra=data.length-(srcPos+ex_pos);
								if(extra<0){extra=0;ex_pos=0;}
								for (int k = 0; k < pr; k++){
									filter.Update((float)dataOnly[ex_pos-pr+k]);
									Ddata[k]=filter.getValue();
								}
								i=0;
							}
							else{
								System.arraycopy(data, srcPos, dataOnly, 0, Ddata.length-srcPos);
								int newC=C-(Ddata.length-srcPos);
								short[] fftdata=new short[newC];
								
								try{ 
									for(int j=0;j<newC;j++){
										if(isRecording)
											fftdata[j]=items.take();
										else
											return;
									}
								}catch(Exception e){}
								
								System.arraycopy(fftdata, 0, dataOnly, Ddata.length-srcPos,fftdata.length);
							//call decoder
								try{
									sh=d.decode8FSK(dataOnly,samples,databits,datasize,Encoding,NewFile);
								}catch(ArrayIndexOutOfBoundsException e){
								}catch(Exception e){}
								ex_pos=sh*1536+(kya-1)*datasize;
								extra=dataOnly.length-ex_pos;
								if(extra<0){extra=0;ex_pos=0;}
								for (int k = 0; k < pr; k++){
									filter.Update((float)dataOnly[ex_pos-pr+k]);
									Ddata[k]=filter.getValue();
								}
								i=0;
							}								
						}
						else{
							int srcPos=(int)peakInd+pr+SSsize;
							l=kya*datasize;
							if(data.length-srcPos>=l){
								System.arraycopy(data, srcPos, dataOnly, 0, data.length-srcPos);
								//call decoder
								try{
									sh=d.decode8FSK(dataOnly,samples,databits,datasize,Encoding,NewFile);
								}catch(ArrayIndexOutOfBoundsException e){
								}catch(Exception e){}
								
								ex_pos=sh*1536+(kya-1)*datasize;
								extra=data.length-(srcPos+ex_pos);
								if(extra<0){extra=0;ex_pos=0;}
								for (int k = 0; k < pr; k++){
									filter.Update((float)dataOnly[ex_pos-pr+k]);
									Ddata[k]=filter.getValue();
								}
								i=0;
							}
							else{
								System.arraycopy(data, srcPos, dataOnly, 0, Ddata.length-srcPos);
								int newC=C-(Ddata.length-srcPos);
								short[] fftdata=new short[newC];
								try{ 
									for(int j=0;j<newC;j++){
										if(isRecording)
											fftdata[j]=items.take();
										else
											return;
									}
								}catch(Exception e){}
								System.arraycopy(fftdata, 0, dataOnly, Ddata.length-srcPos,fftdata.length);
								//call decoder
								try{
									sh=d.decode8FSK(dataOnly,samples,databits,datasize,Encoding,NewFile);
								}catch(ArrayIndexOutOfBoundsException e){
								}catch(Exception e){}
								ex_pos=sh*1536+(kya-1)*datasize;
								extra=dataOnly.length-ex_pos;
								if(extra<0){extra=0;ex_pos=0;}
								for (int k = 0; k < pr; k++){
									filter.Update((float)dataOnly[ex_pos-pr+k]);
									Ddata[k]=filter.getValue();
								}
								i=0;
							}								
						}
					}
					else{
						System.arraycopy(data, strPos, dataOnly, 0, l);
						int newC=C-l;
						short[] fftdata=new short[newC];
						try{ 
							for(int j=0;j<newC;j++){
								if(isRecording)
									fftdata[j]=items.take();
								else
									return;
							}
						}catch(Exception e){}
						System.arraycopy(fftdata, 0, dataOnly, l, C-l);
						System.arraycopy(dataOnly, 0, startSymbol, 0, SSsize);
						double ind=d.decodeSS(startSymbol,SSsize);
						double f=ind*44100/SSsize;
						if(f<18010 && f>17995){
							int srcPos=SSsize;
							l=databits*datasize;
							short[] dataDecoder=new short[C-SSsize];
							System.arraycopy(dataOnly, srcPos, dataDecoder, 0, C-srcPos);
							//call decoder
							try{
								sh=d.decode8FSK(dataDecoder,samples,databits,datasize,Encoding,NewFile);
							}catch(ArrayIndexOutOfBoundsException e){
							}catch(Exception e){}
							
							ex_pos=srcPos+sh*1536+(kya-1)*datasize;
							extra=dataOnly.length-ex_pos;
							if(extra<0){extra=0;ex_pos=0;}
							for (int k = 0; k < pr; k++){
								filter.Update((float)dataOnly[ex_pos-pr+k]);
								Ddata[k]=filter.getValue();
							}
							i=0;
						}
						else{
							int srcPos=SSsize;
							l=databits*datasize;
							short[] dataDecoder=new short[C-SSsize];
							System.arraycopy(dataOnly, srcPos, dataDecoder, 0, C-srcPos);
							//call decoder
							try{
								sh=d.decode8FSK(dataDecoder,samples,databits,datasize,Encoding,NewFile);
							}catch(ArrayIndexOutOfBoundsException e){
							}catch(Exception e){}
							
							ex_pos=srcPos+sh*1536+(kya-1)*datasize;
							extra=dataOnly.length-ex_pos;
							if(extra<0){extra=0;ex_pos=0;}
							for (int k = 0; k < pr; k++){
								filter.Update((float)dataOnly[ex_pos-pr+k]);
								Ddata[k]=filter.getValue();
							}
							i=0;	
						}   
					}
				}
				else{
					System.arraycopy(Ddata, C, Ddata, 0, pr);
					i=0;
				}
			}		
	}
	
}
