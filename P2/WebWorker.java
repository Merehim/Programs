/**
*Author: Jose Pacheco
*
 **/

/**
 * Web worker: an object of this class executes in its own new thread
 * to receive and respond to a single HTTP request. After the constructor
 * the object executes on its "run" method, and leaves when it is done.
 *
 * One WebWorker object is only responsible for one client connection. 
 * This code uses Java threads to parallelize the handling of clients:
 * each WebWorker runs in its own thread. This means that you can essentially
 * just think about what is happening on one client at a time, ignoring 
 * the fact that the entirety of the webserver execution might be handling
 * other clients, too. 
 *
 * This WebWorker class (i.e., an object of this class) is where all the
 * client interaction is done. The "run()" method is the beginning -- think
 * of it as the "main()" for a client interaction. It does three things in
 * a row, invoking three methods in this class: it reads the incoming HTTP
 * request; it writes out an HTTP header to begin its response, and then it
 * writes out some HTML content for the response content. HTTP requests and
 * responses are just lines of text (in a very particular format). 
 *
 **/

import java.io.*;
import java.lang.Runnable;
import java.net.Socket;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

public class WebWorker implements Runnable
{
	
	int errCode;
	String fileName;
	
	Date date = new Date();
	DateFormat formatedDate = DateFormat.getDateTimeInstance();
	formatedDate.setTimeZone(TimeZone.getTimeZone("GMT")
	
	private Socket socket;	

	/**
	 * Constructor: must have a valid open socket
	 **/
	public WebWorker(Socket s)
	{
		socket = s;
	}

	/**
	 * Worker thread starting point. Each worker handles just one HTTP
	 * request and then returns, which destroys the thread. This method
	 * assumes that whoever created the worker created it with a valid
	 * open socket object.
	 **/
	@Override
	public void run()
	{
		System.err.println("Handling connection...");
		try 
		{
			String contentType = "";	
			InputStream  is = socket.getInputStream();	
			OutputStream os = socket.getOutputStream();
			String pathName = readHTTPRequest(is);
			
			if(pathName.contains(".png ")
			{
				contentType = "image/png";
			}
			else if(pathName.contains(".jpeg ")
			{
				contentType = "image/jpeg";
			}
			else if(pathName.contains(".gif ")
			{
				contentType = "image/gif";
			}
			else if(pathName.contains(".ico ")
			{
				contentType = "image/x-icon";
			}
			else
			{
				contentType = "text/html";	
			}
			
			writeHTTPHeader(os, contentType, pathName);			
			writeContent(os, pathName, contentType);		
						
			os.flush();
			socket.close();
		} 
		catch (Exception e) 
		{
			System.err.println("Output error: " + e);
		}

		System.err.println("Done handling connection.");
		return;
	}

	/**
	 * Read the HTTP request header.
	 **/
	private String readHTTPRequest(InputStream is)
	{
		String line;
		String path = "";
		BufferedReader r = new BufferedReader(new InputStreamReader(is));

		while (true) 
		{
			try 
			{
				while (!r.ready()) Thread.sleep(1);
				line = r.readLine();
				
				// Parse the file name and extension
				if (line.contains("GET ")) 
				{
					path = line.substring(4);
					for (int i = 0; i < path.length(); i++) 
					{
						if (path.charAt(i) == ' ')
						{
							path = path.substring(0,i);
							break;		
						}
					}
					System.err.println("Path collected: " + path);
				}
				System.err.println("Request line: ("+line+")");
				if (line.length() == 0) { break; }
			}
			catch (Exception e) 
			{
				System.err.println("Request error: " + e);
				break;
			}
		}
		path = System.getProperty("user.dir") + path;
		return path;
	}

	/**
	 * Write the HTTP header lines to the client network connection.
	 * @param os is the OutputStream object to write to
	 * @param contentType is the string MIME content type (e.g. "text/html")
	 **/
	private void writeHTTPHeader(OutputStream os, String contentType, String contentPath) throws Exception
	{
		File contentFile = new File(contentPath);
		// If the file created exists then send 200
		if(contentFile.exists()) 
		{
			os.write("HTTP/1.1 200 WORKING\n".getBytes());
			errCode = 200;
		}
		else 
		{
			os.write("HTTP/1.1 404 ERROR\n".getBytes());
			System.err.println("ERROR: File " + contentFile.toString() + " does not exist!");
			errCode = 404;
		}
		// Write all data of header
		os.write("Date: ".getBytes());
		os.write((formatDate.format(date)).getBytes());
		os.write("\n".getBytes());
		os.write("Server: Joey's Server\n".getBytes());
		os.write("Connection: close\n".getBytes());
		os.write("Content-Type: ".getBytes());
		os.write(contentType.getBytes());
		os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
		return;
	}

	/**
	 * Write the data content to the client network connection. This MUST
	 * be done after the HTTP header has been written out.
	 * @param os is the OutputStream object to write to
	 **/
	private void writeContent(OutputStream os, String contentType, String contentPath) throws Exception
	{
		String content = "";
		String pathContent = contentPath;

		if (errCode == 200) 
		{
			File fileName = new File(pathContent);
			BufferedReader buffer = null;
			try
			{
				buffer = new BufferedReader(new FileReader(fileName));
			}
			catch (FileNotFoundException e)
			{
				os.write("<body bgcolor = \"#666FFF\">".getBytes());
				os.write("<h1><b>404: Not Found</b></h1>".getBytes());
				os.write("The page you are looking for does not exist!".getBytes());
				throw e;
			}
			
			if (contentType.equals("text/html"))
			{
				while ((content = buffer.readLine()) != null) 
				{
					if(content.contains("<cs371date>"))
					{
						content += formatDate.format(date); // Replace <cs371date> tag with today's date
					}
					else if(content.contains("<cs371server>"))
					{
						content += "NewTag"; // Replace <cs371server> with specified string
					}			
						
					os.write(content.getBytes());
					os.write( "\n".getBytes());
				}
			}
			else if (contentType.contains("image"))
			{
				FileInputStream imageIn = new FileInputStream(filename);
				byte imageArray[] = new byte [(int) filename.length()]; 
				imageIn.read(imageArr);
				DataOutputStream imgOut = new DataOutputStream(os);
				imgOut.write(imgArr);
			}	
	}
} // end class