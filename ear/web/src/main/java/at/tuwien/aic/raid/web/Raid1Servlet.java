package at.tuwien.aic.raid.web;

import java.io.ByteArrayInputStream;
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

import at.tuwien.aic.raid.Raid1;
import at.tuwien.aic.raid.data.FileObject;
import at.tuwien.aic.raid.data.FileViewObject;
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
	
	public static final String SHOW_HISTORY = "history";
	@EJB
	private RaidSessionBeanInterface raid;
	public static String GET_FILE_LIST = "list";

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

	private void copyFile(String from2, String to2, String fn, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.getOutputStream().write("TODO ".getBytes());
		
	}

	private void getFileHistory(String fn, HttpServletRequest req, HttpServletResponse resp) throws IOException {

		resp.getOutputStream().write(("<h1>the history for  " + fn + " will be here </h1>").getBytes());

	}

	private void getFileInfo(String fn, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.getOutputStream().write(raid.getFileInfo(fn).getBytes());

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
			sb.append("<td colspan=\"4\"><strong>FileName</strong></td>");

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

			ArrayList<FileViewObject> fvol = raid.listFiles();

			for (FileViewObject fvo : fvol) {
				FileObject f = fvo.getGlobalFo();

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

				sb.append("<td>");
				sb.append(getShowHistoryLink(f));
				sb.append("</td>");
				sb.append("</td>");

				sb.append("<td");
				if (ii % 2 == 0) {
					sb.append(" bgcolor=\"#eeeeff\"");
				}
				sb.append(">");
				String id = f.getName().replace(".", "").replaceAll("#", "").replaceAll(" ", "") + "TD";
				sb.append(getDeleteLink(f));
				sb.append("</td>");
				sb.append("<td id='" + id + "'>");
				sb.append("<a href=\"javascript:void\" onclick=\"loadFileInfo('" + f.getName() + "','" + id + "');\" > load</a>");
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

		return "<a target='_blank' href=\"raid1?task=" + Raid1Servlet.DOWNLOAD_OPERATION + "&" + Raid1Servlet.FILE_NAME + "=" + f.getName()
				+ "\"> <img src=\"/web/pic/download.png\" alt=\"download\"/> </a>";
	}

	// This changes have no effect!
	private String getDeleteLink(FileObject f) {

		return "<a target='_blank' href='javascript:void' onclick=\"jQuery.get('raid1?task=" + Raid1Servlet.DELETE_OPERATION + "&" + Raid1Servlet.FILE_NAME + "=" + f.getName()
				+ "', '', callback1, 'text' )\" > <img src=\"/web/pic/delete.png\" alt=\"delete\"/> </a>";
	}

	private Object getShowHistoryLink(FileObject f) {
		return "<a target='_blank' href=\"raid1?task=" + Raid1Servlet.SHOW_HISTORY + "&" + Raid1Servlet.FILE_NAME + "=" + f.getName() + "\"> <img src=\"/web/pic/history.png\" alt=\"history\"/> </a>";
	}

}
