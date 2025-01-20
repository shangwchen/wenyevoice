package org.Jasper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.*;
import java.util.List;
import java.io.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


class MainFrame extends JFrame {
    private String[] labels = {"主音色", "辅助音色", "听感反馈", " ", "发展音色", "攻受属性", "声音色系", "听感年龄", "听感身高", "推荐音伴", "声音评级"};
    private JTextField[] textFields;
    private JLabel[] labelComponents;
    private int labelTextFieldSpacing = 5;
    private Point initialClick;
    private JLabel headerLabel;
    private SettingsManager settingsManager = new SettingsManager();
    private JLabel toggleImageLabel;
    private boolean isImageVisible = false;
    private Image originalImage;




    public MainFrame() {

        setTitle("温野（声鉴）");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 1000);
        setLayout(new BorderLayout());


        getContentPane().setBackground(Color.BLACK);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);

        addDefaultImage(layeredPane);

        headerLabel = new JLabel("你不好奇自己的声音吗", JLabel.LEADING);
        headerLabel.setFont(new Font("Serif", Font.BOLD, 30));
        headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setBounds(60, 60, 380, 30);
        headerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

        headerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });

        headerLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = headerLabel.getLocation().x;
                int thisY = headerLabel.getLocation().y;

                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                int X = thisX + xMoved;
                int Y = thisY + yMoved;

                headerLabel.setLocation(X, Y);
            }
        });

        layeredPane.add(headerLabel, JLayeredPane.DRAG_LAYER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setBackground(Color.BLACK);
        inputPanel.setBounds(60, 100, 400, 450);
        inputPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

        inputPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });

        inputPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = inputPanel.getLocation().x;
                int thisY = inputPanel.getLocation().y;

                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                int X = thisX + xMoved;
                int Y = thisY + yMoved;

                inputPanel.setLocation(X, Y);
            }
        });

        textFields = new JTextField[labels.length];
        labelComponents = new JLabel[labels.length];

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Default", Font.PLAIN, 25));

            JTextField textField = new JTextField();
            textField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            textField.setFont(new Font("Default", Font.PLAIN, 20));
            textField.setForeground(Color.WHITE);
            textField.setBackground(Color.BLACK);
            textFields[i] = textField;
            labelComponents[i] = label;

            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.insets = new Insets(3, 3, 3, labelTextFieldSpacing);
            inputPanel.add(label, gbc);

            gbc.gridx = 1;
            gbc.gridy = i;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(2, labelTextFieldSpacing, 2, 2);
            inputPanel.add(textField, gbc);
        }

        layeredPane.add(inputPanel, JLayeredPane.DEFAULT_LAYER);

        JButton toggleImageButton = new JButton("显示/隐藏图片");
        toggleImageButton.setBounds(140, getHeight() - 90, 150, 30); // 设置按钮位置和大小
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                toggleImageButton.setBounds(140, getHeight() - 100, 150, 30);
            }
        });

        toggleImageButton.addActionListener(e -> toggleImage());
        layeredPane.add(toggleImageButton, JLayeredPane.PALETTE_LAYER);

        toggleImageLabel = new JLabel();
        toggleImageLabel.setVisible(false);
        String imagePath = getClass().getClassLoader().getResource("111.png").getPath();
        ImageIcon icon = new ImageIcon(imagePath);

        if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
            originalImage = icon.getImage();
            toggleImageLabel.setIcon(icon);
            toggleImageLabel.setBounds(200, 600, icon.getIconWidth(), icon.getIconHeight());
        } else {
            System.err.println("图片加载失败，请检查路径：" + imagePath);
        }

        layeredPane.add(toggleImageLabel, JLayeredPane.DEFAULT_LAYER);

        toggleImageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }
        });

        toggleImageLabel.addMouseMotionListener(new MouseMotionAdapter() {
            private boolean resizing = false;
            private double aspectRatio = 0.85;

            @Override
            public void mouseMoved(MouseEvent e) {
                if (e.getX() >= toggleImageLabel.getWidth() - 10 && e.getY() >= toggleImageLabel.getHeight() - 10) {
                    toggleImageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                    resizing = true;
                } else {
                    toggleImageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    resizing = false;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (resizing) {
                    int newWidth = e.getX();
                    int newHeight = (int) (newWidth / aspectRatio);

                    if (newWidth > 50 && newHeight > 50) {
                        toggleImageLabel.setSize(newWidth, newHeight);

                        Image scaledImage = getScaledImage(originalImage, newWidth, newHeight);
                        toggleImageLabel.setIcon(new ImageIcon(scaledImage));
                    }
                } else {
                    int thisX = toggleImageLabel.getLocation().x;
                    int thisY = toggleImageLabel.getLocation().y;

                    int xMoved = e.getX() - initialClick.x;
                    int yMoved = e.getY() - initialClick.y;

                    int X = thisX + xMoved;
                    int Y = thisY + yMoved;

                    toggleImageLabel.setLocation(X, Y);
                }
            }
        });

        JButton clearButton = new JButton("一键清除");
        clearButton.setBounds(10, getHeight() - 90, 120, 30);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                clearButton.setBounds(10, getHeight() - 100, 120, 30);
            }
        });
        clearButton.addActionListener(e -> clearInputs());
        layeredPane.add(clearButton, JLayeredPane.PALETTE_LAYER);

        addTextFieldPopupFunctionality();

        JMenuBar menuBar = new JMenuBar();

        JMenu optionsMenu = new JMenu("选项");

        JMenuItem saveItem = new JMenuItem("保存");
        saveItem.addActionListener(e -> saveInputs());
        optionsMenu.add(saveItem);

        JMenuItem colorItem = new JMenuItem("设置标签和背景颜色");
        colorItem.addActionListener(e -> setColors());
        optionsMenu.add(colorItem);

        JMenuItem textFieldColorItem = new JMenuItem("设置文本框颜色");
        textFieldColorItem.addActionListener(e -> setTextFieldColors());
        optionsMenu.add(textFieldColorItem);

        JMenuItem textColorItem = new JMenuItem("设置文本颜色");
        textColorItem.addActionListener(e -> setTextColors());
        optionsMenu.add(textColorItem);

        JMenuItem fontSizeItem = new JMenuItem("设置字体大小");
        fontSizeItem.addActionListener(e -> setFontSize());
        optionsMenu.add(fontSizeItem);

        JMenuItem labelFontSizeItem = new JMenuItem("设置标签字体大小");
        labelFontSizeItem.addActionListener(e -> setLabelFontSize());
        optionsMenu.add(labelFontSizeItem);

        JMenuItem spacingItem = new JMenuItem("设置标签和文本框间距");
        spacingItem.addActionListener(e -> setLabelTextFieldSpacing());
        optionsMenu.add(spacingItem);

        JMenuItem editHeaderItem = new JMenuItem("编辑顶部文字");
        editHeaderItem.addActionListener(e -> editHeaderText());
        optionsMenu.add(editHeaderItem);

        JMenuItem headerColorItem = new JMenuItem("设置顶部文字颜色");
        headerColorItem.addActionListener(e -> setHeaderTextColor());
        optionsMenu.add(headerColorItem);

        JMenuItem headerSizeItem = new JMenuItem("设置顶部文字大小");
        headerSizeItem.addActionListener(e -> setHeaderTextSize());
        optionsMenu.add(headerSizeItem);

        JMenuItem addImageMenuItem = new JMenuItem("添加图片");
        addImageMenuItem.addActionListener(e -> addDraggableImage(layeredPane));
        optionsMenu.add(addImageMenuItem);

        JMenuItem editLabelItem = new JMenuItem("编辑标签内容");
        editLabelItem.addActionListener(e -> editLabels());
        optionsMenu.add(editLabelItem);


        menuBar.add(optionsMenu);
        setJMenuBar(menuBar);

        add(layeredPane, BorderLayout.CENTER);
    }

    private void clearInputs() {
        for (JTextField textField : textFields) {
            textField.setText("");
        }
    }

    private Image getScaledImage(Image srcImg, int width, int height) {
        BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(srcImg, 0, 0, width, height, null);
        g2.dispose();
        return resizedImg;
    }


    private void saveInputs() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (int i = 0; i < labels.length; i++) {
                    writer.write(labels[i] + ": " + textFields[i].getText());
                    writer.newLine();
                }
                JOptionPane.showMessageDialog(this, "保存成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "保存失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void toggleImage() {
        isImageVisible = !isImageVisible;
        toggleImageLabel.setVisible(isImageVisible);
        if (isImageVisible) {
            JLayeredPane layeredPane = (JLayeredPane) toggleImageLabel.getParent();
            layeredPane.setLayer(toggleImageLabel, JLayeredPane.DRAG_LAYER);
            layeredPane.repaint();
        }
    }

    private void addDefaultImage(JLayeredPane layeredPane) {
        String imagePath = getClass().getClassLoader().getResource("000.jpg").getPath();
        ImageIcon icon = new ImageIcon(imagePath);

        if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
            int defaultWidth = 180;
            int defaultHeight = 200;

            Image scaledImage = icon.getImage().getScaledInstance(defaultWidth, defaultHeight, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);

            JLabel imageLabel = new JLabel(scaledIcon);

            imageLabel.setBounds(30, 560, defaultWidth, defaultHeight);
            imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    initialClick = e.getPoint();
                }
            });

            imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int thisX = imageLabel.getLocation().x;
                    int thisY = imageLabel.getLocation().y;

                    int xMoved = e.getX() - initialClick.x;
                    int yMoved = e.getY() - initialClick.y;

                    int X = thisX + xMoved;
                    int Y = thisY + yMoved;

                    imageLabel.setLocation(X, Y);
                }
            });

            layeredPane.add(imageLabel, JLayeredPane.DEFAULT_LAYER);
        } else {
            System.err.println("图片加载失败，请检查路径：" + imagePath);
        }
    }



    private void editLabels() {
        String[] options = {"编辑标签", "添加新标签", "删除标签"};
        String choice = (String) JOptionPane.showInputDialog(
                this,
                "请选择操作：",
                "标签操作",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if ("编辑标签".equals(choice)) {
            String selectedLabel = (String) JOptionPane.showInputDialog(
                    this,
                    "请选择要编辑的标签：",
                    "编辑标签内容",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    labels,
                    labels[0]
            );

            if (selectedLabel != null) {
                String newLabel = JOptionPane.showInputDialog(
                        this,
                        "输入新的标签内容：",
                        selectedLabel
                );

                if (newLabel != null && !newLabel.trim().isEmpty()) {
                    for (int i = 0; i < labels.length; i++) {
                        if (labels[i].equals(selectedLabel)) {
                            labels[i] = newLabel;
                            labelComponents[i].setText(newLabel);
                            break;
                        }
                    }
                }
            }
        } else if ("添加新标签".equals(choice)) {
            String newLabel = JOptionPane.showInputDialog(
                    this,
                    "输入新的标签名称：",
                    "添加新标签",
                    JOptionPane.PLAIN_MESSAGE
            );

            if (newLabel != null && !newLabel.trim().isEmpty()) {
                JTextField newTextField = new JTextField();
                JLabel newLabelComponent = new JLabel(newLabel);

                newLabelComponent.setForeground(Color.WHITE);

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = labels.length;
                gbc.anchor = GridBagConstraints.EAST;
                gbc.insets = new Insets(3, 3, 3, labelTextFieldSpacing);
                ((JPanel) labelComponents[0].getParent()).add(newLabelComponent, gbc);

                gbc.gridx = 1;
                gbc.weightx = 1.0;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(2, labelTextFieldSpacing, 2, 2);
                ((JPanel) labelComponents[0].getParent()).add(newTextField, gbc);

                labels = extendArray(labels, newLabel);
                textFields = extendArray(textFields, newTextField);
                labelComponents = extendArray(labelComponents, newLabelComponent);

                getContentPane().revalidate();
                getContentPane().repaint();
            }
        } else if ("删除标签".equals(choice)) {
            String selectedLabel = (String) JOptionPane.showInputDialog(
                    this,
                    "请选择要删除的标签：",
                    "删除标签",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    labels,
                    labels[0]
            );

            if (selectedLabel != null) {
                for (int i = 0; i < labels.length; i++) {
                    if (labels[i].equals(selectedLabel)) {
                        ((JPanel) labelComponents[i].getParent()).remove(labelComponents[i]);
                        ((JPanel) textFields[i].getParent()).remove(textFields[i]);

                        labels = removeFromArray(labels, i);
                        textFields = removeFromArray(textFields, i);
                        labelComponents = removeFromArray(labelComponents, i);

                        getContentPane().revalidate();
                        getContentPane().repaint();
                        break;
                    }
                }
            }
        }
    }

    private <T> T[] extendArray(T[] original, T newItem) {
        T[] extended = java.util.Arrays.copyOf(original, original.length + 1);
        extended[original.length] = newItem;
        return extended;
    }

    private <T> T[] removeFromArray(T[] original, int index) {
        T[] reduced = java.util.Arrays.copyOf(original, original.length - 1);
        if (index < original.length - 1) {
            System.arraycopy(original, index + 1, reduced, index, original.length - index - 1);
        }
        return reduced;
    }


    private void setColors() {
        Color backgroundColor = JColorChooser.showDialog(this, "选择背景颜色", getContentPane().getBackground());
        if (backgroundColor != null) {
            getContentPane().setBackground(backgroundColor);
            for (Component component : getContentPane().getComponents()) {
                if (component instanceof JPanel) {
                    component.setBackground(backgroundColor);
                } else if (component instanceof JLayeredPane) {
                    for (Component innerComponent : ((JLayeredPane) component).getComponents()) {
                        if (innerComponent instanceof JPanel) {
                            innerComponent.setBackground(backgroundColor);
                        }
                    }
                }
            }
        }

        Color fontColor = JColorChooser.showDialog(this, "选择字体颜色", Color.BLUE);
        if (fontColor != null) {
            for (Component component : getContentPane().getComponents()) {
                if (component instanceof JPanel) {
                    for (Component innerComponent : ((JPanel) component).getComponents()) {
                        if (innerComponent instanceof JLabel) {
                            ((JLabel) innerComponent).setForeground(fontColor);
                        }
                    }
                } else if (component instanceof JLayeredPane) {
                    for (Component innerComponent : ((JLayeredPane) component).getComponents()) {
                        if (innerComponent instanceof JPanel) {
                            for (Component innerInnerComponent : ((JPanel) innerComponent).getComponents()) {
                                if (innerInnerComponent instanceof JLabel) {
                                    ((JLabel) innerInnerComponent).setForeground(fontColor);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void setTextFieldColors() {
        Color textFieldColor = JColorChooser.showDialog(this, "选择文本框背景颜色", Color.WHITE);
        if (textFieldColor != null) {
            for (JTextField textField : textFields) {
                textField.setBackground(textFieldColor);
            }
        }
    }

    private void setTextColors() {
        Color textColor = JColorChooser.showDialog(this, "选择文本颜色", Color.BLACK);
        if (textColor != null) {
            for (JTextField textField : textFields) {
                textField.setForeground(textColor);
            }
        }
    }



    private void setFontSize() {
        String sizeStr = JOptionPane.showInputDialog(this, "输入字体大小：", "设置字体大小", JOptionPane.PLAIN_MESSAGE);
        if (sizeStr != null && !sizeStr.isEmpty(

        )) {
            try {
                int size = Integer.parseInt(sizeStr);
                Font font = new Font("Default", Font.PLAIN, size);

                for (JTextField textField : textFields) {
                    textField.setFont(font);

                    FontMetrics fm = textField.getFontMetrics(font);
                    int textHeight = fm.getHeight();
                    int textWidth = fm.charWidth('W') * 15;
                    textField.setPreferredSize(new Dimension(textWidth, textHeight + 5));

                    textField.setSize(new Dimension(textWidth, textHeight + 5));
                }
                getContentPane().revalidate();
                getContentPane().repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的数字！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }



    private void setLabelFontSize() {
        String sizeStr = JOptionPane.showInputDialog(this, "输入标签字体大小：", "设置标签字体大小", JOptionPane.PLAIN_MESSAGE);
        if (sizeStr != null && !sizeStr.isEmpty()) {
            try {
                int size = Integer.parseInt(sizeStr);
                Font font = new Font("Default", Font.PLAIN, size);
                for (JLabel label : labelComponents) {
                    label.setFont(font);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的数字！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setLabelTextFieldSpacing() {
        String spacingStr = JOptionPane.showInputDialog(this, "输入标签和文本框间距：", "设置标签和文本框间距", JOptionPane.PLAIN_MESSAGE);
        if (spacingStr != null && !spacingStr.isEmpty()) {
            try {
                labelTextFieldSpacing = Integer.parseInt(spacingStr);
                getContentPane().revalidate();
                getContentPane().repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的数字！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editHeaderText() {
        String newText = JOptionPane.showInputDialog(this, "输入新的顶部文字：", "编辑顶部文字", JOptionPane.PLAIN_MESSAGE);
        if (newText != null && !newText.isEmpty()) {
            headerLabel.setText(newText);
        }
    }

    private void setHeaderTextColor() {
        Color newColor = JColorChooser.showDialog(this, "选择顶部文字颜色", headerLabel.getForeground());
        if (newColor != null) {
            headerLabel.setForeground(newColor);
        }
    }

    private void setHeaderTextSize() {
        String sizeStr = JOptionPane.showInputDialog(this, "输入顶部文字大小：", "设置顶部文字大小", JOptionPane.PLAIN_MESSAGE);
        if (sizeStr != null && !sizeStr.isEmpty()) {
            try {
                int size = Integer.parseInt(sizeStr);
                headerLabel.setFont(headerLabel.getFont().deriveFont((float) size));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的数字！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addDraggableImage(JLayeredPane layeredPane) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());

            double aspectRatio = (double) icon.getIconWidth() / icon.getIconHeight();

            JLabel imageLabel = new JLabel(icon);
            imageLabel.setBounds(100, 100, icon.getIconWidth(), icon.getIconHeight());
            imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

            imageLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    initialClick = e.getPoint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        int confirm = JOptionPane.showConfirmDialog(
                                layeredPane,
                                "确定要删除这张图片吗？",
                                "确认删除",
                                JOptionPane.YES_NO_OPTION
                        );

                        if (confirm == JOptionPane.YES_OPTION) {
                            layeredPane.remove(imageLabel);
                            layeredPane.repaint();
                        }
                    }
                }
            });

            imageLabel.addMouseMotionListener(new MouseMotionAdapter() {
                private boolean resizing = false;

                @Override
                public void mouseMoved(MouseEvent e) {
                    if (e.getX() >= imageLabel.getWidth() - 10 && e.getY() >= imageLabel.getHeight() - 10) {
                        imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                        resizing = true;
                    } else {
                        imageLabel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                        resizing = false;
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (resizing) {
                        int newWidth = e.getX();
                        int newHeight = (int) (newWidth / aspectRatio);

                        if (newWidth > 50 && newHeight > 50) {
                            imageLabel.setSize(newWidth, newHeight);
                            ImageIcon scaledIcon = new ImageIcon(
                                    icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH)
                            );
                            imageLabel.setIcon(scaledIcon);
                        }
                    } else {
                        int thisX = imageLabel.getLocation().x;
                        int thisY = imageLabel.getLocation().y;

                        int xMoved = e.getX() - initialClick.x;
                        int yMoved = e.getY() - initialClick.y;

                        int X = thisX + xMoved;
                        int Y = thisY + yMoved;

                        imageLabel.setLocation(X, Y);
                    }
                }
            });
            layeredPane.add(imageLabel, JLayeredPane.DRAG_LAYER);
            layeredPane.repaint();
        }
    }

    private Rectangle lastPopupBounds = new Rectangle(100, 100, 300, 200);
    private JFrame popupFrame = null; // 用于记录当前的弹窗实例
    private int activeTextFieldIndex = -1; // 用于跟踪当前激活的文本框索引
    private Map<String, List<String>> wordCategories = new LinkedHashMap<>(); // 用于存储词语分类及词语列表

    // 文件路径用于保存和加载词语分类
    private static final String SAVE_FILE_PATH = "word_categories.json";

    private void addTextFieldPopupFunctionality() {
        for (int i = 0; i < textFields.length; i++) {
            final int index = i; // 用于捕获当前文本框索引
            textFields[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        activeTextFieldIndex = index; // 更新激活的文本框索引
                        if (popupFrame == null || !popupFrame.isVisible()) {
                            showWordSelectionPopup();
                        }
                    }
                }
            });
        }
        loadWordCategories(); // 加载保存的分类数据
    }

    private void saveWordCategories() {
        try (FileWriter writer = new FileWriter(SAVE_FILE_PATH)) {
            Gson gson = new Gson();
            gson.toJson(wordCategories, writer);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "保存词语分类失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadWordCategories() {
        try (FileReader reader = new FileReader(SAVE_FILE_PATH)) {
            Gson gson = new Gson();
            java.lang.reflect.Type type = new TypeToken<Map<String, List<String>>>() {}.getType();
            wordCategories = gson.fromJson(reader, type);
            if (wordCategories == null) {
                wordCategories = new LinkedHashMap<>();
            }
        } catch (IOException e) {
            // 如果文件不存在或读取失败，使用默认分类
            wordCategories.put("常用词", new ArrayList<>(Arrays.asList("甜美", "磁性", "温暖", "清脆", "柔和")));
        }
    }

    private void showWordSelectionPopup() {
        // 如果弹窗已存在且可见，则不重新创建
        if (popupFrame != null && popupFrame.isVisible()) {
            return;
        }

        // 创建一个新窗口
        popupFrame = new JFrame("选择一个词语");
        popupFrame.setSize(lastPopupBounds.width, lastPopupBounds.height);
        popupFrame.setLocation(lastPopupBounds.x, lastPopupBounds.y);
        popupFrame.setLayout(new BorderLayout());

        // 分类列表
        JList<String> categoryList = new JList<>(new Vector<>(wordCategories.keySet()));
        categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryList.setSelectedIndex(0);

        // 词语按钮区域
        JPanel wordPanel = new JPanel(new FlowLayout());
        updateWordPanel(wordPanel, categoryList.getSelectedValue());

        // 分类切换
        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateWordPanel(wordPanel, categoryList.getSelectedValue());
            }
        });

        // 工具栏（添加和删除功能）
        JPanel toolBar = new JPanel(new FlowLayout());
        JButton addWordButton = new JButton("添加词语");
        JButton removeWordButton = new JButton("删除词语");
        JButton addCategoryButton = new JButton("添加分类");
        JButton removeCategoryButton = new JButton("删除分类");

        // 添加词语
        addWordButton.addActionListener(e -> {
            String newWord = JOptionPane.showInputDialog(popupFrame, "输入新词语：");
            if (newWord != null && !newWord.trim().isEmpty()) {
                String selectedCategory = categoryList.getSelectedValue();
                if (selectedCategory != null) {
                    wordCategories.get(selectedCategory).add(newWord);
                    updateWordPanel(wordPanel, selectedCategory);
                }
            }
        });

        // 删除词语
        removeWordButton.addActionListener(e -> {
            String selectedCategory = categoryList.getSelectedValue();
            if (selectedCategory != null) {
                String wordToRemove = JOptionPane.showInputDialog(popupFrame, "输入要删除的词语：");
                if (wordToRemove != null && wordCategories.get(selectedCategory).remove(wordToRemove)) {
                    updateWordPanel(wordPanel, selectedCategory);
                } else {
                    JOptionPane.showMessageDialog(popupFrame, "词语不存在！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 添加分类
        addCategoryButton.addActionListener(e -> {
            String newCategory = JOptionPane.showInputDialog(popupFrame, "输入新分类名称：");
            if (newCategory != null && !newCategory.trim().isEmpty() && !wordCategories.containsKey(newCategory)) {
                wordCategories.put(newCategory, new ArrayList<>());
                categoryList.setListData(new Vector<>(wordCategories.keySet()));
            }
        });

        // 删除分类
        removeCategoryButton.addActionListener(e -> {
            String selectedCategory = categoryList.getSelectedValue();
            if (selectedCategory != null && wordCategories.containsKey(selectedCategory)) {
                wordCategories.remove(selectedCategory);
                categoryList.setListData(new Vector<>(wordCategories.keySet()));
                wordPanel.removeAll();
                wordPanel.revalidate();
                wordPanel.repaint();
            }
        });

        toolBar.add(addWordButton);
        toolBar.add(removeWordButton);
        toolBar.add(addCategoryButton);
        toolBar.add(removeCategoryButton);

        popupFrame.add(new JScrollPane(categoryList), BorderLayout.WEST);
        popupFrame.add(new JScrollPane(wordPanel), BorderLayout.CENTER);
        popupFrame.add(toolBar, BorderLayout.SOUTH);

        // 在弹窗关闭时记录其位置和大小
        popupFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                lastPopupBounds = popupFrame.getBounds();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                lastPopupBounds = popupFrame.getBounds();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                popupFrame = null; // 重置弹窗引用
            }
        });

        popupFrame.setVisible(true);
    }

    private void updateWordPanel(JPanel wordPanel, String category) {
        wordPanel.removeAll();
        if (category != null && wordCategories.containsKey(category)) {
            for (String word : wordCategories.get(category)) {
                JButton wordButton = new JButton(word);
                wordPanel.add(wordButton);

                // 点击词语按钮时将其添加到当前激活的文本框
                wordButton.addActionListener(e -> {
                    if (activeTextFieldIndex >= 0 && activeTextFieldIndex < textFields.length) {
                        String currentText = textFields[activeTextFieldIndex].getText();
                        if (!currentText.isEmpty()) {
                            currentText += ", ";
                        }
                        textFields[activeTextFieldIndex].setText(currentText + word);
                    }
                });
            }
        }
        wordPanel.revalidate();
        wordPanel.repaint();
    }

}