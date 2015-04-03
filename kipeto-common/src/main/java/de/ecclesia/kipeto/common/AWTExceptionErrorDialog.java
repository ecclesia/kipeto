/*
 * #%L
 * Kipeto Common
 * %%
 * Copyright (C) 2010 - 2011 Ecclesia Versicherungsdienst GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.ecclesia.kipeto.common;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class AWTExceptionErrorDialog extends JDialog implements ActionListener {
	protected JList<String> messageList;
	protected JTextArea stackTraceView;

	public AWTExceptionErrorDialog(Exception exception) {
		this(null, exception);
	}

	public AWTExceptionErrorDialog(Frame parent, Exception exception) {
		super(parent, "Error");

		String nativeLF = UIManager.getSystemLookAndFeelClassName();
		// Install the look and feel
		try {
			UIManager.setLookAndFeel(nativeLF);
		} catch (Exception e) {
		}
		setModal(true);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(12, 12, 12, 12));

		panel.setLayout(new BorderLayout(12, 12));
		StringWriter stackTraceWriter = new StringWriter();
		exception.printStackTrace(new PrintWriter(stackTraceWriter));
		String stackTrace = stackTraceWriter.toString();

		JLabel messageLabel = new JLabel("Fehler beim Aktualisieren. \nBitte wenden Sie sich an Ihren Administrator.");
		panel.add(messageLabel, BorderLayout.NORTH);

		Box box = new Box(BoxLayout.Y_AXIS);

		// make list of nested exceptions
		ArrayList<String> tempExceptionList = new ArrayList<String>();
		addExceptionsToList(exception, tempExceptionList);
		messageList = new JList<String>(tempExceptionList.toArray(new String[tempExceptionList.size()]));
		JScrollPane messageListScrollPane = new JScrollPane(messageList);
		// put nice border around it
		Border border = BorderFactory.createEtchedBorder(BevelBorder.LOWERED);
		Border border2 = BorderFactory.createTitledBorder(border, "Nested messages :");
		Border border3 = BorderFactory.createCompoundBorder(border2, messageListScrollPane.getBorder());
		messageListScrollPane.setBorder(border3);
		messageListScrollPane.setPreferredSize(new Dimension(400, 80));
		box.add(messageListScrollPane);

		// make view of last stack trace
		stackTraceView = new JTextArea(stackTrace);
		stackTraceView.setFont(new Font("Monospaced", Font.PLAIN, 12));
		stackTraceView.setCaretPosition(0);
		stackTraceView.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(stackTraceView);
		scrollPane.setPreferredSize(new Dimension(400, 200));

		Border border4 = BorderFactory.createEtchedBorder(BevelBorder.LOWERED);
		Border border5 = BorderFactory.createTitledBorder(border4, "Stacktrace :");
		Border border6 = BorderFactory.createCompoundBorder(border5, scrollPane.getBorder());
		scrollPane.setBorder(border6);

		box.add(scrollPane);
		panel.add(box, BorderLayout.CENTER);

		Box buttons = new Box(BoxLayout.X_AXIS);
		buttons.add(Box.createGlue());
		JButton closeButton = new JButton("Close");
		closeButton.setActionCommand("close");
		closeButton.addActionListener(this);
		buttons.add(closeButton);

		panel.add(buttons, BorderLayout.SOUTH);

		this.setContentPane(panel);

		pack();

		// center on screen
		Dimension dimension = getSize();
		Dimension dimension2 = getToolkit().getScreenSize();
		setLocation((dimension2.width - dimension.width) / 2, (dimension2.height - dimension.height) / 2);
	}

	public void addExceptionsToList(Throwable e, List<String> list) {
		list.add(e.getMessage());
		if (e.getCause() != null) {
			addExceptionsToList(e.getCause(), list);
		}
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals("close")) {
			this.dispose();
		}
	}

}
