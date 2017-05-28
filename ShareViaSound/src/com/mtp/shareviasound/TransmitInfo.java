package com.mtp.shareviasound;

import java.util.Random;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;

public class TransmitInfo {
	final Handler handler=new Handler();
	final Random randomGenerator = new Random();
	final double lambda=0.35;
	 

	float rate=44100; //sampling rate
	int len=2048; // #samples per bit
	int amplitude=25000;  // for mobile phones
	int freq;
	int vol=amplitude;
	final int sampleRate = 44100;
	int bitlen=len;
	short[] buf=new short[bitlen]; // to represent one bit
	int[] store={1,1,1,0,0,0,1,0,0,1,0}; // stored preamble pattern
	int[] pr=new int[store.length];
	int bitlen1=(int)rate/(store.length*3*4); // # samples per bit in preamble (decided by experiments) ~= 334 samples 
	int d=(int)Math.ceil((double)56/3); // # data bits per frame
	final AudioTrack audioTrack3 = new AudioTrack(AudioManager.STREAM_MUSIC,
			sampleRate, AudioFormat.CHANNEL_OUT_MONO,
			AudioFormat.ENCODING_PCM_16BIT,2*(bitlen1*pr.length+4096+bitlen*d),//bufer size...+4096+bitlen*d.length/2),
			AudioTrack.MODE_STREAM);
	short[] ubuf1=new short[bitlen1*pr.length+4096+bitlen*d];//frame length
	short[] ubuf2=new short[bitlen1*pr.length+4096+bitlen*d]; // not in use
	boolean Fframe=true;
	int randomInt;
	int cellID,rssi;
	String operator=MainActivity.operator; 
	int[] opID=new int[4];
	boolean stop=true; csvWriter obj3=new csvWriter();
	
	/*generating 8 FSK modulation and send to channel of bits */
	
	public void Play(){
		stop=true;
		System.arraycopy(store, 0, pr, 0, store.length);
		for(int k=0;k<ubuf2.length;k++){ // it was used for experiment purpose, not in use now.... 
			double angle = ((k/(rate))*18500*2.0*Math.PI);
			ubuf2[k]=(short)(Math.sin(angle)*vol);
		}
		audioTrack3.play();
		
		final Runnable r=new Runnable(){
			public void run(){
			
				if(!stop){
					handler.removeCallbacks(this);
					//MainActivity.txView.setText("Stopped");
					return;
				}
				cellID = MainActivity.cellID;
				rssi=MainActivity.rssi;
				if(rssi==99)	{rssi=32;}
				String S1=Integer.toBinaryString(cellID); // s1=cell id (16 bit)
       			String S2=Integer.toBinaryString(rssi); // s2=rssi (6 bits)
       			int x;
       			int interval;
       			final int a[]=new int[20];String s="";
       			int c1=0;int j1=0;
       			if(Fframe==true){
       				x=1;
       				interval=(int)(x*1300); // 1300ms in between 2 consicutive frames
       				randomInt = randomGenerator.nextInt(15); // to generate random sequence number
       				String S3=Integer.toBinaryString(randomInt); // S3 is sequence number
       				for(int idx=4-(S3.length());idx<4;++idx){
       					a[idx]=S3.charAt(idx+S3.length()-4)-'0'; // a is an array to represent first frame bits
       				}
       				for (int idx = 4+16-(S1.length()); idx <a.length; ++idx){
       					a[idx]=S1.charAt(idx-20+S1.length())-'0'; // add sequence number first in array and then add cellid
       				}
	             
       			}
       			else{
       				x=3; // 3*1300ms in between different frames
       				interval=(int)(x*1300);
       				operator=MainActivity.operator;
       				if(operator.equalsIgnoreCase("Dolphin")) {
       					opID[0]=0;opID[1]=0;opID[2]=0;opID[3]=1;
       				}
       				else if(operator.equalsIgnoreCase("VODAFONE IN")) {
       					opID[0]=0;opID[1]=0;opID[2]=1;opID[3]=0;
       				}
       				else if(operator.equalsIgnoreCase("idea")){
       					opID[0]=0;opID[1]=0;opID[2]=1;opID[3]=1;
       				}
       				else{
       					opID[0]=0;opID[1]=0;opID[2]=0;opID[3]=0;
       				}
	    			 
	       		//	 obj3.writeStringFile("/sdcard/generated.csv",MainActivity.operator);
	    			
       				String S3=Integer.toBinaryString(randomInt);
       				for(int idx=4-(S3.length());idx<4;++idx){
       					a[idx]=S3.charAt(idx+S3.length()-4)-'0';
       				}
       				for (int idx =4+6-(S2.length()); idx<10;++idx){
       					a[idx]=S2.charAt(idx-10+S2.length())-'0';
       				}
       				for(int idx=10;idx < 10+opID.length; ++idx){
       					a[idx]=opID[idx-10];
       				}
       			}
       			for(int idx=0;idx < a.length; ++idx)
       				s=s+a[idx];
	            
       		//	obj3.writeStringFile("/sdcard/generated.csv",s);
       			
       			int gen[]=CRC_At_Sender.genCRC(a); // it will generate CRC for array a and then append it
       			
       			while(c1<pr.length){
       				if(pr[c1]==0)
       					vol=0;
       				else
       					vol=amplitude;
       				
       				for(int k=0;k<bitlen1;k++){
       					double angle = ((k/(rate))*16000*2.0*Math.PI);
       					// first increase vol till half length and then decrease in second half, to avoid glitch sound
					if(k<bitlen1/2)
       						ubuf1[c1*bitlen1+k]=(short)(Math.sin(angle)*vol*2*k/bitlen1);//buf1[k];
       					else	
       						ubuf1[c1*bitlen1+k]=(short)(Math.sin(angle)*vol*2*(bitlen1-k)/bitlen1);
       				}
       				c1++;
       			}
       			int j=j1;
       			vol=amplitude;
       			for(int k=0;k<4096;k++){
       				double angle = ((k/(rate))*18000*2.0*Math.PI);
       				if(k<4096/2) 
       					ubuf1[c1*bitlen1+k]=(short)(Math.sin(angle)*vol*k*2/4096);
       				else  
       					ubuf1[c1*bitlen1+k]=(short)(Math.sin(angle)*vol*2*(4096-k)/4096);
       				j++;
       			}  
       			int c=0;
       			j=pr.length*bitlen1+4096;
			// create frequency samples according to array bits in gen (a+CRC)
       			while(c+2<gen.length){
       				if(gen[c]==0 && gen[c+1]==0 && gen[c+2]==0)
       					freq=16494;
       				else if(gen[c]==0 && gen[c+1]==0 && gen[c+2]==1)
       					freq=17250;
       				else if(gen[c]==0 && gen[c+1]==1 && gen[c+2]==0)
       					freq=17750;
       				else if(gen[c]==0 && gen[c+1]==1 && gen[c+2]==1)
       					freq=18497;
       				else if(gen[c]==1 && gen[c+1]==0 && gen[c+2]==0)
       					freq=18250;
       				else if(gen[c]==1 && gen[c+1]==0 && gen[c+2]==1)
       					freq=16250;
       				else if(gen[c]==1 && gen[c+1]==1 && gen[c+2]==0)
       					freq=17506;
       				else
       					freq=17011;
       				for(int k=0;k<bitlen;k++){
       					double angle = ((k/(rate))*freq*2.0*Math.PI);
       					if(k<bitlen/2)
       						buf[k]=(short)(Math.sin(angle)*vol*k*2/bitlen);//buf1[k];
       					else
       						buf[k]=(short)(Math.sin(angle)*vol*(bitlen-k)*2/bitlen);
       					ubuf1[j]=buf[k];
       					
       					j++;
       				}
       				c=c+3;
       			}
       			if(c+1==gen.length){ 
       				if(gen[c]==0){
       					freq=16494;
       				}
       				else{
       					freq=17506;
       				}
       				for(int k=0;k<bitlen;k++){
       					double angle = ((k/(rate))*freq*2.0*Math.PI);
       					if(k<bitlen/2)
       						buf[k]=(short)(Math.sin(angle)*vol*k*2/bitlen);//buf1[k];
       					else
       						buf[k]=(short)(Math.sin(angle)*vol*(bitlen-k)*2/bitlen);
       					ubuf1[j]=buf[k];
       					j++;
       				}
       			}
       			if(c+2==gen.length){
       				if(gen[c]==0 && gen[c+1]==0)
       					freq=16494;
       				else if(gen[c]==0 && gen[c+1]==1)
       					freq=17750;
       				else if(gen[c]==1 && gen[c+1]==0)
       					freq=18250;
       				else
       					freq=17506;
       				for(int k=0;k<bitlen;k++){
       					double angle = ((k/(rate))*freq*2.0*Math.PI);
       					if(k<bitlen/2)
       						buf[k]=(short)(Math.sin(angle)*vol*k*2/bitlen);//buf1[k];
       					else
       						buf[k]=(short)(Math.sin(angle)*vol*(bitlen-k)*2/bitlen);
       					ubuf1[j]=buf[k];
       					j++;
       				}
       				c+=2;
       			}
       			
       			audioTrack3.write(ubuf1, 0,ubuf1.length);
       			Fframe=!(Fframe);
       			handler.postDelayed(this,interval);  
			}
       		 };
       		 handler.post(r);
	}
	public void stopPlay(){
		stop=false;
	}
}
