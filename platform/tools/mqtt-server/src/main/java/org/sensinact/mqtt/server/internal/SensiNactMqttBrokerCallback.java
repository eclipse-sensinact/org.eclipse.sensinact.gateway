package org.sensinact.mqtt.server.internal;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;


public class SensiNactMqttBrokerCallback implements MqttCallback
{   

    enum Resource
    {
        LOCATION(new String[]{"Time", "Date", "Latitude", "Longitude"}),
        LUMINOSITY(new String[]{"Time", "Date", "Luminosity", "Ticks ADC"}),
        TEMPERATURE(new String[]{"Time", "Date", "Unit", "Temperature", "Ticks ADC"}),
        HUMIDITY(new String[]{"Time", "Date", "Unit", "Humidity", "Ticks ADC"}),
        DETECTION(new String[]{"Time", "Date", "Unit", "State", "Distance" }),
        BATTERY(new String[]{"Time", "Date", "Unit", "Battery", "Ticks ADC"}),
        ULTRASONIC(new String[]{"Time", "Date", "Period", "Threshold"}),
        BUZZER(new String[]{"Time", "Date", "Unit", "Time ON", "Time OFF"}),
        LIGHT(new String[]{"Time", "Date", "Unit", "Color 1", "Color 2", "Time 1", "Time 2"}),
        BLINK(new String[]{});
       
        private String[] fields;

        Resource(String[] fields)
        {
            this.fields = fields;
        }
     
      String[] fields()
        {
            return fields;
        }
    }
	private static final String PATH_SEPARATOR = "/";
    private static final String ROOT = "SmartParking";
    private static final String COMMAND_REQUEST = "Command";
    private static final String COMMAND_RESPONSE = "Response";
    private static final String SENSOR = "Sensor A";

    private static final String FREE = "Free";
    private static final String OCCUPIED = "Occupied";
    
    private Calendar calendar = Calendar.getInstance();
    private Random randomTemperature = new Random();
    private Random randomHumidity = new Random();
    private Random randomDistance = new Random();
    private Random randomBattery = new Random();

    private int ticks = 0;
    private int distance = 0;
    
    private float temperature = -30 + (randomTemperature.nextFloat() * 60) ;
    private float humidity = randomHumidity.nextFloat() * 100;
    private float battery = 75 + (randomBattery.nextFloat() * 25);
    
    private String state = FREE;

    private int time_on = 1;
    private int time_off = 0;
    
    private int time_1 = 1;
    private int time_2 = 1;
    private String color_1 = "Off";
    private String color_2 = "Off";
    
    private int period = 0;
    private int threshold = 0;
	private MqttConnectOptions connectionOptions;
	private String clientId;
	private MqttClient mqttClient;
	private MqttClientPersistence dataStore;
	
	public SensiNactMqttBrokerCallback() throws MqttException
	{
		String tmpDir = System.getProperty("java.io.tmpdir");
		File temporary = new File(tmpDir.concat("/behaviour"));
		if(temporary.exists() || temporary.mkdir())
		{
	        this.dataStore = new MqttDefaultFilePersistence(
	        		temporary.getAbsolutePath());
		}
		this.connectionOptions = new MqttConnectOptions();
    	this.connectionOptions.setCleanSession(false);
    	this.clientId = new StringBuilder().append("Behaviour_"
    		        ).append(this.hashCode()).toString();
    	
    	this.mqttClient = new MqttClient("tcp://localhost:1885", 
               this.clientId, dataStore!=null?dataStore:new MemoryPersistence());
        
    	this.mqttClient.setCallback(this);  
    	
    	try
    	{
    		if(this.connect())
    		{     			
    			this.mqttClient.publish("SmartParking/Command/Sensor A/Init", new byte[0],2,false);
    		}
        } catch (MqttException e)
        {
            e.printStackTrace();
            throw e;
        }
	}
	/**
	 * Connects the associated Mqtt client to the targeted broker
	 * and returns true if it has effectively been connected
	 * or false if an error occurred
	 * 
	 * @return
	 *     true if the associated Mqtt client has been connected
     *     or false if an error occurred
	 */
	protected boolean connect()
	{
		if(this.mqttClient.isConnected())
		{
			return true;
		}
        try
        {    	
        	this.mqttClient.connect(connectionOptions);
        	return true;
        	
        } catch(MqttException e)
        {
            e.printStackTrace();
        }
        return false;
	}
	
    public void messageArrived(String topic, MqttMessage message) throws Exception 
    {
    	  final byte[] payloadBytes = message.getPayload();
    	  final String[] topicElements = topic.split(PATH_SEPARATOR);
          
    	  new Thread(new Runnable()
    	  {
    	   public void run()
    	   {
    		   synchronized(SensiNactMqttBrokerCallback.this)
    		   {
		        	if(topicElements.length == 4)
		        	{
		            String root = topicElements[0];
		            String command = topicElements[1];
		            String sensor = topicElements[2];
		            String resourceName = topicElements[3];
		            
		            Resource resource = Resource.valueOf(resourceName.toUpperCase());
			            if(resource == null)
			            {
			                return;
			            }
			            if(root.intern() == ROOT.intern() 
			                    && command.intern() == COMMAND_REQUEST.intern()
			                    && sensor.intern() == SENSOR.intern())
			            {
			            String payload = new String(payloadBytes);
			            String[] requestParameters = null;
			               int length = 0;
			            
			                if(payload != null)
			                {
			                     requestParameters = payload.split(";");
			                     length = requestParameters.length;
			                }
			                switch(resource)
			                {
				                    case ULTRASONIC:
				                        if(length == 2)
				                        {
				                        	SensiNactMqttBrokerCallback.this.period = Integer.parseInt(requestParameters[0]);
				                        	SensiNactMqttBrokerCallback.this.threshold = Integer.parseInt(requestParameters[1]);
				                        	length = 0;
				                        }
				                    case BUZZER:
				                        if(length == 2)
				                        {
				                        	SensiNactMqttBrokerCallback.this.time_on = Integer.parseInt(requestParameters[0]);
				                        	SensiNactMqttBrokerCallback.this.time_off = Integer.parseInt(requestParameters[1]);
				                        }
				                    case BLINK:
				                        if(length == 4)
				                        {                                       
				                        	SensiNactMqttBrokerCallback.this.color_1 = requestParameters[0];
				                        	SensiNactMqttBrokerCallback.this.color_2 = requestParameters[1];
				                        	SensiNactMqttBrokerCallback.this.time_1 = Integer.parseInt(requestParameters[2]);
				                        	SensiNactMqttBrokerCallback.this.time_2 = Integer.parseInt(requestParameters[3]);
				                        }
				                    case LIGHT:                           
				                        if(length == 1)
				                        {                                       
				                        	SensiNactMqttBrokerCallback.this.color_1 = requestParameters[0];
				                        	SensiNactMqttBrokerCallback.this.color_2 = "Off";
				                        	SensiNactMqttBrokerCallback.this.time_1 = 1;
				                        	SensiNactMqttBrokerCallback.this.time_2 = 1;
				                        }
				                    case BATTERY:
				                    case DETECTION:
				                    case HUMIDITY:
				                    case LOCATION:
				                    case LUMINOSITY:
				                    case TEMPERATURE:   	                  
				                            MqttMessage mqttMessage = createMqttMessage(
				                                    resource.equals(Resource.BLINK)? Resource.LIGHT:resource,
				                                            2,true);
					                        if(mqttMessage == null)
					                        {
					                            return;
					                        }
			                        		try
			                        		{
				                        StringBuilder builder = new StringBuilder();
				                        builder.append(ROOT);
				                        builder.append(PATH_SEPARATOR);
				                        builder.append(COMMAND_RESPONSE);
				                        builder.append(PATH_SEPARATOR);
				                        builder.append(SENSOR);
				                        builder.append(PATH_SEPARATOR);
				                        builder.append(resource.equals(Resource.BLINK)?"Light":
				                            resourceName);
			                        
				                        if(SensiNactMqttBrokerCallback.this.connect())
				                        {
				                        	MqttTopic mqttTopic = SensiNactMqttBrokerCallback.this.mqttClient.getTopic(builder.toString());
				                        	MqttDeliveryToken token = mqttTopic.publish(mqttMessage);
				                        	token.waitForCompletion();
				                        }
			                        } catch(Exception e)
			                        {
			                        	e.printStackTrace();
			                        }
			                    }
			                  }
		        		}
		            }
    	   }
    	  }).start();
        }
    
        public MqttMessage createMqttMessage(Resource resource, int qos, boolean retained)
        {
            MqttMessage mqttMessage = null; 
            if(resource != null)
            {
            StringBuilder builder = new StringBuilder();
                int length = 0;
            builder.append("{");
                
                for(String field : resource.fields())
                {
               String keyValuePair = this.createKeyValuePair(field, resource);
                    if(keyValuePair != null)
                    {
                        builder.append(length > 0?",":"");
                        builder.append(keyValuePair);
                        length++;
                    }
                }
            builder.append("}");
            mqttMessage = new MqttMessage();                
            mqttMessage.setPayload(builder.toString().getBytes());
            mqttMessage.setQos(qos);
            mqttMessage.setRetained(retained);
            }
            return mqttMessage;
        }
    
        /**
         * Returns the current time as a string
         *  
         * @return
         *      the current time as a string
         */
        private String getTime()
        {
            int hours = this.calendar.get(Calendar.HOUR);
            int minutes =  this.calendar.get(Calendar.MINUTE);
            int seconds = this.calendar.get(Calendar.SECOND);
            
            return new StringBuilder().append(hours).append(":").append(
                    minutes).append(":").append(seconds).toString();
        }
        
        /**
         * Returns the current time during which the buzzer is 
         * turned on in milliseconds
         *  
         * @return
         *      the current time during which the buzzer is 
         *      turned on in milliseconds
         */
        private int getTime_ON()
        {
           return this.time_on;
        }
        
        /**
         * Returns the current time during which the buzzer is 
         * turned off in milliseconds
         *  
         * @return
         *      the current time during which the buzzer is 
         *      turned off in milliseconds
         */
        private int getTime_OFF()
        {
           return this.time_off;
        }
        
        /**
         * Returns the color of the first light
         *  
         * @return
         *      the color of the first light
         */
        private String getColor_1()
        {
           return this.color_1;
        }
        
        /**
         * Returns the current time during which the first light 
         * is turned on in seconds
         *  
         * @return
         *      the current time during which the first light 
         *      is turned on in seconds
         */
        private int getTime_1()
        {
           return this.time_1;
        }
        
        /**
         * Returns the color of the second light
         *  
         * @return
         *      the color of the second light
         */
        private String getColor_2()
        {
           return this.color_2;
        }

        /**
         * Returns the current time during which the second light 
         * is turned on in seconds
         *  
         * @return
         *      the current time during which the second light 
         *      is turned on in seconds
         */
        private int getTime_2()
        {
           return this.time_2;
        }
        
        /**
         * Returns the ultrasonic detection period value
         *  
         * @return
         *      the ultrasonic detection period value
         */
        private int getPeriod()
        {
           return this.period;
        }
        
        /**
         * Returns the ultrasonic detection threshold value
         *  
         * @return
         *      the ultrasonic detection threshold value
         */
        private int getThreshold()
        {
           return this.threshold;
        }
        
        /**
         * Returns the current date as a string
         *  
         * @return
         *      the current date as a string
         */
        private String getDate()
        {
            int day = this.calendar.get(Calendar.DAY_OF_MONTH);
            int month =  this.calendar.get(Calendar.MONTH);
            int year = this.calendar.get(Calendar.YEAR);
            
            return new StringBuilder().append(year).append("-").append(
                    month+1).append("-").append(day).toString();
        }
        
        /**
         * Return the sensor latitude
         * 
         * @return
         *      the sensor latitude
         */
        private double getLatitude()
        {
            return 43.46944006615525;
        }

        /**
         * Return the sensor longitude
         * 
         * @return
         *      the sensor longitude
         */
        private double getLongitude()
        {
            return -3.76651294150156;
        }
        
        /**
         * Return the number of request
         * 
         * @return
         *      the number of request
         */
        private int getTicks_ADC()
        {
            return ticks++;
        }
        
        /**
         * Return the sensor longitude
         * 
         * @return
         *      the sensor longitude
         */
        private String getLuminosity()
        {
            return System.currentTimeMillis()%2==0?"Low":"High";
        }  
        
        /**
         * Return the current temperature
         * 
         * @return
         *      the current temperature
         */
        private String getTemperature()
        {
          this.temperature = System.currentTimeMillis()%2==0
                    ?(this.temperature+randomTemperature.nextFloat())
                            :(this.temperature-randomTemperature.nextFloat());
          return ""+this.temperature;
        } 
        
        /**
         * Return the current humidity
         * 
         * @return
         *      the current humidity
         */
        private String getHumidity()
        {
          this.humidity = System.currentTimeMillis()%2==0
                    ?(this.humidity+randomHumidity.nextFloat())
                            :(this.humidity-randomHumidity.nextFloat());
          return ""+this.humidity;
        } 
        
        /**
         * Return the current battery level
         * 
         * @return
         *      the current battery level
         */
        private String getBattery()
        {
          this.battery = this.battery - randomBattery.nextFloat();
          return ""+this.battery;
        } 
        
        /**
         * Returns the distance to the nearest detected
         * object
         * 
         * @return
         *      the distance to the nearest detected
         *      object
         */
        private int getDistance()
        {
            if(this.state.intern() == FREE.intern())
            {
                this.distance = (int)(100 + (randomDistance.nextFloat() * 50));
            } else
            {
                this.distance = (int)(randomDistance.nextFloat() * 50);
            }
            return this.distance;
        }
        
        /**
         * Returns the state of the parking
         * 
         * @return
         *       the state of the parking
         */
        private String getState()
        {
            this.state = System.currentTimeMillis()%2==0
                    ?FREE.intern():OCCUPIED.intern();
                    
            return this.state;
        }
        
        /**
         * Return the unit for the Resource passed
         * as parameter
         * 
         * @param resource
         *      the {@link Resource} for which return the 
         *      unit
         * @return
         *      the unit for the Resource passed
         *      as parameter
         */
        private String getUnit(Resource resource)
        {
            String unit = null;
            switch(resource)
            {
                case BATTERY:
                case HUMIDITY:
                    unit = "Percentage";
                    break;
                case TEMPERATURE:
                    unit = "Degrees";
                    break;
                case DETECTION:
                    unit = "Centimeters";
                    break;
                case LIGHT:
                    unit = "Seconds";
                    break;
                case BUZZER:
                    unit = "Milliseconds";
                    break;
                case BLINK:
                case LOCATION:
                case LUMINOSITY:
                case ULTRASONIC:
                default:
                    break;
            }
            return unit;
        }  

        /**
         * Returns a key - value pair separated by colon
         * 
         * @param key
         *      the key of the key - value pair to create    
         * @param resource
         *      the {@link Resource} constant for which to create the key - value pair
         * @return
         *      a key - value pair separated by colon
         */
        private String createKeyValuePair(String key, Resource resource)
        {
            String keyValuePair = null;
            Method method = null;
            Object parameter = null;
            
            String methodName = new StringBuilder().append(
                    "get").append(key.replace(' ', '_')).toString();
            try
            {
                method = getClass().getDeclaredMethod(methodName,
                        Resource.class);
                parameter = resource;
                
            } catch (Exception e)
            {
               try
               {
                   method = getClass().getDeclaredMethod(methodName);
                           
               } catch(Exception ex)
               {
                   System.out.println(e.getMessage());
                   System.out.println(ex.getMessage());
               }
            } 
            if(method != null)
            {
                try
                {
                    Object value = null;
                    if(parameter == null)
                    {
                       value =  method.invoke(this);
                       
                    } else
                    {
                        value = method.invoke(this, parameter);
                    }
                    StringBuilder builder = new StringBuilder();
                    builder.append("\"");
                    builder.append(key);
                    builder.append("\"");
                    builder.append(" : ");
                    builder.append("\"");
                    builder.append(String.valueOf(value));
                    builder.append("\"");
                    keyValuePair = builder.toString();
                    
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return keyValuePair;
        }
        
        public void deliveryComplete(IMqttDeliveryToken token) 
        {
        	 if(token != null)
        	 {
	        	 MqttException exception = token.getException();
	             
	             System.out.println(new StringBuilder().append(
	                 token.getMessageId()).append(" message ").append(
	                  Arrays.toString(token.getTopics())).append(
	                         exception!=null?" triggering an exception : ":" without error"
	                                 ).append(exception != null?exception.getMessage():""
	                                     ).toString());
        	 }
        }

        public void connectionLost(Throwable cause) 
        {
        	if(!connect())
        	{
        		System.out.println("Connection Lost ! ");
        	}
        }

		public void start() 
		{
			Resource[] resources = new Resource[]{
					Resource.LUMINOSITY,
					Resource.LOCATION,
					Resource.TEMPERATURE,
					Resource.HUMIDITY,
					Resource.DETECTION,
					Resource.BATTERY,
					Resource.LIGHT,
					Resource.BLINK,
					Resource.BUZZER
			};
			for(Resource resource : resources)
			{
				StringBuilder builderCommand = new StringBuilder();
				String resourceTopic = resource.name().toLowerCase();

				builderCommand.append("SmartParking/Command/Sensor A/").append(
						Character.toUpperCase(resourceTopic.charAt(0))).append(
						        resourceTopic.substring(1));

				if(connect())
				{
					try
					{
						this.mqttClient.subscribe(builderCommand.toString(),2);
						
					} catch (MqttException e) 
					{
						e.printStackTrace();						
					}
				} 
			}
		}

		public void stop()
		{
			try 
			{
				this.mqttClient.disconnect();
				while(this.mqttClient.isConnected());
				
			} catch (MqttException e)
			{
				e.printStackTrace();
			}
			try
			{
                if(this.dataStore != null)
                {
                    this.dataStore.close();
                }
			}
			catch (Exception e)
			{
				//e.printStackTrace();
			}
		}
 }