import java.io.*;
import java.nio.file.*;

public class Fix {
    public static void main(String[] args) throws Exception {
        Files.walk(Paths.get("d:/Java/G-003-Chain-Reaction/src")).filter(Files::isRegularFile).forEach(path -> {
            try {
                if(path.toString().contains("HandlerPlayers.java")) return;
                
                String content = new String(Files.readAllBytes(path));
                boolean changed = false;
                if (content.contains("HandlerPlayers.")) { content = content.replace("HandlerPlayers.", "main.handlerPlayers."); changed = true; }
                if (content.contains("main.saveStates()")) { content = content.replace("main.saveStates()", "main.saveState()"); changed = true; }
                if (content.contains("main.undoStates()")) { content = content.replace("main.undoStates()", "main.undoState()"); changed = true; }
                if (changed) Files.write(path, content.getBytes());
            } catch(Exception e){}
        });
    }
}
