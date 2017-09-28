import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by David on 04/01/2017.
 */
public class Setup {
    public Setup(String user) {
        System.out.println("USER SETUP: " + user);
        songSetup(user);
        videoSetup(user);
        accountSetup(user);
    }
    public void accountSetup(String user)
    {
        try {
            File fHtml = new File(System.getProperty("user.dir") + "\\src\\HTML\\manage_account_temp.html");
            File fdata = new File(System.getProperty("user.dir") + "\\src\\Accounts\\" + user + "\\" + user + ".txt");
            String html = null;
            String data = null;
            if (fHtml.exists()) {
                FileReader fis = new FileReader(fHtml);
                BufferedReader br = new BufferedReader(fis);
                for(int i = 0; i < fHtml.length(); i++)
                {
                    html += br.readLine();
                    html += "\r\n";
                }
            }
            if (fHtml.exists()) {
                FileReader fis = new FileReader(fdata);
                BufferedReader br = new BufferedReader(fis);
                for(int i = 0; i < fdata.length(); i++)
                {
                    data += br.readLine();
                    data += "\r\n";
                }
            }
            html = html.replace("null", "").trim();
            data = data.replace("null", "").trim();
            data += "\r\n";
            System.out.println(html);
            System.out.println(data);
            System.out.println(data.indexOf("\r\n", data.indexOf("country")));
            html = html.replace("\"Username\"", data.substring(data.indexOf("uname=")+"uname=".length(), data.indexOf("\r\n")));
            html = html.replace("\"First Name\"", data.substring(data.indexOf("fname=")+"fname=".length(), data.indexOf("\r\n", + data.indexOf("fname"))));
            html = html.replace("\"Second Name\"", data.substring(data.indexOf("sname=")+"sname=".length(), data.indexOf("\r\n", data.indexOf("sname"))));
            html = html.replace("\"DATE\"", data.substring(data.indexOf("DOB=")+"DOB=".length(), data.indexOf("\r\n", data.indexOf("DOB"))));
            html = html.replace("\"Address Line 1\"", data.substring(data.indexOf("addr1=")+"addr1=".length(), data.indexOf("\r\n", data.indexOf("addr1"))));
            html = html.replace("\"Address Line 2\"", data.substring(data.indexOf("addr2=")+"addr2=".length(), data.indexOf("\r\n", data.indexOf("addr2"))));
            html = html.replace("\"City\"", data.substring(data.indexOf("city=")+"city=".length(), data.indexOf("\r\n", data.indexOf("city"))));
            html = html.replace("\"State\"", data.substring(data.indexOf("state=")+"state=".length(), data.indexOf("\r\n", data.indexOf("state"))));
            html = html.replace("\"Country\"", data.substring(data.indexOf("country=")+"country=".length(), data.indexOf("\r\n", data.indexOf("country"))));
            File fout = new File(System.getProperty("user.dir") + "\\src\\HTML\\manage_account.html");
            FileWriter fw = new FileWriter(fout);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(html, 0, html.length());
            bw.flush();
            bw.close();
        }
        catch(Exception ee)
        {
            ee.printStackTrace();
        }
    }
    public void songSetup(String user)
    {
        File f = new File(System.getProperty("user.dir") + "\\src\\Accounts\\" + user + "\\Audio");
        File musicPlayer = new File(System.getProperty("user.dir")+"\\src\\HTML\\music_player.html");
        File deleteFile = new File(System.getProperty("user.dir") + "\\src\\Accounts\\" + user + "\\Audio\\desktop.ini");
        if(deleteFile.exists())
        {
            deleteFile.delete();
        }
        System.out.println(f);
        if(f.exists())
        {
            System.out.println("ITS HERE");
        }
        else
        {
            System.out.println("File does not exist");
        }
        String htmlCode = "";
        String input = "";
        File[] files = null;
        files = f.listFiles();
        String[] songName = new String[files.length];
        String[] songArtist = new String[files.length];
        String[] songDuration = new String[files.length];
        for(int i = 0; i < files.length; i++)
        {
            System.out.println(files[i]);
        }
        try{
            for(int i = 0; i < files.length; i++) {
                if (files[i].toString().contains("-")) {
                    songName[i] = files[i].toString().substring(files[i].toString().lastIndexOf("\\") + 1, files[i].toString().lastIndexOf("-"));
                    songArtist[i] = files[i].toString().substring(files[i].toString().lastIndexOf("-") + 1, files[i].toString().lastIndexOf(".mp3"));
                    AudioFileFormat baseFileFormat = new MpegAudioFileReader().getAudioFileFormat(files[i]);
                    Map properties = baseFileFormat.properties();
                    double duration = (long) properties.get("duration");
                    int minute = (int) ((duration / 1000) / 60) / 1000;
                    int seconds = (int) (60 * ((((duration / 1000) / 60) / 1000) - minute));
                    songDuration[i] = minute + ":" + seconds;
                    System.out.println(songName[i]);
                    System.out.println(songArtist[i]);
                    System.out.println(songDuration[i]);
                }
            }
            FileReader fr = new FileReader(System.getProperty("user.dir")+"\\src\\HTML\\music_player.html");
            BufferedReader br = new BufferedReader(fr);


            do {
                input=br.readLine()+"\r\n";
                htmlCode+=input;
            }while(!htmlCode.contains("<tbody>") && input != null);


            for(int j = 0; j < files.length; j++)
            {
                htmlCode+=("\t\t\t\t<tr>\r\n");
                htmlCode+=("\t\t\t\t\t<td>"+(j+1)+"</td>\r\n" +
                        "  \t\t\t\t\t<td>"+songName[j]+"</td>\r\n" +
                        "  \t\t\t\t\t<td>"+songArtist[j]+"</td>\r\n" +
                        "  \t\t\t\t\t<td>"+songDuration[j]+"</td>\r\n" +
                        "  \t\t\t\t\t<td><button type=\"button\" id=\"btn"+j+"\" class=\"btn btn-active btn-xs\" onclick=\"loadDoc('"+songName[j].replace(" ", "@")+"-"+songArtist[j].replace(" ", "@")+"', '"+j+"')\">Play</button><td>\r\n");
                htmlCode+=("\t\t\t\t</tr>\r\n");
            }

            do {
                input = br.readLine();
            }while(!input.contains("</tbody>") && input != null);
            htmlCode+=input+"\r\n";
            do{
                input = br.readLine()+"\r\n";
                htmlCode+=input;
            } while(!htmlCode.contains("</html>") && input != null);
            //System.out.println(htmlCode);
            br.close();

            FileWriter fw = new FileWriter(musicPlayer);
            PrintWriter pw = new PrintWriter(fw);
            htmlCode.replace("null", "");
            pw.write(htmlCode);
            pw.flush();
            pw.close();
        }catch(Exception ee)
        {
            ee.printStackTrace();
        }
    }
    public void videoSetup(String user)
    {
        File f = new File(System.getProperty("user.dir") + "\\src\\Accounts\\" + user + "\\Videos");
        File musicPlayer = new File(System.getProperty("user.dir")+"\\src\\HTML\\theatre.html");
        File deleteFile = new File(System.getProperty("user.dir") + "\\src\\Accounts\\" + user + "\\Videos\\desktop.ini");
        if(deleteFile.exists())
        {
            deleteFile.delete();
        }
        String htmlCode = "";
        String input = "";
        File[] files = null;
        files = f.listFiles();
        String[] videoTitle= new String[files.length];
        for(int i = 0; i < files.length; i++)
        {
            System.out.println(files[i]);
        }
        try{
            for(int i = 0; i < files.length; i++) {
                    videoTitle[i] = files[i].toString().substring(files[i].toString().lastIndexOf("\\") + 1, files[i].toString().lastIndexOf(".mp4"));
                    System.out.println(files[i].toString());
                }
            FileReader fr = new FileReader(System.getProperty("user.dir")+"\\src\\HTML\\theatre.html");
            BufferedReader br = new BufferedReader(fr);


            do {
                input=br.readLine()+"\r\n";
                htmlCode+=input;
            }while(!htmlCode.contains("<tbody>") && input != null);

            System.out.println(files.length);
            for(int j = 0; j < files.length; j++)
            {
                htmlCode+=("\t\t\t\t<tr>\r\n");
                htmlCode+=("\t\t\t\t\t<td>"+(j+1)+"</td>\r\n" +
                        "  \t\t\t\t\t<td>"+videoTitle[j]+"</td>\r\n" +
                        "  \t\t\t\t\t<td><button type=\"button\" id=\"btn"+j+"\" class=\"btn btn-active btn-sm\" onclick=\"loadDoc('"+videoTitle[j]+"', '"+j+"')\">Play<span class=\"glyphicon glyphicon-play\"></span></button><td>\r\n");
                htmlCode+=("\t\t\t\t</tr>\r\n");
            }
            do {
                input = br.readLine();
            }while(!input.contains("</tbody>") && input != null);
            htmlCode+=input+"\r\n";
            do{
                input = br.readLine()+"\r\n";
                htmlCode+=input;
            } while(!htmlCode.contains("</html>") && input != null);
            //System.out.println(htmlCode);
            br.close();

            FileWriter fw = new FileWriter(musicPlayer);
            PrintWriter pw = new PrintWriter(fw);
            htmlCode.replace("null", "");
            pw.write(htmlCode);
            pw.flush();
            pw.close();
        }catch(Exception ee)
        {
            ee.printStackTrace();
        }
    }
}