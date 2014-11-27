package at.tuwien.aic.raid.connector;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.sun.xml.internal.ws.util.ByteArrayBuffer;

import at.tuwien.aic.raid.ConnectorInterface;
import at.tuwien.aic.raid.data.FileObject;

public class S3Connector implements ConnectorInterface {
	
	
	private String bucketName = "g6-t2-test";
	private AmazonS3 s3; 

	public S3Connector() {
		
		/*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * middleware/aws.properties
         */
		
		ProfileCredentialsProvider p=new ProfileCredentialsProvider(new File("aws.properties").getAbsolutePath(), "default");
		s3 = new AmazonS3Client(p);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        s3.setRegion(usWest2);        
		
	}
	
	   
	    
	
	

	@Override
	public void create(FileObject file) {
		   ObjectMetadata metadata= new ObjectMetadata();
		   metadata.setContentLength(file.getData().length);
	//	  System.out.println(file.getData().length);
		   
		   
		s3.putObject(new PutObjectRequest(bucketName, file.getName(), new ByteArrayInputStream(file.getData()), metadata ));
		
	
	}
	@Override
	public  FileObject read(FileObject file) throws IOException {
		
		/*
         * Download an object - When you download an object, you get all of
         * the object's metadata and a stream from which to read the contents.
         * It's important to read the contents of the stream as quickly as
         * possibly since the data is streamed directly from Amazon S3 and your
         * network connection will remain open until you read all the data or
         * close the input stream.
         *
         * GetObjectRequest also supports several other options, including
         * conditional downloading of objects based on modification times,
         * ETags, and selectively downloading a range of an object.
         */
        
		
        S3Object object = s3.getObject(new GetObjectRequest(bucketName, file.getName()));
                ByteArrayBuffer b = new ByteArrayBuffer();
                
                b.write(object.getObjectContent());
                
//        BufferedReader reader = new BufferedReader(new InputStreamReader(C));
//        while (true) {
//            String line = reader.readLine();
//            if (line == null) break;
//
//            System.out.println("    " + line);
//        }
//        System.out.println();
//        
          file.setData(b.getRawData());      
                
		return file ;
	}

	@Override
	public void update(FileObject file) throws IOException {
		this.delete(file);
		this.create(file);

	}

	@Override
	public void delete(FileObject file) throws IOException {
		
		s3.deleteObject(bucketName, file.getName());

	}

	@Override
	public ArrayList<FileObject> listFiles() throws Exception {
		
		
		ObjectListing listing = s3.listObjects(new ListObjectsRequest()
        .withPrefix("My"));
		
		List<S3ObjectSummary> summaries = listing.getObjectSummaries();

		while (listing.isTruncated()) {
		   listing = s3.listNextBatchOfObjects (listing);
		   summaries.addAll (listing.getObjectSummaries());
		}
			
		
		
		return summaries;
	}

}

