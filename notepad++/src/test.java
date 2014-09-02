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

public class test
{
	public static String url="http://svn.tuxfamily.org";
	public static String path="k:/notepad++";
	public static int filethreadnum=0;
	
	public static boolean setinit=false;//�Ƿ�ǿ�Ƴ�ʼ�� 
	public static String[] initstring={};//��ʼ��Ŀ¼λҪ��ʼ���µ�Ŀ¼�������˳��
	public static int curdepth=0;//��ǰ��ʼ����� 

	public static class FileThread extends Thread
	{
		String curnode;
		String filepath;
		
		FileThread(String curnode,String filepath)
		{
			this.curnode=curnode;
			this.filepath=filepath;
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
			    URL weburl=new URL(url+curnode.replaceAll("view=.*$","view=co"));
			    URLConnection con=weburl.openConnection();
			    InputStream instream=con.getInputStream();
			    FileOutputStream fs=new FileOutputStream((path+filepath).replace("%20"," "));
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
				filethreadnum--;
//				new File(path+filepath).deleteOnExit();;
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
			if(ch == '\\' || ch == '*' || ch == '?' || ch == '"' || ch == '<' || ch == '>' || ch == '|')
				sbuf.append('x');//���Բ������ļ������ַ�
			else 
				sbuf.append(ch);
		}
	
		return sbuf.toString();
	}
	
	public static String createFolder(String folderPath) 
	{
		String txt = folderPath;
		txt.replace('\\','/');
		if(txt.charAt(txt.length()-1) != '/')
			txt+='/';
		try 
		{
			File myFilePath = new File(txt);
			txt = folderPath;
			if (!myFilePath.exists()) 
			{
				if(!myFilePath.mkdir())
				{
					String newpath=folderPath.substring(0,folderPath.length()-1);
					newpath=newpath.substring(0,newpath.lastIndexOf('/'));
					createFolder(newpath);
					myFilePath.mkdir();
				}
			}
		}
		catch (Exception e) 
		{
			System.out.println("����!");
		}
		return txt;
	}
	
	public static void myresolve(Element e) throws IOException
	{
		try
		{
			String curnode=e.attr("href");
			System.out.println(curnode);
			if(setinit)
			{
				if(!curnode.equals(initstring[curdepth]))
					return;
				else
					curdepth++;
				if(curdepth >= initstring.length)
					setinit=false;
			}
	  
			if(!curnode.contains(".."))
			{//�Ǹ�Ŀ¼
				if(curnode.charAt(curnode.length()-1) == '/')
				{//Ŀ¼
					createFolder((path+curnode).replace("%20"," "));
					Document doc=Jsoup.connect(url+curnode).timeout(0).get();
					System.out.println("��ǰĿ¼��"+url+curnode);
					Elements items = new Elements();
					Elements items1=doc.select("tr.vc_row_even td[style] a");
					items.addAll(items1);
					items1=doc.select("tr.vc_row_odd td[style] a");
					items.addAll(items1);
					for(Element ele:items)
					{
						myresolve(ele);
					}
				}
				else
				{//�ļ�
					String filepath=curnode.substring(0,curnode.lastIndexOf('/')+1)+e.attr("name");
					File curfile=new File((path+filepath).replace("%20"," "));
					if(curfile.exists())
						return;
					while(filethreadnum>30)
					{
						Thread.sleep(1000);
					}
					(new FileThread(curnode,filepath)).start();
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
			Document doc = Jsoup.connect(url+"/").timeout(0).get();
			Elements items=doc.select("tr.vc_row_odd a");
			items.addAll(doc.select("tr.vc_row_even a"));
			createFolder(path);
			for(Element e:items)
			{
				myresolve(e);
			}
		}
		catch(Exception exc)
		{
			System.out.println("����!");
		}
	}
}
