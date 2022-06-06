package app;
import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;

public class event extends gui{
    Connection con;
    Statement stSend,stSignIn,stReceive,stSignUp,stForget,stLogs;
    ResultSet rsSend,rsSignIn,rsForget,rsLogs;
    TOOL t;
    static int count=0,portReceive=5555,portSend=6666,portSignIn=8080,portSignUp=3333,portForget=2222,portForgetThread=7777,portReceiveFile=5431;
    ServerSocket serverSocketSend,serverSocketReceive,serverSocketSignUp,serverSocketSignIn,serverSocketForget,serverSocketForgetPass;
    static String log="";
    ArrayList logs;

    //creating events handlers:-
    event(){

        long start = System.currentTimeMillis();


        t=new TOOL();
        logs=new ArrayList<String>();


        try {
            con=DriverManager.getConnection("jdbc:mysql://localhost/emailSystem","root","root");
            stLogs=con.createStatement();
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
                    }catch (Exception er){
                        String sql="INSERT INTO _logs(log) VALUES ('"+ er +"');";
                        String sql1="select * from _logs;";
                        try {
                            stLogs.executeUpdate(sql);
                            rsLogs=stLogs.executeQuery(sql1);
                            while(rsLogs.next()){
                                log+=rsLogs.getString("log")+"\t"+rsLogs.getString("_date")+"\n\n\n";
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                        logs_txt.setText(log);
                    }

                    while (true){
                        try {
                            String sender = t.receive(serverSocketReceive.accept());
                            String receiver = t.receive(serverSocketReceive.accept());
                            String msg = t.receive(serverSocketReceive.accept());


                            if (!sender.equals("") && !receiver.equals("") && !msg.equals("")) {
                                String query = "INSERT INTO email(sender,receiver,msg) VALUES ( '" + sender + "', '" + receiver + "', '" + msg + "');";
                                stReceive.executeUpdate(query);

                                //setting the logs up:-
                                String log="Sender:"+sender+" sending "+"Mail:"+ msg +" to:"+receiver+"\n\n\n";
                                String sql="INSERT INTO _logs(log) VALUES ('"+ log +"');";
                                stLogs.executeUpdate(sql);

                                //displaying the logs into the screen:-
                                String sql1="select * from _logs;";
                                rsLogs=stLogs.executeQuery(sql1);

                                while(rsLogs.next()){
                                    logs.add(rsLogs.getString("log")+"\t"+rsLogs.getString("_date")+"\n\n");
                                }
                                logs_txt.setText(Arrays.toString(logs.toArray()));

                            }

                        } catch (Exception er) {
                            String sql="INSERT INTO _logs(log) VALUES ('"+ er +"');";
                            String sql1="select * from _logs;";
                            try {
                                stLogs.executeUpdate(sql);
                                rsLogs=stLogs.executeQuery(sql1);
                                while(rsLogs.next()){
                                    log+=rsLogs.getString("log")+"\t"+rsLogs.getString("_date")+"\n\n\n";
                                }
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            logs_txt.setText(log);
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
                                Thread.sleep(200);
                                String sender=rsSend.getString("sender");
                                String receiver=rsSend.getString("receiver");
                                String msg=rsSend.getString("msg");
                                t.send(serverSocketSend.accept(),sender);
                                t.send(serverSocketSend.accept(),receiver);
                                t.send(serverSocketSend.accept(),msg);
                            }
                        }catch (Exception er){
                            String sql1="INSERT INTO _logs(log) VALUES ('"+ er +"');";
                            String sql2="select * from _logs;";
                            try {
                                stLogs.executeUpdate(sql1);
                                rsLogs=stLogs.executeQuery(sql2);
                                while(rsLogs.next()){
                                    log+=rsLogs.getString("log")+"\t"+rsLogs.getString("_date")+"\n\n\n";
                                }
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            logs_txt.setText(log);
                        }
                    }
                });


                Thread signIn=new Thread(()-> {
                    try {
                        serverSocketSignIn=new ServerSocket(portSignIn);
                        stSignIn=con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                    }catch (Exception er){er.printStackTrace();}

                    while (true){
                        try {
                            String sql = "select * from auth;";
                            rsSignIn = stSignIn.executeQuery(sql);
                            String user = t.receive(serverSocketSignIn.accept());
                            String pass = t.receive(serverSocketSignIn.accept());

                                boolean flag=false;
                                while (rsSignIn.next()){
                                    String username=rsSignIn.getString("username");
                                    String password=rsSignIn.getString("password");

                                    if(user.equals(username) && pass.equals(password)) {
                                        flag=true;
                                        t.send(serverSocketSignIn.accept(), "yes");
                                        //setting the logs up:-
                                        String log="SignIn username:"+user+" password:"+pass+" Server Response: yes!"+"\n\n\n";
                                        String sql1="INSERT INTO _logs(log) VALUES ('"+ log +"');";
                                        stLogs.executeUpdate(sql1);

                                        //displaying the logs into the screen:-
                                        String sql2="select * from _logs;";
                                        rsLogs=stLogs.executeQuery(sql2);

                                        while(rsLogs.next()){
                                            logs.add(rsLogs.getString("log")+"\t"+rsLogs.getString("_date")+"\n\n");
                                        }
                                        logs_txt.setText(Arrays.toString(logs.toArray()));


                                        break;
                                    }
                                }
                                if (flag==false) {

                                    //setting the logs up:-
                                    String log="SignIn username:"+user+" password:"+pass+" Server Response: no!"+"\n\n\n";
                                    String sql1="INSERT INTO _logs(log) VALUES ('"+ log +"');";
                                    stLogs.executeUpdate(sql1);

                                    //displaying the logs into the screen:-
                                    String sql2="select * from _logs;";
                                    rsLogs=stLogs.executeQuery(sql2);

                                    while(rsLogs.next()){
                                        logs.add(rsLogs.getString("log")+"\t"+rsLogs.getString("_date")+"\n\n");
                                    }
                                    logs_txt.setText(Arrays.toString(logs.toArray()));

                                    t.send(serverSocketSignIn.accept(), "no");
                                }


                        }catch (Exception er){
                            String sql1="INSERT INTO _logs(log) VALUES ('"+ er +"');";
                            String sql2="select * from _logs;";
                            try {
                                stLogs.executeUpdate(sql1);
                                rsLogs=stLogs.executeQuery(sql2);
                                while(rsLogs.next()){
                                    log+=rsLogs.getString("log")+"\t"+rsLogs.getString("_date")+"\n\n\n";
                                }
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            logs_txt.setText(log);
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


                                //setting the logs up:-
                                String log="SignUp username:"+user+" password:"+pass+" Server Response: yes!"+"\n\n\n";
                                String sql1="INSERT INTO _logs(log) VALUES ('"+ log +"');";
                                stLogs.executeUpdate(sql1);

                                //displaying the logs into the screen:-
                                String sql2="select * from _logs;";
                                rsLogs=stLogs.executeQuery(sql2);

                                while(rsLogs.next()){
                                    logs.add(rsLogs.getString("log")+"\t"+rsLogs.getString("_date")+"\n\n");
                                }
                                logs_txt.setText(Arrays.toString(logs.toArray()));


                            }

                        }catch (Exception er){
                            String sql1="INSERT INTO _logs(log) VALUES ('"+ er +"');";
                            String sql2="select * from _logs;";
                            try {
                                stLogs.executeUpdate(sql1);
                                rsLogs=stLogs.executeQuery(sql2);
                                while(rsLogs.next()){
                                    log+=rsLogs.getString("log")+"\t"+rsLogs.getString("_date")+"\n\n\n";
                                }
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            logs_txt.setText(log);
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

                                    //setting the logs up:-
                                    String log="Forget Request username:"+user+ " Retrieved password:"+pass+"\n\n\n";
                                    String sql1="INSERT INTO _logs(log) VALUES ('"+ log +"');";
                                    stLogs.executeUpdate(sql1);

                                    //displaying the logs into the screen:-
                                    String sql2="select * from _logs;";
                                    rsLogs=stLogs.executeQuery(sql2);

                                    while(rsLogs.next()){
                                        logs.add(rsLogs.getString("log")+"\t"+rsLogs.getString("_date")+"\n\n");
                                    }
                                    logs_txt.setText(Arrays.toString(logs.toArray()));

                                    break;
                                }
                            }
                        }

                        }catch (Exception er){
                            String sql1="INSERT INTO _logs(log) VALUES ('"+ er +"');";
                            String sql2="select * from _logs;";
                            try {
                                stLogs.executeUpdate(sql1);
                                rsLogs=stLogs.executeQuery(sql2);
                                while(rsLogs.next()){
                                    log+=rsLogs.getString("log")+"\t"+rsLogs.getString("_date")+"\n\n\n";
                                }
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                            logs_txt.setText(log);
                        }
                    }
                });


                Thread fileReceiving=new Thread(()->{
                    TOOL tool=new TOOL();
                    try {
                        byte[]data=tool.receiveBytes(new ServerSocket(portReceiveFile).accept());

                        if(data!="".getBytes(StandardCharsets.UTF_8)){
                            File f=new File("file.txt");
                            OutputStream out=new FileOutputStream(f);
                            out.write(data);
                            out.flush();
                            out.close();
                        }


                    }catch (Exception er){
                        er.printStackTrace();
                    }
                });



                fileReceiving.start();
                forget.start();
                receive.start();
                send.start();
                signIn.start();
                signUp.start();

            }
            count++;
        });


        long end = System.currentTimeMillis();



        System.out.println("benchmarks:"+(end - start));


    }
}
