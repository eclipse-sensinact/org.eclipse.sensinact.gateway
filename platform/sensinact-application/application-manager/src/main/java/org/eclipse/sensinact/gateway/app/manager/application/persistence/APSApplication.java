package org.eclipse.sensinact.gateway.app.manager.application.persistence;

import org.eclipse.sensinact.gateway.app.api.persistence.ApplicationPersistenceService;
import org.eclipse.sensinact.gateway.app.api.persistence.dao.Application;
import org.eclipse.sensinact.gateway.app.api.persistence.exception.ApplicationPersistenceException;
import org.eclipse.sensinact.gateway.app.api.persistence.listener.ApplicationAvailabilityListener;
import org.eclipse.sensinact.gateway.app.manager.application.persistence.exception.ApplicationParseException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

public class APSApplication implements ApplicationPersistenceService,Runnable {
    private final Logger LOG=LoggerFactory.getLogger(APSApplication.class);
    private final File directoryMonitor;
    private final List<String> files=new ArrayList<>();
    private final Map<String,Application> filesPath=new HashMap<>();
    private final List<ApplicationAvailabilityListener> listener=new ArrayList<ApplicationAvailabilityListener>();
    private final Long readingDelay;
    private final String fileExtention;
    private Boolean active=Boolean.TRUE;

    public APSApplication(File directoryMonitor, Long readingDelay,String fileExtention){
        this.directoryMonitor=directoryMonitor;
        this.readingDelay=readingDelay;
        this.fileExtention=fileExtention;
    }

    @Override
    public void persist(Application application) throws ApplicationPersistenceException {

        final String filename=directoryMonitor+File.separator+application.getName()+"."+fileExtention;
        File file=new File(filename);
        try {
            file.createNewFile();
            FileOutputStream fos=new FileOutputStream(file);
            fos.write(application.getContent().toString().getBytes());
            fos.close();
        } catch (IOException e) {
            LOG.error("Failed to create application file {} into the disk.",filename);
        }
    }

    @Override
    public void delete(String applicationName) throws ApplicationPersistenceException {
        final String filename=directoryMonitor+File.separator+applicationName+"."+fileExtention;

        File file=new File(filename);
        try {
            file.delete();
            notifyRemoval(filename);
        } catch (Exception e) {
            LOG.error("Failed to remove application file {} from the disk.",filename);
        }
    }

    @Override
    public JSONObject fetch(String applicationName) throws ApplicationPersistenceException {
        throw new UnsupportedOperationException("Persistence to the disk is not available");
    }

    @Override
    public Collection<Application> list() {
        return Collections.unmodifiableCollection(filesPath.values());
    }

    @Override
    public void registerServiceAvailabilityListener(ApplicationAvailabilityListener listenerClient) {
        this.listener.add(listenerClient);
    }

    @Override
    public void unregisterServiceAvailabilityListener(ApplicationAvailabilityListener listenerClient) {
        this.listener.remove(listenerClient);
    }

    public void run(){
        notifyServiceAvailable();
        while(active){

            try {

                Thread.sleep(readingDelay);

                List<String> filesToBeProcessed=new ArrayList<>();

                for(File applicationFile:directoryMonitor.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith("."+fileExtention);
                    }
                })){
                    filesToBeProcessed.add(applicationFile.getAbsolutePath());
                }

                List<String> filesRemoved=new ArrayList<>(files);
                filesRemoved.removeAll(filesToBeProcessed);

                //Remove old application files
                for(String fileRemoved:filesRemoved){
                    notifyRemoval(fileRemoved);
                }

                //Process (new files or already installed) files
                for(String toprocess:filesToBeProcessed){
                    try{
                        Boolean fileManaged=filesPath.containsKey(toprocess);
                        if(!fileManaged){ //new file
                            LOG.info("Application file {} will be loaded.",toprocess);
                            notifyInclusion(toprocess);
                        }else {
                            Application applicationManaged=filesPath.get(toprocess);
                            Application applicationInFs=FileToApplicationParser.parse(toprocess);
                            //taken into account modified files
                            if(!applicationManaged.getDiggest().equals(applicationInFs.getDiggest())){
                                LOG.info("Application file {} was already loaded but its content changed, dispatching update.",toprocess);
                                notifyRemoval(toprocess);
                                notifyInclusion(toprocess);
                                LOG.info("Application file {}, update procedure finished.",toprocess);
                            }else {
                                //Dont do anything, file already taken into account
                            }
                        }

                    }catch(Exception e){
                        LOG.warn("Failed to process application description file {}",toprocess,e);
                    }

                }

                Thread.sleep(readingDelay);

            } catch (InterruptedException e) {
                LOG.error("Application persistency system failed",e);
            }

        }
        notifyServiceUnavailable();
        LOG.error("Application persistency system is exiting");

    }

    private void notifyInclusion(String filepath){
        try {
            Application application=FileToApplicationParser.parse(filepath);

            LOG.info("Notifying application '{}' deployment ",filepath);

            for(ApplicationAvailabilityListener list:listener){
                try {
                    list.applicationFound(application.getName(),application.getContent().toString());
                }catch(Exception e){
                    LOG.error("Failed to add application {} into the platform, is ApplicationManager running?",application.getName(),e);
                }

            }

            filesPath.put(filepath,application);
            files.add(filepath);

        } catch (ApplicationParseException e) {
            LOG.error("Failed to read application file",e);
        }


    }

    private void notifyRemoval(String filepath){
        LOG.info("Notifying application '{}' removal",filepath);
        Application application=filesPath.remove(filepath);

        if(application!=null){
            files.remove(filepath);
            for(ApplicationAvailabilityListener list:listener){
                try {
                    list.applicationRemoved(application.getName());
                }catch(Exception e){
                    LOG.error("Failed to remove application from the platform",e);
                }

            }
        }else {
            LOG.warn("The application file '{}' was already notified by the system",filepath);
        }


    }

    private void notifyServiceUnavailable(){
        LOG.debug("Persistence service is going offline");
        for(ApplicationAvailabilityListener list:listener){
            try {
                list.serviceOffline();
            }catch(Exception e){
                LOG.error("Persistence service is going offline",e);
            }

        }
    }

    private void notifyServiceAvailable(){
        LOG.debug("Persistence service is going online");
        for(ApplicationAvailabilityListener list:listener){
            try {
                list.serviceOnline();
            }catch(Exception e){
                LOG.error("Persistence service is going online",e);
            }

        }
    }
}
