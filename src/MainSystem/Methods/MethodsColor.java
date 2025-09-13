
package MainSystem.Methods;

import java.awt.Color;

public class MethodsColor{

    public static String getColorName(Color color){
        if(color == Color.green){
            return "green";
        }else if(color == Color.white){
            return "white";
        }else if(color == Color.lightGray){
            return "lightGray";
        }else if(color == Color.gray){
            return "gray";
        }else if(color == Color.darkGray){
            return "darkGray";
        }else if(color == Color.black){
            return "black";
        }else if(color == Color.red){
            return "red";
        }else if(color == Color.pink){
            return "pink";
        }else if(color == Color.orange){
            return "orange";
        }else if(color == Color.yellow){
            return "yellow";
        }else if(color == Color.magenta){
            return "magenta";
        }else if(color == Color.cyan){
            return "cyan";
        }else if(color == Color.blue){
            return "blue";
        }
        return null;
    }
    
    public static Color getColor(String name){
        return switch(name){
            case "green" -> Color.green;
            case "white" -> Color.white;
            case "lightGray" -> Color.lightGray;
            case "gray" -> Color.gray;
            case "darkGray" -> Color.darkGray;
            case "black" -> Color.black;
            case "red" -> Color.red;
            case "pink" -> Color.pink;
            case "orange" -> Color.orange;
            case "yellow" -> Color.yellow;
            case "magenta" -> Color.magenta;
            case "cyan" -> Color.cyan;
            case "blue" -> Color.blue;
            default -> null;
        };
    }
    
    public static Color getNextAvailableColor(Color c){
        if(c == Color.green){
            return Color.pink;
            
        }else if(c == Color.pink){
            return Color.orange;
            
        }else if(c == Color.orange){
            return Color.yellow;
            
        }else if(c == Color.yellow){
            return Color.magenta;
            
        }else if(c == Color.magenta){
            return Color.cyan;
            
        }else if(c == Color.cyan){
            return Color.blue;
            
        }else if(c == Color.blue){
            return Color.green;
        }
        return null;
    }
    
}
