package com.secres.secresmail_lib;

import java.awt.Color;
import java.awt.Component;
import java.io.File;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

/**
 * A FileListCellRenderer for a File.
 * 
 * @author Andrew Thompson
 */
public class FileListCellRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = -7799441088157759804L;
	private FileSystemView fileSystemView;
	private JLabel label;
	private Color textSelectionColor = Color.BLACK;
	private Color backgroundSelectionColor = Color.CYAN;
	private Color textNonSelectionColor = Color.BLACK;
	private Color backgroundNonSelectionColor = Color.WHITE;

	public FileListCellRenderer() {
		label = new JLabel();
		label.setOpaque(true);
		fileSystemView = FileSystemView.getFileSystemView();
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected,
			boolean expanded) {
		File file = (File) value;
		if(fileSystemView.getSystemIcon(file) != null) {
			label.setIcon(fileSystemView.getSystemIcon(file));
		}
		else {
			label.setIcon(UIManager.getIcon("Tree.leafIcon"));
		}
		label.setText(fileSystemView.getSystemDisplayName(file));
		label.setToolTipText(file.getPath());

		if(selected) {
			label.setBackground(backgroundSelectionColor);
			label.setForeground(textSelectionColor);
		}
		else {
			label.setBackground(backgroundNonSelectionColor);
			label.setForeground(textNonSelectionColor);
		}

		return label;
	}

}
