import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.MouseInfo;
import java.awt.Point;
 public class MouseControll 
{
    private Robot hand;
    MouseControll() throws Exception
    {
        hand=new Robot();
    }

    void moveMouse(int x,int y)throws Exception {
        Point p=MouseInfo.getPointerInfo().getLocation();
        hand.mouseMove(p.x+x,p.y+y);
    }

    void leftClickMouse() throws Exception
    {
        hand.mousePress(MouseEvent.BUTTON1_DOWN_MASK);
        hand.mouseRelease(MouseEvent.BUTTON1_DOWN_MASK);
    }
    void rightClickMouse() throws Exception
    {
        hand.mousePress(MouseEvent.BUTTON3_DOWN_MASK);
        hand.mouseRelease(MouseEvent.BUTTON3_DOWN_MASK);
    }
}
