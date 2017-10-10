package org.eclipse.sensinact.gateway.core.security;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SessionKey
{
	private String token;
	private UserKey userKey;
	private AccessTree<? extends AccessNode> tree;	
	private int localID;
	

	public SessionKey(int localID, 
		String token, AccessTree<? extends AccessNode> tree)
	{
		this.localID = localID;
		this.token = token;
		this.tree = tree;
	}
	
	/**
	 * @return
	 */
	public AccessTree<? extends AccessNode>  getAccessTree()
	{
		return this.tree;
	}
	
	/**
	 * @return
	 */
	public int localID()
	{
		return this.localID;
	}
	
	/**
	 * @param userKey
	 */
	public void setUserKey(UserKey userKey) 
	{
		this.userKey = userKey;
	}
	

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.security.UserKey#getPublicKey()
	 */
	public String getPublicKey()
	{
		return this.userKey.getPublicKey();
	}
	
	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.security.SessionKey#getToken()
	 */
	public String getToken() 
	{
		return this.token;
	}
}