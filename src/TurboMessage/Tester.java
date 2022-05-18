package TurboMessage;

public class Tester {
    public static void main(String[] args) {
        User user1 = new User("Usuario 1");
        User user2 = new User("Usuario 2");
        //user1.addChat(user2);
        user1.startListening();
        user2.startListening();
        user2.sendMessage("Hola desde user2", user1);

    }//main
}//class
