package com.mtp.shareviasound;

import java.util.Random;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;

public class audioPlay {
  	final Handler handler=new Handler();
	final Random randomGenerator = new Random();
	final double lambda=0.45;
	 

	
	float rate=44100;
	int len=2048;
	int amplitude=25000;
	int freq;
	int vol=amplitude;
	final int sampleRate = 44100;
	int bitlen=len;
	short[] buf=new short[bitlen];
	int[] store={1,1,1,0,0,0,1,0,0,1,0}; 
	int[] pr=new int[store.length];
	int bitlen1=(int)rate/(store.length*3*4);
	int d=(int)Math.ceil((double)56/3);
	final AudioTrack audioTrack3 = new AudioTrack(AudioManager.STREAM_MUSIC,
			sampleRate, AudioFormat.CHANNEL_OUT_MONO,
			AudioFormat.ENCODING_PCM_16BIT,2*(bitlen1*pr.length+4096+bitlen*d),//+4096+bitlen*d.length/2),
			AudioTrack.MODE_STREAM);
	short[] ubuf1=new short[bitlen1*pr.length+4096+bitlen*d];
	short[] ubuf2=new short[bitlen1*pr.length+4096+bitlen*d];
	boolean stop=true;
	int cellID,rssi;
	 
	int var1=1058;int var2;
	 
/*generating 8 FSK modulation and send to channel of bits */
	 
	public void Play(){
		stop=true;
		System.arraycopy(store, 0, pr, 0, store.length);
		for(int k=0;k<ubuf2.length;k++){
			double angle = ((k/(rate))*18500*2.0*Math.PI);
			ubuf2[k]=(short)(Math.sin(angle)*vol);
		}
		audioTrack3.play();
		final Runnable r=new Runnable(){
			public void run(){
				if(!stop){
					handler.removeCallbacks(this);
				//	MainActivity.txView.setText("Stopped");
					return;
				}
				
				final	int a[]=new int[20];String s="";
				int c1=0;int j1=0;
				double x=Math.log(randomGenerator.nextDouble())/(-lambda);
				int interval=(int)(x*1058);
				var2=interval;
				for (int idx = 0; idx < a.length; ++idx){
					int randomInt = randomGenerator.nextInt(2);
		  				a[idx]=randomInt;s=s+a[idx];
		  				System.out.println(a[idx]);
				}
				int gen[]=CRC_At_Sender.genCRC(a);
	                	
				while(c1<pr.length){
					if(pr[c1]==0)
						vol=0;
					else
						vol=amplitude;
					
					for(int k=0;k<bitlen1;k++){
						double angle = ((k/(rate))*16000*2.0*Math.PI);
						if(k<bitlen1/2)
							ubuf1[c1*bitlen1+k]=(short)(Math.sin(angle)*vol*2*k/bitlen1);
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
				while(c+2<gen.length)
				{
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
							buf[k]=(short)(Math.sin(angle)*vol*k*2/bitlen);
						else
							buf[k]=(short)(Math.sin(angle)*vol*(bitlen-k)*2/bitlen);
						ubuf1[j]=buf[k];
						j++;
					}
					c+=2;
				}
		           
				int findex1=(int)Math.ceil((double)var1*44.1);
				int findex2=(int)Math.ceil((double)var2*44.1);
				if(var1>=1058 && var2>=1058){
					audioTrack3.write(ubuf1, 0,ubuf1.length);
				}
				if(var1>=1058 && var2<1058){
					System.arraycopy(ubuf2, 0, ubuf1, ubuf1.length-findex2, findex2);
					audioTrack3.write(ubuf1, 0,ubuf1.length);
				}
				if(var1<1058 && var2>=1058){
					audioTrack3.write(ubuf1, ubuf1.length-findex1,findex1);
				}
				if(var1<1058 && var2<1058){
					System.arraycopy(ubuf2, 0, ubuf1, ubuf1.length-findex2, findex2);
					audioTrack3.write(ubuf1, ubuf1.length-findex1,findex1);
				}
				var1=var2;
		        
				//audioTrack3.write(ubuf1, 0,ubuf1.length);
		 
				handler.postDelayed(this,2000);  
			}
		};
		handler.post(r);
	}
	public void stopPlay(){
		stop=false;
	}
}
