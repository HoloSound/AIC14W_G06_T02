package at.tuwien.aic.raid.web;

import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import at.tuwien.aic.raid.data.FileObject;
import at.tuwien.aic.raid.sessionbean.RaidSessionBeanInterface;

public class Controller {
	private RaidSessionBeanInterface raid;

	public Controller() {

		InitialContext ctx;
		try {
			ctx = new InitialContext();
			// all beans have to be "injected" here the JNDI bindings name is
			// displayed on deployment
			this.raid = (RaidSessionBeanInterface) ctx
					.lookup("java:global/ear/sessionBean/RaidSessionBean");

		} catch (NamingException e) {

			e.printStackTrace();
			System.out.println("ERROR CANT INIT CONTROLLER ");
		}

	}

	public String listFiles() {
		try {
			StringBuilder sb = new StringBuilder();

			sb.append("<table>");
			sb.append("<thead>");
			sb.append("<tr>");
			sb.append("<td>FileName</td>");
			sb.append("</tr>");
			sb.append("</thead>");

			ArrayList<FileObject> fl = raid.listFiles();
			for (FileObject f : fl) {
				sb.append("<tr>");

				sb.append("<td>");
				sb.append(f.getName());
				sb.append("<td>");

				sb.append("<td>");
				sb.append(getDownloadLink(f));
				sb.append("<td>");

				sb.append("<td>");
				sb.append(getDeleteLink(f));
				sb.append("<td>");

				sb.append("</tr>");
			}

			sb.append("</table>");
			return sb.toString();
		} catch (Exception e) {
			return "error " + e.getMessage();
		}

	}

	

	private String getDownloadLink(FileObject f) {

		return "<a target='_blank' href=\"raid1?task="
				+ Raid1Servlet.DOWNLOAD_OPERATION + "&"
				+ Raid1Servlet.FILE_NAME + "\"> download</a>";
	}

	private String getDeleteLink(FileObject f) {

		return "<a target='_blank' href='javascript:void' onclick=\"jQuery.get('raid1?task="
				+ Raid1Servlet.DELETE_OPERATION + "&" + Raid1Servlet.FILE_NAME
				+ "', '', callback, 'text' )\" > delete</a>";
	}

}
