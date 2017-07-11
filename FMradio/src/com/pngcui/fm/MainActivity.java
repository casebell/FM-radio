package com.pngcui.fm;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends Activity {

	Jni jni = new Jni();
	
	private ImageButton up,down,setFreq,search_down,search_up;
	
	private Button save,mute,play,autoSearch,menu;
	
	private  PopupWindow popupWindow = null;
	
	private TextView adcText;
	private TextView freqText;
	
	private ImageView freqBar;
	private ListView lvPopupList;
	
	private Boolean FmSearchUp = true;
	private Boolean FmSearchDown = true;
	
	List<Map<String, String>> freqList;
	Map<String, String> map;
	
	private static int flagMap[] = new int[250];
	
	private int freq,adc,tempFreq;
	
	int i = 1;
	
	private Boolean flagAutoSearch = false;
	private Boolean flaghandSearch = false;
	
	private Handler uiHandler;
	
	//��������
    SharedPreferences sp = null;
    Editor editor = null;
    //�ж��Ƿ��һ������
    private boolean isFirstUse;
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		init();
		
	}
	
	private void init() {
		
		jni.Open();
		
		setFreq = (ImageButton)findViewById(R.id.setFreq);
		adcText = (TextView)findViewById(R.id.adc);
		
		freqText = (TextView)findViewById(R.id.freq);
		
		up = (ImageButton)findViewById(R.id.up);
		down = (ImageButton)findViewById(R.id.down);
		freqBar = (ImageView)findViewById(R.id.freq_bar);
		search_down = (ImageButton)findViewById(R.id.search_down);
		search_up = (ImageButton)findViewById(R.id.search_up);
		
		
		save = (Button)findViewById(R.id.save);
		autoSearch = (Button)findViewById(R.id.autoSearch);
		mute = (Button)findViewById(R.id.mute);
		play = (Button)findViewById(R.id.play);
		menu = (Button)findViewById(R.id.menu);
		
		setFreq.setOnClickListener(new Click());
		
		up.setOnClickListener(new Click());
		down.setOnClickListener(new Click());
		search_up.setOnClickListener(new Click());
		search_down.setOnClickListener(new Click());
		
		save.setOnClickListener(new Click());
		autoSearch.setOnClickListener(new Click());
		mute.setOnClickListener(new Click());
		play.setOnClickListener(new Click());
		menu.setOnClickListener(new Click());
		
		//�رվ���
		jni.CloseMute();
		
		//����app������оƬ��ĵ�ǰƵ��
		setTextView();
		
		freqList = new ArrayList<Map<String, String>>();  
        
        map = new HashMap<String, String>();
        
        sp = this.getSharedPreferences("FMDATA", MODE_PRIVATE);
        editor = sp.edit();
        
        if(!isFirstStart()){
        	
        	//��ȡ�ϴδ洢�ĵ�̨fm
            getFMData();
            setMapDate();
        }
        
	}
	
	class Click implements OnClickListener{
		
		@Override
		public void onClick(View v) {
			
			switch (v.getId()) {
			case R.id.setFreq:
				if(!flagAutoSearch && !flaghandSearch){
					setFreq();
				}else{
					Toast.makeText(getApplicationContext(), "�����Զ������У����Ժ�...", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.up:
				if(!flagAutoSearch && !flaghandSearch){
					jni.HandSearch(1);
					setTextView();
				}else{
					Toast.makeText(getApplicationContext(), "�����Զ������У����Ժ�...", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.down:
				if(!flagAutoSearch && !flaghandSearch){
					jni.HandSearch(0);
					setTextView();
				}else{
					Toast.makeText(getApplicationContext(), "�����Զ������У����Ժ�...", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.search_up:
				if(!flagAutoSearch && !flaghandSearch){
					flaghandSearch = true;
					handSearch(1);
				}else{
					Toast.makeText(getApplicationContext(), "�����Զ������У����Ժ�...", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.search_down:
				if(!flagAutoSearch && !flaghandSearch){
					flaghandSearch = true;
					handSearch(0);
				}else{
					Toast.makeText(getApplicationContext(), "�����Զ������У����Ժ�...", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.save:
				if(!flagAutoSearch && !flaghandSearch){
					saveFreq();
				}else{
					Toast.makeText(getApplicationContext(), "�����Զ������У����Ժ�...", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.autoSearch:
				if(!flaghandSearch){
					if(flagAutoSearch){
						flagAutoSearch = false;
						autoSearch.setText("�Զ�����");
					}
					else{
						flagAutoSearch = true;
						autoSearch.setText("ֹͣ����");
						autoSearch();
					}
				}else{
					Toast.makeText(getApplicationContext(), "�����ֶ������У����Ժ�...", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.mute:
				if(!flagAutoSearch && !flaghandSearch){
					jni.OpenMute();
				}else{
					Toast.makeText(getApplicationContext(), "�����Զ������У����Ժ�...", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.play:
				if(!flagAutoSearch && !flaghandSearch){
					jni.CloseMute();
				}else{
					Toast.makeText(getApplicationContext(), "�����Զ������У����Ժ�...", Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.menu:
				showPopupWindow(v);
				break;

			default:
				break;
			}
		
		}
	}
	
	//���浱ǰƵ��
	private void saveFreq() {
		
		freq = jni.GetFreq();
		addMapDate(freq);
		
		Toast.makeText(getApplicationContext(), "�ѱ���", Toast.LENGTH_SHORT).show();
		
	}
	
	//�ӵ�ǰƵ��ѭ���Զ�����
	@SuppressLint("HandlerLeak")
	private void autoSearch() {
			
		 	initMapDate();
			
			freq = jni.GetFreq();
			tempFreq = freq;
			
			//���մ����߳��յ�����Ϣ
			uiHandler = new Handler(){

				@Override
				public void handleMessage(Message msg) {
					
					//����setTextView���������freq��ֵ
					
					switch(msg.what){
					case 0:
						setTextView();
						break;
					case 1:
						setTextView();
						addMapDate(freq);
						break;
					case 3:
						flagAutoSearch = false;
						autoSearch.setText("�Զ�����");
						break;
					default :
						break;
					}
				}
				
			};
			
			//����һ���߳�
			//ִ������Զ�����
			new Thread(new Runnable() {
						
				@Override
				public void run() {
					
					while(flagAutoSearch){
					
						if(jni.AutoSearch(freq,4) == 1){
							uiHandler.sendEmptyMessage(1);
							SystemClock.sleep(2000);
						}else{
							uiHandler.sendEmptyMessage(0);
						}
						SystemClock.sleep(50);
						if(freq < 87600){
							break;
						}
					}
					
					while(freq < tempFreq && flagAutoSearch){
						
						if(jni.AutoSearch(freq,4) == 1){
							uiHandler.sendEmptyMessage(1);
							System.out.println(freq+"is ok!");
							SystemClock.sleep(2000);
						}else{
							System.out.println(freq+"is shut!");
							uiHandler.sendEmptyMessage(0);
						}
						SystemClock.sleep(50);
					}
					
				freq = tempFreq;
				uiHandler.sendEmptyMessage(3);
				}
			}).start();  
	}

	//�ֶ�����
	private void handSearch(int arg){
		freq = jni.GetFreq();
		if(arg == 1){
			FmSearchUp = true;
		}else if (arg == 0){
			FmSearchUp = false;
		}
		//���մ����߳��յ�����Ϣ
		uiHandler = new Handler(){

			@SuppressLint("HandlerLeak")
			@Override
			public void handleMessage(Message msg) {
				
				//����setTextView���������freq��ֵ
				
				switch(msg.what){
				case 0:
					setTextView();
					break;
				case 1:
					setTextView();
					addMapDate(freq);
					break;
				case 3:
					flaghandSearch = false;
					setTextView();
					break;
				default :
					break;
				}
			}
			
		};
		
		//����һ���߳�
		new Thread(new Runnable() {
					
			@Override
			public void run() {
				if(FmSearchUp){
					while(freq < 108000){
						if(jni.AutoSearch(freq,4) == 1){
							uiHandler.sendEmptyMessage(1);
							break;
						}else{
							uiHandler.sendEmptyMessage(0);
						}
						SystemClock.sleep(50);
					}
				}else{
					while(freq > 87500){
						if(jni.AutoSearch(freq,40) == 1){
							uiHandler.sendEmptyMessage(1);
							break;
						}else{
							uiHandler.sendEmptyMessage(0);
						}
						SystemClock.sleep(50);
					}
					
				}
				uiHandler.sendEmptyMessage(3);
			}
		}).start();
		
		return ;
	}
    
	//�����༭��������Ҫд���Ƶ��
	private void setFreq() {
	
		final EditText inputServer = new EditText(this);
        inputServer.setFocusable(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setTitle("������Ƶ�ʣ�");
        builder.setView(inputServer);
        
        builder.setPositiveButton("ȷ��",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        
                        try {
                        	String inputName = inputServer.getText().toString();
                        
                        	 Float freq = Float.valueOf(inputName);
                             int freqInt = (int) (freq * 1000);

                             if(freqInt >87499 && freqInt < 108001){
                             	jni.SetFreq(freqInt);
                             	setTextView();
                             }
                             else{
                             	Toast.makeText(getApplicationContext(), "������87.5-108֮�������", Toast.LENGTH_LONG).show();
                             }
                        
                        } catch (Exception e) {
                        	Toast.makeText(getApplicationContext(), "�Ƿ����룡������87.5-108֮�������", Toast.LENGTH_SHORT).show();
                        }
                       
                    }
                });
        
        builder.setNegativeButton("ȡ��",null);
        builder.show();
		
	}


	//�������Item,freQ=87500
	public void addMapDate(int freQ){
		
		System.out.println("add freq = "+freQ);
		//��flagMap[2]��ʼ
		int k = (freQ-87300)/100 ;
		if(flagMap[k] == 0){
			flagMap[k] = 1;
			
			DecimalFormat decimalFormat=new DecimalFormat(".0");
			//����һλС��
			
			float Freq=(float) freQ;

			String p=decimalFormat.format(Freq/1000);
			//format ���ص����ַ���
			map = new HashMap<String, String>();
	        map.put("freqKey", p);
	        freqList.add(map);
	        
		}
		
	}
	
	public void initMapDate() {
		
		freqList = new ArrayList<Map<String, String>>();  
        
        map = new HashMap<String, String>();
        
        for(i = 0;i<flagMap.length;i++)
        	flagMap[i] = 0;
        
	}
	//��ʼ��
	public void setMapDate(){
		
		for(int i = 1;i<flagMap.length;i++){
			if(flagMap[i] == 1){
				flagMap[i] = 0;
				addMapDate((i*100)+87300);
			}
		}
		
		return ;
	}
	
	
	//���������б�
	private void showPopupWindow(View v) {

		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.group_list, null);
		lvPopupList = (ListView) view.findViewById(R.id.freq_list);
		
		popupWindow = new PopupWindow(view);
		popupWindow.setFocusable(true);
		
		lvPopupList.setAdapter(new SimpleAdapter(MainActivity.this, freqList, 
                R.layout.list_item, new String[] { "freqKey" }, 
                new int[] { R.id.list_item }));  
		
        lvPopupList.setOnItemClickListener(new OnItemClickListener() {
  
            @Override  
            public void onItemClick(AdapterView<?> parent, View view,  
                    int position, long id) {  
  
            	String s =  freqList.get(position).get("freqKey");
            	
            	float f = Float.parseFloat(s);
            	
            	int freqx = (int) (f*1000);
            	
            	if(freqx >87399 && freqx < 108001){
                 	jni.SetFreq(freqx);
                 	setTextView();
                 }
                 else{
                 	Toast.makeText(getApplicationContext(), "��Ч��̨��", Toast.LENGTH_LONG).show();
                 }
            }

        }); 
        
        lvPopupList.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popupWindow.setWidth(lvPopupList.getMeasuredWidth() + 50);
        popupWindow.setHeight(lvPopupList.getMeasuredHeight()*6);
        
          
        popupWindow.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.item_bg));  
        popupWindow.setOutsideTouchable(true);
        
        //��ʾ
        if(popupWindow.isShowing()){
        	popupWindow.dismiss();
        }else{
        	popupWindow.showAsDropDown(menu);
        }
        
	}
	
	//����UI����
	public void setTextView() {
		
		adc = jni.GetAdc();

		freq = jni.GetFreq();
		
		DecimalFormat decimalFormat=new DecimalFormat(".0");
		//����һλС��
		
		float Freq=(float) freq;

		String p=decimalFormat.format(Freq/1000);
		//format ���ص����ַ���
		
		adcText.setText(adc+"");
		freqText.setText(p);
		
		setFreqBar();
	}
	
	//����freqBar��λ��
	private void setFreqBar() {
		
		int freqBarLocation = (int) (freq - 87400)/100;
		
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		
		lp.setMargins((int)(165 + freqBarLocation*3.8), 150, 0, 0);
		
		freqBar.setLayoutParams(lp);
		
	}
	
	//�洢�Ѵ��̨��flagMap[]��ʹ��sharepreference��ʽ��
	public void saveFMData(){
		
		//���ԭ������
		editor.clear();
		editor.commit();

		//int[]-->string
		String str = "0";
		for(int i = 1;i<flagMap.length;i++){
			str = str + "," + flagMap[i];
			System.out.println(str);
		}
		
		//��������
        editor.putString("FMDATA",str);
        editor.commit();
        
        return ;
		
	}
	
	//��ȡ�Ѵ��̨��flagMap[]��
	public void getFMData(){
		
		String str[] = sp.getString("FMDATA", "").split(",");
		
		//��stringת����int[]
		if((str[0] == "0")||(str[0] == "1")){
			for(int i = 0;i<str.length;i++){
				flagMap[i] = Integer.parseInt(str[i]);
			}
		}
		return ;
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		getFMData();
		super.onRestart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		saveFMData();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		saveFMData();
		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK ){  
            
			AlertDialog exit = new AlertDialog.Builder(this).create();
			
			exit.setTitle("ϵͳ��ʾ");
			exit.setMessage("ȷ���˳���");
			
			exit.setButton("��ȫ�˳�", listener);  
            exit.setButton2("��̨����", listener);
            exit.setButton3("ȡ��", listener);
			
            exit.show(); 
			
            return false;  
        }  
		return super.onKeyDown(keyCode, event);
	}
	
	//���õ�̨��ť
	 DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()  
	    {  
	        public void onClick(DialogInterface dialog, int which)  
	        {  
	            switch (which)  
	            {  
	            case AlertDialog.BUTTON_POSITIVE:// "ȷ��"��ť�˳�����  
	            	jni.SetShutDown();
	                finish(); 
	                break;  
	            case AlertDialog.BUTTON_NEGATIVE:// "ȡ��"�ڶ�����ťȡ���Ի���  
	            	finish();
	                break;
	            default:  
	                break;
	            }  
	        }  
	    };    
	    
	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	private boolean isFirstStart(){
		
		boolean temp = false;
		
		sp = getSharedPreferences("isFirstUse", MODE_WORLD_READABLE);
		
		isFirstUse = sp.getBoolean("isFirstUse", true);
		
		//��ʾ��һ������
		if(isFirstUse){
			
			temp = true;
			
			Editor editor = sp.edit();
		
			editor.putBoolean("isFirstUse", false);
			
			editor.commit();
		}
		
		return temp;
	}

	static {
		System.loadLibrary("FM");
	}
	

}
