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
	@Override
	public void error(String message, Throwable e) {
		super.error(message, e);
		writeError(message);
		writeError("-------");
		error(e);
	}

	@Override
	public void error(Throwable e) {
		super.error(e);
		
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
		writeError("-------");
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
		writeError("-------");
	}

	private void writeError(String message)
	{
		if (message == null)
			return;

		message += "\n";

		try {
			Files.write(FileSystems.getDefault().getPath(".", "ErrorLog"),
					message.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "An error occurred trying to write to the error log:" + e.getMessage(), "Error writing to error log", JOptionPane.ERROR_MESSAGE);
		}
	}
}
