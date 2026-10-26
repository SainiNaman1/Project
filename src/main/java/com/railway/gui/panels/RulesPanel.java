package com.railway.gui.panels;

import com.railway.gui.UITheme;
import com.railway.model.RailwayRule;
import com.railway.service.TrainService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

/**
 * Read-only panel displaying railway baggage guidelines, cancellation refund slabs,
 * and passenger identification policies.
 */
public class RulesPanel extends JPanel {

    private final TrainService trainService;
    private JPanel containerPanel;

    public RulesPanel(TrainService trainService) {
        this.trainService = trainService;
        initUI();
        loadRules();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(248, 250, 252));

        JPanel topBar = UITheme.createCardPanel();
        topBar.setLayout(new BorderLayout());
        JLabel lblTitle = UITheme.createHeaderLabel("Railway Guidelines & Passenger Policies");
        topBar.add(lblTitle, BorderLayout.WEST);
        add(topBar, BorderLayout.NORTH);

        containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.setBackground(new Color(248, 250, 252));
        containerPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(containerPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadRules() {
        new SwingWorker<List<RailwayRule>, Void>() {
            @Override
            protected List<RailwayRule> doInBackground() throws Exception {
                return trainService.getAllActiveRules();
            }

            @Override
            protected void done() {
                try {
                    List<RailwayRule> rules = get();
                    containerPanel.removeAll();

                    for (RailwayRule rule : rules) {
                        JPanel card = UITheme.createCardPanel();
                        card.setLayout(new BorderLayout(8, 8));

                        JLabel lblRuleTitle = new JLabel(rule.getTitle() + " [" + rule.getCategory() + "]");
                        lblRuleTitle.setFont(UITheme.FONT_HEADER);
                        lblRuleTitle.setForeground(UITheme.PRIMARY_NAVY);
                        card.add(lblRuleTitle, BorderLayout.NORTH);

                        JTextArea txtContent = new JTextArea(rule.getContent());
                        txtContent.setFont(UITheme.FONT_BODY);
                        txtContent.setLineWrap(true);
                        txtContent.setWrapStyleWord(true);
                        txtContent.setEditable(false);
                        txtContent.setBackground(Color.WHITE);
                        card.add(txtContent, BorderLayout.CENTER);

                        containerPanel.add(card);
                        containerPanel.add(javax.swing.Box.createVerticalStrut(12));
                    }
                    containerPanel.revalidate();
                    containerPanel.repaint();
                } catch (Exception e) {
                    containerPanel.add(new JLabel("Could not load rules: " + e.getMessage()));
                }
            }
        }.execute();
    }
}
