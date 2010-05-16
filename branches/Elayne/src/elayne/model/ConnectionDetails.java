package elayne.model;

/**
 * This class is used to define, retrieve and store login information of the
 * ELAYNE user.
 * @author polbat02
 */
public class ConnectionDetails
{
	/** Login Details */
	private final String _userId;
	private final String _password;

	/**
	 * Constructor that defines a new user in this session.
	 * @param userId
	 * @param password
	 */
	public ConnectionDetails(String userId, String password)
	{
		_userId = userId;
		_password = password;
	}

	/** Returns a the password of this user */
	public String getPassword()
	{
		return _password;
	}

	public String getResource()
	{
		return String.valueOf(System.currentTimeMillis());
	}

	/** Returns the user Id of this user */
	public String getUserId()
	{
		return _userId;
	}
}
