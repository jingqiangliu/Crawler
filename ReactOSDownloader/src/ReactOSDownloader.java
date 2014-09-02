import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ReactOSDownloader
{
	 public static String url="http://svn.reactos.org/";
	 public static String path="k:/reactos/";
	 public static int filethreadnum=0;
	 
	 public static boolean setinit=true;//�Ƿ�ǿ�Ƴ�ʼ�� 
	 public static String[] initstring={"reactos/","vendor/"};//��ʼ��Ŀ¼λҪ��ʼ���µ�Ŀ¼�������˳��
	 public static int curdepth=0;//��ǰ��ʼ����� 

	 public static class FileThread extends Thread
	 {
		  String filepath;
		  String curnode;
		  
		  FileThread(String filepath,String curnode)
		  {
			   this.filepath=filepath;
			   this.curnode=curnode;
		  }
		  
		  public void run()
		  {
			   try
			   {
				    while(filethreadnum>30)
				    {
					     sleep(1000);
				    }
				    filethreadnum++;
				    int byteread=0;
				    int bytesum=0;
				    URL weburl=new URL(url+filepath+curnode);
				    URLConnection con=weburl.openConnection();
				    InputStream instream=con.getInputStream();
				    FileOutputStream fs=new FileOutputStream(escape((path+filepath+curnode).replace("%20"," ")));
				    byte[] buffer=new byte[65536];
				    while((byteread=instream.read(buffer)) != -1)
				    {
					     bytesum+=byteread;
					     fs.write(buffer,0,byteread);
					     System.out.println("\t\t��ǰ�����ļ���"+filepath+curnode+"\t��ǰ��С��"+bytesum);
				    }
				    fs.close(); 
				    instream.close();
				    filethreadnum--;
			   }
			   catch(Exception e)
			   {
				    System.out.println("error");
				    new File(path+filepath+curnode).deleteOnExit();;
			   }
		  }
	 }
	 
	 public static String escape(String src)
	 {
		StringBuffer sbuf=new StringBuffer();
		int len=src.length();
		for(int i=0;i<len;i++)
		{
			int ch=src.charAt(i);
			if(ch == '\\' || ch == '/' || ch == ':' || ch == '*' || ch == '?' || ch == '"' || ch == '<' || ch == '>' || ch == '|')
				sbuf.append('x');
			else 
				sbuf.append(ch);
		}
		
		 return sbuf.toString();
	 }
	 
	 public static String createFolder(String folderPath) 
	 {
	        String txt = folderPath;
	        try 
	        {
	            File myFilePath = new File(txt);
	            txt = folderPath;
	            if (!myFilePath.exists()) 
	            {
	                myFilePath.mkdir();
	            }
	        }
	        catch (Exception e) 
	        {
	            System.out.println("����!");
	        }
	        return txt;
	    }
	 
	 public static void myresolve(Element e,String filepath) throws IOException
	 {
		  try
		  {
			   String curnode=e.attr("href");
			   if(setinit)
			   {
				   if(!curnode.equals(initstring[curdepth]))
					   return;
				   else
					   curdepth++;
				   if(curdepth >= initstring.length)
					   setinit=false;
			   }
			   
			   if(!curnode.equals("../") && !curnode.equals("svn/"))
			   {//�Ǹ�Ŀ¼
				    if(curnode.charAt(curnode.length()-1) == '/')
				    {//Ŀ¼
					     createFolder((path+filepath+curnode).replace("%20"," "));
					     Document doc=Jsoup.connect(url+filepath+curnode).timeout(0).get();
					     System.out.println("��ǰĿ¼��"+url+filepath+curnode);
					     Elements items=doc.select("tbody tr a");
					     for(Element ele1:items)
					     {
						      myresolve(ele1,filepath+curnode);
					     }
					     items.clear();
					     items=doc.select("ul li a");
					     for(Element ele2:items)
					     {
						      myresolve(ele2,filepath+curnode);
					     }
				    }
				    else
				    {//�ļ�
					     File curfile=new File((path+filepath+curnode).replace("%20"," "));
					     if(curfile.exists())
					      return;
					     while(filethreadnum>30)
					     {
						      Thread.sleep(1000);
					     }
					     (new FileThread(filepath,curnode)).start();
				    }
			   }
		  }
		  catch(Exception exc)
		  {
			   System.out.println("����!");
		  }
	 }
	 
	 public static void main(String[] args) throws IOException
	 { 
		  try
		  {
			   Document doc = Jsoup.connect(url).timeout(0).get();
			   Elements items=doc.select("tbody tr a");
			   createFolder(path);
			   for(Element e1:items)
			   {
				    myresolve(e1,"");
			   }
			   items=doc.select("ul li a");
			   for(Element e2:items)
			   {
				    myresolve(e2,"");
			   }
			   while(filethreadnum != 0)
			   {
				    Thread.sleep(1000);
			   }
		  }
		  catch(Exception exc)
		  {
			   System.out.println("����!");
		  }
	 }
}

