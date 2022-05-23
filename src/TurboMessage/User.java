package TurboMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.Serializable;
import java.util.*;
import java.util.Queue;


public class User implements Serializable {
    private final static String url = ActiveMQConnection.DEFAULT_BROKER_URL;

    private static int userCount = 1;
    private final String key;
    private final String name;
    private ArrayList<User> contacts;
    private ArrayList<ArrayList<Msg>> chats;
    private ArrayList<Msg> requests;

    public User(String name){
        this.name = name;
        key = name + "#" + userCount;
        userCount++;
        contacts = new ArrayList<User>();
        requests = new ArrayList<Msg>();
        chats = new ArrayList<ArrayList<Msg>>();
    }//builder

    public User(String name, int num){
        this.name = name;
        key = name + "#" + num;
    }//builder

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public ArrayList<User> getContacts(){
        return contacts;
    }


    public ArrayList<Msg> getChat(User target){
        int index = contacts.indexOf(target);
        return chats.get(index);
    }//method

    public User searchContact(String name){
        if(contacts.size() == 0) {
            return null;
        }
        int i = 0;
        User current = contacts.get(i);
        while(i < contacts.size() && !current.getName().equals(name))
            current = contacts.get(i++);
        if (i >= contacts.size()) {
            return null;
        }
        return current;
    }//method

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return key.equals(user.key) && name.equals(user.name);
    }

    public void addChat(User target){
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
                requests.add(msg);
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
            System.out.println("Recibiste un nuevo mensaje de " +msg.getSender().getName());
        }//if
        else{ // sender is not a contact yet
            System.out.println(msg.getSender().name + " le quiere enviar un mensaje");
            System.out.println("¿Acepta? (Y/N)");
            requests.add(msg);
            //System.out.println(accept);
        }//else
    }//method


    public void acceptRequest(){
        Msg msg = requests.remove(0);
        msg.updateStatus(2);
        User sender = msg.getSender();
        respondRequest(sender, msg, true);
    }//method

    public void denyRequest(){
        Msg msg = requests.remove(0);
        User sender = msg.getSender();
        respondRequest(sender, msg, true);

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
        if(response){ // target wants to chat with us :)
            System.out.println("Aceptaste la solicitud de " + target.name);
            contacts.add(target);
            chats.add(new ArrayList<Msg>());
            chats.get(chats.size() - 1).add(msg);
        }//if
        try {
            RequestResponse rR = new RequestResponse(this, target, msg, response);
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
        Msg msg = response.getMsg();
        if(res){ // target wants to chat with us :)
            System.out.println(target.name + " aceptó su solicitud de mensaje");
            contacts.add(target);
            chats.add(new ArrayList<Msg>());
            chats.get(chats.size() - 1).add(msg);
        }//if
        else{ // target doesn't want to chat with us :(
            System.out.println(target.name + " rechazó su solicitud de mensaje");
        }//else
        requests.remove(msg);
    }//method

    public void readAll(Msg msg){
        User target = msg.getSender();
        int chatIndex = contacts.indexOf(target), i;
        if(chatIndex < 0)
            return;
        ArrayList<Msg> chat = chats.get(chatIndex);
        Date readDate = new Date();
        i = chat.indexOf(msg);
        while(i >= 0 && chat.get(i).getStatus() != 3){
            chat.get(i).updateStatus(3);
            chat.get(i).setDateRead(readDate);
            i--;
        }//while
    }//method

    public void updateMsgStatus(Msg msg, int newStatus){
        User target = msg.getTarget();
        int chatIndex = contacts.indexOf(target);
        ArrayList<Msg> chat = chats.get(chatIndex);
        Date date = new Date();
        if(newStatus == 2)
            msg.setDateReceived(date);
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

    @Override
    public String toString() {
        return "User{" +
                "key='" + key + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    // methods to display console ui
    public void displayChat(String targetName, int objCount){
        User target = searchContact(targetName);
        Msg lastMsg = getLastMsgFromSender(target);
        String currentMsg = "";
        if(lastMsg!= null)
            readAll(lastMsg);
        int chatIndex = contacts.indexOf(target) ,index = Math.max(0, chats.get(chatIndex).size()-objCount);
        for(Msg msg: chats.get(chatIndex)){
            if(msg.getSender().equals(this))
                currentMsg = "You: ";
            else
                currentMsg = msg.getSender().getName() + ": ";
            currentMsg += msg.toString();
            System.out.println(currentMsg);
        }//for
    }//method

    public Msg getLastMsgFromSender(User target){
        int j = 0 , i = 1;
        while(j < contacts.size() && !contacts.get(j).getName().equals(target.getName()))
            j++;
        if(j == contacts.size())
            return null;
        ArrayList<Msg> chat = chats.get(j);
        if(chat.size() == 0)
            return null;
        while(i < chat.size() && chat.get(chat.size()-i).getSender().equals(this))
            i++;
        return chat.get(chat.size()-(i));
    }//method

    public void displayChats(){

    }//method

    public void displayContacts(){
        System.out.println("Para abrir algún chat usa el comando '?chat' seguido del nombre de la persona con quien quieres chatear");
        System.out.println("Ejemplo: '?chat Juan' para chatear con Juan");
        System.out.println("Contactos:");
        String currentChatDisplay = "";

        for (User contact : contacts) {
            System.out.println(contact);
        }//method
    }//method

}//class
