import java.io.FileOutputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MSDNDownloader 
{
	public static String htmldirtree="";//����Ŀ¼��
	public static String htmlbody="";//ƴ���½�
	public static String urlbase="http://msdn.microsoft.com";//��·����ַ
	public static String root="/en-us/library/ff551063(v=vs.85).aspx";
	public static int maxcount=10000;
	
	public static void resolve(String cururl,int curdepth)
	{
		boolean islastlayer=false;
		try
		{
			if(maxcount-- <= 0)
				return;
			System.out.println(maxcount);

			Document doc=Jsoup.connect(cururl).timeout(0).get();
			if(!doc.select("div.toclevel2.current a").isEmpty())
				islastlayer=true;
			//����ӵ�ǰ��Ŀ¼
			Element curtitle;
			if(islastlayer)
				curtitle=doc.select("div.toclevel2.current a").get(0);
			else
				curtitle=doc.select("div.toclevel1.current a").get(0);
			htmldirtree+= "<H"+curdepth+" style=\"margin-left:"+20*curdepth+"px\">";
			htmldirtree+= curtitle.text()+"</H"+curdepth+">\n";
			System.out.println(curtitle.text());
			//����ӵ�ǰ������
			htmlbody += doc.select(".topic").toString()+"\n";
			
			if(!islastlayer)
			{
				//������һ��
				Elements subtitle=doc.select("div.toclevel2 a");
				if(!doc.select("div.toclevel2.current a").isEmpty())
				{
					subtitle.remove(0);
				}
				for(Element everytitle:subtitle)
				{
					resolve(everytitle.attr("abs:href"),curdepth+1);
				}					
			}
		}
		catch(Exception exc)
		{
			System.out.println("����!");
//			System.exit(0);
		}
	}
	
	public static void main(String[] args)
	{
		try
		{
			resolve(urlbase+root,1);
			
			FileOutputStream fs=new FileOutputStream("result.htm");
			String html="<!DOCTYPE html>\n<html>\n<head><title>result</title></head>\n<body>\n<font color=0x00ffff>\n";
			html += htmldirtree+"\n</font>\n"+htmlbody+"\n</body>\n</html>";
			fs.write(html.getBytes());
			fs.close();
		}
		catch(Exception exc)
		{
			System.out.println("����!");
			System.exit(0);
		}
	}
}
