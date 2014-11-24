package at.tuwien.aic.raid;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import at.tuwien.aic.raid.connector.S3Connector;

public class S3SampleTest {

	@Test
	public void test() {
		String[] a=new String[0];
		try {
			S3Connector.main(a);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
