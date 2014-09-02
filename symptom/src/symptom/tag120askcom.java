package symptom;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import SQLite.Database;
import SQLite.Exception;

public class tag120askcom
{
	public final static int SYMPTOM=0;
	public final static int DISEASE=1;
	public static int threadnum=10;
	private String beginstr=null;
	
	private Queue<ELE> dataQueue=new LinkedList<ELE>();//֢״����
	public Database db=null;
	private int total;
	
	public class ELE
	{
		public ELE(String name,String url,int type,String bodypart,String keshi)
		{
			if(name == null || name.equals(""))
				System.out.println("error");
			this.name=name;
			this.url=url;
			this.type=type;
			this.keshi=keshi;
			this.bodypart=bodypart;
		}
		
		/**���ݿ��ʽ
		 * 	Name		֢״/������
			Id			ȫ��Ψһ��ʶ
			IsSymptom	֢״/������
			BodyPart	���岿λ		----֢״	ͷ:�۾�
			Keshi		�Һſ���		----����	�ڿ�:�п�
			Description	����			
			DDescription����
			WithSymptom	����֢״		----������Ӧ֢״���������Ÿ���
			WithDSymptom��ϸ֢״		----������Ӧ֢״����
			PossibleIll	��ؼ���		----֢״���ܼ���������Ÿ�����С��ð�Ÿ��� ����:����:����֢״
			Pathogeny	����			
			Check		���
			Diagnosis	��ϼ���
			Popularity	����/֢״ �����ȶ�
		 * 
		 */
		
		public String getName() 
		{
			return name;
		}

		public void setName(String name) 
		{
			this.name = name;
		}

		public String getUrl() 
		{
			return url;
		}

		public void setUrl(String url) 
		{
			this.url = url;
		}

		public int getType() 
		{
			return type;
		}

		public void setType(int type) 
		{
			this.type = type;
		}

		public String getKeshi()
		{
			return keshi;
		}

		public void setKeshi(String keshi) 
		{
			this.keshi = keshi;
		}

		public String getBodypart() 
		{
			return bodypart;
		}

		public void setBodypart(String bodypart) 
		{
			this.bodypart = bodypart;
		}

		private String name="";//֢״��
		private String url="";//���ݵ�ַ
		private int type=SYMPTOM;//��������֢״
		private String keshi="";//�������ң�����ң�keshi+":"+ѪҺ�ڿ�
		private String bodypart="";//���岿λ	ͷ:�۾�
		
		public void resolve()
		{
			String description="";
			String ddescription="";
			String withsymptom="";
			String withdsymptom="";
			StringBuilder possibleill=new StringBuilder();
			String pathogeny="";
			String check="";
			String diagnosis="";
			threadnum--;
			String sql="";
			
			try 
			{
				Document doc=Jsoup.connect(url).timeout(0).get();
				
				if(type == SYMPTOM)
				{//�����֢״
					description=doc.select("dl.p_sibox1dl.clears p").text();
					String temp=doc.select("dl.p_sibox1dl.clears p a").attr("abs:href");
					ddescription=Jsoup.connect(temp).timeout(0).get().select("div.p_cleftartbox").text();
					
					Elements tagnames=doc.select("div.w_headnav_div a");
					for(Element tag:tagnames)
					{
						if(tag.text().equals("����"))
						{
							pathogeny=Jsoup.connect(tag.attr("abs:href")).timeout(0).get().select("div.p_cleftartbox").text();
						}
						else if(tag.text().equals("���"))
						{
							check=Jsoup.connect(tag.attr("abs:href")).timeout(0).get().select("div.p_cleftartbox").text();
						}
						else if(tag.text().equals("��ϼ���"))
						{
							diagnosis=Jsoup.connect(tag.attr("abs:href")).timeout(0).get().select("div.p_cleftartbox").text();
						}
						else if(tag.text().equals("��ؼ���"))
						{
							Elements ll=Jsoup.connect(tag.attr("abs:href")).timeout(0).get().select("ul.p_cbox3ul");
							if(!ll.isEmpty())
							{
								Elements eles=ll.get(0).children();
								for(Element ele:eles)
								{
									possibleill.append(ele.select("span.p_cbox3li1 a").text());
									possibleill.append(":");
									possibleill.append(ele.select("span.p_cbox3li2 a").text());
									possibleill.append(":");
									possibleill.append(ele.select("span.p_cbox3li3 a").text());
									possibleill.append(",");
								}
								if(possibleill.length() >= 1)
								{
									possibleill.deleteCharAt(possibleill.length()-1);//ȥ�������','
								}
							}
						}
					}
					
					description=description.replace('\'',' ').replace('"',' ');
					ddescription=ddescription.replace('\'',' ').replace('"',' ');
					withsymptom=withsymptom.replace('\'',' ').replace('"',' ');
					withdsymptom=withdsymptom.replace('\'',' ').replace('"',' ');
					String ill=possibleill.toString().replace('\'',' ').replace('"',' ');
					pathogeny=pathogeny.replace('\'',' ').replace('"',' ');
					check=check.replace('\'',' ').replace('"',' ');
					diagnosis=diagnosis.replace('\'',' ').replace('"',' ');
					
					sql="insert into symptomdata values('"+name+"','0','1','"+bodypart+"','"+keshi+"','"+description+
							"','"+ddescription+"','"+withsymptom+"','"+withdsymptom+"','"+ill+"','"+
							pathogeny+"','"+check+"','"+diagnosis+"','0')";
					db.exec(sql,null);
					System.out.println(name+" left:"+dataQueue.size()+" total:"+total);
				}
				else if(type == DISEASE)
				{	
					description=doc.select("div.p_lbox1_ab p").text();
					String temp=doc.select("div.p_lbox1_ab p a").attr("abs:href");
					Document doc2=Jsoup.connect(temp).timeout(0).get();
					ddescription=doc2.select("div.p_cleftartbox").text();
					
					Elements tagnames=doc.select("div.p_topbox a");
					for(Element tag:tagnames)
					{
						if(tag.text().equals("����"))
						{
							pathogeny=Jsoup.connect(tag.attr("abs:href")).timeout(0).get().select("div.p_cleftartbox").text();
						}
						else if(tag.text().equals("���"))
						{
							check=Jsoup.connect(tag.attr("abs:href")).timeout(0).get().select("div.p_cleftartbox").text();
						}
						else if(tag.text().equals("����"))
						{
							diagnosis=Jsoup.connect(tag.attr("abs:href")).timeout(0).get().select("div.p_cleftartbox").text();
						}
						else if(tag.text().equals("֢״"))
						{
							withdsymptom=Jsoup.connect(tag.attr("abs:href")).timeout(0).get().select("div.p_cleftartbox").text();
						}
						else if(tag.text().equals("����֢"))
						{
							withsymptom=Jsoup.connect(tag.attr("abs:href")).timeout(0).get().select("div.p_cleftartbox").text();
						}
					}
					
					description=description.replace('\'',' ').replace('"',' ');
					ddescription=ddescription.replace('\'',' ').replace('"',' ');
					withsymptom=withsymptom.replace('\'',' ').replace('"',' ');
					withdsymptom=withdsymptom.replace('\'',' ').replace('"',' ');
					String ill=possibleill.toString().replace('\'',' ').replace('"',' ');
					pathogeny=pathogeny.replace('\'',' ').replace('"',' ');
					check=check.replace('\'',' ').replace('"',' ');
					diagnosis=diagnosis.replace('\'',' ').replace('"',' ');
					
					sql="insert into symptomdata values('"+name+"','0','0','"+bodypart+"','"+keshi+"','"+description+
							"','"+ddescription+"','"+withsymptom+"','"+withdsymptom+"','"+ill+"','"+
							pathogeny+"','"+check+"','"+diagnosis+"','0')";
					db.exec(sql,null);
					System.out.println(name+" left:"+dataQueue.size()+" total:"+total);
				}
			} 
			catch (IOException e) 
			{
				System.out.println(sql);
				System.out.println(url);
				e.printStackTrace();
			} 
			catch (Exception e) 
			{
				System.out.println(sql);
				System.out.println(url);
				e.printStackTrace();
			}
			finally
			{
				threadnum++;
			}
		}
	}
	
	public tag120askcom()
	{
		try
		{
			db=new Database();
			db.open("J:/symptom.db",0666);

			Document doc=null;
			Elements eles=null;
			
			//��֢״��ѯ
			doc=Jsoup.connect("http://tag.120ask.com/zhengzhuang/").timeout(0).get();
			eles=doc.select("ul.p_leftdivnav").get(0).children();
			for(Element ele:eles)
			{
				Elements test=ele.select("div.p_lnavdisdiv h3 a");
				Document innerdoc=Jsoup.connect(test.attr("abs:href")).timeout(0).get();
				Element toprocess=innerdoc.select("div.w_header").get(0);
				String bigbodypart=toprocess.select("h3").text();
				Elements subset=toprocess.select("a");
				
				//��ֱ�ӱ���֢״
				Elements innerele=innerdoc.select("div.w_neike.clears").get(0).children();
				for(int i=0;i<innerele.size();i+=2)
				{
					String bigclass=innerele.get(i).text();//��ȡ�������
					for(Element tempele:innerele.get(i+1).select("a"))
					{
						dataQueue.offer(new ELE(tempele.attr("title"),tempele.attr("abs:href"),SYMPTOM,bigbodypart,bigclass));
					}
				}
				if(!subset.isEmpty())
				{//���ӽڵ�����Ҫ���ӵ�ÿ����ַȻ�����֢״
					for(Element curele:subset)
					{
						Document temp=Jsoup.connect(curele.attr("abs:href")).timeout(0).get();
						Elements innerelel=temp.select("div.w_neike.clears").get(0).children();
						for(int i=0;i<innerelel.size();i+=2)
						{
							String bigclass=innerelel.get(i).text();//��ȡ�������
							for(Element tempele:innerelel.get(i+1).select("a"))
							{
								dataQueue.offer(new ELE(tempele.attr("title"),tempele.attr("abs:href"),SYMPTOM,
										bigbodypart+":"+curele.text(),bigclass));
							}
						}
					}
				}
			}
			
			//��������ѯ
			doc=Jsoup.connect("http://tag.120ask.com/jibing/").timeout(0).get();
			eles=doc.select("ul.p_leftdivnav").get(0).children();
			for(Element ele:eles)
			{
				Elements test=ele.select("div.p_lnavdisdiv h3 a");
				Document innerdoc=Jsoup.connect(test.attr("abs:href")).timeout(0).get();
				Element toprocess=innerdoc.select("div.w_header").get(0);
				String bigbodypart=toprocess.select("h3").text();
				Elements subset=toprocess.select("a");
				//��ֱ�ӱ���֢״
				Elements innerele=innerdoc.select("div.w_neike.clears").get(0).children();
				for(int i=0;i<innerele.size();i+=2)
				{
					String bigclass=innerele.get(i).text();//��ȡ�������
					for(Element tempele:innerele.get(i+1).select("a"))
					{
						dataQueue.offer(new ELE(tempele.attr("title"),tempele.attr("abs:href"),DISEASE,bigbodypart,bigclass));
					}
				}
				if(!subset.isEmpty())
				{//���ӽڵ�����Ҫ���ӵ�ÿ����ַȻ�����֢״
					for(Element curele:subset)
					{
						Document temp=Jsoup.connect(curele.attr("abs:href")).timeout(0).get();
						Elements innerele1=innerdoc.select("div.w_neike.clears").get(0).children();
						for(int i=0;i<innerele1.size();i+=2)
						{
							String bigclass=innerele1.get(i).text();//��ȡ�������
							for(Element tempele:innerele1.get(i+1).select("a"))
							{
								dataQueue.offer(new ELE(tempele.attr("title"),tempele.attr("abs:href"),DISEASE,bigbodypart,bigclass));
							}
						}
					}
				}
			}
			System.out.println(dataQueue.size());
			for(ELE ele:dataQueue)
			{
				System.out.println(ele.name);
			}
			total=dataQueue.size();
			
			if(beginstr != null)
			{
				while(!dataQueue.poll().getName().equals(beginstr))
				{
					;
				}
			}
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		new Thread()
		{
			@Override
			public void run()
			{
				try 
				{
					sleep(1000);
					while(!dataQueue.isEmpty())
					{
						int num=1;
						while(num-- > 0 && threadnum > 0 && !dataQueue.isEmpty())
						{
							new Thread()
							{
								@Override
								public void run()
								{
									ELE curele=dataQueue.poll();
									if(curele != null)
										curele.resolve();
								}
							}.start();
						}
						sleep(200);
					}
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	public static void main(String[] args)
	{
		new tag120askcom();
	}
}
