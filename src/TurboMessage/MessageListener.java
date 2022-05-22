package TurboMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class MessageListener extends Thread{
    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    private User user;

    public MessageListener(User user){
        this.user = user;
    }

    @Override
    public void run() {
        try{
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            connectionFactory.setTrustAllPackages(true);
            Connection connection = connectionFactory.createConnection();
            connection.start();

            Session session = connection.createSession(false /*Transacter*/, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(user.getKey());
            MessageConsumer messageConsumer = session.createConsumer(destination);

            while (true) {
                //System.out.println("Waiting for messages...");
                ObjectMessage objectMessage = (ObjectMessage) messageConsumer.receive();
                if (objectMessage != null) {
                    //System.out.println(objectMessage.toString());
                    //System.out.print("Received the following message: ");
                    Object obj = objectMessage.getObject();
                    if(obj instanceof Msg) { //se recibió un mensaje normal
                        Msg msg = (Msg) obj;
                        //System.out.println(msg.getMsg());
                        //System.out.println();
                        user.receiveMessage(msg);
                    }//if
                    else{
                        if(obj instanceof RequestResponse) {// se recibió un requestResponse
                            RequestResponse response = (RequestResponse) obj;
                            user.receiveRequestResponse(response);
                        }//if
                        else{
                            if(obj instanceof MessageUpdate){
                                MessageUpdate update = (MessageUpdate) obj;
                                Msg msg = update.getMsg();
                                if(update.getReadAll()){ // sender read the conversation
                                    user.readAll(msg);
                                }//if
                                else{ // sender hasn't read conversation
                                    user.updateMsgStatus(msg, update.getNewStatus());
                                }//else
                            }//if
                            else{ // this shouldn't happen
                                System.out.println("idk what's happening *panics*");
                            }//else
                        }//else
                    }//else
                }//if
            }//while
        }//try
        catch(Exception exception){
            exception.printStackTrace();
        }//catch
    }
}//class
