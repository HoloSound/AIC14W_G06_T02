package at.tuwien.aic.raid.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import at.tuwien.aic.raid.ConnectorInterface;
import at.tuwien.aic.raid.data.FileObject;
import at.tuwien.aic.raid.data.FileViewObject;
import at.tuwien.aic.raid.data.Raid1DTO;
import at.tuwien.aic.raid.sessionbean.RaidSessionBeanInterface;

@WebServlet("/raid1")
public class Raid1Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String DELETE_OPERATION = "delete";
	public static final String FILE_NAME = "fileName";
	public static final String DOWNLOAD_OPERATION = "download";
	public static final String UPLOAD_OPERATION = "upload";
	public static final String FILE_INFO = "fileInfo";
	public static final String COPY = "copy";
	public static final String FROM = "from";
	public static final String TO = "to";
	public static final String GET_FILE_LIST = "list";	
	private static final String NON_EXISTENT = "--------------------------------";
	
	public static final String SHOW_HISTORY = "history";
	@EJB
	private RaidSessionBeanInterface raid;


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (UPLOAD_OPERATION.equals(req.getParameter("task"))) {// lets upload a
																// file
			handleUpload(req, resp);

		}
	}

	private void handleUpload(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// process only if its multipart content
		if (ServletFileUpload.isMultipartContent(req)) {
			try {
				DiskFileItemFactory fac = new DiskFileItemFactory();
				fac.setSizeThreshold(Integer.MAX_VALUE);// set maximum possible
														// if we cant hold it in
														// memory than we cant
														// have it as a byte[]
				List<FileItem> multiparts = new ServletFileUpload(fac).parseRequest(req);

				for (FileItem item : multiparts) {
					if (!item.isFormField()) {
						File aFile = new File(item.getName());

						FileObject f = new FileObject(aFile.getName());

						f.setData(item.get());

						raid.write(f);

					}
				}

				// File uploaded successfully
				resp.getOutputStream().write("Upload success".getBytes());
			} catch (Exception ex) {
				resp.getOutputStream().write((ex.getMessage() + "Upload fail").getBytes());
			}

		}

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			if (FILE_INFO.equals(req.getParameter("task"))) {// lets
				String fn = req.getParameter(FILE_NAME);
				getFileInfo(fn, req, resp);
			}

			if (COPY.equals(req.getParameter("task"))) {// lets
				String from = req.getParameter(FROM);
				String to = req.getParameter(TO);
				String fn = req.getParameter(FILE_NAME);
							
				copyFile(from,to,fn, req, resp);
			}
			if (SHOW_HISTORY.equals(req.getParameter("task"))) {// lets
				String fn = req.getParameter(FILE_NAME);
				getFileHistory(fn, req, resp);
			}
			if (GET_FILE_LIST.equals(req.getParameter("task"))) {// lets
				listFiles(req, resp);
			}
			if (DELETE_OPERATION.equals(req.getParameter("task"))) {// lets
																	// delete a
																	// file
				String fn = req.getParameter(FILE_NAME);
				if (fn == null) {
					error("parameter Error", resp);
					return;
				} else {
					deleteFile(fn, resp);
				}
			}

			if (DOWNLOAD_OPERATION.equals(req.getParameter("task"))) {// lets
																		// download
																		// a
																		// file
				String fn = req.getParameter(FILE_NAME);
				if (fn == null) {
					error("parameter Error", resp);
					return;
				} else {
					downloadFile(fn, resp);
				}
			}
		} catch (Exception e) {
			error(e.getMessage(), resp);
		}

	}



	private void getFileHistory(String fn, HttpServletRequest req, HttpServletResponse resp) 
			throws IOException 
	{
	
		try {
			StringBuilder sb = new StringBuilder();

			sb.append( "<h1>RAID1 history of file: " + fn + "</h1>" );
			
			// building up a table
			sb.append("<table border=\"1\">");
			sb.append("<colgroup>");
			sb.append("<col width=\"300\" />");
			sb.append("<col width=\"35\" />");
			sb.append("<col width=\"35\" />");
			sb.append("</colgroup>");

			// building up the table header
			sb.append("<thead>");
			sb.append("<tr>");
			sb.append("<td colspan=\"3\"><strong>FileName</strong></td>");

			sb.append("</tr>");

			sb.append("</thead>");

			int ii = 0;

			// this listFiles is method which compromizes the fileinformation
			// --> global info
			// We should here show a
			// ArrayList<FileObjectView> ... that means global info and
			// information per interface!
			// ArrayList<FileObject> fl = raid.listFiles();

			// here we should to more things.
			// we generate a column for each interface
			// we fill the generated table with additional information
			// ArrayList<FileObject> as = raid.listFiles( 0 );
			// ArrayList<FileObject> box = raid.listFiles( 1 );
			// ArrayList<FileObject> dBox = raid.listFiles( 2 );
			// The creation of viewing would be the wrong place - we should here
			// only output it!

			// building up a table row
			Raid1DTO raid1Dto = raid.getFileHistory( fn );
			ArrayList<FileViewObject> fvol = raid1Dto.getFileViewObjects();

			for (FileViewObject fvo : fvol) {
				FileObject f = fvo.getGlobalFo();
				String id = f.getName().replace(".", "").replaceAll("#", "").replaceAll(" ", "") + "TD";

				sb.append("<tr>");

				// may be done via class - and css definition
				sb.append("<td");
				if (ii % 2 == 0) {
					sb.append(" bgcolor=\"#eeeeff\"");
				}
				sb.append(">");

				// in principle the name itself may be the downloadlink
				sb.append("<tt>");
				sb.append(f.getName());
				sb.append("</tt></td>");

				sb.append("<td");
				if (ii % 2 == 0) {
					sb.append(" bgcolor=\"#eeeeff\"");
				}
				sb.append(">");

				sb.append(getDownloadLink(f));
				sb.append("</td>");				
				
				sb.append("<td");
				if (ii % 2 == 0) {
					sb.append(" bgcolor=\"#eeeeff\"");
				}
				sb.append(">");
				sb.append(getDeleteLink(f));
				sb.append("</td>");

				sb.append("</tr>");

				ii++;
			}

			sb.append("</table>");
			resp.getWriter().write(sb.toString());
		} catch (Exception e) {
			resp.getWriter().write("error " + e.getMessage());
		}

	}

	private void copyFile(String from2, String to2, String fn, HttpServletRequest req, HttpServletResponse resp) 
			throws IOException 
	{
		// resp.getOutputStream().write( raid.copyFile(fn, from2, to2 ).getBytes() );

		try 
		{
			StringBuffer sb = new StringBuffer();
			String hashValues[] = new String[3];
			
			sb.append("<p>");			
			
			// building up a table row
			Raid1DTO raid1Dto = raid.copyFile(fn, from2, to2 );
			ArrayList<FileViewObject> fvol = raid1Dto.getFileViewObjects();
			String[] interfaceNames = raid1Dto.getInterfaceNames();
			
			for (FileViewObject fvo : fvol) 
			{
				FileObject[] fols = fvo.getInterfaceInformationFos();
				
				int ii = 0;
				
				for( FileObject fo : fols )
				{
					sb.append( "<tt>" );
					sb.append( fo.getHash() );
					hashValues[ii] = fo.getHash();
					sb.append( "</tt>&nbsp;&nbsp;" );
					
					sb.append( interfaceNames[ii] );
					
					sb.append( "<br />" );
					ii++;
				}
			}
			
			compareHashValues( sb, fn, interfaceNames, hashValues );
			
			sb.append("</p>");
			
			resp.getWriter().write(sb.toString());
		} 
		catch (Exception e) 
		{
			resp.getWriter().write("error " + e.getMessage());
		}
	}
	
	private void getFileInfo(String fn, HttpServletRequest req,
			HttpServletResponse resp) 
					throws IOException 
	{
		// resp.getOutputStream().write( raid.getFileInfo(fn).getBytes() );

		try 
		{
			StringBuffer sb = new StringBuffer();
			String hashValues[] = new String[3];
			
			sb.append("<p>");			
			
			// building up a table row
			Raid1DTO raid1Dto = raid.getFileInfo( fn );
			ArrayList<FileViewObject> fvol = raid1Dto.getFileViewObjects();
			String[] interfaceNames = raid1Dto.getInterfaceNames();
			
			for (FileViewObject fvo : fvol) 
			{
				FileObject[] fols = fvo.getInterfaceInformationFos();
				
				int ii = 0;
				
				for( FileObject fo : fols )
				{
					sb.append( "<tt>" );
					sb.append( fo.getHash() );
					hashValues[ii] = fo.getHash();
					
log( "Id: " + ii + " | " + hashValues[ii] );					
					
					sb.append( "</tt>&nbsp;&nbsp;" );	
					
					sb.append( interfaceNames[ii] );
					
					sb.append( "<br />" );
					ii++;
				}
			}
				
			compareHashValues( sb, fn, interfaceNames, hashValues );
			
			sb.append("</p>");
			
			resp.getWriter().write( sb.toString() );
		} 
		catch (Exception e) 
		{
			resp.getWriter().write("error " + e.getMessage());
		}
	}

	private void listFiles(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		try {
			StringBuilder sb = new StringBuilder();

			// building up a table
			sb.append("<table border=\"1\">");
			sb.append("<colgroup>");
			sb.append("<col width=\"300\" />");
			sb.append("<col width=\"35\" />");
			sb.append("<col width=\"35\" />");
			sb.append("</colgroup>");

			// building up the table header
			sb.append("<thead>");
			sb.append("<tr>");
			sb.append("<td colspan=\"5\"><strong>FileName</strong></td>");

			sb.append("<td><strong>Info</strong></td>");

			sb.append("</tr>");

			sb.append("</thead>");

			int ii = 0;

			// this listFiles is method which compromizes the fileinformation
			// --> global info
			// We should here show a
			// ArrayList<FileObjectView> ... that means global info and
			// information per interface!
			// ArrayList<FileObject> fl = raid.listFiles();

			// here we should to more things.
			// we generate a column for each interface
			// we fill the generated table with additional information
			// ArrayList<FileObject> as = raid.listFiles( 0 );
			// ArrayList<FileObject> box = raid.listFiles( 1 );
			// ArrayList<FileObject> dBox = raid.listFiles( 2 );
			// The creation of viewing would be the wrong place - we should here
			// only output it!

			// building up a table row
			Raid1DTO raid1Dto = raid.listFiles();
			ArrayList<FileViewObject> fvol = raid1Dto.getFileViewObjects();

			for (FileViewObject fvo : fvol) {
				FileObject f = fvo.getGlobalFo();
				String id = f.getName().replace(".", "").replaceAll("#", "").replaceAll(" ", "") + "TD";

				sb.append("<tr>");

				// may be done via class - and css definition
				sb.append("<td");
				if (ii % 2 == 0) {
					sb.append(" bgcolor=\"#eeeeff\"");
				}
				sb.append(">");

				// in principle the name itself may be the downloadlink
				sb.append("<tt>");
				sb.append(f.getName());
				sb.append("</tt></td>");

				sb.append("<td");
				if (ii % 2 == 0) {
					sb.append(" bgcolor=\"#eeeeff\"");
				}
				sb.append(">");

				sb.append(getDownloadLink(f));
				sb.append("</td>");				
				
				sb.append("<td");
				if (ii % 2 == 0) {
					sb.append(" bgcolor=\"#eeeeff\"");
				}
				sb.append(">");
				sb.append(getDeleteLink(f));
				sb.append("</td>");
				
				sb.append("<td");
				if (ii % 2 == 0) {
					sb.append(" bgcolor=\"#eeeeff\"");
				}
				sb.append(">");
				sb.append(getShowHistoryLink(f));
				sb.append("</td>");

				sb.append("<td");
				if (ii % 2 == 0) {
					sb.append(" bgcolor=\"#eeeeff\"");
				}
				sb.append(">");
				sb.append( getShowInfoLink(id, f) );
				sb.append("</td>");
				
				sb.append("<td id='" + id + "'>");
				sb.append("<a href=\"javascript:void\" onclick=\"loadFileInfo('" + f.getName() + "','" + id + "');\" title=\"Show file info\"> load</a>");
				sb.append("</td>");

				sb.append("</tr>");

				ii++;
			}

			sb.append("</table>");
			resp.getWriter().write(sb.toString());
		} catch (Exception e) {
			resp.getWriter().write("error " + e.getMessage());
		}

	}

	private void downloadFile(String fn, HttpServletResponse resp) throws IOException {
		FileObject f = raid.getFile(fn);
		resp.setContentType("application/octet-stream");
		resp.setHeader("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"");
		resp.getOutputStream().write(f.getData());

	}

	private void deleteFile(String fn, HttpServletResponse resp) throws IOException {
		raid.delete(fn);
		resp.getOutputStream().write("delete sucessfull".getBytes());

	}

	private void error(String string, HttpServletResponse resp) throws IOException {
		resp.getOutputStream().write(string.getBytes());
	}

	// This changes have no effect!
	private String getDownloadLink(FileObject f) {

		return "<a target='_blank' title=\"Download file\" href=\"raid1?task=" + Raid1Servlet.DOWNLOAD_OPERATION + "&" + Raid1Servlet.FILE_NAME + "=" + f.getName()
				+ "\"> <img src=\"/web/pic/download.png\" alt=\"download\"/> </a>";
	}

	// This changes have no effect!
	private String getDeleteLink(FileObject f) {

		return "<a target='_blank' title=\"Delete file\" href='javascript:void' onclick=\"jQuery.get('raid1?task=" + Raid1Servlet.DELETE_OPERATION + "&" + Raid1Servlet.FILE_NAME + "=" + f.getName()
				+ "', '', callback1, 'text' )\" > <img src=\"/web/pic/delete.png\" alt=\"delete\"/> </a>";
	}

	private Object getShowHistoryLink(FileObject f) {
		return "<a target='_blank' title=\"Show file history\" href=\"raid1?task=" + Raid1Servlet.SHOW_HISTORY + "&" + Raid1Servlet.FILE_NAME + "=" + f.getName() + "\"> <img src=\"/web/pic/history.png\" alt=\"history\"/> </a>";
	}

	private Object getShowInfoLink(String id, FileObject f) {
		return "<a href=\"javascript:void\" onclick=\"loadFileInfo('" + f.getName() + "','" + id + "');\" title=\"Show file info\"><img src=\"/web/pic/info.png\" alt=\"info\"/></a>";
	}
	
	
	/* Methods from Raid1.java --> should be taken here to produce HTML output!
	 * At the moment there's only String interface ...
	 * *DTO may be necessary (depending on RAID1 / RAID5)
	 */
	/**
	 * addTwoLinks - generate the file copy between two interfaces depending on the
	 * hashValues of the files.
	 * 
	 * @param b			concatenation Buffer
	 * @param file		name of the file
	 * @param from		index of from interface
	 * @param fromIsEmpty	defining if file does no exist
	 * @param to		index of the to interface
	 * @param toIsEmpty		defining if file does no exist
	 */
	
	private void addTwoLinks( StringBuffer b, String file, 
			int from, boolean fromIsEmpty,
			int to, boolean toIsEmpty )
	{
		b.append("&nbsp;");	
		
log( "File: " + file + " FROM: " + from + " | " + fromIsEmpty 
		+ " TO: " + to + " | " + toIsEmpty );
		
		if( toIsEmpty == false )
			addLink( b, file, to, from, true );
		
		b.append("&nbsp;|&nbsp;");	
		
		if( fromIsEmpty == false )
			addLink( b, file, from, to, false );
		
		b.append("&nbsp;");	
	}
	
	private void addLink( StringBuffer b, String file, int from, int to, boolean isLeft )
	{
		b.append("<a target='_blank' href=\"raid1?task=copy&from=" + from  
				+ "&to=" + to 
				+ "&fileName=" + file + "\">" );
		
		if( isLeft == true )
			b.append( "<img src=\"/web/pic/copy_left.png\" alt=\"copy left\" />");
		else
			b.append( "<img src=\"/web/pic/copy_right.png\" alt=\"copy right\" />");
			
		b.append("</a>");			
	}
	
	private void generateCopyButtons( StringBuffer b, String fileName, String[] interfaceNames, String[] hashValues )
	{
		String firstHashValue = null;
		String actHashValue = null;
		String previousHashValue = null;
		String firstConnectorInterfaceName = null;
		int actId = 0;
		int firstId = -1;
		int previousId = -1;
		boolean firstIsEmpty = false;
		boolean actIsEmpty = false;
		boolean previousIsEmpty = false;

		b.append("<p>");
		
		for( String interfaceName : interfaceNames ) 
		{			
			actHashValue = hashValues[actId];
			
			if( actHashValue.compareTo( NON_EXISTENT ) == 0 )
				actIsEmpty = true;
			else
				actIsEmpty = false;
		
			
			if( firstHashValue == null )
			{
				firstHashValue = actHashValue;
				firstId = actId;
				firstConnectorInterfaceName = interfaceName;
				
				if( firstHashValue.compareTo( NON_EXISTENT ) == 0 )
				{
					firstIsEmpty = true;
				}
			}
			
			if( previousHashValue != null )
			{
				log(  "PRE: " + previousHashValue + " --> ACT: " + actHashValue );
				if( previousHashValue.compareTo( actHashValue ) != 0  )
				{
					// generate "<" and ">" button
					addTwoLinks( b, fileName, previousId, previousIsEmpty, actId, actIsEmpty );
				}
				else
				{
					b.append("&nbsp;|&nbsp;");							
				}
			}
			else
			{
				b.append("&nbsp;|&nbsp;");							
			}
			
			b.append( "<bold>" + interfaceName + "</bold>" );
			
			previousHashValue = actHashValue;
			previousId = actId;
			
			if( previousHashValue.compareTo( NON_EXISTENT ) == 0 )
				previousIsEmpty = true;
			else
				previousIsEmpty = false;
			
			actId++;
		}
		
		if( firstHashValue != null )
		{
			if( firstHashValue.compareTo( actHashValue ) != 0  )
			{
				// generate "<" and ">" button
				addTwoLinks( b, fileName, previousId, previousIsEmpty, firstId, firstIsEmpty );
			}
			else
			{
				b.append("&nbsp;|&nbsp;");							
			}
		}
		else
		{
			b.append("&nbsp;|&nbsp;");							
		}
		
		b.append( "<bold>" + firstConnectorInterfaceName + "</bold>&nbsp;|" );
		
		b.append("</p>");		
	}
	
	private String compareHashValues( StringBuffer b, String fileName, String[] interfaceNames, String hashValues[] )
	{
		try
		{
			boolean differentHashvalues = false;
			String preHash = null;

			for (String hashValue : hashValues) 
			{
				if (preHash == null) 
				{
					preHash = hashValue;
				} 
				else 
				{
					if (preHash.compareTo(hashValue) != 0) 
					{
						differentHashvalues = true;
						break;
					}
				}
			}

			// now we are generating some exchange Buttons
			if( differentHashvalues ) 
			{
				generateCopyButtons( b, fileName, interfaceNames, hashValues );
			}

			return b.toString();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return "error:" + e.getMessage();
		}
	}
}
