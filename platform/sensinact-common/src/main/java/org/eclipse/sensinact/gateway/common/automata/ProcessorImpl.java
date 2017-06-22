/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.common.automata;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.xml.XMLUtil;

import java.net.URL;
import java.util.Stack;

/**
 * Implementation of the {@link Processor} service
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ProcessorImpl implements Processor
{	
	private static final int MAX_FRAME_LENGTH = 1024;
	
	/**
	 * Lock for synchronized accesses
	 */
	private final Object sync = new Object();
	
	/**
	 * the {@link ServiceMediator}
	 */
	private Mediator mediator;
	
	/**
	 * the {@link FrameModel} used to process 
	 * bytes array frame
	 */
	private FrameModel model;

	/**
	 * The last build {@link Frame}
	 */
	private Frame frame;

	/**
	 * Buffer bytes array to store read bytes
	 */
	private byte[] buffer;
	
	/**
	 * the delimitation type
	 */
	private int delimitation = -1;
	
	/**
	 * the frame starting delimiter 
	 */
	private Byte startDelimiter = null;

	/**
	 * the frame ending delimiter 
	 */
	private Byte endDelimiter = null;
	
	/**
	 * the frame ending delimiter 
	 */
	private Byte escape = null;

	/**
	 * the state machine used to process frames
	 */
	private Automata automata;

	/**
	 * the recipient of read frames
	 */
	private ProcessorListener listener;
	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * 		the {@link ServiceMediator} to use
	 */
	public ProcessorImpl(Mediator mediator, 
			ProcessorListener listener)
	{
		this.mediator = mediator;
		this.listener = listener;
		this.buffer = new byte[0];
	}

	/**
	 * @inheritDoc
	 * 
	 * @see sensinact.box.services.api.frame.raw.RawBytesProviderListenerItf#
	 * push(byte[])
	 */
	public void push(byte[] bytes) 
	{		
		if(bytes == null)
		{
			this.mediator.warn("Null bytes array");
			return;
		}
		if(this.model == null && this.listener!= null)
		{
			URL xmlModel = this.listener.getModelURL();
			if(xmlModel != null)
			{
				try
				{
					this.setXmlModel(xmlModel.toExternalForm());
					
				} catch (FrameModelException e)
				{
					if(this.mediator.isErrorLoggable())
					{
						this.mediator.error(e,e.getMessage());
					}
				}
			}
			if(this.model == null)
			{
				if(this.mediator.isWarningLoggable())
				{
					this.mediator.warn("No model defined");
					return;
				}
			}
		}		
		synchronized(sync)
		{
			byte[] newBuffer = new byte[this.buffer.length+bytes.length];
			System.arraycopy(this.buffer, 0, newBuffer, 0, this.buffer.length);
			System.arraycopy(bytes, 0, newBuffer,this.buffer.length,bytes.length);			
			this.buffer = newBuffer;
		}
		try
		{
			this.automata.run();
			
		} catch (Exception e) 
		{
			if(this.mediator.isErrorLoggable())
			{
				this.mediator.debug(e.getMessage());			
			}
		}
	}
	
	/**
	 * @inheritDoc
	 * 
	 * @see Processor.box.services.api.frame.processor.FrameProcessorItf#
	 * getFrame()
	 */
	public Frame getFrame()
	{
		return this.frame;
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see Processor#getDelimitation()
	 */
	public int getDelimitation()
	{
		return this.delimitation;
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see Processor#getDelimiters()
	 */
	public byte[] getDelimiters()
	{
		Stack<Byte> delimitersStack = new Stack<Byte>();
		
		if(this.delimitation == Frame.START_DELIMITED  ||
				this.delimitation == Frame.START_END_DELIMITED|| 
				this.delimitation == Frame.SIZE_START_DELIMITED ||
				this.delimitation == Frame.SIZE_START_END_DELIMITED)
		{
			delimitersStack.push(this.startDelimiter);					
		} 
		if(this.delimitation == Frame.END_DELIMITED  ||
				this.delimitation == Frame.START_END_DELIMITED|| 
				this.delimitation == Frame.SIZE_END_DELIMITED ||
				this.delimitation == Frame.SIZE_START_END_DELIMITED)
		{
			delimitersStack.push(this.endDelimiter);
		}
		byte[] delimiters = new byte[delimitersStack.size()];
		int index = 0;
		
		while(!delimitersStack.isEmpty())
		{
			delimiters[index] = delimitersStack.pop();
		}
		return delimiters;
	}
	
	/**
	 * Sets the current {@link Frame} implementation instance. If the 
	 * frame is not null, it is send to the registered {@link ProcessorListener}
	 * implementation instances
	 *  
	 * @param frame
	 * 		the current frame
	 */
	private void setFrame(Frame frame) 
	{
		if(this.listener != null)
		{
			byte[] delimiters = getDelimiters();
			this.listener.push(frame,delimitation,delimiters);
		}
		this.frame = frame;
	}
	
	/**
	 * Defines the path of the xml file describing the frame model 
	 *  
	 * @param xmlModel
	 * 		the path of the xml file describing the frame model
	 * 
	 * @throws FrameModelException 
	 * 		if an error occurred during the model building
	 */
	public void setXmlModel(String xmlModel) throws FrameModelException 
	{
		FrameModelHandler handler = newModel(xmlModel);
		try
		{
			this.model = handler.getModel();
			this.model.checkValid();
			
			this.delimitation = handler.getDelimitation();
			this.startDelimiter = handler.getStartDelimiter();
			this.endDelimiter = handler.getEndDelimiter();
			this.escape = handler.getEscape();

			this.automata = new Automata(this.startDelimiter,
					this.endDelimiter, this.escape);
			
		} catch(FrameModelException e)
		{
			throw e;
			
		} catch(Exception e)
		{
			throw new FrameModelException(e);
		}
	}
	
	/**
	 * Return the {@link FrameModel} implementation instance 
	 * 
	 * @return
	 * 		a {@link FrameModel} implementation instance
	 */
	public FrameModel getModel()
	{
		return this.model;
	}	

	/**
	 * @inheritDoc
	 * 
	 * @see Processor.box.services.api.frame.processor.FrameProcessorItf#newFrame()
	 */
	public Frame newFrame(FrameModel model) throws FrameProcessorException, FrameException 
	{
		FrameFactory factory = null;		
		if((this.model == null) || (this.listener == null) ||((factory =
				this.listener.getFrameFactory()) ==null))
		{
			throw new FrameProcessorException("Unable create a new Frame");
		}
		Frame newFrame = factory.newInstance(model);
		return newFrame;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see Processor.box.services.api.frame.processor.FrameProcessorItf#
	 * newModel(java.lang.String)
	 */
	public FrameModelHandler newModel(String xmlModelPath) 
			throws FrameModelException 
	{
		FrameModelHandler handler = new FrameModelHandlerImpl(
				this.mediator);
		URL schemaLocation = ProcessorImpl.class.getClassLoader(
				).getResource("frame.xsd");
		
		if(schemaLocation == null)
		{
			throw new FrameSchemaNotFoundException(
					"'frames.xsd' schema cannot be found");
		}
		try
		{
			XMLUtil.parse(handler,schemaLocation, xmlModelPath);
			return handler;
			
		} catch(Exception e)
		{
			throw new FrameModelException(e);
		}
	}
	
	/**
	 * The processor state machine check each byte of the 
	 * bytes array frame to create and validate building Frame
	 */
	private class Automata
	{
		//Possible states of the machine 
		private static final int WAITING_START_END_NO_SIZE = 0;
		private static final int WAITING_END_START_NO_SIZE = 1;
		private static final int WAITING_START_NO_END_NO_SIZE = 2;
		private static final int WAITING_START_NO_END_SIZE = 4;
		private static final int WAITING_START_END_SIZE = 5;
		private static final int WAITING_END_START_SIZE = 6;
		private static final int WAITING_END_NO_START_NO_SIZE = 7;
		private static final int WAITING_END_NO_START_SIZE = 8;
		private static final int WAITING_SIZE = 9;
		private static final int WAITING_EMPTY = 10;
		
		//Unknown state if the configuration is no valid
		private static final int UNKNOWN = -1;

		
		//Defines whether the current read byte is a start delimiter
		private boolean isStartByte = false;
		//Defines whether the current read byte is an end delimiter
		private boolean isEndByte = false;
		//Defines whether the current read byte is an end delimiter
		private boolean isEscaped = false;
		//The current state of the automata
		private int currentState;
		private final int firstState;
		
		//frame being processed
		private Frame currentFrame;
		private int frameIndex = 0;
		private int frameSize = 0;
		
		//Defines whether the current read byte is a start delimiter
		private final byte startDelimiter;
		//Defines whether the current read byte is an end delimiter
		private final byte endDelimiter;
		//Defines whether the current read byte is an end delimiter
		private final byte escape;
		
		private final boolean startDelimiterExists;
		private final boolean endDelimiterExists;
		private final boolean escapeExists;
		
		/**
		 * Constructor
		 * 
		 * current state initialization
		 */
		Automata(Byte startDelimiter,Byte endDelimiter, Byte escape)
		{
			switch(ProcessorImpl.this.delimitation)
			{
				case Frame.START_DELIMITED:
					this.firstState = WAITING_START_NO_END_NO_SIZE;
				break;
				case Frame.END_DELIMITED:
					this.firstState = WAITING_END_NO_START_NO_SIZE;
				break;
				case Frame.START_END_DELIMITED:
					this.firstState = WAITING_START_END_NO_SIZE;
				break;
				case Frame.SIZE_START_DELIMITED:
					this.firstState = WAITING_START_NO_END_SIZE;
				break;
				case Frame.SIZE_END_DELIMITED:
					this.firstState = WAITING_END_NO_START_SIZE;
				break;
				case Frame.SIZE_START_END_DELIMITED:
					this.firstState = WAITING_START_END_SIZE;
				break;
				case Frame.SIZE_DELIMITED:
					this.firstState = WAITING_SIZE;
				break;
				case Frame.EMPTY_BUFFER_DELIMITED:
					this.firstState = WAITING_EMPTY;
				break;
				default : this.firstState = UNKNOWN;
			}
			this.currentState = this.firstState;	
			this.startDelimiterExists = (startDelimiter!=null);
			this.endDelimiterExists = (endDelimiter!=null);
			this.escapeExists = (escape!=null);
			if(startDelimiterExists)
			{
				this.startDelimiter = startDelimiter.byteValue();
				
			} else
			{
				this.startDelimiter = 0;
			}
			if(endDelimiterExists)
			{
				this.endDelimiter = endDelimiter.byteValue();
				
			} else
			{
				this.endDelimiter = 0;
			}
			if(escapeExists)
			{
				this.escape = escape.byteValue();
				
			} else
			{
				this.escape = 0;
			}
		}
		
		/**
		 * Define the new state of the machine according to
		 * the last read byte
		 * 
		 * @throws FrameException 
		 * 
		 * @throws FrameProcessorException 
		 */
		private void run() throws FrameProcessorException, 
		FrameException
		{
			this.frameIndex = 0;				
			checkValid();			
			try
			{
				byte currentByte = 0;
				
				while(true)
				{	
					synchronized(ProcessorImpl.this.sync)
					{
						currentByte = nextByte(currentByte);
					}
					boolean newInstance = false;
					boolean newInstanceIfNull = false;
					boolean preComplete = false;
					boolean postComplete = false;
					boolean preAppend = false;
					boolean postAppend = false;
					
					switch(currentState)
					{
						case WAITING_START_NO_END_NO_SIZE:						
							if(isStartByte && !isEscaped)
							{	
								if(this.currentFrame != null)
								{
									this.currentFrame.setLength(this.frameSize);
								}
								preComplete = true;
								newInstance = true;
								
							} else
							{
								postAppend = true;
							}
						case WAITING_START_END_NO_SIZE:	
							if(isStartByte && !isEscaped)
							{
								this.currentState = WAITING_END_START_NO_SIZE;
								newInstance = true;
							}
						break;
						case WAITING_START_NO_END_SIZE:							
							if(isStartByte && !isEscaped)
							{
								newInstance = true;	
								
							} else
							{
								preAppend = true;							
								preComplete = true;
							} 
						break;
						case WAITING_START_END_SIZE:
							if(isStartByte && !isEscaped)
							{
								this.currentState = WAITING_END_START_SIZE;
								newInstance = true;
							}
						break;
						case WAITING_END_START_NO_SIZE:				
							if(isEndByte && !isEscaped)
							{	
								if(this.currentFrame != null)
								{
									this.currentFrame.setLength(this.frameSize);
								}
								preComplete = true;								
								this.currentState = this.firstState;
								
							} else if(isStartByte && !isEscaped)
							{
								newInstance = true;
								
							} else
							{
								postAppend = true;
							}
						case WAITING_END_START_SIZE:				
							if(isEndByte && !isEscaped)
							{	
								preComplete = true;								
								this.currentState = this.firstState;
								
							} else if(isStartByte && !isEscaped)
							{
								newInstance = true;
								
							} else
							{
								postAppend = true;
							}
						break;
						case WAITING_END_NO_START_SIZE:					
							if(isEndByte && !isEscaped)
							{
								preComplete = true;
									
							} else 
							{
								newInstanceIfNull = true;
								postAppend = true;
							}
						case WAITING_END_NO_START_NO_SIZE:					
							if(isEndByte && !isEscaped)
							{
								if(this.currentFrame != null)
								{
									this.currentFrame.setLength(this.frameSize);
								}
								preComplete = true;
									
							} else 
							{
								newInstanceIfNull = true;
								postAppend = true;
							}
						break;
						case WAITING_SIZE:							
							newInstanceIfNull = true;
							postAppend = true;
							postComplete = true;
							break;
						case WAITING_EMPTY:
							newInstanceIfNull = true;
							postAppend = true;
							break;
						default:
							//It should never happen
							System.out.println("Error State ="+currentState);
					}	
					//Append byte before a potential new frame instantiation
					if(preAppend && this.currentFrame != null)
					{
						this.currentFrame.append(currentByte);
						this.frameSize++;
					}
					//check if the current frame is complete
					if(preComplete)
					{
						complete();
					}
					//instantiate a new frame if needed
					newFrame(newInstanceIfNull,newInstance);
					//Append byte after the potential new frame instantiation
					if(postAppend && this.currentFrame!=null)
					{
						this.currentFrame.append(currentByte);
						this.frameSize++;
					}
					//check if the current frame is complete
					if(postComplete)
					{
						complete();
					}
				}
			} catch(FrameProcessorException e)
			{
				boolean emptyBufferEvent = false;				
				synchronized(ProcessorImpl.this.sync)
				{
					emptyBufferEvent = (this.frameIndex == ProcessorImpl.this.buffer.length 
							&& this.currentState == WAITING_EMPTY);
				}
				if(emptyBufferEvent)
				{
					complete();
				}
			} catch(Exception e)
			{
				if(ProcessorImpl.this.mediator.isErrorLoggable())
				{
					ProcessorImpl.this.mediator.error(e, e.getMessage());
				}
				synchronized(ProcessorImpl.this.sync)
				{
					cleanBuffer();
				}
				throw new FrameException(e) ;
			}
		}
		
		/**
		 * The ProcessorImpl's bytes array frame is set to 
		 * the current one, and the current frame is set to null.
		 */
		private void complete()
		{	
			if(this.currentFrame!=null && this.currentFrame.isComplete())
			{
					this.currentFrame.clean();
					ProcessorImpl.this.setFrame(this.currentFrame);
					
					this.currentFrame = null;
					this.frameSize=0;
					
			} else if(this.frameSize > ProcessorImpl.MAX_FRAME_LENGTH)
			{
				ProcessorImpl.this.mediator.error(
					"Maximum frame length exceeded : more than " 
						+ ProcessorImpl.MAX_FRAME_LENGTH + " bytes");	
				
				this.currentFrame = null;
				this.frameSize=0;
			}
		}

		/**
		 * Sets the currentFrame field to a new {@link Frame} 
		 * implementation instance if required 
		 *  
		 * @param ifNull
		 * 		create a new {@link Frame} if the current one 
		 * 		is null
		 * @param whatEver
		 * 		create a new {@link Frame} or not whatever is the 
		 * 		current one
		 * @param factory
		 * 		the {@link FrameFactory} use to create a new 
		 * 		{@link Frame} if required
		 * 
		 * @throws FrameException 
		 * @throws FrameProcessorException 
		 */
		private void newFrame(boolean ifNull,boolean whatEver) 
				throws FrameException, FrameProcessorException
		{		
			boolean tocreate = ifNull?
					((this.currentFrame == null)?true:false):whatEver;
					
			if(tocreate)
			{	
				this.currentFrame = ProcessorImpl.this.newFrame(
						ProcessorImpl.this.model);
			}
		}
		
		/**
		 * Extract the next byte from the buffer and test whether it is 
		 * a start delimiter or not, or an end delimiter or not
		 * 
		 * @param currentByte 
		 *  	the currentByte value
		 * @return
		 * 		the next byte from the buffer
		 * 
		 * @throws FrameProcessorException 
		 */
		private byte nextByte(byte currentByte) throws FrameProcessorException
		{
			if(this.frameIndex < ProcessorImpl.this.buffer.length)
			{
				byte nextByte = ProcessorImpl.this.buffer[this.frameIndex++];
				
				this.isStartByte=(!this.startDelimiterExists)?
						false:(this.startDelimiter==nextByte)?true:false;

				this.isEndByte=(!this.endDelimiterExists)?
						false:(this.endDelimiter==nextByte)?true:false;
				
				this.isEscaped=(!this.escapeExists)?
						false:(this.escape==currentByte && !this.isEscaped)?true:false;
				
				return nextByte;
			}
			throw new FrameProcessorException("Empty buffer ["+
			this.frameIndex+"/"+ProcessorImpl.this.buffer.length+"]");
		}
		
		/**
		 * Removes treated bytes from the buffer 
		 */
		private void cleanBuffer()
		{
			int length = ProcessorImpl.this.buffer.length - this.frameIndex;
			byte[] newBuffer = new byte[length]; 
			if(length > 0)
			{
				System.arraycopy(ProcessorImpl.this.buffer,
						this.frameIndex, newBuffer, 0, length);
			}
			ProcessorImpl.this.buffer = newBuffer;
		}
		
		/**
		 * Checks whether the automata's configuration is valid
		 *  
		 * @throws FrameProcessorException
		 * 		if the configuration is not valid
		 */
		private void checkValid() throws FrameProcessorException
		{
			if(this.currentState == UNKNOWN)
			{
				throw new FrameProcessorException(
						"Unable to process : Invalid configuration");
			}
		}
	}
}
