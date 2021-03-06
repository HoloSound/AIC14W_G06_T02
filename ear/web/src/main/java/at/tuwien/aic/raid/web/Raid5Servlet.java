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

import at.tuwien.aic.raid.data.FileObject;
import at.tuwien.aic.raid.data.FileViewObject;
import at.tuwien.aic.raid.data.Raid5DTO;
import at.tuwien.aic.raid.sessionbean.Raid5sessionBeanInterface;

@WebServlet("/raid5")
public class Raid5Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String DELETE_OPERATION = "delete";
	public static final String DELETE_HISTORY_OPERATION = "delete_history";
	public static final String FILE_NAME = "fileName";
	public static final String DOWNLOAD_OPERATION = "download";
	public static final String DOWNLOAD_HISTORY_OPERATION = "download_history";
	public static final String UPLOAD_OPERATION = "upload";
	public static final String GET_HISTORY_LIST = "history";
	
	@EJB
	private Raid5sessionBeanInterface raid;
	public static String GET_FILE_LIST = "list";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		if (UPLOAD_OPERATION.equals(req.getParameter("task"))) {// lets upload a
																// file
			handleUpload(req, resp);

		}
	}

	private void handleUpload(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		// process only if its multipart content
		if (ServletFileUpload.isMultipartContent(req)) {
			try {
				DiskFileItemFactory fac = new DiskFileItemFactory();
				fac.setSizeThreshold(Integer.MAX_VALUE);// set maximum possible
														// if we cant hold it in
														// memory than we cant
														// have it as a byte[]
				List<FileItem> multiparts = new ServletFileUpload(fac)
						.parseRequest(req);

				for (FileItem item : multiparts) {
					if (!item.isFormField()) 
					{
						File aFile = new File( item.getName() );

						FileObject f = new FileObject( aFile.getName() );

						f.setData(item.get());

						raid.write(f);

					}
				}

				// File uploaded successfully
				resp.getOutputStream().write("Upload success".getBytes());
			} catch (Exception ex) {
				resp.getOutputStream().write(
						(ex.getMessage() + "Upload fail").getBytes());
			}

		}

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			if (GET_HISTORY_LIST.equals(req.getParameter("task"))) {// lets
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
			if (DELETE_HISTORY_OPERATION.equals(req.getParameter("task"))) {// lets
				// delete a
				// file
				String fn = req.getParameter(FILE_NAME);
				if (fn == null) {
					error("parameter Error", resp);
					return;
				} else {
					deleteHistoryFile(fn, resp);
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
			if (DOWNLOAD_HISTORY_OPERATION.equals(req.getParameter("task"))) {// lets
				// download
				// a
				// file
				String fn = req.getParameter(FILE_NAME);
				if (fn == null) {
					error("parameter Error", resp);
					return;
				} else {
					downloadHistoryFile(fn, resp);
				}
			}
		} catch (Exception e) {
			error(e.getMessage(), resp);
		}

	}

	private void listFiles(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		StringBuilder sb = new StringBuilder();

		sb.append("<table border=\"1\">");
		sb.append("<colgroup>");
		sb.append("<col width=\"300\" />");
		sb.append("<col width=\"35\" />");
		sb.append("<col width=\"35\" />");
		sb.append("</colgroup>");
		
		sb.append("<tr>");
		sb.append("<td colspan=\"4\"><strong>FileName</strong></td>");

		sb.append("<td><strong>DropBox</strong></td>");

		sb.append("<td><strong>Box</strong></td>");

		sb.append("<td><strong>AS3</strong></td>");
		sb.append("</tr>");

		int ii=0;
		
		Raid5DTO raid5Dto =  raid.listFiles();
		ArrayList<FileViewObject> fvol = raid5Dto.getFileViewObjects();
		
		for( FileViewObject fvo : fvol ) 
		{
			FileObject f = fvo.getGlobalFo();
			sb.append("<tr>");

			// may be done via class - and css definition
			sb.append("<td");
			if( ii % 2 == 0 )
			{
				sb.append( " bgcolor=\"#eeeeff\"" );
			}
			sb.append( ">" );
			
			// in principle the name itself may be the downloadlink
			sb.append( "<tt>" );
			sb.append(f.getName());
			sb.append("</tt></td>");

			sb.append("<td");
			if( ii % 2 == 0 )
			{
				sb.append( " bgcolor=\"#eeeeff\"" );
			}
			sb.append( ">" );
			
			sb.append(getDownloadLink(f));
			sb.append("</td>");

			sb.append("<td");
			if( ii % 2 == 0 )
			{
				sb.append( " bgcolor=\"#eeeeff\"" );
			}
			sb.append( ">" );
			
			sb.append(getDeleteLink(f));
			sb.append("</td>");
			
			sb.append("<td");
			if (ii % 2 == 0) {
				sb.append(" bgcolor=\"#eeeeff\"");
			}
			sb.append(">");
			sb.append( getShowHistoryLink( f ) );
			sb.append("</td>");
			
			
			
			// now appending the further columns
			FileObject[] interfaceInformationFos = fvo.getInterfaceInformationFos();
			
			for( int jj = 0 ; jj < 3 ; jj++ )
			{
				FileObject actFO = interfaceInformationFos[jj];		
				
				sb.append("<td");
				if( ii % 2 == 0 )
				{
					sb.append( " bgcolor=\"#eeeeff\"" );
				}
				sb.append( ">" );
				
				if( actFO != null )
				{
					sb.append( "<tt>" + actFO.getName() + "</tt>");
				}
				else
				{
					sb.append( "&nbsp;" );
				}
				
				sb.append("</td>");						
			}	
			

			sb.append("</tr>");
			
			ii++;
		}

		sb.append("</table>");
		resp.getWriter().write(sb.toString());
	}

	private void getFileHistory(String fn, HttpServletRequest req, HttpServletResponse resp) 
			throws IOException 
	{
	
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("<head>   <script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js\"></script> <SCRIPT type=\"text/javascript\" src=\"script.js\"></script></head>");
			
			sb.append( "<h1>RAID5 history of file: " + fn + "</h1>" );

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

			Raid5DTO raid5Dto =  raid.getFileHistory( fn );
			ArrayList<FileViewObject> fvol = raid5Dto.getFileViewObjects();
			
			if( fvol.size() == 0 )
			{
				sb.append("<tr>");
				sb.append("<td colspan=\"3\"><strong>No History available.</strong></td>");
				sb.append("</tr>");
			}
			else
			{
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
	
					sb.append(getDownloadHistoryLink(f));
					sb.append("</td>");				
					
					sb.append("<td");
					if (ii % 2 == 0) {
						sb.append(" bgcolor=\"#eeeeff\"");
					}
					sb.append(">");
					String l = "<a  class='btn btn-sm alert-danger-transparent' title=\"Delete file\" href='javascript:;' onclick=\"jQuery.get('raid5?task="
					+ Raid5Servlet.DELETE_HISTORY_OPERATION + "&" + Raid5Servlet.FILE_NAME+"="+f.getName()
					+ "', '', alertAndReload, 'text' )\" > <img src=\"/web/pic/delete.png\" alt=\"delete\"/> </a>";
					
					sb.append(l);
					sb.append("</td>");
	
					sb.append("</tr>");
	
					ii++;
				}
			}

			sb.append("</table>");
			resp.getWriter().write(sb.toString());
		} catch (Exception e) {
			resp.getWriter().write("error " + e.getMessage());
		}

	}
	
	private void downloadFile(String fn, HttpServletResponse resp)
			throws IOException 
	{
		log( "downloadFile( " + fn + " )" );
		
		FileObject f = raid.getFile( fn );
		
		log( "downloadFile() result f may be not correct!" );
		log( "downloadFile( " + f.getName() + " ) " );
		log( "downloadFile() I'M HERE! - CORRECT" );
		
		resp.setContentType("application/octet-stream");
		resp.setHeader("Content-Disposition",
				"attachment; filename=\"" + f.getName() + "\"");
		resp.getOutputStream().write( f.getData() );

	}
	
	private void downloadHistoryFile(String fn, HttpServletResponse resp)
			throws IOException 
	{
		log( "downloadHistoryFile( " + fn + " )" );
		
		FileObject f = raid.getHistoryFile( fn );
		
		log( "downloadHistoryFile() result f may be not correct!" );
		log( "downloadHistoryFile( " + f.getName() + " ) " );
		log( "downloadHistoryFile() I'M HERE! - CORRECT" );
		
		resp.setContentType("application/octet-stream");
		resp.setHeader("Content-Disposition",
				"attachment; filename=\"" + f.getName() + "\"");
		resp.getOutputStream().write( f.getData() );

	}

	private void deleteFile(String fn, HttpServletResponse resp)
			throws IOException {
		raid.delete(fn);
		resp.getOutputStream().write("Delete sucessfull".getBytes());

	}
	
	private void deleteHistoryFile(String fn, HttpServletResponse resp)
			throws IOException {
		raid.deleteHistory(fn);
		resp.getOutputStream().write("Delete sucessfull".getBytes());

	}

	private void error(String string, HttpServletResponse resp)
			throws IOException {
		resp.getOutputStream().write(string.getBytes());
	}

	
	private String getDownloadLink(FileObject f) {

		return "<a target='_blank' class='btn btn-sm alert-success-transparent' title=\"Download file\" href=\"raid5?task="
				+ Raid5Servlet.DOWNLOAD_OPERATION + "&"
				+ Raid5Servlet.FILE_NAME + "="+f.getName()+"\"> <img src=\"/web/pic/download.png\" alt=\"download\"/> </a>";
	}
	
	private String getDownloadHistoryLink(FileObject f) {

		return "<a target='_blank' class='btn btn-sm alert-success-transparent' title=\"Download file\" href=\"raid5?task="
				+ Raid5Servlet.DOWNLOAD_HISTORY_OPERATION + "&"
				+ Raid5Servlet.FILE_NAME + "="+f.getName()+"\"> <img src=\"/web/pic/download.png\" alt=\"download\"/> </a>";
	}

	private String getDeleteLink(FileObject f) {

		return "<a target='_blank' class='btn btn-sm alert-danger-transparent' title=\"Delete file\" href='javascript:;' onclick=\"jQuery.get('raid5?task="
				+ Raid5Servlet.DELETE_OPERATION + "&" + Raid5Servlet.FILE_NAME+"="+f.getName()
				+ "', '', callback5, 'text' )\" > <img src=\"/web/pic/delete.png\" alt=\"delete\"/> </a>";
	}
	
	private String getDeleteHistoryLink(FileObject f) {

		return "<a target='_blank' class='btn btn-sm alert-danger-transparent' title=\"Delete file\" href='javascript:;' onclick=\"jQuery.get('raid5?task="
				+ Raid5Servlet.DELETE_HISTORY_OPERATION + "&" + Raid5Servlet.FILE_NAME+"="+f.getName()
				+ "', '', callback5, 'text' )\" > <img src=\"/web/pic/delete.png\" alt=\"delete\"/> </a>";
	}

	private Object getShowHistoryLink(FileObject f) {
		return "<a target='_blank' class='btn btn-sm alert-warning-transparent' title=\"Show file history\" href=\"raid5?task=" + Raid5Servlet.GET_HISTORY_LIST + "&" + Raid1Servlet.FILE_NAME + "=" + f.getName() + "\"> <img src=\"/web/pic/history.png\" alt=\"history\"/> </a>";
	}
	
}
