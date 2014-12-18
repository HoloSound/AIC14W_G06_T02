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
					.lookup("java:global/ear/sessionBean/Raid1SessionBean");

		} catch (NamingException e) {

			e.printStackTrace();
			System.out.println("ERROR CANT INIT CONTROLLER ");
		}

	}

	public String listFiles() {
		try {
			StringBuilder sb = new StringBuilder();

			sb.append("<table border=\"1\">");
			sb.append("<colgroup>");
			sb.append("<col width=\"200\" />");
			sb.append("<col width=\"100\" />");
			sb.append("<col width=\"100\" />");
			sb.append("</colgroup>");
			sb.append("<thead>");
			sb.append("<tr>");
			sb.append("<td><strong>FileName</strong></td>");
			sb.append("</tr>");
			sb.append("</thead>");

			int ii=0;
			ArrayList<FileObject> fl = raid.listFiles();
			for (FileObject f : fl) {
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

				sb.append("</tr>");
				
				ii++;
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
				+ Raid1Servlet.FILE_NAME + "="+f.getName()+"\"> download</a>";
	}

	private String getDeleteLink(FileObject f) {

		return "<a target='_blank' href='javascript:void' onclick=\"jQuery.get('raid1?task="
				+ Raid1Servlet.DELETE_OPERATION + "&" + Raid1Servlet.FILE_NAME+"="+f.getName()
				+ "', '', callback1, 'text' )\" > delete</a>";
	}

}
