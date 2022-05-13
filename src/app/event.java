package app;
import java.net.*;
import java.sql.*;

public class event extends gui{
    Connection con;
    Statement stSend,stSignIn,stReceive,stSignUp,stForget;
    ResultSet rsSend,rsSignIn,rsForget;
    TOOL t;
    static int count=0,portReceive=5555,portSend=6666,portSignIn=4444,portSignUp=3333,portForget=2222,portForgetThread=7777;
    ServerSocket serverSocketSend,serverSocketReceive,serverSocketSignUp,serverSocketSignIn,serverSocketForget,serverSocketForgetPass;

    //creating events handlers:-
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
                    }catch (Exception er){er.printStackTrace();}

                    while (true){
                        try {
                            String sender = t.receive(serverSocketReceive.accept());
                            String receiver = t.receive(serverSocketReceive.accept());
                            String msg = t.receive(serverSocketReceive.accept());

                            System.out.println(sender+" "+receiver+" "+msg);

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
                    }catch (Exception er){er.printStackTrace();}

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
                    }catch (Exception er){er.printStackTrace();}

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
                    }catch (Exception er){er.printStackTrace();}

                    while(true){
                        try {
                            String user=t.receive(serverSocketSignUp.accept());
                            String pass=t.receive(serverSocketSignUp.accept());
                            System.out.println(user+" "+pass);
                            if(!user.equals("") && !pass.equals("")){
                                System.out.println(user+" "+pass);
                                String sql="INSERT INTO auth(username,password) VALUES ( '"+user+"', '"+pass+"');";
                                stSignUp.executeUpdate(sql);
                                t.send(serverSocketSignUp.accept(),"yes");
                            }

                        }catch (Exception er){
                            System.out.println("signUp error:"+er);
                        }
                    }

                });

                Thread forget=new Thread(()->{
                    try {
                        serverSocketForget=new ServerSocket(portForget);//for receiving username
                        serverSocketForgetPass=new ServerSocket(portForgetThread);//for sending the password to the user
                        stForget=con.createStatement();
                    }catch (Exception er){er.printStackTrace();}

                    while(true){
                        try {
                            String user=t.receive(serverSocketForget.accept());

                        if(!user.equals("")){
                            System.out.println(user);
                            String sql="select * from auth;";
                            rsForget=stForget.executeQuery(sql);
                            while(rsForget.next()){
                                String username=rsForget.getString("username");
                                String pass=rsForget.getString("password");
                                if(username.equals(user)){
                                    t.send(serverSocketForgetPass.accept(),pass);
                                    break;
                                }
                            }
                        }

                        }catch (Exception er){
                            System.out.println("signUp error:"+er);
                        }
                    }
                });

                forget.start();
                receive.start();
                send.start();
                signIn.start();
                signUp.start();
            }
            count++;
        });

    }
}
