//MessageServer.java

/*
Name: Alexander Sadakhom
ID (Student #): 0838002
ASSIGNMENT 1
CIS*4110
PROF. Rozita Dara
*/

//Assignment Package
package a2src;

//Imported libraries
import java.io.*;
import java.util.LinkedHashMap;
import java.util.*;

import net.thegreshams.firebase4j.error.FirebaseException;
import net.thegreshams.firebase4j.error.JacksonUtilityException;
import net.thegreshams.firebase4j.model.FirebaseResponse;
import net.thegreshams.firebase4j.service.Firebase;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;


import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;

//Entire message server class, extends JFrame library (GUI TOOLS) and action listener(Button responses)
public class messageServer extends JFrame implements ActionListener 
{
   //GUI layout variables
    public static final int WIDTH = 700;
    public static final int HEIGHT = 600;
    public static final int MAX_SIZE = 5;
    public static final int SMALL_WIDTH = 200;
    public static final int SMALL_HEIGHT = 100;
    
    //String that is pulled from the user's chatfield when they send a message
    public String inputString;
    //temp string variable to grab whatever chat already exists in the text area
    public String currentText;
    //concatenated string that includes timestamp, previous messages
    //and the message they are sending.
    public static String updatedChat;
    
    public static JTextArea chatField ;
    
    //variable that is used to check every .5 of a second to see if a message
    //has been sent to the server
    private static Timer newMessageTimer = new Timer();
   
    //integer that tracks the total number of active users in the chat
    public static int totalUsers = 0;
    
    public static JLabel titleTing;
    
    //Variable that connects to the database
    public static Firebase firebase;
    public static int i = 1;
    
    //varible that grabs messages (responses) from the server.
    public static FirebaseResponse response;
    
    //hash map that is used to store key values and their messages that are sent to the server
    public static Map<String, Object> dataMap = new LinkedHashMap<String, Object>();
    
    //GUI variables
    public static messageServer server;
    public static loginWindow loginWindow;

    public static String userName;
    
    //variable that is used to check every .5 of a second to see if number of active users has changed
    public static Timer activeUserTimer = new Timer();
    
    //variable that grabs the subdirectory that contains the active user number
    public static FirebaseResponse activeUserResponse;
    
    //hash map that stores the active user key & integer variable object.
    public static Map<String, Object> activeUserMap = new LinkedHashMap<String, Object>();
    public static JLabel activeUserLabel;
    
    public static java.util.Date timestamp = new java.util.Date();
   
    public static FirebaseResponse leaveJoinResponse;
    public static Map<String,Object> leaveJoinMap = new LinkedHashMap<String, Object>();
    
    //if 0 then not anonymous, if 1 then anonymous
    public static int anonymity = 1;
    
    
    public static JLabel anonyLabel;
    
    //Subclass that contains the GUI information for the login window
    public static class loginWindow extends JFrame implements ActionListener
    {
        public loginWindow() throws FirebaseException, JsonParseException, JsonMappingException, IOException, JacksonUtilityException
        {
            //declaring the specifics of the loginWindow GUI such as size, layout & background colour
            super("Login Buddy");
            setSize(400,250);
            setLayout(new FlowLayout());
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            getContentPane().setBackground(Color.WHITE);
            
            
            JPanel overviewPanel = new JPanel();
            overviewPanel.setBackground(Color.WHITE);
            overviewPanel.setSize(100,100);
            overviewPanel.setLayout(new FlowLayout());
            JLabel pleaseEnterUser = new JLabel("PLEASE ENTER YOUR EXISTING OR DESIRED USERNAME:");
            overviewPanel.add(pleaseEnterUser);
            
            //textfield where user enters their desired username
            JTextField userEnterField = new JTextField(25);
            
            userEnterField.setEditable(true);
            
            
            add(overviewPanel);
            add(userEnterField);
            
            JButton cancelButton = new JButton("CANCEL");
            JButton confirmButton = new JButton("CONFIRM");
            
            add(cancelButton);
            add(confirmButton);
            
            JLabel nameCheck = new JLabel("Please enter a user name of length 10 or less & with no spaces.");
            
            add(nameCheck);
            nameCheck.setVisible(true);
            
            
            JButton anony = new JButton("Toggle anonymity"); 
            anonyLabel = new JLabel("Anonymity is : ON");
            
            add(anony);
            add(anonyLabel);
            
            
            anony.addActionListener(new ActionListener()
            {
               public void actionPerformed(ActionEvent arg0)
               {
                    if(anonymity == 0)
                    {
                        anonymity = 1;
                        anonyLabel.setText("Anonymity is: ON");
                    }
                    else
                    {
                        anonymity = 0;
                        anonyLabel.setText("Anonymity is: OFF");
                    }
               }
            });
            
            //action listener that runs when the cancel button is clicked, it closes program completely
            cancelButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent arg0)
                {
                    System.exit(0);
                }
            });
            
            //runs when confirmButton is clicked, it does defensive programming to see if entered
            //username is up to specifications, if not it does nothing, if so it sends them to
            //the client
            confirmButton.addActionListener(new ActionListener()
            {
                
                public void actionPerformed(ActionEvent arg0)
                {
                    userName = userEnterField.getText();
                    if(userName.contains(" ") == true || userName.length() == 0 || userName.length() > 10)
                    {
                        userEnterField.setText("");
                    }
                    else
                    {
                        //this means username is accepted, sets login window to be invisible
                        //and sets client window to be true
                        loginWindow.setVisible(false);
                        server.setVisible(true);
                        titleTing.setText("WELCOME TO THE SERVER: " + userName);
                       
                        //increment total users and send it to the database
                        totalUsers++;
                        activeUserMap.clear();
                        activeUserMap.put("totalUsers" , totalUsers);
                        
                        try {
                            firebase.patch( "userList", activeUserMap );
                        } catch (FirebaseException ex) {
                            Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (JacksonUtilityException ex) {
                            Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
                        }  
                    }  
                }
            });
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    //GUI for the Client
    public messageServer() throws FirebaseException, JsonParseException, JsonMappingException, IOException, JacksonUtilityException
        {
            super("Alex Sadakhom Cis*4110 Assignment #1");
            setSize(WIDTH,HEIGHT);
            
            setLayout(new FlowLayout());
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            getContentPane().setBackground(Color.white);
            
            JPanel titlePanel = new JPanel();
            titlePanel.setLayout(new FlowLayout());
            titlePanel.setBackground(Color.GREEN);
            titlePanel.setPreferredSize(new Dimension(700,25));
            titleTing = new JLabel("Welcome To The Server: ");
            titlePanel.add(titleTing);
            add(titlePanel);
            
            JPanel chatBoxOuterPanel = new JPanel();
            chatBoxOuterPanel.setBackground(Color.BLUE);
            chatBoxOuterPanel.setLayout(new FlowLayout());
            chatBoxOuterPanel.setPreferredSize(new Dimension(700,400));
            
            JPanel chatBoxInnerPanel = new JPanel();
            chatBoxInnerPanel.setBackground(Color.WHITE);
            chatBoxInnerPanel.setPreferredSize(new Dimension(685,390));
            chatField = new JTextArea();
            chatField.setEditable(false);
            chatField.setLineWrap(true);
            chatField.setWrapStyleWord(true);
            chatField.setText("");
            JScrollPane chatBox = new JScrollPane(chatField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            chatBox.setPreferredSize(new Dimension(550, 385));
            chatBox.setBorder(BorderFactory.createLineBorder(Color.blue, 5));
            chatBoxInnerPanel.add(chatBox);
            chatBoxOuterPanel.add(chatBox);
            
            JPanel activeUserPanel = new JPanel();
            activeUserPanel.setBackground(Color.GREEN);
            
            //dynamically updating label that holds total active users.
            String labelString = ("ACTIVE USERS: " + Integer.toString(totalUsers));
            activeUserLabel = new JLabel(labelString);
            activeUserPanel.setPreferredSize(new Dimension(120,25));
            
            
            chatBox.setBorder(BorderFactory.createLineBorder(Color.blue, 5));
            
            activeUserPanel.add(activeUserLabel);
            chatBoxOuterPanel.add(activeUserPanel);
            
            add(chatBoxOuterPanel);
            
            //following block of code contains the send button and user input text field
            JButton sendButton = new JButton("SEND");
            JTextField inputField = new JTextField(49);
            JPanel inputFieldAndButtonPanel = new JPanel();
            inputFieldAndButtonPanel.add(sendButton);
            inputFieldAndButtonPanel.add(inputField);
            add(inputFieldAndButtonPanel);
            
            JButton quitButton = new JButton("QUIT");
            JButton clearChatLog = new JButton("Clear Chat Log");
            JPanel quitPanel = new JPanel();
            
            quitPanel.add(clearChatLog);
            quitPanel.add(quitButton);
            add(quitPanel);
            
            //quit button action listener, decerements total active by users by 1 and sends updated
            //number to the database. Then quits program completely.
            quitButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent arg0)
                {
                    //ADD CODE FOR THIS USER LEAVING THE CHANNEL TO BE REMOVED FROM THE SCROLLBOX
                    //THAT CONTAINS ALL ACTIVE USERS
                    
                    totalUsers--;
                    activeUserMap.clear();
                    activeUserMap.put("totalUsers" , totalUsers);
        
                    try {
                        firebase.patch( "userList", activeUserMap );
                    } catch (FirebaseException ex) {
                        Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (JacksonUtilityException ex) {
                        Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
        
                    activeUserMap.clear();

                    System.exit(0);
                }
            });
            
            clearChatLog.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent arg0)
                {
                    chatField.setText("");
                }
            });
            
            
            //action listener for send button. Grabs input string from the user input field, 
            //concatenates their message, timestamp, and the previous
            //messages on the database together into one string and reprints it onto the server
            sendButton.addActionListener(new ActionListener() 
            {
                public void actionPerformed(ActionEvent arg0)
                {
                    inputString  = inputField.getText();
                    
                    if(anonymity == 1)
                    {
                        inputString = inputString + " (" + timestamp + ")";
                        currentText = chatField.getText();
                        inputField.setText("");
                    }
                    
                    else
                    {
                        inputString = userName + ": " + inputString + " (" + timestamp + ")";
                        currentText = chatField.getText();
                        inputField.setText("");
                    }
                    
                    try 
                    {
                        sendToServer(firebase, inputString);
                    } catch (FirebaseException ex) {
                        Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (JacksonUtilityException ex) {
                        Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } 
            });
            
            //grabbing the body of messages from the root directory on the database
            //loops through entire array of messages and prints them all out in the text area
            response = firebase.get();
            dataMap = response.getBody();
            int j = 1;
            while(dataMap.get(Integer.toString(j)) != null)
            {
                chatField.append(((String) dataMap.get(Integer.toString(j))) + "\n");
                j++;
            }
            
            i = j;
	}
    
    public static void main (String args[]) throws FirebaseException, JsonParseException, JsonMappingException, IOException, JacksonUtilityException
    {
        //initializes the database with the link that is connected to my database
	firebase = new Firebase( "https://cis4110a1-681df.firebaseio.com/" );
        //sets login window to visible
        loginWindow = new loginWindow();
        loginWindow.setVisible(true);
        
        //sets server window to invisible until they enter an accepted username
        //refer to action listener code for confirmButton inside of the login window
        server = new messageServer();
        server.setVisible(false);
        
        //initializing the activeUser label
        activeUserResponse = firebase.get("userList");
        activeUserMap = activeUserResponse.getBody();
        totalUsers = (int) activeUserMap.get("totalUsers");
        activeUserLabel.setText("ACTIVE USERS: " + Integer.toString(totalUsers));
        
        //timer for checking if the message variable has incremented on the database &&
        //timer for checking to see if the activeUser count has changed.
        newMessageTimer.schedule(new checkForIncrement(), 0, 0500);
        activeUserTimer.schedule(new activeUsers(), 0, 0500);  
    }
    
    //method that pushes a message to the server
    public void sendToServer(Firebase fb, String messageString) throws FirebaseException, JacksonUtilityException, UnsupportedEncodingException
    {
        //clears datamap initially so it doesnt send duplicate message
        dataMap.clear();
        //Pushes the newest message into the dataMap hashMap, and adds i as the key (keeps track
        //of how many times i has been incremented from the database variable)
        dataMap.put( Integer.toString(i), messageString );
        //increments number of messages
        i++;
        //patches the newly made hashmap value to the database
	fb.patch( dataMap );
        
        //clears the hashmap just to be safe
        dataMap.clear();
        
        //grabs message variable from the database that checks to see if any new messages have been
        //sent to the server and increments it by one then sends it back to the server
        response =fb.get("messageSent");
        dataMap = response.getBody();
        int message = (int) dataMap.get("message");
        message += 1;       
        dataMap.put("message", message);
        fb.patch("messageSent", dataMap);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    //subclass that is used to update the active users list every 1/2 of a second
    public static class activeUsers extends TimerTask
    {
        public void run()
        {
            int activeUsers = totalUsers;
            try {
                activeUserResponse = firebase.get("userList");
                activeUserMap = activeUserResponse.getBody();
                totalUsers = (int) activeUserMap.get("totalUsers");
                activeUserLabel.setText("ACTIVE USERS: " + Integer.toString(totalUsers));
                
            } catch (FirebaseException ex) {
                Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //checks to see if previous count of active users is less than new check for active users
            //if so, it broadcasts message stating an anonymous user has left chat
            if (activeUsers < totalUsers)
            {
                try {
                    leaveJoinResponse = firebase.get("joinLeave");
                } catch (FirebaseException ex) {
                    Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                leaveJoinMap = leaveJoinResponse.getBody();
                String joinStr = (String) leaveJoinMap.get("join");
                chatField.append(joinStr);
            }
            //checks to see if previous count of active users is greater than new check for active users
            //if so, it broadcasts message stating an anonymous user has joined chat
            if (activeUsers > totalUsers)
            {
                try {
                    leaveJoinResponse = firebase.get("joinLeave");
                } catch (FirebaseException ex) {
                    Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                leaveJoinMap = leaveJoinResponse.getBody();
                String joinStr = (String) leaveJoinMap.get("leave");
                chatField.append(joinStr);
            }
        }
    }
    
    //subclass that grabs the message variable from the database (variable that checks if a new
    //message has been sent to the server and is unread). 
    //it grabs that variable and checks if it's greater than 0, if so, it grabs the entire
    //message log from the subdirectory and reprints it all into the chat window
    public static class checkForIncrement extends TimerTask 
    {
        public void run() 
        {
            try {
                response = firebase.get("messageSent");
                dataMap = response.getBody();
                int message = (int) dataMap.get("message");
                
                if (message > 0)
                {
                    int j = 1;
                    int r = 0;
                    
                    response = firebase.get();
                    dataMap = response.getBody();
                    
                    while(dataMap.get(Integer.toString(j)) != null)
                    {
                        //chatField.append(((String) dataMap.get(Integer.toString(j))) + "\n");
                        j++;
                    }
                    
                    r = j - message;
                    while(dataMap.get(Integer.toString(r)) != null)
                    {
                        chatField.append(((String) dataMap.get(Integer.toString(r))) + "\n");
                        r++;
                    }
                    //CHANGE WHILE LOOP TO REMOVE THE CODE ABOVE J++ AND ONLY LOOP UNTIL J - message
                    //then finish looping until null and print latest messages I Think
                    
                    
                    i = j;
                    
                    dataMap.clear();
                    dataMap.put("message", 0);
                    firebase.patch("messageSent", dataMap);
                }
            } catch (FirebaseException ex) {
                Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JacksonUtilityException ex) {
                Logger.getLogger(messageServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }   
    
}