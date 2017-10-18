package util;

import javax.sound.sampled.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 播放音樂(支援WAV, AIFF, AU) 2011/10/09
 * 
 * 2012-12-08 <br>
 * 1.增加播放結束時callback <br>
 * 2.修正bug: 無限次播放時，無法stop()<br>
 * 
 * 2017-10-18<br>
 * 1.[bug]修正不斷播重建物件進行播放會造成卡頓<br>
 * 2.移除左、右聲道切換功能<br>
 * 3.移除close()關閉音檔功能<br>
 * 
 * @author Ray
 */
public class AudioPlayer {
	private static ConcurrentHashMap<String, CacheReuseList<Pair<AudioInputStream, Clip>>> sCacheAudioClip = new ConcurrentHashMap<String, CacheReuseList<Pair<AudioInputStream, Clip>>>();

	private Clip mClip;

	private float mGain;
	private FloatControl mGainControl;

	// 控制靜音 開/關
	private boolean mute;
	private BooleanControl mMuteControl;

	// 播放次數,小於等於0:無限次播放,大於0:播放次數
	private int mPlayCount;
	private int mCacheCount;

	// 音樂播放完畢時，若有設定回call的對象，則會通知此對象
	private AudioPlayerCallback mCallbackTartet;
	private Object mCallbackObj;
	private boolean mIsPause;

	public AudioPlayer() {
		initialize();
	}

	public void initialize() {
		mClip = null;
		mGain = 0.5f;
		mGainControl = null;
		mute = false;
		mMuteControl = null;
		mPlayCount = 0;
		mCacheCount = 20;
		mIsPause = false;
	}
	
	/**
	 * 設定每個音檔cache的數量，若過少又要reuse聲音的話會發生播音馬上被停止
	 * @param count
	 */
	public void setCacheCount(int count){
		mCacheCount = count;
	}
	/**
	 * 設定要接收音樂播放完時事件的對象
	 * 
	 * @param cb
	 *            接收callback的對象
	 * @param obj
	 *            callback回來的物件
	 */
	public void setCallbackTartet(AudioPlayerCallback cb, Object obj) {
		mCallbackTartet = cb;
		mCallbackObj = obj;
	}

	/**
	 * 設定播放次數,播放次數,小於等於0:無限次播放,大於0:播放次數
	 * 
	 * @param c
	 */
	public void setPlayCount(int c) {
		if (c < -1) {
			c = -1;
		}
		mPlayCount = c - 1;
	}

	/**
	 * 指定路徑讀取音檔,回傳true:播放成功,false:播放失敗
	 * 
	 * @param filePath
	 * @param obj
	 *            目前物件放置的package路徑
	 */
	public boolean loadAudio(String filePath) {
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
	 * 
	 * @param filePath
	 * @param obj
	 *            目前物件放置的package路徑
	 * @return 回傳true:播放成功,false:播放失敗
	 */
	public boolean loadAudio(final String filePath, final Object obj) {
		try {
			if (obj != null) {
				doLoadingAudio(filePath, new Callable<InputStream>(){

					@Override
					public InputStream call() throws Exception {
						return obj.getClass().getResourceAsStream(filePath);
					}
				});
			} else {
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
	public void loadAudio(final URL url) throws Exception {
		doLoadingAudio(url.toString(), new Callable<URL>(){

			@Override
			public URL call() throws Exception {
				return url;
			}
		});
	}

	/**
	 * 讀取本地端音檔
	 * 
	 * @param file
	 * @throws Exception
	 */
	public void loadAudio(final File file) throws Exception {
		doLoadingAudio(file.getPath(), new Callable<File>(){

			@Override
			public File call() throws Exception {
				return file;
			}
		});
	}

	/**
	 * load完音檔後，進行播放設定
	 */
	private <T> void doLoadingAudio(String key, final Callable<T> callable) throws Exception {
		CacheReuseList<Pair<AudioInputStream,Clip>> audioClipList = sCacheAudioClip.get(key);

		if(audioClipList == null){
			audioClipList = new CacheReuseList<Pair<AudioInputStream,Clip>>(mCacheCount, new Callable<Pair<AudioInputStream,Clip>>(){
				@Override
				public Pair<AudioInputStream,Clip> call() throws Exception {
					//載入音檔來源轉為串流
					AudioInputStream audio = getAudioInputStream(callable.call());
					AudioFormat audioFormat = audio.getFormat();
					DataLine.Info dlInfo = new DataLine.Info(Clip.class, audioFormat,
							((int) audio.getFrameLength() * audioFormat.getFrameSize()));
					
					//開啟串流轉換為Clip
					Clip clip = (Clip) AudioSystem.getLine(dlInfo);
					clip.open(audio);
					clip.addLineListener(new LineListener() {
			            @Override
			            public void update(LineEvent event) {
			                if (event.getType().equals(LineEvent.Type.STOP)) {
			                    if (!mIsPause) {
			                        if (mCallbackTartet != null) {
			                            mCallbackTartet.audioPlayEnd(mCallbackObj);
			                        }
			                    }
			                }
			            }
			        });
					return new Pair<AudioInputStream,Clip>(audio, clip);
				}});
			sCacheAudioClip.put(key, audioClipList);
			
			mClip = audioClipList.next().getSecond();
		}else{
			mClip = audioClipList.next().getSecond();
		}
	}

	/**
	 * 播放音檔
	 */
	public void play() {
		if (mClip != null) {
			mClip.setFramePosition(0);
			mClip.loop(mPlayCount);
		}
	}

	/**
	 * 恢復播放音檔
	 * 
	 */
	public void resume() {
		mIsPause = false;

		if (mClip != null) {
			mClip.setFramePosition(mClip.getFramePosition());
			mClip.loop(mPlayCount);
		}

	}

	/**
	 * 暫停播放音檔
	 */
	public void pause() {
		mIsPause = true;
		if (mClip != null) {
			mClip.stop();
		}
	}

	/**
	 * 停止播放音檔,且將音檔播放位置移回開始處
	 */
	public void stop() {
		if (mClip != null) {
			mClip.stop();
		}
	}

	/**
	 * 設定音量
	 * 
	 * @param dB
	 *            0~1,預設為0.5
	 */
	public void setVolume(float dB) {
		float tempB = floorPow(dB, 1);
		// System.out.println("目前音量+"+tempB);
		mGain = tempB;
		resetVolume();

	}

	/**
	 * @param min
	 *            要無條件捨去的數字
	 * @param Num
	 *            要捨去的位數
	 * 
	 */
	private float floorPow(float min, int Num) {
		float n = (float) Math.pow(10, Num);
		return ((int) (min * n)) / n;
	}

	/**
	 * 重設音量
	 */
	protected void resetVolume() {
		mGainControl = (FloatControl) mClip
				.getControl(FloatControl.Type.MASTER_GAIN);
		// double gain = .5D; // number between 0 and 1 (loudest)
		float dB = (float) (Math.log(mGain) / Math.log(10.0) * 20.0);
		mGainControl.setValue(dB);
	}

	/**
	 * 設定靜音狀態,true:靜音,false:不靜音
	 * 
	 * @param m
	 */
	public void setMute(boolean m) {
		mute = m;
		resetMute();
	}

	/**
	 * 重設靜音狀態
	 * 
	 */
	protected void resetMute() {
		mMuteControl = (BooleanControl) mClip
				.getControl(BooleanControl.Type.MUTE);
		mMuteControl.setValue(mute);
	}

	/**
	 * 
	 * @return
	 */
	public int getFramePosition() {
		try {
			return mClip.getFramePosition();
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * 取得音檔的串流
	 * 
	 * @return
	 */
	public AudioInputStream getAudioInputStream(Object loadReference) {
		try {
			AudioInputStream aiStream;
			if (loadReference == null) {
				return null;
			} else if (loadReference instanceof URL) {
				URL url = (URL) loadReference;
				aiStream = AudioSystem.getAudioInputStream(url);
			} else if (loadReference instanceof File) {
				File file = (File) loadReference;
				aiStream = AudioSystem.getAudioInputStream(file);
			} else if (loadReference instanceof AudioInputStream) {
				AudioInputStream stream = (AudioInputStream) loadReference;
				aiStream = AudioSystem.getAudioInputStream(stream.getFormat(),
						stream);
				stream.reset();
			} else {

				InputStream inputStream = (InputStream) loadReference;
				aiStream = AudioSystem.getAudioInputStream(inputStream);
			}
			
			return aiStream;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 取得剪輯音檔
	 * 
	 * @return
	 */
	public Clip getClip() {
		return mClip;
	}
}