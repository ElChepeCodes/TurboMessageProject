package TurboMessage;

public class Command {
    private String name;
    private String description;

    public Command(String name, String description){
        this.name = name;
        this.description = description;
    }//builder

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}//class