package TurboMessage;

import java.io.Serializable;
import java.util.Date;

public class Msg implements Serializable {

    private static int localMsgCount = 0;
    private int id;
    private String msg;
    private User sender, target;
    private Date dateSent, dateReceived, dateRead;
    private int status; // 0-sending, 1-sent, 2-received, 3-read
    private boolean friendRequest;

    public Msg(String msg, User sender, User target){
        this.id = localMsgCount++;
        this.msg = msg;
        this.sender = sender;
        this.target = target;
        dateSent = new Date();
        status = 0;
        friendRequest = false;
    }//builder

    public Msg(User sender, User target, boolean friendRequest){
        this.sender = sender;
        this.target = target;
        this.friendRequest = friendRequest;
    }//builder

    public String getMsg() {
        return msg;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public Date getDateReceived() {
        return dateReceived;
    }

    public Date getDateRead() {
        return dateRead;
    }

    public void setDateReceived(Date dateReceived) {
        this.dateReceived = dateReceived;
    }

    public void setDateRead(Date dateRead) {
        this.dateRead = dateRead;
    }

    public int getStatus() {
        return status;
    }

    public User getSender() {
        return sender;
    }

    public User getTarget() {
        return target;
    }

    public int getId(){
        return id;
    }

    public void updateStatus(int status){
        this.status = status;
    }//method

    @Override
    public String toString() {
        String res = msg;
        if(getDateRead() != null)
            res += " ---(Read: " + getDateRead() + ")";
        else if(getDateReceived() != null)
            res += " ---(Received: " + getDateReceived() + ")";
        else
            res += " ---(Sent: " + getDateSent() + ")";
        return res;
    }
}//class
