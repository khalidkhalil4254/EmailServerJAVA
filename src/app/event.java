package app;
import java.io.*;
import java.net.*;
import java.sql.*;

public class event extends gui{
    Connection con;
    Statement stSend,stSignIn,stReceive,stSignUp;
    ResultSet rsSend,rsSignIn;
    TOOL t;
    static int count=0,portReceive=5555,portSend=6666,portSignIn=4444,portSignUp=3333;
    ServerSocket serverSocketSend,serverSocketReceive,serverSocketSignUp,serverSocketSignIn;


    event(){
        t=new TOOL();


        try {
            con=DriverManager.getConnection("jdbc:mysql://localhost/emailSystem","root","root");
            System.out.println("DB connected successfully!");
        }catch (Exception er){er.printStackTrace();}

        //creating events handlers:-
        ext_btn.addActionListener((e)-> System.exit(1));

        start_btn.addActionListener((e)-> {

            if(count==0){
                status_lbl.setText("Status: Started!");
                Thread receive=new Thread(()-> {
                    try{
                        serverSocketReceive=new ServerSocket(portReceive);
                        stReceive= con.createStatement();
                    }catch (Exception er){}

                    while (true){
                        try {
                            String sender = t.receive(serverSocketReceive.accept());
                            String receiver = t.receive(serverSocketReceive.accept());
                            String msg = t.receive(serverSocketReceive.accept());

                            if (!sender.equals("") && !receiver.equals("") && !msg.equals("")) {
                                String query = "INSERT INTO email(sender,receiver,msg) VALUES ( '" + sender + "', '" + receiver + "', '" + msg + "');";
                                stReceive.executeUpdate(query);
                            }

                        } catch (Exception ex) {
                            System.out.println("receiving error:"+ex);
                        }
                    }
                });

                Thread send=new Thread(()->{

                    try{
                        serverSocketSend=new ServerSocket(portSend);
                        stSend=con.createStatement();
                    }catch (Exception er){}

                    while(true){
                        String sql="select * from email;";
                        try {
                            rsSend=stSend.executeQuery(sql);
                            while(rsSend.next()){
                                String sender=rsSend.getString("sender");
                                String receiver=rsSend.getString("receiver");
                                String msg=rsSend.getString("msg");
                                t.send(serverSocketSend.accept(),sender);
                                t.send(serverSocketSend.accept(),receiver);
                                t.send(serverSocketSend.accept(),msg);
                            }
                        }catch (Exception er){
                            System.out.println("sending error:"+er);
                        }
                    }
                });


                Thread signIn=new Thread(()-> {
                    try {
                        serverSocketSignIn=new ServerSocket(portSignIn);
                        stSignIn=con.createStatement();
                    }catch (Exception er){}

                    while (true){
                        try {
                            String sql = "select * from auth;";
                            rsSignIn = stSignIn.executeQuery(sql);
                            String user = t.receive(serverSocketSignIn.accept());
                            System.out.println("username received!="+user);
                            String pass = t.receive(serverSocketSignIn.accept());
                            System.out.println("PASSWORD received!="+pass);
                            if(!user.equals("") && !pass.equals("")){
                                while (rsSignIn.next()){
                                    String username=rsSignIn.getString("username");
                                    String password=rsSignIn.getString("password");
                                    if(user.equals(username) && pass.equals(password)){
                                        t.send(serverSocketSignIn.accept(),"yes");
                                        break;
                                    }else if(!user.equals(username) && !pass.equals(password)){
                                        t.send(serverSocketSignIn.accept(),"no");
                                    }
                                }
                            }
                        }catch (Exception er){
                            System.out.println("signIn error:"+er);
                        }
                    }
                });

                Thread signUp=new Thread(()->{

                    try {
                        serverSocketSignUp=new ServerSocket(portSignUp);
                        stSignUp=con.createStatement();
                    }catch (Exception er){}

                    while(true){
                        try {
                            String user=t.receive(serverSocketSignUp.accept());
                            String pass=t.receive(serverSocketSignUp.accept());

                            if(!user.equals("") && !pass.equals("")){
                                String sql="INSERT INTO auth(username,password) VALUES ( '"+user+"', '"+pass+"');";
                                stSignUp.executeUpdate(sql);
                            }

                        }catch (Exception er){
                            System.out.println("signUp error:"+er);
                        }
                    }

                });


                receive.start();
                send.start();
                signIn.start();
                signUp.start();
            }

            count++;

        });

    }
}
