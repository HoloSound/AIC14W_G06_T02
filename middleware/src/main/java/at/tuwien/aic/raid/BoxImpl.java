package at.tuwien.aic.raid;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.box.boxjavalibv2.BoxClient;
import com.box.boxjavalibv2.BoxConfigBuilder;
import com.box.boxjavalibv2.IBoxConfig;
import com.box.boxjavalibv2.dao.BoxFolder;
import com.box.boxjavalibv2.dao.BoxItem;
import com.box.boxjavalibv2.dao.BoxOAuthToken;
import com.box.boxjavalibv2.dao.BoxTypedObject;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.box.boxjavalibv2.exceptions.BoxJSONException;
import com.box.boxjavalibv2.exceptions.BoxServerException;
import com.box.boxjavalibv2.jsonparsing.BoxJSONParser;
import com.box.boxjavalibv2.jsonparsing.BoxResourceHub;
import com.box.restclientv2.exceptions.BoxRestException;
import com.box.restclientv2.requestsbase.BoxFileUploadRequestObject;
import com.google.common.io.Files;

import at.tuwien.aic.raid.data.FileObject;

/**
 * Implementation for the BOX-Connection
 * @author daniel
 *
 */
public class BoxImpl implements ConnectorInterface
{
	private static final File boxProps = new File( "/home/tomas/Documents/skola/advanced internet computing/AIC14W_G06_T02/middleware/src/main/resources/box.properties" ); 
	private static final String propertyFileLocation = boxProps.toString();
	private  final String PORT;
	private  final String key;
	private  final String secret;
	private  final  String redirectUrl;
	
	public static final String bFolder = "0";
	
	private BoxClient client;
	private HashMap<String, String> fileIDs = null;
	
	

	public BoxImpl()
	{
		
		PropertyFile pf = new PropertyFile();
		pf.setPropertyFileName(propertyFileLocation);
		
		key = pf.getProperty( "key" );
		secret = pf.getProperty( "secret" );
		redirectUrl = pf.getProperty("redirect");
		PORT = pf.getProperty("port");
		
		try {
			this.createConnection();
			this.getFileIDs();
		} catch (AuthFatalFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BoxServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BoxRestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private  synchronized void createConnection() throws AuthFatalFailureException, BoxServerException, BoxRestException {
		String code = "";
        String url = "https://www.box.com/api/oauth2/authorize?response_type=code&client_id=" + key;
        if(redirectUrl != null) {
        	url += redirectUrl + PORT;
        }
        try {
            Desktop.getDesktop().browse(java.net.URI.create(url));
            code = getCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        client = getAuthenticatedClient(code);
        
        
	}
	
	/**
	 * reads all available files and stores their names and ids into the fileIDs HashMap
	 * @throws AuthFatalFailureException 
	 * @throws BoxServerException 
	 * @throws BoxRestException 
	 */
	private  synchronized  void getFileIDs() throws BoxRestException, BoxServerException, AuthFatalFailureException {
		fileIDs = new HashMap<String, String>();
		BoxFolder boxFolder = client.getFoldersManager().getFolder(bFolder,null);
        ArrayList<BoxTypedObject> folderEntries = boxFolder.getItemCollection().getEntries();
        int folderSize = folderEntries.size();
        for (int i = 0; i <= folderSize-1; i++){
            BoxTypedObject folderEntry = folderEntries.get(i);
            String filename = (folderEntry instanceof BoxItem) ? ((BoxItem)folderEntry).getName() : "(unknown)";
            fileIDs.put(filename, folderEntry.getId());
        }
	}
	
    private   synchronized  BoxClient getAuthenticatedClient(String code) throws BoxRestException,     BoxServerException, AuthFatalFailureException {
        BoxResourceHub hub = new BoxResourceHub();
        BoxJSONParser parser = new BoxJSONParser(hub);
        IBoxConfig config = (new BoxConfigBuilder()).build();
        BoxClient client = new BoxClient(key, secret, hub, parser, config);
        BoxOAuthToken bt = client.getOAuthManager().createOAuth(code, key, secret, "http://localhost:" + PORT);
        client.authenticate(bt);
        return client;
    }


    private  synchronized  String getCode() throws IOException {

        @SuppressWarnings("resource")
		ServerSocket serverSocket = new ServerSocket(Integer.parseInt(PORT));
        Socket socket = serverSocket.accept();
        BufferedReader in = new BufferedReader (new InputStreamReader (socket.getInputStream ()));
        while (true)
        {
            String code = "";
            try
            {
                BufferedWriter out = new BufferedWriter (new OutputStreamWriter (socket.getOutputStream ()));
                out.write("HTTP/1.1 200 OK\r\n");
                out.write("Content-Type: text/html\r\n");
                out.write("\r\n");

                code = in.readLine ();
                System.out.println (code);
                String match = "code";
                int loc = code.indexOf(match);

                if( loc >0 ) {
                    int httpstr = code.indexOf("HTTP")-1;
                    code = code.substring(code.indexOf(match), httpstr);
                    String parts[] = code.split("=");
                    code=parts[1];
                    out.write("Code found. Close the window.");
                } else {
                    // It doesn't have a code
                    out.write("Code not found in the URL!");
                }

                out.close();

                return code;
            }
            catch (IOException e)
            {
                //error ("System: " + "Connection to server lost!");
                System.exit (1);
                break;
            }
        }
        return "";
    }


	/**
	 * creates a FileObject
	 * Does not proof if the file already exists!
	 */
	@Override
	public synchronized   void create(FileObject file) throws IOException {
		InputStream is = null;
		
		byte[] bytes = file.getData();
		
		is = new ByteArrayInputStream( bytes );
		
		try {
			fileIDs.put(file.getName(), client.getFilesManager().uploadFile(BoxFileUploadRequestObject.uploadFileRequestObject(bFolder, file.getName(), is)).getId());
		} catch (BoxRestException e) {
			//e.printStackTrace();
			throw new IOException(e);
		} catch (BoxServerException e) {
			//e.printStackTrace();
			throw new IOException(e);
		} catch (AuthFatalFailureException e) {
			//e.printStackTrace();
			throw new IOException(e);
		} catch (BoxJSONException e) {
			//e.printStackTrace();
			throw new IOException(e);
		} catch (InterruptedException e) {
			//e.printStackTrace();
			throw new IOException(e);
		}
		
	}

	/**
	 * reads a FileObject
	 * @param The file to read
	 * @return FileObject
	 */
	@Override
	public  synchronized  FileObject read(FileObject name) throws IOException {
		File f = null;
		try {
			f = File.createTempFile("temp", "deleteme");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			client.getFilesManager().downloadFile(fileIDs.get(name.getName()), f, null, null);
		} catch (BoxRestException e) {
			//e.printStackTrace();
			throw new IOException(e);
		} catch (BoxServerException e) {
			//e.printStackTrace();
			throw new IOException(e);
		} catch (IllegalStateException e) {
			//e.printStackTrace();
			throw new IOException(e);
		} catch (AuthFatalFailureException e) {
			//e.printStackTrace();
			throw new IOException(e);
		} catch (IOException e) {
			//e.printStackTrace();
			throw new IOException(e);
		} catch (InterruptedException e) {
			//e.printStackTrace();
			throw new IOException(e);
		}
		
		try {
			name.setData(Files.toByteArray(f));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		f.delete();
		
		return name;
	}

	/**
	 * Updates the file
	 * @param file
	 */
	@Override
	public  synchronized  void update(FileObject file) throws IOException {
			this.delete(file);
			this.create(file);
	}

	/**
	 * Deletes the file
	 * @param file
	 */
	@Override
	public   synchronized  void delete(FileObject file) throws IOException {
		try {
			client.getFilesManager().deleteFile(fileIDs.get(file.getName()), null);
		} catch (BoxRestException e) {
			//e.printStackTrace();
			throw new IOException(e);
		} catch (BoxServerException e) {
			//e.printStackTrace();
			throw new IOException(e);
		} catch (AuthFatalFailureException e) {
			//e.printStackTrace();
			throw new IOException(e);
		}
		
	}
	
	@Override
	public  synchronized  ArrayList<FileObject> listFiles() {
		//refresh the fileIDs HashMap with all Files (and IDs) from BOX
		try {
			this.getFileIDs();
			
			ArrayList<FileObject> listedFiles = new ArrayList<FileObject>();
			for(Map.Entry<String, String> entry : fileIDs.entrySet()) {
	        	listedFiles.add(new FileObject(entry.getKey()));
	        }
			
			return listedFiles;
			
		} catch (BoxRestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BoxServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AuthFatalFailureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
}
