package at.tuwien.aic.raid.web;

import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import at.tuwien.aic.raid.data.FileObject;
import at.tuwien.aic.raid.data.FileViewObject;
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

	// TODO why do we have the same condig twice?
	// see also Raid1Servlet.listFiles() !?
	public String listFiles() {
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

			int ii=0;
			
			// this listFiles is method which compromizes the fileinformation --> global info
			// We should here show a 
			// ArrayList<FileObjectView> ... that means global info and information per interface!
			// ArrayList<FileObject> fl = raid.listFiles();
			
			// here we should to more things.
			// we generate a column for each interface
			// we fill the generated table with additional information
//			ArrayList<FileObject> as = raid.listFiles( 0 );
//			ArrayList<FileObject> box = raid.listFiles( 1 );
//			ArrayList<FileObject> dBox = raid.listFiles( 2 );
// The creation of viewing would be the wrong place - we should here only output it!
			
			// building up a table row

			ArrayList<FileViewObject> fvol = raid.listFiles();
			
			for( FileViewObject fvo : fvol ) 
			{
				FileObject f = fvo.getGlobalFo();
				String id=f.getName().replace(".", "").replaceAll("#", "").replaceAll(" ", "")+"TD";
				
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
				if( ii % 2 == 0 )
				{
					sb.append( " bgcolor=\"#eeeeff\"" );
				}
				sb.append( ">" );
				sb.append( getShowHistoryLink(f) );
				sb.append("</td>");				

				sb.append("<td");
				if( ii % 2 == 0 )
				{
					sb.append( " bgcolor=\"#eeeeff\"" );
				}
				sb.append( ">" );
				sb.append( getShowInfoLink( id, f ) );
				sb.append("</td>");	
				
				sb.append("<td id='"+id+"'>");
				sb.append("<a href=\"javascript:void\" onclick=\"loadFileInfo('"+f.getName()+"','"+id+"');\" title=\"Show file info\"> load</a>");
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

	

	private Object getShowHistoryLink(FileObject f) {
		return "<a target='_blank' title=\"Show file history\" href=\"raid1?task="
				+ Raid1Servlet.SHOW_HISTORY + "&"
				+ Raid1Servlet.FILE_NAME + "="+f.getName()+"\"> <img src=\"/web/pic/history.png\" alt=\"history\"/> </a>";
	}

	private Object getShowInfoLink( String id, FileObject f ) {
		return "<a href=\"javascript:void\" onclick=\"loadFileInfo('"+f.getName()+"','"+id+"');\" title=\"Show file info\"><img src=\"/web/pic/info.png\" alt=\"info\"/></a>";
	}
	
	private String getDownloadLink(FileObject f) {

		return "<a target='_blank' title=\"Download file\" href=\"raid1?task="
				+ Raid1Servlet.DOWNLOAD_OPERATION + "&"
				+ Raid1Servlet.FILE_NAME + "="+f.getName()+"\"> <img src=\"/web/pic/download.png\" alt=\"download\"/> </a>";
	}

	private String getDeleteLink(FileObject f) {

		return "<a target='_blank' title=\"Delete file\" href='javascript:void' onclick=\"jQuery.get('raid1?task="
				+ Raid1Servlet.DELETE_OPERATION + "&" + Raid1Servlet.FILE_NAME+"="+f.getName()
				+ "', '', callback1, 'text' )\" > <img src=\"/web/pic/delete.png\" alt=\"delete\"/> </a>";
	}

}
