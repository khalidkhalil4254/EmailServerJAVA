package app;
import javax.swing.*;
import java.awt.*;

public class gui {
    JFrame app;
    JLabel logs_lbl,status_lbl;
    JTextArea logs_txt;
    JButton start_btn,ext_btn;
    JScrollPane scroll;

    gui(){

        //creating the App JFrame:-
        app=new JFrame("Server");
        app.setLayout(null);
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.setResizable(false);
        app.setSize(800,800);

        //creating the App components and setting positions:-
        logs_lbl=new JLabel("Logs:-");
        logs_lbl.setFont(new Font("segue UI",Font.BOLD,20));
        logs_lbl.setBounds(30,30,100,30);

        logs_txt=new JTextArea();
        logs_txt.setEditable(false);
        logs_txt.setFont(new Font("verdana",Font.PLAIN,18));
        scroll = new JScrollPane (logs_txt, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBounds(50,90,680,400);

        status_lbl=new JLabel("Status: Stopped!");
        status_lbl.setFont(new Font("segue UI",Font.BOLD,14));
        status_lbl.setBounds(338,520,180,30);

        start_btn=new JButton("Start");
        start_btn.setFont(new Font("segue UI",Font.BOLD,16));
        start_btn.setBounds(300,570,200,60);

        ext_btn=new JButton("Exit");
        ext_btn.setFont(new Font("segue UI",Font.BOLD,16));
        ext_btn.setBounds(300,650,200,60);

        //adding the components into the JFrame:-
        app.add(logs_lbl);
        app.add(scroll);
        app.add(status_lbl);
        app.add(start_btn);
        app.add(ext_btn);

        //showing the app to the user:-
        app.setVisible(true);

    }
}
