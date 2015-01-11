package at.tuwien.aic.raid.connector;

import at.tuwien.aic.raid.ConnectorInterface;

public class ConnectorConstructor
{
	private static DropBoxImpl dropBox;
	private static BoxImpl box;
	private static S3Connector s3;
	
	public synchronized static int getMaxId()
	{
		return 3;
	}
	
	/**
	 * Maximum interfaces fixed to 3
	 * 
	 * @param ii
	 * @return
	 */
	public synchronized static ConnectorInterface getInstance( int ii )
	{
		if( ii == 0 )
		{
			return (ConnectorInterface) dropBoxInstance();
		}
		else if( ii == 1 )
		{
			return (ConnectorInterface) boxInstance();
		}
		else
		{
			return (ConnectorInterface) s3Instance();
		}
	}

	
	public synchronized static ConnectorInterface dropBoxInstance()
	{
		if( dropBox == null )
		{
			dropBox = new DropBoxImpl();
		}

		return dropBox;
	}

	public synchronized static ConnectorInterface boxInstance()
	{
		if( box == null )
		{
			box = new BoxImpl();
		}

		return box;
	}

	public synchronized static ConnectorInterface s3Instance()
	{
		if( s3 == null )
		{
			s3 = new S3Connector();
		}

		return s3;
	}

}
