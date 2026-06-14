import java.io.*;
import java.nio.file.*;

public class FixMain {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("d:/Java/G-003-Chain-Reaction/src/MainSystem/Main/Main.java");
        String content = new String(Files.readAllBytes(path));
        
        // 1. Add imports
        content = content.replace("import ManagerSystem.Handlers.HandlerSystem.HandlerRender;", "import ManagerSystem.Handlers.HandlerSystem.HandlerClick;\r\nimport ManagerSystem.Handlers.HandlerSystem.HandlerRender;");
        
        // 2. Add instance fields
        content = content.replace("public AbstractMap map = new Map16();", "public AbstractMap map = new Map16();\r\n    \r\n    public final HandlerRender handlerRender = new HandlerRender();\r\n    public final HandlerTick handlerTick = new HandlerTick();\r\n    public final HandlerCell handlerCell = new HandlerCell();\r\n    public final HandlerPlayers handlerPlayers = new HandlerPlayers();\r\n    public final HandlerObjects handlerObjects = new HandlerObjects();\r\n    public final HandlerClick handlerClick = new HandlerClick();");
        
        // 3. Remove static main assignments
        content = content.replace("HandlerRender.main = main;\r\n        HandlerTick.main = main;\r\n        HandlerCell.main = main;\r\n        HandlerPlayers.main = main;", "");
        content = content.replace("HandlerRender.main = main;\n        HandlerTick.main = main;\n        HandlerCell.main = main;\n        HandlerPlayers.main = main;", "");
        
        // 4. updateGridSize
        content = content.replace("SettingsCell.xCell = mapSize[0];\r\n            SettingsCell.yCell = mapSize[1];", "SettingsCell.xCell = mapSize[0];\r\n            SettingsCell.yCell = mapSize[1];\r\n            handlerCell.updateGridSize();");
        content = content.replace("SettingsCell.xCell = mapSize[0];\n            SettingsCell.yCell = mapSize[1];", "SettingsCell.xCell = mapSize[0];\n            SettingsCell.yCell = mapSize[1];\n            handlerCell.updateGridSize();");
        
        // 5. Replace Handler method calls with instance method calls
        content = content.replace("HandlerCell.updateCells();", "handlerCell.updateCells(this);");
        content = content.replace("HandlerObjects.add(new PlayerIndicate((double)(canvasSize.width - 110), (double)y, dP));", "handlerObjects.add(new PlayerIndicate((double)(canvasSize.width - 110), (double)y, dP), this);");
        content = content.replace("HandlerCell.getArray()", "handlerCell.getArray()");
        content = content.replace("HandlerPlayers.tick();", "handlerPlayers.tick(this);");
        content = content.replace("HandlerPlayers.getPlayerMoves()", "handlerPlayers.getPlayerMoves()");
        content = content.replace("HandlerPlayers.getPlayerColor()", "handlerPlayers.getPlayerColor()");
        content = content.replace("HandlerCell.getCellMoveableArray()", "handlerCell.getCellMoveableArray()");
        content = content.replace("HandlerObjects.add(c);", "handlerObjects.add(c, this);");
        content = content.replace("HandlerCell.Reset();", "handlerCell.Reset();");
        content = content.replace("HandlerPlayers.Reset();", "handlerPlayers.Reset();");
        content = content.replace("HandlerObjects.RemoveAll();", "handlerObjects.RemoveAll(this);");
        content = content.replace("HandlerCell.ResetState();", "handlerCell.ResetState();");
        content = content.replace("HandlerPlayers.ResetState();", "handlerPlayers.ResetState();");
        content = content.replace("HandlerCell.SaveState();", "handlerCell.SaveState();");
        content = content.replace("HandlerPlayers.SaveStates();", "handlerPlayers.SaveStates(undoLimit);");
        content = content.replace("HandlerTick.pause", "handlerTick.pause");
        content = content.replace("HandlerCell.UndoState();", "handlerCell.UndoState();");
        content = content.replace("HandlerPlayers.UndoStates();", "handlerPlayers.UndoStates();");

        Files.write(path, content.getBytes());
    }
}
