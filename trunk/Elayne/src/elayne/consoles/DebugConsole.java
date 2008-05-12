/**
 * 
 */
package elayne.consoles;

import java.io.PrintStream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * @author polbat02
 */
public class DebugConsole extends MessageConsole
{
	private MessageConsoleStream inMessageStream;
	private MessageConsoleStream inErrorMessageStrieam;

	/**
	 * @param name
	 * @param imageDescriptor
	 */
	public DebugConsole(String name, ImageDescriptor imageDescriptor)
	{
		super(name, imageDescriptor);
		inMessageStream = newMessageStream();
		inMessageStream.setColor(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));

		inErrorMessageStrieam = newMessageStream();
		inErrorMessageStrieam.setColor(Display.getCurrent().getSystemColor(SWT.COLOR_RED));

		System.setOut(new PrintStream(inMessageStream));
		System.setErr(new PrintStream(inErrorMessageStrieam));
	}

	/**
	 * @param name
	 * @param imageDescriptor
	 * @param autoLifecycle
	 */
	public DebugConsole(String name, ImageDescriptor imageDescriptor, boolean autoLifecycle)
	{
		super(name, imageDescriptor, autoLifecycle);
	}
}
