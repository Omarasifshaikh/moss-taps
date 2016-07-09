package edu.gatech.edtech;

import it.zielke.moji.MossException;
import it.zielke.moji.SocketClient;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

public class Submission {
	private Properties mossProps;
	private SoftwareLanguage language;
	private boolean validInfo = false;
	private boolean successful = false;
	private List<MossReply> replies = new ArrayList<MossReply>();
	private File parentFolder;
	private File baseFolder;

	public Submission(String parentFolderName, String baseFolderName, SoftwareLanguage language, Properties mossProps) throws Exception {
		this.mossProps = mossProps;
		this.language = language;
		this.parentFolder = new File(parentFolderName);
		this.baseFolder = new File(baseFolderName);
		
		this.mossProps.setProperty("language", language.getParameter());
		this.mossProps.setProperty("optC", language.getLanguageName()+" : "+parentFolderName);
		this.validInfo = testInfoValid();
	}
	
	public boolean submit() throws IOException {	
		// collect listing of files by extension recursively
		Collection<File> files = new ArrayList<File>();
		if(parentFolder.exists()){
			files= FileUtils.listFiles(parentFolder,
				new String[] {language.getExtension()}, true);
		}
		else System.err.println("**ERROR** Upload Directory "+parentFolder.getName()+ " does not exist. Cannot proceed.");
//		showFiles(files);  //uncomment to see uploading
		
		Collection<File> baseFiles = new ArrayList<File>();
		if(baseFolder.exists()){
			baseFiles = FileUtils.listFiles(this.baseFolder,
					new String[] {language.getExtension()}, true);			
		}
		
		//TODO ENHANCEMENT split non-current directories if too large into multiple
		// submission groups and put submission into loop
		
		return successful = singleSubmit(files, baseFiles);
	}

	private boolean singleSubmit(Collection<File> files, Collection<File> baseFiles) throws IOException {
		// set up and start moji socket client for Moss
		SocketClient socketClient = new SocketClient(
				mossProps.getProperty("server"),
				Integer.valueOf(mossProps.getProperty("port")),
				mossProps.getProperty("language"));
		socketClient.setUserID(mossProps.getProperty("userID"));
		socketClient.setOptC(mossProps.getProperty("optC"));
		socketClient.setOptM(Long.valueOf(mossProps.getProperty("optM")));
		socketClient.setOptN(Long.valueOf(mossProps.getProperty("optN")));
		socketClient.setOptX(Integer.valueOf(mossProps.getProperty("optX")));		
		System.out.println("starting SocketClient "+mossProps.getProperty("optC"));
		try {
			socketClient.run();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (MossException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
        // upload all base files
        System.out.println("uploading basefiles");
        for (File f : baseFiles) {
            try {
				socketClient.uploadBaseFile(f);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
        }

        //upload all source files of students
        System.out.println("uploading sourcefiles");
        for (File f : files) {
            try {
				socketClient.uploadFile(f);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
        }

        //finished uploading, tell server to check files
        System.out.println("sending query to "+mossProps.getProperty("server"));
        try {
			socketClient.sendQuery();
		} catch (MossException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

        //get URL with Moss results and do something with it
        URL results = socketClient.getResultURL();
        socketClient.close();
        System.out.println("Results available at " + results.toString());
        replies.add(new MossReply(results,this.language));
		return true;
	}
	
	public boolean testInfoValid() {
		if (!parentFolder.exists()) return false;
		if (!baseFolder.exists()) return false;
		return true;
	}

	public SoftwareLanguage getLanguage() {
		return language;
	}

//	public String getParentFolder() {
//		return parentFolder;
//	}
//
	public String getComment() {
		return mossProps.getProperty("optC");
	}

	public boolean isValidInfo() {
		return validInfo;
	}

	@SuppressWarnings("unused")
	private static void showFiles(Collection<File> files) {
		for (File file : files) {
			System.out.println(file.getAbsolutePath());
		}
	}

	public boolean isSuccessful() {
		return successful;
	}

	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	public List<MossReply> getReplies() {
		return replies;
	}	

}
