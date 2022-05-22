package TurboMessage;

import java.io.Serializable;

public class RequestResponse implements Serializable {
    private boolean response;
    private User sender, target;
    private Msg msg;

    public RequestResponse(User sender, User target, Msg msg, boolean response){
        this.sender = sender;
        this.target = target;
        this.msg = msg;
        this.response = response;
    }

    public boolean getResponse(){
        return  response;
    }
    public User getSender(){
        return sender;
    }
    public User getTarget() {
        return target;
    }
    public Msg getMsg(){
        return msg;
    }
}//class
