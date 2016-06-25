package com.mtp.shareviasound;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;


public class MainActivity extends Activity{

	public TelephonyManager tm;
    public MyPhoneStateListener MyListener;
    public static int cellID = 0, rssi = 0;
    public static String Iframe="";
	public static String operator="";
	public static TextView txView,txView1,txView2,txView3,txView4,txView5,txView6 ;
	
	Handler handler = new Handler();
	boolean isRecording=false;
	boolean isPlaying=false;
	audioPlay AP=new audioPlay();
	TransmitInfo gsm=new TransmitInfo();
	ReceiveInfo rcvInfo=new ReceiveInfo();
	int btnFlag=0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
			setContentView(R.layout.activity_main);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			txView= (TextView) findViewById(R.id.textView0);
			txView1= (TextView) findViewById(R.id.textView1);
			txView2= (TextView) findViewById(R.id.textView2);
			txView3= (TextView) findViewById(R.id.textView3);
			txView4= (TextView) findViewById(R.id.textView4);
			txView5= (TextView) findViewById(R.id.textView5);
			txView6= (TextView) findViewById(R.id.textView6);
			setButtonHandlers();
			enableButtons(isRecording);
	    
			tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	        MyListener = new MyPhoneStateListener();
	        tm.listen(MyListener, MyPhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	        tm.listen(MyListener, MyPhoneStateListener.LISTEN_CELL_LOCATION);
	        
	}
	private void setButtonHandlers() {
			((Button) findViewById(R.id.btnStart)).setOnClickListener(btnClick);
			((Button) findViewById(R.id.btnStop)).setOnClickListener(btnClick);
		    ((Button) findViewById(R.id.btnPlay)).setOnClickListener(btnClick);
		//    ((Button) findViewById(R.id.btnStPlay)).setOnClickListener(btnClick);
	}
	private void enableButton(int id, boolean isEnable) {
		    ((Button) findViewById(id)).setEnabled(isEnable);
	}
	public void enableButtons(boolean isRecording) {
		    enableButton(R.id.btnStart,!isRecording);
		    enableButton(R.id.btnStop, isRecording);
		    enableButton(R.id.btnPlay, !isRecording);
		//    enableButton(R.id.btnStPlay, isRecording);
	}
	private void enablePlayButton(boolean isPlaying) {
		    enableButton(R.id.btnStart,!isPlaying);
		    enableButton(R.id.btnStop, isPlaying);
		    enableButton(R.id.btnPlay, !isPlaying);
	///	    enableButton(R.id.btnStPlay, isPlaying);
	}
	private void enableRecordButton(boolean isRecording) {
		    enableButton(R.id.btnStart,!isRecording);
		    enableButton(R.id.btnStop, isRecording);
		    enableButton(R.id.btnPlay, !isRecording);
		//    enableButton(R.id.btnStPlay, !isRecording);
	}
		
	    
	private View.OnClickListener btnClick = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.btnPlay:{
					
					isPlaying=true;
					enablePlayButton(isPlaying);
					txView.setText("Transmitting...");
					
					stopUpdater = false;
					updater = new Updater();
					updateHandler = new Handler();
					updateHandler.postDelayed(updater,20);
					txView1.setText("Operator");
					txView2.setText("CellID");
					txView3.setText("RSSI");
					
					//AP.Play();
					gsm.Play();
					
					break;
				}
				case R.id.btnStart: {
					isRecording=true;
					enableRecordButton(isRecording);
					txView.setText("Receiving...");
			
					stopUpdater = false;
					updater = new Updater();
					updateHandler = new Handler();
					updateHandler.postDelayed(updater,20);
					txView1.setText("Operator");
					txView2.setText("CellID");
					txView3.setText("RSSI");
					rcvInfo.startRecording();
					break;
				}
				case R.id.btnStop: {
					enableButtons(false);
					if(isPlaying){
						isPlaying=false;
						
						txView.setText("");
						txView1.setText("");
						txView2.setText("");
						txView3.setText("");
						txView4.setText("");
						txView5.setText("");
						txView6.setText("");
						//AP.stopPlay();
						gsm.stopPlay();
						stopUpdater = true;
						
					}
					if(isRecording){
						isRecording=false;
						txView.setText("");
						txView1.setText("");
						txView2.setText("");
						txView3.setText("");
						txView4.setText("");
						txView5.setText("");
						txView6.setText("");
						GetMessage.cellId=0;
						GetMessage.operator="";
						GetMessage.rssi=0;
						Iframe="";
						rcvInfo.stopRecording();
						stopUpdater = true;
					}
					
					break;
				}
				
			
			}
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
		
	}
	

	
	
	Handler updateHandler;
	boolean stopUpdater = false;
	Updater updater;
	
	
class Updater implements Runnable{	
		public void run(){
			if(!stopUpdater){
				
				if(isRecording){
					btnFlag++;
					if(btnFlag>0 && btnFlag<20)
						txView.setText("Receiving");
					else if(btnFlag>=20 && btnFlag<40)
						txView.setText("Receiving .");
					else if(btnFlag>=40 && btnFlag<60)
						txView.setText("Receiving . .");
					else if(btnFlag>=60 && btnFlag<80)
						txView.setText("Receiving . . .");
					else
						btnFlag=0;
					txView4.setText(GetMessage.operator);
					txView5.setText(Integer.toString(GetMessage.cellId));
					txView6.setText(Integer.toString(GetMessage.rssi));
					updateHandler.postDelayed(updater, 20);
				}
				if(isPlaying){
					btnFlag++;
					if(btnFlag>0 && btnFlag<40){
						txView.setTextColor(Color.parseColor("#0e8466"));
						txView.setText("Transmitting");
						}
					else if(btnFlag>=40 && btnFlag<80){
							txView.setTextColor(Color.parseColor("#991f39"));
							txView.setText("Transmitting");
						}
					
					else
						btnFlag=0;
					txView4.setText(operator);
					txView5.setText(Integer.toString(cellID));
					txView6.setText(Integer.toString(-113+2*rssi));
					updateHandler.postDelayed(updater, 20);
				}
			}
			if(stopUpdater){
				 updateHandler.removeCallbacks(this);
			}
		}
	}	

public class MyPhoneStateListener extends PhoneStateListener {
	    GsmCellLocation loc;
	    
	    @Override
	    public void onCellLocationChanged(CellLocation location) {
	        super.onCellLocationChanged(location);
	        loc = (GsmCellLocation) tm.getCellLocation();
            operator=tm.getNetworkOperatorName();

	        try{
	            cellID = loc.getCid() & 0xffff;
	        }catch(NullPointerException ne) {
	            cellID = 0;
	        }
	    }

	    @Override
	    public void onSignalStrengthsChanged(SignalStrength signalStrength){
	        super.onSignalStrengthsChanged(signalStrength);
	        rssi=signalStrength.getGsmSignalStrength();
	    }
}
	

		
}