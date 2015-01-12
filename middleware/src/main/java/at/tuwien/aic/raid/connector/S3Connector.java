package at.tuwien.aic.raid.connector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import at.tuwien.aic.raid.ConnectorInterface;
import at.tuwien.aic.raid.data.FileObject;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3Connector implements ConnectorInterface {

	private String bucketName = "g6-t2-test";
	private AmazonS3 s3;
	
	private String name = "AS3";

	public S3Connector() {

		/*
		 * The ProfileCredentialsProvider will return your [default] credential
		 * profile by reading from the credentials file located at
		 * middleware/aws.properties
		 */

		Properties pf = new Properties();
		try {
			pf.load( BoxImpl.class.getResourceAsStream( "aws.properties" ) );
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException();
			
		}
		BasicAWSCredentials credentials = new BasicAWSCredentials(pf.getProperty("aws_access_key_id"), pf.getProperty("aws_secret_access_key"));
		s3 = new AmazonS3Client(credentials);
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		s3.setRegion(usWest2);

	}

	@Override
	public void create(FileObject file) {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(file.getData().length);


		s3.putObject(new PutObjectRequest(bucketName, file.getName(),
				new ByteArrayInputStream(file.getData()), metadata));

	}

	@Override
	public FileObject read(FileObject file) throws IOException {

		/*
		 * Download an object - When you download an object, you get all of the
		 * object's metadata and a stream from which to read the contents. It's
		 * important to read the contents of the stream as quickly as possibly
		 * since the data is streamed directly from Amazon S3 and your network
		 * connection will remain open until you read all the data or close the
		 * input stream.
		 * 
		 * GetObjectRequest also supports several other options, including
		 * conditional downloading of objects based on modification times,
		 * ETags, and selectively downloading a range of an object.
		 */

		S3Object object = s3.getObject(new GetObjectRequest(bucketName, file
				.getName()));

		S3ObjectInputStream is = object.getObjectContent();
		
/* 		
		// OLD CODE
	 
		ByteArrayOutputStream b = new ByteArrayOutputStream(); 
		
		byte[] buff=new byte[8];
		while(-1!=is.read(buff)){
			b.write(buff);
		
		}
		
		file.setData(b.toByteArray());
 */
		byte[] buffer;	
		buffer = IOUtils.toByteArray( is );
	
		file.setData( buffer );

		return file;
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
		ArrayList<FileObject> ret = new ArrayList<FileObject>();

		ObjectListing listing = s3.listObjects(new ListObjectsRequest()
				.withBucketName(bucketName));

		List<S3ObjectSummary> summaries = listing.getObjectSummaries();

		Iterator<S3ObjectSummary> iterator = summaries.iterator();

		while (iterator.hasNext()) {
			S3ObjectSummary aSummary = iterator.next();

			FileObject aFileObject = new FileObject();
			aFileObject.setName(aSummary.getKey());
			ret.add(aFileObject);

			System.out.println("    " + aSummary.getKey() + ": "
					+ aSummary.toString());
		}

		return ret;
	}
	
	public String getName()
	{
		return name;
	}

}