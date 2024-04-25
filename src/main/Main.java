package main;
import javax.swing.*;
import java.awt.*;
public class Main{
    public static void main(String[] args) {

        JFrame window=new JFrame("Simple Chess");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        GamePanel gp=new GamePanel();
        window.add(gp);
        window.pack();
        gp.launchGame();

        window.setLocationRelativeTo(null);
        window.setVisible(true);


    }
}
