import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Semaphore;


public class WebWorker extends Thread
{
	private String website;
	private Semaphore semaphore;
	private WebFrame webFrame;
	private int rowNum;
	
	public WebWorker(WebFrame webFrame,String website, int rowNum, Semaphore semaphore) //got help making this from tutor
	{
		this.website = website;
		this.semaphore = semaphore;
		this.webFrame = webFrame;
		this.rowNum = rowNum;
	}
	
	@Override
	public void run() 
	{
		try
		{
			semaphore.acquire();
			webFrame.runIncrease();
			download();
			webFrame.runDecrease();
			semaphore.release();
		} 
		catch (InterruptedException e)
		{
			webFrame.completeRun(rowNum, "Interruptted");
		} 
		webFrame.changeTime();
	}
	
	public void download()  
	{
		InputStream input = null;
		StringBuilder contents = null;
		webFrame.setRunningStatus(rowNum,"Running");
		try 
		{
			URL url = new URL(website);
			URLConnection connection = url.openConnection();
		
			connection.setConnectTimeout(5000);
			
			connection.connect();
			input = connection.getInputStream();

			BufferedReader reader  = new BufferedReader(new InputStreamReader(input));
		
			char[] array = new char[1000];
			int len;
			contents = new StringBuilder(1000);
			while ((len = reader.read(array, 0, array.length)) > 0)
			{
				contents.append(array, 0, len);
				Thread.sleep(100);
			}

			webFrame.completeRun(rowNum, "Success");
		}
		
		catch(MalformedURLException ignored)
		{
			webFrame.completeRun(rowNum, "Bad url");
		}
		catch(InterruptedException exception)
		{
			webFrame.completeRun(rowNum, "Interrupted while running");
		}
		catch(IOException ignored)
		{
			webFrame.completeRun(rowNum, "Reading error");
		}
	
		finally 
		{
			try
			{
				if (input != null)
				{ 
					input.close();
				}
			}
			catch(IOException ignored) {}
		}
 
	}
}
