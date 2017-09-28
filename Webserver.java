/**
 * Created by David on 08/11/2016.
 */
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.io.*;
import java.net.*;
import java.io.File;


public class Webserver {

    public Webserver() {
        int threadID = 0;
        try {
            ServerSocket ss = new ServerSocket(8777);
            while (true) {
                Socket sc = ss.accept();
                new threadServer(sc, threadID);
                threadID++;
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }
}


class threadServer implements Runnable {
    static String currentUser = null;
    String html = "";
    Socket clientConnection = null;
    int threadNo = 0;

    public threadServer(Socket sc, int threadID) {
        clientConnection = sc;
        threadNo = threadID;
        Thread thr = new Thread(this);
        thr.start();
    }

    public void run() {
        processRequest(clientConnection);
    }

    void processRequest(Socket sc) {
        try {
            // Used to write out HTML to the client
            InputStream is = sc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            DataInputStream dis = new DataInputStream(is);

            // Used for loading images to the client - Raw byte data for non-text files
            OutputStream os = sc.getOutputStream();
            PrintWriter pr = new PrintWriter(os);
            DataOutputStream dos = new DataOutputStream(os);

            System.out.println("Thread " + threadNo + " Begins");

            String method = br.readLine();
            String temp = "";
            String request = "";
            String[] elements = null;

            String[] currentPage = new String[1];   // Keeps track of what page the current user is connected to
            currentPage[0] = "\\src\\HTML\\index_OUT.html";   // Assign login page for newly connected clients
            if (method == null) {   // If the request method is null - false request, close all streams
                br.close();
                pr.close();
                sc.close();
                dos.close();
            } else {
                if (method.contains("POST")) {      // If post request is received then process request as POST
                    elements = doPost(br, dis,  method, currentPage);     // Receive POST data elements
                } else if (method.contains("GET")) {    // If get request is received then process request as GET
                    elements = doGet(br, method);   // Receive GET data elements
                }

                elements[0] = elements[0].toLowerCase();
                System.out.println();
                File f = null;
                try {
                    if (elements[0].indexOf("link") > -1) {
                        System.out.println("LINK");
                        int startIndex = elements[0].indexOf("link_") + 5;
                        int endIndex = elements[0].indexOf("html", startIndex) + 4;
                        currentPage[0] = "\\src\\HTML\\" + elements[0].substring(startIndex, endIndex);
                        //System.out.println("Current Page: " + currentPage[0]);
                        f = new File(System.getProperty("user.dir") + currentPage[0]);
                        FileInputStream fr = new FileInputStream(f);
                        //html+="HTTP/1.1 200 OK\n";
                        //html+="HOST: localhost\n";
                        //html+="Content-Type: text/html\n";
                        //html+="Content-Length: " + f.length() + "\n";
                        //html+="Connection: keep-alive\n";
                        while (fr.available() > 0) {
                            html += (char) fr.read();
                        }
                        pr.print(html);
                        pr.flush();
                        pr.close();
                        sc.close();
                    }
                    else if(elements[0].indexOf(".png")>-1 || elements[0].indexOf(".jpg")>-1 || elements[0].indexOf(".mp4")>-1 || elements[0].indexOf(".mp3")>-1) {
                        System.out.print("MEDIA - ");
                        elements[0] = elements[0].replace("%20", " ");
                        String source = null;
                        if (elements[0].indexOf(".png") > -1 || elements[0].indexOf(".jpg") > -1) {
                            source = System.getProperty("user.dir") + "\\src\\Images\\" + elements[0];
                            System.out.println("IMAGE");
                        } else if (elements[0].indexOf(".mp4") > -1) {
                            source = System.getProperty("user.dir") + "\\src\\Accounts\\" + currentUser + "\\Videos\\" + elements[0];
                            System.out.println("VIDEO");
                        } else if (elements[0].indexOf(".mp3") > -1) {
                            source = System.getProperty("user.dir") + "\\src\\Accounts\\" + currentUser + "\\Audio\\" + elements[0];
                            System.out.println("AUDIO");
                        }
                        else
                        {
                            System.out.println("UNKNOWN");
                        }
                        f = new File(source);
                        System.out.println(source);
                        if(f.exists()) {                                                // Check to see if the file exists
                            byte[] dataFileByteArray = new byte[(int) f.length()];     // Declare Byte Array holder for video data
                            FileInputStream fr = new FileInputStream(f);                //
                            BufferedInputStream bis = new BufferedInputStream(fr);
                            bis.read(dataFileByteArray, 0, dataFileByteArray.length);
                            dos.write(dataFileByteArray, 0, dataFileByteArray.length);
                            fr.close();
                            dos.flush();
                            dos.close();
                        }
                        else{
                            System.out.println("ERROR: The file requested does not exist !");
                        }
                    }
                    else
                    {
                        System.out.println("AJAX/Refresh");
                        f = new File(System.getProperty("user.dir") + currentPage[0]);
                        FileInputStream fr = new FileInputStream(f);
                       // html+="HTTP/1.1 200 OK\n";
                        //html+="HOST: localhost\n";
                       // html+="Content-Type: text/html\n";
                       // html+="Content-Length: " + f.length() + "\n";
                       // html+="Connection: keep-alive\n";
                        while (fr.available() > 0) {
                            html += (char) fr.read();
                        }
                        pr.print(html);
                        pr.flush();
                        pr.close();
                        sc.close();

                    }
                    //Testing
                    //for(int i = 0; i < elements.length; i++)
                    // {
                    //    System.out.println(elements[i]);
                    //}
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }

        } catch (Exception ee) {
            ee.printStackTrace();
        }
        System.out.println("Thread " + threadNo + " Ends");
    }

    String[] doGet(BufferedReader reader, String firstLine) {
        System.out.println("GET REQUEST");
        BufferedReader br = reader;
        String[] formElements = new String[50];
        try {
            String request = firstLine;
            String temp;
            do {
                temp = br.readLine();
                request += temp;
            } while (!temp.isEmpty());
            //System.out.println(request);
            if (request.contains("?")) {
                int startIndex = request.indexOf('?');
                int endIndex = request.indexOf("Cookie:");
                request = request.substring(startIndex + 1, endIndex);
                formElements = request.split("&");
            } else {
                int startIndex = request.indexOf('/');
                int endIndex = request.indexOf(' ', startIndex);
                formElements[0] = request.substring(startIndex + 1, endIndex);
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        return formElements;
    }

    String[] doPost(BufferedReader reader, DataInputStream dataReader, String firstLine, String[] currentPage) {
        System.out.println("POST REQUEST");
        BufferedReader br = reader;     // Setup a reader to process the http request
        String request = firstLine;     // add the first line read to the request string
        String temp;
        String[] formElements = null;   // Empty container to hold the processed form Elements
        try {
            do {
                temp = br.readLine();   // Read a line
                request += temp;// Add the newly read line to the request
                request += "~\r\n";
            } while (!temp.isEmpty());  // while there is data to read, continue reading in the next line
            System.out.println(request);

            // Get the content lenght of the data to be received
            int contentLengthStrIndexStart = request.indexOf("Content-Length: ");   // Find the start of the content length in the request string
            if ((contentLengthStrIndexStart > -1)) { // if the content length was found proceed
                String contentLengthStr = request.substring(contentLengthStrIndexStart + 16, request.indexOf("~", contentLengthStrIndexStart)); // Extract the content length substring from request
                int contentSize = Integer.parseInt(contentLengthStr);   // Convert the content length substring into a numeric int value
                System.out.println("Content Length: " + contentSize);
                System.out.println(request);

                char[] payload = new char[contentSize + 1];
                br.read(payload, 0, contentSize);

                System.out.println("Hello");
                String payloadStr = new String(payload);

                if(request.contains("boundary="))
                {
                    System.out.println(payloadStr);
                    String delim = payloadStr.substring(payloadStr.indexOf("boundary=")+1, payloadStr.indexOf("\r\n", payloadStr.indexOf("boundary=")+1)) + "\r\n";
                    int delimSize = delim.length();
                    System.out.println("Delim = " + delim);

                    String substrings[] = new String[4];
                    substrings = payloadStr.split(delim);
                    payloadStr = substrings[0];

                    String dataInfoStr = substrings[1].substring(substrings[1].indexOf("Content"), substrings[1].indexOf("\r\n", substrings[1].indexOf("\r\n")+1));
                    System.out.println("dataInfo: " + dataInfoStr);
                    String dataStr = substrings[1].substring(dataInfoStr.length());
                    System.out.println("dataStr: " + dataStr);

                    String nameStr = substrings[2].substring(substrings[2].indexOf("\"filename\"\r\n\r\n")+("\"filename\"\r\n\r\n".length()));
                    nameStr = nameStr.trim();
                    System.out.println("nameStr: " + nameStr);

                    String locationStr = substrings[3].substring(substrings[3].indexOf("\"destination\"\r\n\r\n") + ("\"destination\"\r\n\r\n".length()));
                    locationStr = locationStr.trim();
                    System.out.println("locStr: " + locationStr);

                    String extension = dataInfoStr.substring(dataInfoStr.indexOf("."), dataInfoStr.lastIndexOf("\""));
                    File fout = new File(System.getProperty("user.dir") + "\\src\\" + locationStr + "\\" + nameStr + extension);
                    byte data[] = dataStr.getBytes();

                    FileOutputStream fos = new FileOutputStream(fout);
                    DataOutputStream uploadDos = new DataOutputStream(fos);

                    uploadDos.write(data, 0, data.length);
                    uploadDos.flush();
                    uploadDos.close();
                    fos.close();
                }
                payloadStr.replace('+', ' ');
                formElements = payloadStr.split("&");
                System.out.println("PAYLOAD:");
                System.out.println(payloadStr);
                if (firstLine.contains("create_account.html")) {
                    createUser(formElements, payloadStr);
                } else if(firstLine.contains("manage_account.html")) {
                    amendUser(formElements, payloadStr);
                } else if (firstLine.contains("login.html") || firstLine.contains("login_f.html")) {
                    if (userLogIn(formElements, payloadStr)) {
                        currentPage[0] = "\\src\\HTML\\index_IN.html";
                    } else {
                        currentPage[0] = "\\src\\HTML\\login_f.html";
                    }
                } else {
                    System.out.println("Incorrect Data Received");
                }
            } else {
                System.out.println("Invalid Request - No content length found");
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        //for(int i = 0; i < formElements.length; i++)
        //{
        //    System.out.println(formElements[i]);}
        return formElements;
    }

    boolean userLogIn(String[] formElements, String payloadStr)
    {
        boolean dataOK = true;
        try {
            if (!payloadStr.contains("=&")) {
                int startIndex = formElements[0].indexOf("=") + 1;
                for(int i = 0; i < formElements.length; i++)
                {
                    System.out.println(formElements[i]);
                    formElements[i] = formElements[i].replace("+", " ");
                    System.out.println(formElements[i]);
                }
                File f = new File(System.getProperty("user.dir") + "\\src\\Accounts\\" + formElements[0].substring(startIndex) + "\\" + formElements[0].substring(startIndex) + ".txt");
                if (f.exists()) {
                    System.out.println("Checking User Data");
                    InputStream fis = new FileInputStream(f);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);

                    String[] userData = new String[2];
                    for(int i = 0; i < 2; i++){
                        userData[i] = br.readLine();
                        userData[i] = userData[i].trim();
                        formElements[i] = formElements[i].trim();
                        System.out.println('"'+userData[i]+'"' + " = " + '"'+formElements[i]+'"');
                        System.out.println(userData[i].length());
                        System.out.println(formElements[i].length());
                        if(userData[i].equals(formElements[i])){
                            System.out.println("Logged In");
                        }
                        else
                        {
                            dataOK = false;
                            System.out.println("Failed to login");
                        }
                    }
                }else
                {
                    System.out.println(f);
                    System.out.println("Failed to login");
                    dataOK = false;

                }
            } else {
                System.out.println("Account details are not valid");
                dataOK = false;
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        if(dataOK)
        {
            currentUser = formElements[0].substring(formElements[0].indexOf("=")+1);
            System.out.println(currentUser);
            Setup setup = new Setup(currentUser);
        }
        return dataOK;
    }

    void createUser(String[] formElements, String payloadStr) {
        try {
            if (!payloadStr.contains("=&")) {
                int startIndex = formElements[0].indexOf("=") + 1;
                for(int i = 0; i < formElements.length; i++)
                {
                    formElements[i] = formElements[i].replace("+", " ");
                }
                File fdir = new File(System.getProperty("user.dir") + "\\src\\Accounts\\" + formElements[0].substring(startIndex));
                if(fdir.exists())
                {
                    System.out.println("That account already exists !");
                }
                else
                {
                    if (fdir.mkdir()) {
                        String folderContents[] = {"Videos", "Images", "Audio"};
                        File f = null;
                        for(int i = 0; i < folderContents.length; i++)
                        {
                            f = new File(System.getProperty("user.dir") + "\\src\\Accounts\\" + formElements[0].substring(startIndex) + "\\" + folderContents[i]);
                            if(f.mkdir())
                            {
                                System.out.println(folderContents + " directory has been created for user \"" + formElements[0].substring(startIndex) + "\"");
                            }
                            else
                            {
                                System.out.println("ERROR: " + folderContents + " has not been created for user \"" + formElements[0].substring(startIndex) + "\"");
                            }
                        }
                        f = new File(System.getProperty("user.dir") + "\\src\\Accounts\\" + formElements[0].substring(startIndex) + "\\" + formElements[0].substring(startIndex) + ".txt");
                        System.out.println(f);
                        System.out.println("Creating user . . . ");
                        FileOutputStream fos = new FileOutputStream(f);
                        PrintWriter pr = new PrintWriter(fos);
                        for (int i = 0; i < formElements.length; i++) {
                            pr.println(formElements[i]);
                        }
                        pr.flush();
                        pr.close();
                    }
                    else{
                        System.out.println("Problem creating account");
                    }
                }
            } else {
                System.out.println("Account details are not valid");
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    void amendUser(String[] formElements, String payloadStr) {
        try {
            if (!payloadStr.contains("=&")) {
                int startIndex = formElements[0].indexOf("=") + 1;
                for(int i = 0; i < formElements.length; i++)
                {
                    formElements[i] = formElements[i].replace("+", " ");
                }
                File f = new File(System.getProperty("user.dir") + "\\src\\Accounts\\" + currentUser + "\\" + currentUser + ".txt");
                if(f.exists())
                {
                    FileOutputStream fos = new FileOutputStream(f);
                    PrintWriter pr = new PrintWriter(fos);
                    for (int i = 0; i < formElements.length; i++) {
                        pr.println(formElements[i]);
                    }
                    pr.flush();
                    pr.close();
                }
                else
                {
                    System.out.println("That account does not exist !" );
                }
            } else {
                System.out.println("Account details are not valid");
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }
}
