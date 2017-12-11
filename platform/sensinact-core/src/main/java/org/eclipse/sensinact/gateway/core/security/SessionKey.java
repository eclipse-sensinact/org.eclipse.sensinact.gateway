package org.eclipse.sensinact.gateway.core.security;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.message.SnaAgent;
import org.eclipse.sensinact.gateway.core.message.MidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaAgentImpl;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SessionKey
{
	private Mediator mediator;
	
	private String token;
	private UserKey userKey;
	private int localID;
	
	private AccessTree<? extends AccessNode> tree;	
	private List<String> agents;

	/**
	 * @param mediator
	 * @param localID
	 * @param token
	 * @param tree
	 */
	public SessionKey(Mediator mediator, int localID, 
		String token, AccessTree<? extends AccessNode> tree)
	{
		this.localID = localID;
		this.token = token;
		this.tree = tree;
		
		this.agents = new ArrayList<String>();
		this.mediator = mediator;
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
	 * @return
	 */
	public String getPublicKey()
	{
		return this.userKey.getPublicKey();
	}
	
	/**
	 * @return
	 */
	public String getToken() 
	{
		return this.token;
	}
	
	/**
	 * 
	 * @param callback
	 * @param filter
	 * @return
	 */
	public String registerAgent(MidAgentCallback callback, SnaFilter filter)
	{		
		final SnaAgentImpl agent = SnaAgentImpl.createAgent(
			mediator, callback, filter, getPublicKey());
		
		final String identifier = new StringBuilder().append(
			"agent_").append(agent.hashCode()).toString();
		
		Dictionary<String,Object> props = new Hashtable<String,Object>();
		props.put("org.eclipse.sensinact.gateway.agent.id",identifier);
	    props.put("org.eclipse.sensinact.gateway.agent.local",(localID()==0));
	    	
		agent.start(props);
		this.agents.add(identifier);
		return identifier;
	}
	
	/**
	 * 
	 * @param callback
	 * @param filter
	 * @return
	 */
	public boolean unregisterAgent(String agentId)
	{	
		if(!this.agents.remove(agentId))
		{
			return false;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("(&(org.eclipse.sensinact.gateway.agent.local=");
		builder.append(localID()==0);
		builder.append(")(");
		builder.append("org.eclipse.sensinact.gateway.agent.id=");
		builder.append(agentId);
		builder.append("))");
		
		return this.mediator.callService(SnaAgent.class, 
		builder.toString(), new Executable<SnaAgent, Boolean>()
		{
			@Override
			public Boolean execute(SnaAgent agent)
			        throws Exception
			{
				try
				{
					agent.stop();
					return true;
				} catch(Exception e)
				{
					mediator.error(e);
				}
				return false;
			}
		});
	}
	
	/**
	 * 
	 */
	void unregisterAgents()
	{		
		StringBuilder builder = new StringBuilder();
		builder.append("(&(org.eclipse.sensinact.gateway.agent.local=");
		builder.append(localID()==0);
		builder.append(")(|");
		
		while(!this.agents.isEmpty())
		{
			builder.append("(");
			builder.append("org.eclipse.sensinact.gateway.agent.id=");
			builder.append(this.agents.remove(0));
			builder.append(")");
		}
		builder.append("))");
		this.mediator.callServices(SnaAgent.class, 
		builder.toString(), new Executable<SnaAgent, Void>()
		{
			@Override
			public Void execute(SnaAgent agent)
			        throws Exception
			{
				agent.stop();
				return null;
			}
		});
	}
}