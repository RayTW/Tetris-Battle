package util;

import javax.sound.sampled.*;   

import java.io.*;   
import java.net.*;   

/**
 * 播放音樂(支援WAV, AIFF, AU) 2011/10/09 
 * 
 * 2012/12/08
 * 1.增加播放結束時callback 
 * 2.修正bug: 無限次播放時，無法stop()
 *
 * @version 2
 * @author Ray(吉他手)
 */
public class AudioPlayer{   
	private AudioInputStream currentSound;   

	private Clip clip;   

	private float gain;
	private FloatControl gainControl;

	//控制聲道,-1.0f:只有左聲道, 0.0f:雙聲道,1.0f右聲道
	private float pan;
	private FloatControl panControl;  

	//控制靜音 開/關
	private boolean mute;
	private BooleanControl muteControl;

	//播放次數,小於等於0:無限次播放,大於0:播放次數
	private int playCount;

	private DataLine.Info dlInfo;
	private Object loadReference;   
	private AudioFormat format;

	//音樂播放完畢時，若有設定回call的對象，則會通知此對象
	private AudioPlayerCallback callbackTartet;
	private Object callbackObj ;
	private boolean isPause;

	public AudioPlayer(){   
		AudioPlayerInit();
	}

	public void AudioPlayerInit(){
		currentSound = null;
		clip = null;
		gain = 0.5f;
		gainControl = null;  
		pan = 0.0f;
		panControl = null;
		mute = false;
		muteControl = null;
		playCount = 0;
		dlInfo = null;
		isPause = false;
	}

	/**
	 * 設定要接收音樂播放完時事件的對象
	 * @param cb	接收callback的對象
	 * @param obj	callback回來的物件
	 */
	public void setCallbackTartet(AudioPlayerCallback cb, Object obj){
		callbackTartet = cb;
		callbackObj = obj;
	}

	/**
	 * 設定播放次數,播放次數,小於等於0:無限次播放,大於0:播放次數
	 * @param c
	 */
	public void setPlayCount(int c){
		if(c < -1){
			c = -1;
		}
		playCount = c - 1;
	}

	/**
	 * 指定路徑讀取音檔,回傳true:播放成功,false:播放失敗
	 * @param filePath
	 * @param obj 目前物件放置的package路徑
	 */
	public boolean loadAudio(String filePath){
		try {
			loadAudio(new File(filePath));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 指定路徑讀取音檔,使用目前物件放置的package當相對路徑root,null時不使用物件路徑為root
	 * @param filePath
	 * @param obj 目前物件放置的package路徑
	 * @return 回傳true:播放成功,false:播放失敗
	 */
	public boolean loadAudio(String filePath, Object obj){
		try {
			if(obj != null){
				loadAudio(obj.getClass().getResourceAsStream(filePath));
			}else{
				loadAudio(new File(filePath));
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**  
	 * 從遠端讀取音檔
	 */   
	public void loadAudio(URL url) throws Exception{   
		loadReference = url;    
		currentSound = AudioSystem.getAudioInputStream(url);    
		finishLoadingAudio();    
	}   

	/**
	 * 讀取本地端音檔
	 * @param file
	 * @throws Exception
	 */
	public void loadAudio(File file) throws Exception{   
		loadReference = file;    
		currentSound = AudioSystem.getAudioInputStream(file);    
		finishLoadingAudio();    
	}   

	/**
	 * 從串流讀取音檔
	 * @param iStream
	 * @throws Exception
	 */
	public void loadAudio(InputStream iStream) throws Exception{   
		loadReference = iStream;
		//System.out.println("stream:" + iStream);

		if (iStream instanceof AudioInputStream){   
			currentSound = (AudioInputStream)iStream;   
		} else {
			InputStream bufferedIn = new BufferedInputStream(iStream);
			currentSound = AudioSystem.getAudioInputStream(bufferedIn);   
		}   
		finishLoadingAudio();    
	}   

	/**  
	 * load完音檔後，進行播放設定
	 */   
	protected void finishLoadingAudio() throws Exception {   
		format = currentSound.getFormat();   
		dlInfo = new DataLine.Info(Clip.class, format, ((int) currentSound.getFrameLength() * format.getFrameSize()));   
		clip = (Clip) AudioSystem.getLine(dlInfo);   
		clip.open(currentSound);   
		clip.addLineListener(   
				new LineListener() {   
					@Override
					public void update(LineEvent event) {
						if (event.getType().equals(LineEvent.Type.STOP)){
							if(!isPause){
								if(callbackTartet != null){
									callbackTartet.audioPlayEnd(callbackObj);
								}
								close();
							}
						}   
					}   
				}   
		);   
	}

	/**
	 * 播放音檔
	 */
	public void play(){
		if(clip != null){
			clip.setFramePosition(0);  
			clip.loop(playCount);
		}
	}

	/**
	 * 恢復播放音檔
	 *
	 */
	public void resume(){
		isPause = false;
		
		if(clip != null){
			clip.setFramePosition(clip.getFramePosition());
			clip.loop(playCount);
		}
		
	}

	/**
	 * 暫停播放音檔
	 */
	public void pause(){
		isPause = true;
		if(clip != null){
			clip.stop();
		}
	}

	/**
	 * 停止播放音檔,且將音檔播放位置移回開始處
	 */
	public void stop(){
		if(clip != null){
			clip.stop();   
		}
	}   

	/**
	 * 設定音量
	 * @param dB 0~1,預設為0.5
	 */
	public void setVolume(float dB){
		float tempB = floor_pow(dB,1);
		//System.out.println("目前音量+"+tempB);
		gain = tempB;   
		resetVolume();

	}

	/**
	 * @param min 要無條件捨去的數字
	 * @param Num 要捨去的位數
	 *
	 */
	private float floor_pow(float min, int Num){
		float n = (float)Math.pow(10, Num);
		float tmp_Num = ((int)(min*n))/n;
		return tmp_Num ;
	}

	/**
	 * 重設音量
	 */
	protected void resetVolume(){
		gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		//double gain = .5D; // number between 0 and 1 (loudest)
		float dB = (float) (Math.log(gain) / Math.log(10.0) * 20.0);
		gainControl.setValue(dB);
	}   

	/**
	 * 設定聲道,-1.0f:只有左聲道, 0.0f:雙聲道,1.0f右聲道
	 * @param p
	 */
	public void setPan(float p){   
		pan = p;   
		resetPan();   
	}   

	/**
	 * 重設單雙道、雙聲道
	 */
	protected void resetPan(){   
		panControl = (FloatControl) clip.getControl(FloatControl.Type.PAN);   
		panControl.setValue(this.pan);   
	}

	/**
	 * 設定靜音狀態,true:靜音,false:不靜音
	 * @param m
	 */
	public void setMute(boolean m){
		mute  = m;
		resetMute();
	}

	/**
	 * 重設靜音狀態
	 *
	 */
	protected void resetMute(){
		muteControl = (BooleanControl) clip.getControl(BooleanControl.Type.MUTE);
		muteControl.setValue(mute);
	}

	/**
	 *
	 * @return
	 */
	public int getFramePosition(){   
		try {   
			return clip.getFramePosition();   
		} catch (Exception e) {   
			return -1;   
		}   
	}   

	/**
	 * 取得音檔格式
	 * @return
	 */
	public AudioFormat getCurrentFormat(){   
		return format;   
	}   

	/**
	 * 取得音檔的串流
	 * @return
	 */
	public AudioInputStream getAudioInputStream(){   
		try {   
			AudioInputStream aiStream;   


			if (loadReference == null){   
				return null;   
			} else if (loadReference instanceof URL) {   
				URL url = (URL)loadReference;   
				aiStream = AudioSystem.getAudioInputStream(url);   
			} else if (loadReference instanceof File) {   
				File file = (File)loadReference;   
				aiStream = AudioSystem.getAudioInputStream(file);   
			} else if (loadReference instanceof AudioInputStream){   
				AudioInputStream stream = (AudioInputStream)loadReference;   
				aiStream = AudioSystem.getAudioInputStream(stream.getFormat(), stream);   
				stream.reset();   
			} else {   

				InputStream inputStream = (InputStream)loadReference;   
				aiStream = AudioSystem.getAudioInputStream(inputStream);   
			}   

			return aiStream;   
		} catch (Exception e) {   
			e.printStackTrace();   
			return null;   
		}   
	}   


	/**
	 * 目前音檔是否已存在
	 * @return
	 */
	public boolean isAudioLoaded(){   
		return loadReference!= null;   
	}   

	/**
	 * 取得剪輯音檔
	 * @return
	 */
	public Clip getClip() {   
		return clip;   
	}

	/**  
	 * 關閉音檔
	 */   
	public void close(){   
		try {   
			if (clip != null)   
				clip.close();   
			if (currentSound != null)   
				currentSound.close();   
			loadReference = null;   
		} catch (Exception e){   
			//System.out.println("unloadAudio: " + e);   
			e.printStackTrace();   
		}
		
		currentSound = null;   
		clip = null;   
		gainControl = null;   
		panControl = null;   
		dlInfo = null;   
		loadReference = null;
		muteControl = null;
		callbackTartet = null;
		callbackObj = null;
	}


	public static void main(String [] args){

	}
} 