package tetris.box;

public class CheckCleanLineThread extends Thread{
    private float sec = 0.1f;
    private boolean isRun;
    private CleanLineI obj;

    public CheckCleanLineThread() {
        isRun = true;
        CheckCleanLineThreadInit();
    }

    public void CheckCleanLineThreadInit(){
        //System.out.println("²£¥ÍCheckCleanLineThread");
    }

    public void setObj(CleanLineI o){
        obj = o;
    }

    public void startThread(){
        start();
    }

    public void stopThread(){
        isRun = false;
    }

    @Override
	public void run(){
        while(isRun){
            if(obj != null){
                if(obj.isClean()){
                    obj.cleanLine();
                    break;
                }
            }
            try {
                Thread.sleep((int)(sec * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        close();
    }

    public void close(){
        //System.out.println("CheckCleanLineThread close;");
        obj = null;
    }

}
