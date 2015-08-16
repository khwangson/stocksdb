import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import java.sql.SQLException;
import java.util.*;
 
public class GUIStocksInterface extends JFrame{
    JRadioButton RtoLbutton;
    JRadioButton LtoRbutton;
    static StocksDBActivity Driver;
    public JTextArea display;
    File Script;
    JFileChooser fc;
    FlowLayout experimentLayout = new FlowLayout();
  
    private String[] HEADER = { "Individual", "Total Return", ""};
    public static Object Data [][] = new Object[1000000][3];
    
    //private final String DASH_LINE = "------------------------------------------------------------- ";
    JButton openButton = new JButton("Read Script");
    JButton fundButton = new JButton("Fund");
    JButton individualButton = new JButton("Individual");
    JButton buyButton = new JButton("Buy");
    JButton sellButton = new JButton("Sell");
    JButton buyANDSellButton = new JButton("Buy/Sell");
    JButton rankPortfoliosTR = new JButton("Rank the portfolios(TR)");
    JButton rankIndividualsTR = new JButton("Rank Individuals(TR)");
    JButton  top25stocks = new JButton(" top 25 stocks");
    JButton rankPortfoliosNW = new JButton("Rank the portfolios(NW)");
    JButton rankIndividualsNW = new JButton("Rank Individuals(NW)");
    JButton rankIndividualPortfolio = new JButton("Rank Individual Portfolio");
    JButton ÞveLowestRiskStocks = new JButton("Five lowest-risk stocks");
    JButton companiesStocksInc = new JButton("Companies Stock Increasing");
    JButton export = new JButton("Export Results");
    JButton mystery = new JButton("Mystery Query????");
    JLabel StocksDataBase = new JLabel("Stocks Data Base");
    public static JTable table; 
    
    int width = 650, height = 300;
     
    public GUIStocksInterface(String name) {
        super(name);
    }
     
    public void addComponentsToPane(final Container pane) {
        final JPanel compsToExperiment = new JPanel();
        StocksDataBase.setSize(200, 50);
        compsToExperiment.setLayout(experimentLayout);
        experimentLayout.setAlignment(FlowLayout.TRAILING);
        JPanel controls = new JPanel();
        controls.setLayout(new FlowLayout());
        JPanel actions = new JPanel();
        actions.setLayout(new FlowLayout());
        JPanel actions2 = new JPanel();
        actions2.setLayout(new FlowLayout());
        display = new JTextArea();
		display.setFont(new Font("serif", Font.PLAIN, 14));
		table = new JTable(Data, HEADER);
		
		table.setPreferredScrollableViewportSize(new Dimension(9 * width / 10, 6 * height / 10));
	    table.setFillsViewportHeight(true);
	    JScrollPane scrollPane = new JScrollPane(table);
		compsToExperiment.add(scrollPane);
		 fc = new JFileChooser();
        
        //Add buttons to the experiment layout
     
        //Left to right component orientation is selected by default
     
		 JPanel buttons = new JPanel();
	        buttons.setLayout(new BorderLayout());
	        
	        //Add buttons to the experiment layout
	     
	        //Left to right component orientation is selected by default
	        
	        controls.add(openButton);
	        controls.add(fundButton);
	        controls.add(individualButton);
	        controls.add(buyButton);
	        controls.add(sellButton);
	        controls.add(buyANDSellButton);
	        actions.add(rankPortfoliosTR);
	        actions.add(rankIndividualsTR);
	        actions.add(top25stocks);
	        actions.add(rankPortfoliosNW);
	        actions2.add(rankIndividualsNW);
	        actions2.add(rankIndividualPortfolio);
	        actions2.add(ÞveLowestRiskStocks);
	        actions2.add(companiesStocksInc);
	        actions2.add(mystery);
	        
         
        
        
        //Process the Apply component orientation button press
        openButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Check the selection
                int returnVal = fc.showOpenDialog(GUIStocksInterface.this);
                
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    Script = fc.getSelectedFile();
                    try {
                    	Driver.read_file(Script);
                    } catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
                }
            }
        });
  
        
        fundButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
            	
            	JTextField Portfolio = new JTextField();
            	JTextField Amount = new JTextField();
            	JTextField Date = new JTextField();
            	Object[] message = {
            	    
            	    "Portfolio:", Portfolio,
            	    "Amount", Amount,
            	    "Date", Date
            	};

            	JOptionPane.showConfirmDialog(null, message, "Fund", JOptionPane.OK_CANCEL_OPTION);
             if(Portfolio.getText() == null ||  Amount.getText() == null || Date.getText() == null){
      
             }
             else{
            	 Driver.execute_fund(Portfolio.getText(), Amount.getText(), Date.getText());
             }
             
            }
        });
        
      //Process the Apply component orientation button press
        buyButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Check the selection
            	JTextField name = new JTextField();
            	JTextField Stock_Portfolio = new JTextField();
            	JTextField Amount = new JTextField();
            	JTextField Date = new JTextField();
            	Object[] message = {
            	    "Individual/Portfolio:", name,
            	    "Stock?portfolio:", Stock_Portfolio,
            	    "Amount", Amount,
            	    "Date", Date
            	};

            	JOptionPane.showConfirmDialog(null, message, "Buy", JOptionPane.OK_CANCEL_OPTION);
            	 if(name.getText() == null ||  Stock_Portfolio.getText() == null || Amount.getText() == null || Date.getText() == null){
            	      
                 }
            	 else{
            		 Driver.execute_buy(name.getText(), Stock_Portfolio.getText(), Amount.getText(), Date.getText());
            	 }
            }
        });
        buyANDSellButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Check the selection
            	JTextField name = new JTextField();
            	JTextField Stock_Portfolio1 = new JTextField();
            	JTextField Stock_Portfolio2 = new JTextField();
            	JTextField Amount = new JTextField();
            	JTextField Date = new JTextField();
            	Object[] message = {
            	    "Individual", name,
            	    "Stock/portfolio:", Stock_Portfolio1,
            	    "Stock/portfolio:", Stock_Portfolio2,
            	    "Date", Date
            	};

            	JOptionPane.showConfirmDialog(null, message, "Sell/Buy", JOptionPane.OK_CANCEL_OPTION);
             
            	 if(name.getText() == null ||  Stock_Portfolio1.getText() == null || Stock_Portfolio2.getText() == null || Date.getText() == null){
            	      
                 }else{
                	 Driver.execute_exchange(name.getText(), Stock_Portfolio1.getText(), Stock_Portfolio2.getText(), Date.getText()); 
                 }
            	
            
           
            }
        });
        individualButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
            	JTextField Name = new JTextField();
            	JTextField Amount = new JTextField();
            	JTextField Date = new JTextField();
            	Object[] message = {
            	    
            	    "Name:", Name,
            	    "Amount", Amount,
            	    "Date", Date
            	};

            	JOptionPane.showConfirmDialog(null, message, "Individual", JOptionPane.OK_CANCEL_OPTION);
             
            	 if(Name.getText() == null ||  Amount.getText() == null || Date.getText() == null){
            	      
                 }
            	 else{
            		 Driver.execute_individual(Name.getText(), Amount.getText(), Date.getText());
            	 }
            	
            
            }
        });
        
        sellButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
            	JTextField Name = new JTextField();
            	JTextField Stock = new JTextField();
            	JTextField Date = new JTextField();
            	Object[] message = {
            	    
            	    "Individual/Portfolio:", Name,
            	    "Stock/Portfolio", Stock,
            	    "Date", Date
            	};

            	JOptionPane.showConfirmDialog(null, message, "Sell", JOptionPane.OK_CANCEL_OPTION);
            	if(Name.getText() == null ||  Stock.getText() == null || Date.getText() == null){
          	      
                }else{
                	Driver.execute_sell(Name.getText(), Stock.getText(), Date.getText());
                }
            	
            	
            
            }
        });
        
        rankPortfoliosTR.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Check the selection
            	try {
            		fixHeaders("Portfolio", "Total Rate of Return", null);
					Driver.execute_rankPortfoliosTR();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });
        rankIndividualsTR.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Check the selection
            	fixHeaders("Individual", "Total Rate of Return", null);
            	try {
					Driver.execute_rankIndividualsTR();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });
        top25stocks.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Check the selection
            	fixHeaders("Stock", "Annualized Rate", null);
            	try {
					Driver.execute_top25stocks();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            	
            	
            }
        });
        rankPortfoliosNW.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Check the selection
            	fixHeaders("Portfolio", "NetWorth", null);
            	try {
					Driver.execute_rankPortfoliosNW();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });
        rankIndividualsNW.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Check the selection
            	try {
            		fixHeaders("Individual", "NetWorth", null);
					Driver.execute_rankIndividualsNW();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });
        rankIndividualPortfolio.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Check the selection
            	fixHeaders("Individual", "Stock", "Annualized Rate");
            	try {
					Driver.execute_rankIndividualPortfolio();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        });
        ÞveLowestRiskStocks.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Check the selection
            	fixHeaders("Stocks", "Risk ", null);
            	try {
					Driver.execute_ÞveLowestRiskStocks();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            	
            }
        });
        companiesStocksInc.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Check the selection
            	fixHeaders("Stocks", null, null);
            	try {
					Driver.execute_companiesStocksInc();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            	
            }
        });
        
        export.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Check the selection
            	
            	try {
					Driver.execute_exportToCSV();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            	
            }
        });
        
        mystery.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Check the selection
            	
            	try {
					Driver.execute_mysteryQuery();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            	
            }
        });
        BufferedImage myPicture;
	
        JLabel picLabel = new JLabel(new ImageIcon("Stocks.jpeg"));
        add(picLabel);
        
        
        
        
        pane.add(picLabel, BorderLayout.WEST);
        pane.add(compsToExperiment, BorderLayout.CENTER);
        buttons.add(controls, BorderLayout.NORTH); 
        buttons.add(actions,BorderLayout.CENTER);
        buttons.add(actions2,BorderLayout.SOUTH);
        pane.add(buttons, BorderLayout.SOUTH);
        pane.add(export, BorderLayout.EAST);
    }
    public void fixHeaders(String header0, String header1, String header3){
    	JTableHeader th = table.getTableHeader();
    	TableColumnModel ch = th.getColumnModel();
    	TableColumn tc = ch.getColumn(0);
    	tc.setHeaderValue(header0);
    	TableColumn tc2 = ch.getColumn(1);
    	tc2.setHeaderValue(header1);
    	TableColumn tc3 = ch.getColumn(2);
    	tc3.setHeaderValue(header3);
    	th.repaint();
    }
     
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
    	GUIStocksInterface frame = new GUIStocksInterface("Stocks DATA BASE");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Set up the content pane.
        frame.addComponentsToPane(frame.getContentPane());
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
     
    public static void main(String[] args) {
        /* Use an appropriate Look and Feel */
    	Driver = new StocksDBActivity();
        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        //Schedule a job for the event dispatchi thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
                
            }
        });
    }

	
}