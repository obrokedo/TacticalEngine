package tactical.engine.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.newdawn.slick.util.DefaultLogSystem;

/**
 * A logger utility that writes errors to a file titled "ErrorLog"
 *
 * @author Broked
 *
 */
public class FileLogger extends DefaultLogSystem
{
	private LinkedList<String> recentMessages = new LinkedList<>();
	
	@Override
	public void error(String message, Throwable e) {
		super.error(message, e);
		writeError(message);
		error(e);
	}

	@Override
	public void debug(String message) {
		super.debug(message);
		recentMessages.push(message);
		if (recentMessages.size() > 100)
			recentMessages.removeLast();
	}

	@Override
	public void error(Throwable e) {
		super.error(e);
		writeError(e.getMessage());
		displayError(e);

		try {
			PrintWriter pw = new PrintWriter(new FileOutputStream(
				    new File("ErrorLog"),
				    true /* append = true */));
			e.printStackTrace(pw);
			pw.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, "An error occurred trying to write to the error log:" + e.getMessage(), "Error writing to error log", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void displayError(Throwable ex) {
		JTextArea jta = new JTextArea(5, 50);
		StringWriter sw = new StringWriter();
		sw.append(ex.getMessage() + "\n");
		
		sw.append("---------- Stack trace ----------\n");
		sw.append(ex.toString() + "\n");
		for (StackTraceElement e : ex.getStackTrace())
			sw.append(e.toString() + "\n");
		jta.setText(sw.toString());
		jta.setEditable(false);
		jta.setWrapStyleWord(true);
		jta.setLineWrap(true);
		JOptionPane.showMessageDialog(null, jta, "An error has occurred", JOptionPane.ERROR_MESSAGE);	
	}

	@Override
	public void error(String message) {
		super.error(message);
		writeError(message);
	}

	private void writeError(String message)
	{		
		StringWriter sw = new StringWriter();
		for (String recent : recentMessages) { 
			sw.append(recent);
			sw.append("\n");
		}
		sw.append("Exception Message:");
		sw.append("\n");
		sw.append(message);
		sw.append("\n");
		sw.append("------");
		sw.append("\n");

		try {
			Files.write(FileSystems.getDefault().getPath(".", "ErrorLog"),
					sw.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "An error occurred trying to write to the error log:" + e.getMessage(), "Error writing to error log", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void warn(String message, Throwable e) {
		warn(message);
		// e.printStackTrace(out);
	}
}
