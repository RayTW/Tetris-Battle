package tetris;

/**
 * 定議遊戲中事件，用來處理什流程中播放音效、動畫等等...
 * 
 * @author Ray
 *
 */
public enum GameEvent {
	/** 重畫畫面的全部方塊陣列 */
	REPAINT,
	/** 方塊旋轉 */
	BOX_TURN,
	/** 方塊落到底 */
	BOX_DOWN,
	/** 建立完下一個方塊 */
	BOX_NEXT,
	/** 有方塊可清除,將要清除方塊,可取得要消去的方塊資料 */
	CLEANING_LINE,
	/** 方塊清除完成 */
	CLEANED_LINE,
	/** 計算自己垃圾方塊數 */
	BOX_GARBAGE,
	/** 方塊頂到最高處，遊戲結束 */
	GAME_OVER
}
