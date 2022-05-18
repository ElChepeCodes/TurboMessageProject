package TurboMessage;

import java.io.Serializable;

public class RequestResponse implements Serializable {
    private boolean response;
    private User sender;
    private Msg msg;

    public RequestResponse(User sender, Msg msg, boolean response){
        this.sender = sender;
        this.msg = msg;
        this.response = response;
    }

    public boolean getResponse(){
        return  response;
    }
    public User getSender(){
        return sender;
    }
    public Msg getMsg(){
        return msg;
    }
}//class
