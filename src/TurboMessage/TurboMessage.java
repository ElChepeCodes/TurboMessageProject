package TurboMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class TurboMessage {
    // 0-main menu, 1-chat list, 2-chat with someone
    private int currentStatus, objCount = 15;
    private final User user;
    private User currentChatter;
    private final Command [] commands;
    private Scanner scanner;

    public TurboMessage(User user){
        this.user = user;
        currentStatus = 0;
        commands = loadCommands();
    }//builder

    public Command [] loadCommands(){
        try{
            File file = new File("commands.txt");
            Scanner scanner = new Scanner(file);
            String currentLine;
            String [] splitLine = new String[2];
            Command currentCommand;
            ArrayList<Command> commands = new ArrayList<Command>();
            while(scanner.hasNextLine()){
                currentLine = scanner.nextLine();
                splitLine = currentLine.split("_");
                currentCommand = new Command(splitLine[0], splitLine[1]);
                commands.add(currentCommand);
            }//while
            return commands.toArray(new Command[0]);
        }//try
        catch (Exception e){
            e.printStackTrace();
        }//catch
        return null;
    }//method

    public void sendHelp(){
        System.out.println("Esta es la lista de comandos:\n\n");
        for (Command command : commands)
            System.out.println(command.getName() + ":  " + command.getDescription());
    }//method

    public boolean processCommand(String command){
        String [] lineSplit;
        if(command.equals("?quit")) // quit command
            return true;
        if(command.equals("?help")) // help command
            sendHelp();
        else if(command.equals("?chats"))
            user.displayChats();
        else if(command.equals("?contacts"))
            user.displayContacts();
        else if(command.equals("?up"))
            System.out.println("Aún no se implementa esta funcionalidad");
        else if(command.equals("?down"))
            System.out.println("Aún no se implementa esta funcionalidad");
        else if(command.contains("?setPg")){ // set new page count command
            try{
                lineSplit = command.split(" ");
                objCount = Integer.parseInt(lineSplit[1]);
                System.out.println("Ahora se mostrarán " + objCount + " elementos por página");
            }//try
            catch (Exception e){
                System.out.println("Lo siento, no entendí lo que me pediste, un ejemplo de este comando es");
                System.out.println("?setPg 20");
            }//catch
        }//if
        else if(command.contains("?chat")){ // set new page count command
            try{
                lineSplit = command.split(" ");
                user.displayChat(lineSplit[1], objCount);
            }//try
            catch (Exception e){
                //e.printStackTrace();
                System.out.println("Lo siento, no encontré un chat con esa persona");
            }//catch
        }//if
        else if(command.contains("?msg")){ // set new page count command
            try{
                lineSplit = command.split(" ");
                lineSplit = lineSplit[1].split("#"); // extract target name and number
                User target = new User(lineSplit[0], Integer.parseInt(lineSplit[1]));
                System.out.println("Escribe el mensaje que le quieres enviar a " + lineSplit[0]);
                Scanner scanner = new Scanner(System.in);
                user.sendMessage(scanner.nextLine(), target);
                System.out.println("Se envió tu mensaje exitosamente ");
            }//try
            catch (Exception e){
                System.out.println("Surgió un error al enviar tu mensaje");
            }//catch
        }//if
        return false;
    }//method

    public void StartApp(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("¡Gracias por usar Turbo Message " + user.getName() + "!");
        System.out.println("Este servicio de mensajería funciona desde la consola y para navegar la aplicación es necesario usar comandos");
        System.out.println("Para indicar que quieres usar un comando, comienza con el caracter '?' ");
        System.out.println("Para ver la lista de comandos en cualquier momento utiliza el comando '?help'");
        System.out.println("Puedes cerrar la aplicación con el comando '?quit'");
        if(user.getContacts().size() == 0) {
            System.out.println("Parece que no tienes contactos aún, puedes enviarle un mensaje a cualquier usuario de TM con el comando '?msg' seguido de su key de usuario");
            System.out.println("Ejemplo: '?msg Alex#34'");
            System.out.println("También puedes darle tu key de usuario a cualquier persona usando Turbo Message para que te envíe un mensaje");
            System.out.println("Tu key de usuario es: " + user.getKey());
            currentStatus = 0;
        }//if
        else{
            user.displayContacts();
            currentStatus = 1;
        }//else
        user.startListening();
        boolean exit = false;
        String nextLine;
        while(!exit){
            nextLine = scanner.nextLine();
            if(nextLine.indexOf('?') == 0){
                exit = processCommand(nextLine);
            }//if
            if(nextLine.equalsIgnoreCase("y") || nextLine.equalsIgnoreCase("n"))
                if (nextLine.equalsIgnoreCase("y"))
                    user.acceptRequest();
                else
                    user.denyRequest();
        }//while
    }//method

    // contactos, mensajes, nuevo contacto
    public static void main(String[] args) {
        User Josepe = new User("Josepe");
        TurboMessage turboMessage = new TurboMessage(Josepe);
        turboMessage.StartApp();
    }//main
}//class
