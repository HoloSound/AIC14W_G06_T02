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
import at.tuwien.aic.raid.sessionbean.Raid5sessionBeanInterface;

@WebServlet("/raid5")
public class Raid5Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String DELETE_OPERATION = "delete";
	public static final String FILE_NAME = "fileName";
	public static final String DOWNLOAD_OPERATION = "download";
	public static final String UPLOAD_OPERATION = "upload";
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

	private void listFiles(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		StringBuilder sb = new StringBuilder();

		sb.append("<table border=\"1\">");
		sb.append("<colgroup>");
		sb.append("<col width=\"300\" />");
		sb.append("<col width=\"35\" />");
		sb.append("<col width=\"35\" />");
		sb.append("</colgroup>");
		
		sb.append("<tr>");
		sb.append("<td colspan=\"3\"><strong>FileName</strong></td>");

		sb.append("<td><strong>DropBox</strong></td>");

		sb.append("<td><strong>Box</strong></td>");

		sb.append("<td><strong>AS3</strong></td>");
		sb.append("</tr>");

		int ii=0;
		
		ArrayList<FileViewObject> fvol = raid.listFiles();
		
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
				
				sb.append( "<tt>" + actFO.getName() + "</tt>");
				sb.append("</td>");						
			}	
			

			sb.append("</tr>");
			
			ii++;
		}

		sb.append("</table>");
		resp.getWriter().write(sb.toString());
	}

	private void downloadFile(String fn, HttpServletResponse resp)
			throws IOException {
		FileObject f = raid.getFile(fn);
		resp.setContentType("application/octet-stream");
		resp.setHeader("Content-Disposition",
				"attachment; filename=\"" + f.getName() + "\"");
		resp.getOutputStream().write(f.getData());

	}

	private void deleteFile(String fn, HttpServletResponse resp)
			throws IOException {
		raid.delete(fn);
		resp.getOutputStream().write("delete sucessfull".getBytes());

	}

	private void error(String string, HttpServletResponse resp)
			throws IOException {
		resp.getOutputStream().write(string.getBytes());
	}

	
	private String getDownloadLink(FileObject f) {

		return "<a target='_blank' href=\"raid5?task="
				+ Raid5Servlet.DOWNLOAD_OPERATION + "&"
				+ Raid5Servlet.FILE_NAME + "="+f.getName()+"\"> <img src=\"/web/pic/download.png\" alt=\"download\"/> </a>";
	}

	private String getDeleteLink(FileObject f) {

		return "<a target='_blank' href='javascript:void' onclick=\"jQuery.get('raid5?task="
				+ Raid5Servlet.DELETE_OPERATION + "&" + Raid5Servlet.FILE_NAME+"="+f.getName()
				+ "', '', callback5, 'text' )\" > <img src=\"/web/pic/delete.png\" alt=\"delete\"/> </a>";
	}

}
