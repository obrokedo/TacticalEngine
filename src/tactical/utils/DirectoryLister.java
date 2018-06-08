package tactical.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import tactical.loading.LoadingState;

public class DirectoryLister {
	public static List<File> listFilesInDir(String dir) {
		if (LoadingState.inJar)
				return listFilesInDirJar(dir);
		return listFilesInDirNoJar(dir);
	}
	
	private static List<File> listFilesInDirJar(String dir) {
		Stream<Path> walk = null;
		List<File> files = new ArrayList<>();
		FileSystem fileSystem = null;
		try {
			
			URI uri = DirectoryLister.class.getClassLoader().getResource(dir).toURI();
            fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
            Path myPath = fileSystem.getPath(dir);
		    walk = Files.walk(myPath, 1);
		    
		    for (Iterator<Path> it = walk.iterator(); it.hasNext();){
		    	URI listedUri = it.next().toUri();
		    	System.out.println(listedUri);
		    	if (listedUri.toString() != null && !listedUri.toString().endsWith(dir)) {
		    		String uriS = listedUri.toString();		    		
		    		int index = uriS.indexOf("!");
		    		files.add(new File(uriS.substring(index + 2).replaceAll("%20", " ")));
		    	}
		    		
		    }
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
			return null;
		} finally {
			if (walk != null)
			    walk.close();
			if (fileSystem != null)
				try {
					fileSystem.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	    return files;
	}
	
	private static List<File> listFilesInDirNoJar(String dir) {
		List<File> files = new ArrayList<>();
		for (File f : new File(dir).listFiles()) {
			files.add(f);
		}
		return files;
	}
}
