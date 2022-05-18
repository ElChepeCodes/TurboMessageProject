package TurboMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.jetbrains.annotations.NotNull;

import javax.jms.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


public class User implements Serializable {
    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;

    private static int userCount = 1;
    private String key;
    private String name;
    private ArrayList<User> contacts;
    private ArrayList<ArrayList<Msg>> chats;
    private Set<User> requests;

    public User(String name){
        this.name = name;
        key = name + "#" + userCount;
        userCount++;
        contacts = new ArrayList<User>();
        requests = new HashSet<User>();
        chats = new ArrayList<ArrayList<Msg>>();
    }//builder

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Msg> getChat(User target){
        int index = contacts.indexOf(target);
        return chats.get(index);
    }//method


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return key.equals(user.key) && name.equals(user.name);
    }

    public void addChat(@NotNull User target){
        contacts.add(target);
        chats.add(new ArrayList<Msg>());
    }//method

    public void sendMessage(String message, User target){
        Msg msg = new Msg(message, this, target);
        int index = contacts.indexOf(target);
        MessageProducer messageProducer;
        ObjectMessage objectMessage;
        try {

            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false /*Transacter*/, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(target.key);
            messageProducer = session.createProducer(destination);
            objectMessage = session.createObjectMessage(msg);

            //System.out.println("Sending the following message: " + objectMessage.getObject().toString());
            messageProducer.send(objectMessage);
            // update msg status to sent
            msg.updateStatus(1);
            if(index < 0){ // user not in contact list, so we wait for target to accept or decline messaging us
                requests.add(target);
            }//if
        }
        catch (Exception exception){
            exception.printStackTrace();
        }//catch

    }//method

    public void receiveMessage(Msg msg){
        User sender = msg.getSender();
        int index = contacts.indexOf(sender);
        boolean flag = false; // flag to update msg status
        if(index >= 0) {// sender is a contact already
            msg.updateStatus(2);
            flag = true;
            chats.get(index).add(msg);
        }//if
        else{ // sender is not a contact yet
            Scanner scanner = new Scanner(System.in);
            System.out.println(msg.getSender().name + " le quiere enviar un mensaje");
            System.out.println("¿Acepta? (Y/N)");
            String res = scanner.next();
            boolean accept = res.equalsIgnoreCase("Y");
            System.out.println(accept);
            if(accept){ // message request accepted
                addChat(sender);
                msg.updateStatus(2);
                flag = true;
                chats.get(chats.size() - 1).add(msg); // add initial msg to chat
            }//if
            if(flag){ // send updateStatus to sender
                sendUpdateMsgStatus(msg, 2);
            }//if
            respondRequest(sender, msg, accept);
        }//else
    }//method

    public void sendUpdateMsgStatus(Msg msg, int status){
        MessageProducer messageProducer;
        ObjectMessage objectMessage;
        MessageUpdate update = new MessageUpdate(msg, status);
        try {

            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false /*Transacter*/, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(msg.getSender().key);
            messageProducer = session.createProducer(destination);
            objectMessage = session.createObjectMessage(update);

            //System.out.println("Sending the following message: " + objectMessage.getObject().toString());
            messageProducer.send(objectMessage);
        }
        catch (Exception exception){
            exception.printStackTrace();
        }//catch

    }//method

    public void respondRequest(User target, Msg msg, boolean response){
        MessageProducer messageProducer;
        ObjectMessage objectMessage;
        try {
            RequestResponse rR = new RequestResponse(target, msg, response);
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false /*Transacter*/, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(target.key);
            messageProducer = session.createProducer(destination);
            objectMessage = session.createObjectMessage(rR);

            //System.out.println("Sending the following message: " + objectMessage.getObject().toString());
            messageProducer.send(objectMessage);
        }
        catch (Exception exception){
            exception.printStackTrace();
        }//catch


    }//method

    public void receiveRequestResponse(RequestResponse response){
        User target = response.getSender();
        boolean res = response.getResponse();
        if(res){ // target wants to chat with us :)
            System.out.println(target.name + " aceptó su solicitud de mensaje");
            Msg msg = response.getMsg();
            contacts.add(target);
            chats.add(new ArrayList<Msg>());
            chats.get(chats.size() - 1).add(msg);
        }//if
        else{ // target doesn't want to chat with us :(
            System.out.println(target.name + " rechazó su solicitud de mensaje");
        }//else
        requests.remove(target);
    }//method

    public void readAll(Msg msg){
        User target = msg.getTarget();
        int chatIndex = contacts.indexOf(target), i;
        ArrayList<Msg> chat = chats.get(chatIndex);
        i = chat.indexOf(msg);
        while(i >= 0 && chat.get(i).getStatus() != 3){
            chat.get(i).updateStatus(3);
            i--;
        }//while
    }//method

    public void updateMsgStatus(Msg msg, int newStatus){
        User target = msg.getTarget();
        int chatIndex = contacts.indexOf(target);
        ArrayList<Msg> chat = chats.get(chatIndex);
        chat.get(chat.indexOf(msg)).updateStatus(newStatus);
    }//method

    public void startListening(){
        try{
            MessageListener listener = new MessageListener(this);
            listener.start();
        }//try
        catch(Exception exception){
            exception.printStackTrace();
        }//catch
    }//method


}//class
