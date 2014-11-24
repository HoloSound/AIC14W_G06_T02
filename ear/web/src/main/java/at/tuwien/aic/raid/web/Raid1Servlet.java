package at.tuwien.aic.raid.web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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
import at.tuwien.aic.raid.sessionbean.RaidSessionBeanInterface;

@WebServlet("/raid1")
public class Raid1Servlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String DELETE_OPERATION = "delete";
	public static final String FILE_NAME = "fileName";
	public static final String DOWNLOAD_OPERATION = "download";
	public static final String UPLOAD_OPERATION = "upload";
	@EJB
	private RaidSessionBeanInterface raid;

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
				fac.setSizeThreshold(Integer.MAX_VALUE);//set maximum possible  if we cant hold it in memory than we cant have it as a byte[]
				List<FileItem> multiparts = new ServletFileUpload(fac)
						.parseRequest(req);

				for (FileItem item : multiparts) {
					if (!item.isFormField()) {
						FileObject f = new FileObject(item.getName());

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

}
