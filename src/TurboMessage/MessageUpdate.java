package TurboMessage;

import java.io.Serializable;

public class MessageUpdate implements Serializable {
    private Msg msg;
    private int newStatus;
    private boolean readAll;

    public MessageUpdate(Msg msg, int newStatus){
        this.msg = msg;
        this.newStatus = newStatus;
        this.readAll = false;
    }//builder

    public MessageUpdate(Msg msg){
        this.msg = msg;
        this.readAll = true;
    }//builder

    public boolean getReadAll(){
        return readAll;
    }

    public Msg getMsg(){
        return msg;
    }

    public int getNewStatus(){
        return newStatus;
    }

}//class
