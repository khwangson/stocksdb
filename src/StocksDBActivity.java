import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore.Entry;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.sql.Statement;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
public class StocksDBActivity {
	
	 //Database connection settings
	 private Connection connect = null;
	 private Statement statement = null;
	 private PreparedStatement preparedStatement = null;
	 private ResultSet resultSet = null;
	 private double SellReturn;
	public StocksDBActivity(){
		try {
			initialize_database();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	 
	
	public void initialize_database() throws SQLException, ClassNotFoundException{
		  Class.forName("com.mysql.jdbc.Driver");
	      // setup the connection with the DB.
	      connect = DriverManager.getConnection("jdbc:mysql://localhost/finance?"
	              + "user=root&password=livetowin");//ToDo
	      statement = connect.createStatement();
	}
	public void execute_exchange(String name, String stock1,
			String stock2, String date) {
		String buyAmount = "" + this.execute_sell(name, stock1, date);
		
		this.execute_buy(name, stock2, buyAmount , date);	
	}
	/*buy|sell,name,stock symbol|fund name,dollar amount,date*/
	public double execute_sell(String name, String Symbol_fund_name, String Date ) {
		try {
			
			//Checks if name is a portfolio name
			preparedStatement = connect
			.prepareStatement("SELECT name FROM portfolio WHERE name = ? ; ");
			preparedStatement.setString(1,name);
			ResultSet rs = preparedStatement.executeQuery();
			ResultSet rs2;
			
			if(rs.next() == false){ //enters if name is individual
				int id = 0;
				int personShares = 0;
				double sharePrice = 0;
				double origAmount = 0;	//added this variable instead of temp
				boolean noCloseOpen = false;
				//Checks if symbol is portfolio name
				preparedStatement = connect
				.prepareStatement("SELECT name FROM portfolio WHERE name  = ? ; ");
				preparedStatement.setString(1,Symbol_fund_name);
				rs = preparedStatement.executeQuery();
				
				//Write individual investing in portfolio QUERY here
				
				if(rs.next() == false){ //INVIDIUAL SELLING STOCKS
					
					preparedStatement = connect.prepareStatement("SELECT individual_id from individual WHERE name = ? ;");
					preparedStatement.setString(1, name);
					rs = preparedStatement.executeQuery();
					
					while(rs.next()){
						id = rs.getInt("individual_id");
						PreparedStatement preparedStatement2 = connect.prepareStatement("SELECT num_shares, amount from individual_activity WHERE individual_id = ? and company_symbol = ? and activity_type = ? ;");
						preparedStatement2.setInt(1, id);
						preparedStatement2.setString(2, Symbol_fund_name);
						preparedStatement2.setString(3, "stock");
						rs2 = preparedStatement2.executeQuery();
						if(rs2.next()){	
							personShares = rs2.getInt("num_shares");
							origAmount = rs2.getDouble("amount");
							break; 	//..... because or else it would loop go through multiple bobs and the last bob would be the one who gets his num shares chosen but that isn't necesarrily the right bob. Maybe we don't need this but doesn't hurt.
						}
					}
					
					PreparedStatement deleteIndividual = connect.prepareStatement("DELETE from individual_activity where individual_id = ? ;"); //.... changed this so it is deleted from activity table instead of stocks
					deleteIndividual.setInt(1, id);
					deleteIndividual.executeUpdate();
					
					PreparedStatement symbolDate = connect.prepareStatement("SELECT close from quote_history where company_symbol = ? and date = ? ;");	//... but how much does the person get back if the close price is zero?
					symbolDate.setString(1, Symbol_fund_name);
					symbolDate.setString(2, Date);
					rs = symbolDate.executeQuery();
					
					if(rs.next() == false){
						return 0.0;
						/*
						PreparedStatement symbolOpen = connect.prepareStatement("SELECT open from quote_history where company_symbol = ? and date = ? ;");	//.... but how much does the person get back if the close price is zero?
						symbolOpen.setString(1, Symbol_fund_name);
						symbolOpen.setString(2, Date);
						rs2 = symbolOpen.executeQuery();
						if(rs2.next())
							sharePrice = rs.getDouble("open");		//....assumption, if close price is not available then use the open price. What should we do if the open price is not available? Set to zero? Then the person gets nothing back?
						else {
							noCloseOpen = true;
						}
						*/
					}
					
					sharePrice = rs.getDouble("close");
					
					double temp;					
					PreparedStatement individualID = connect.prepareStatement("SELECT cash from individual where individual_id = ? ;");
					individualID.setInt(1, id);
					rs = individualID.executeQuery();
					rs.next();
					int personCash = rs.getInt("cash");
					if(noCloseOpen == false)	//.... if open and close prices are both unavailable then just give the orig amount invested back to the individual
						temp = personCash + (sharePrice * personShares);
					else
						temp = personCash + origAmount;
					
					PreparedStatement cashIndividual = connect.prepareStatement("UPDATE individual SET cash = ? WHERE individual_id = ? ;");
					cashIndividual.setDouble(1, temp);
					cashIndividual.setInt(2, id);
					cashIndividual.executeUpdate();
						
				}else{//individual selling portfolio
					PreparedStatement preparedStatement = connect.prepareStatement("SELECT individual_id from individual WHERE name = ? ;");
					preparedStatement.setString(1, name);
					rs = preparedStatement.executeQuery();
															
					int ind_id = 0;
					
					while(rs.next()){
						ind_id = rs.getInt("individual_id");
						PreparedStatement preparedStatement2 = connect.prepareStatement("SELECT num_shares, amount from individual_activity WHERE individual_id = ? and company_symbol = ? ;");
						preparedStatement2.setInt(1, id);
						preparedStatement2.setString(2, Symbol_fund_name);
						rs2 = preparedStatement2.executeQuery();
						if(rs2.next()){	
							break; 	//..... because or else it would loop go through multiple bobs and the last bob would be the one who gets his num shares chosen but that isn't necesarrily the right bob. Maybe we don't need this but doesn't hurt.
						}
					}
					
					PreparedStatement getStockToSell = connect.prepareStatement("SELECT company_symbol from individual_activity where individual_id = ? ;");
					getStockToSell.setInt(1, ind_id);
					rs = getStockToSell.executeQuery();
					if(rs.next() == false){
						return 0.0;
					}
					
										
					PreparedStatement portNet = connect.prepareStatement("SELECT net_worth FROM portfolio WHERE name = ? ;");
					portNet.setString(1, Symbol_fund_name);
					ResultSet pNet = portNet.executeQuery();	//...portfolio's net worth
					
					PreparedStatement personAmount = connect.prepareStatement("SELECT amount FROM individual_activity WHERE company_symbol = ? ;");
					personAmount.setString(1, Symbol_fund_name);
					ResultSet pAmount = personAmount.executeQuery();
					if(pAmount.next() == false){
						return 0.0;
					}
					if(pNet.next() == false){
						return 0.0;
					}
					Double temp1 = pAmount.getDouble("amount");
					Double temp2 = pNet.getDouble("net_worth");
					Double personPercent = temp1/temp2;	//....percent of the total investsment in the portfolio that the person holds
					
					//....now you can start calculating how much the company is worth based on appreciations
					
					PreparedStatement getDists2 = connect.prepareStatement("SELECT symbol, percentage FROM portfolio_distribution WHERE portfolio_name = ? ;");
					
					
					
					// TODO put in that loop that Kevin made to check if everything has a close price or else you can't seel on that day
					getDists2.setString(1, Symbol_fund_name);
					ResultSet stockCheck = getDists2.executeQuery();
					
					//Checks if the stocks in fund to buy has invalid closing prices
					while(stockCheck.next()){	//...if any one of the close prices are not available then you can't seel the portfolio on that day.
						PreparedStatement getClosePrice = connect.prepareStatement("SELECT close FROM quote_history WHERE company_symbol = ? and date = ? ;" );
						getClosePrice.setString(1, stockCheck.getString("symbol"));
						getClosePrice.setString(2, Date);
						ResultSet closePrice = getClosePrice.executeQuery();
						
						if(closePrice.next() == false){
							return 0.0;
						}
					}
					
					Double investmentSum = 0.0;
					
					
					PreparedStatement getDists = connect.prepareStatement("SELECT symbol, percentage FROM portfolio_distribution WHERE portfolio_name = ? ;");
					getDists.setString(1, Symbol_fund_name);
					ResultSet percentSet = getDists.executeQuery();
					while(percentSet.next()){
						PreparedStatement getClosePrice = connect.prepareStatement("SELECT close FROM quote_history WHERE company_symbol = ? and date = ? ;" );
						getClosePrice.setString(1, percentSet.getString("symbol"));
						getClosePrice.setString(2, Date);
						ResultSet closePrice = getClosePrice.executeQuery();
						if(closePrice.next() == false){
							
						}
						
						PreparedStatement numShares = connect.prepareStatement("SELECT num_shares FROM portfolio_activity WHERE portfolio_name = ? AND company_symbol = ? ;");
						numShares.setString(1, Symbol_fund_name);
						numShares.setString(2, percentSet.getString("symbol"));
						ResultSet nShares = numShares.executeQuery();
						if(nShares.next() == false){
						
						}
						
					
						
						investmentSum += (closePrice.getDouble("close") * nShares.getInt("num_shares"));
						
						
						PreparedStatement removeActivity =  connect.prepareStatement("UPDATE individual_activity SET activity_type = ? WHERE individual_id = ? AND activity_type = ? ;");
						removeActivity.setString(1,"sold");
						removeActivity.setInt(2, ind_id);
						removeActivity.setString(3, percentSet.getString("symbol"));
						removeActivity.executeUpdate();
					}
					
					//...at this point you've summed up all the investments based on if they were sold today. Now add the company's cash to it.
					
					PreparedStatement getPortCash = connect.prepareStatement("SELECT cash FROM portfolio WHERE name = ? ;");
					getPortCash.setString(1, Symbol_fund_name);
					rs = getPortCash.executeQuery();
					rs.next();
					
					Double portCash = rs.getDouble("cash");
					
					Double portTotalWorth = portCash + investmentSum;
					
					Double personReturn = personPercent * portTotalWorth;
					
					PreparedStatement getCash = connect.prepareStatement("SELECT cash FROM INDIVIDUAL WHERE individual_id = ? ;");
					getCash.setInt(1, ind_id);
					rs = getCash.executeQuery();
					rs.next();
					
					Double currentCash = rs.getDouble("cash");
					Double updatedCash = currentCash + personReturn;
					
					PreparedStatement updateCash = connect.prepareStatement("UPDATE individual SET cash ="+updatedCash+" WHERE individual_id = ? ;");
					updateCash.setInt(1,ind_id);
					updateCash.executeUpdate();	
					
					return personReturn;
					
				}
				
			}else{
				//Portfolio selling stock QUERY here
				
				//Insert into portfolio activity
				preparedStatement = connect
						.prepareStatement("INSERT INTO portfolio_activity VALUES(default,?,?,?,?,?,?)");
				
				
				PreparedStatement q1 = connect.prepareStatement("SELECT num_shares,amount FROM portfolio_activity WHERE company_symbol = ? AND portfolio_name = ?;");
				q1.setString(1, Symbol_fund_name);
				q1.setString(2, name);
				rs = q1.executeQuery();
				while(rs.next()){
						preparedStatement.setString(1,Date);
						preparedStatement.setInt(2,rs.getInt("num_shares"));
						preparedStatement.setDouble(3,rs.getDouble("amount"));
						preparedStatement.setString(4,Symbol_fund_name);
						preparedStatement.setString(5,"sell");
						preparedStatement.setString(6,name);
						preparedStatement.executeUpdate();
					
					
					
					
					//Deletes stock from distribution
					PreparedStatement pid = connect
							.prepareStatement("SELECT portfolio_distribution_id FROM portfolio_distribution WHERE symbol = ? AND portfolio_name = ? ;");
					pid.setString(1, Symbol_fund_name);
					pid.setString(2, name);
					
					rs = pid.executeQuery();
					rs.next();
					
					PreparedStatement deleteStock = connect
							.prepareStatement("DELETE FROM portfolio_distribution WHERE portfolio_distribution_id = ?;");
					deleteStock.setInt(1,rs.getInt("portfolio_distribution_id"));
					
					deleteStock.executeUpdate();
					
					//Update cash value in fund
					PreparedStatement getAmount = connect.
							prepareStatement("SELECT num_shares FROM portfolio_activity WHERE company_symbol = ? AND portfolio_name = ? AND activity_type = ? ;");
					
					getAmount.setString(1, Symbol_fund_name);
					getAmount.setString(2,name);
					getAmount.setString(3,"buy");
					
					rs = getAmount.executeQuery();
					
					rs.next();
					Double num_shares = rs.getDouble("num_shares");
					
					PreparedStatement adjClose = connect.prepareStatement("SELECT close FROM quote_history WHERE company_symbol = ? AND date = ? ;");
					adjClose.setString(1,Symbol_fund_name);
					adjClose.setString(2,Date);
					
					rs = adjClose.executeQuery();
					Double close;
					if(rs.next() == false){
						close = 0.0;
					}else{
					 close =rs.getDouble("close");
					}
					Double cashtoReturn = close* num_shares;
					PreparedStatement getCash = connect
							.prepareStatement("SELECT cash FROM portfolio WHERE name = ?;");
					getCash.setString(1,name);
					rs = getCash.executeQuery();
					rs.next();
					 
					Double cashReturn = cashtoReturn + rs.getDouble("cash"); //new cash value
					
					PreparedStatement updateCash = connect
							.prepareStatement("UPDATE portfolio SET cash ="+cashReturn+"WHERE name = ?; ");
					updateCash.setString(1,name);
					
					updateCash.executeUpdate();
					
					//Update Portfolio Distribution
					//
					PreparedStatement getStocks = connect.prepareStatement("SELECT symbol FROM portfolio_distribution WHERE portfolio_name = ?;");
				
					getStocks.setString(1,name);
					ResultSet updateSet = getStocks.executeQuery();
					
					while(updateSet.next()){
						String current_symbol = updateSet.getString("symbol");
						
						
						getAmount = connect.prepareStatement("SELECT amount FROM portfolio_activity WHERE portfolio_name = ? AND company_symbol = ?;");
						getAmount.setString(1, name);
						getAmount.setString(2, current_symbol);
						ResultSet a = getAmount.executeQuery();
						a.next();
						Double updatedDistribution = (a.getDouble("amount")/(cashReturn + a.getDouble("amount")) * 100);
						
						
						PreparedStatement updateDistribution = connect
								.prepareStatement("UPDATE portfolio_distribution SET percentage ="+updatedDistribution+"WHERE symbol = ? AND portfolio_name =?;");
						updateDistribution.setString(1, current_symbol);
						updateDistribution.setString(2, name);
						updateDistribution.executeUpdate();
						
					}
				
					return cashtoReturn;
				}
			}	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0.0;
	}
	public void execute_buy(String name, String symbol, String dollar_amount, String date) {
		try {
			
			//Checks if name is a portfolio name
			preparedStatement = connect
					.prepareStatement("SELECT name FROM portfolio WHERE name = ? ; ");
			preparedStatement.setString(1,name);
			ResultSet rs = preparedStatement.executeQuery();
			
			if(rs.next() == false){ //enters if name is individual
				
				//Checks if symbol is portfolio name
				preparedStatement = connect
				.prepareStatement("SELECT name FROM portfolio WHERE name  = ? ; ");
				preparedStatement.setString(1,symbol);
				rs = preparedStatement.executeQuery();
				
				
				
				if(rs.next() == false){//INDIVIDUAL BUYING COMPANY
					
					//Updates Individual_Activity Table
					preparedStatement = connect
							.prepareStatement("INSERT INTO individual_activity VALUES (default, ?,?,?,?,?,?,?)");
					
					preparedStatement.setString(1, date);
					
					PreparedStatement getNumShares = connect
							.prepareStatement("SELECT close FROM quote_history WHERE date = ? AND company_symbol = ? ; ");
					getNumShares.setString(1, date);
					getNumShares.setString(2, symbol);
					rs = getNumShares.executeQuery();
				//	rs.next(); //// don't need this
					Double numshares;
					if(rs.next() == false){
					
						return;
						
					}
					
					numshares = Double.parseDouble(dollar_amount)/rs.getDouble("close");
					
					
					int num = numshares.intValue();
					preparedStatement.setInt(2, num);
					preparedStatement.setDouble(3, Double.parseDouble(dollar_amount));
					preparedStatement.setString(4, symbol);
					preparedStatement.setString(5, "stock"); //// changed
					preparedStatement.setDouble(6, 0.0);
						//Query that finds individual_id
						PreparedStatement preparedStatement2 = connect
								.prepareStatement("SELECT individual_id FROM individual WHERE name  = ? ;");
						preparedStatement2.setString(1,name);
						rs = preparedStatement2.executeQuery();
						rs.next();
						int ind_id = rs.getInt("individual_id");
					preparedStatement.setInt(7,ind_id);
					
					preparedStatement.executeUpdate();
					
					//Insert into stocks
			//		PreparedStatement insertStock = connect.prepareStatement("INSERT INTO stocks VALUES (?,?,?)");
			//		insertStock.setInt(1,ind_id);
			//		insertStock.setString(2,symbol);
			//		insertStock.setInt(3,num);
					
			//		insertStock.executeUpdate();
						
					//Updates Individual Table (Cash)
					PreparedStatement getCash = connect.prepareStatement("SELECT cash FROM individual WHERE name = ? ;");	
					getCash.setString(1, name);
					rs = getCash.executeQuery();
					rs.next();
					
					Double updatedCash = rs.getDouble("cash") - Double.parseDouble(dollar_amount);
					
					PreparedStatement cashUpdate = connect
							.prepareStatement("UPDATE individual SET cash = "+updatedCash+" WHERE name = ? ;");
					cashUpdate.setString(1, name);
					cashUpdate.executeUpdate();
					
					
				}else{//INDIVIDUAL BUYING PORTFOLIO
					
					//Log transaction (Individual/Portfolio Activity)
					
					//....start off by finding the distributions of the company and spliting up the investment amount accordingly and then inserting into the activity table
					
										
					//....find the distributions
					PreparedStatement getDists = connect.prepareStatement("SELECT symbol, percentage FROM portfolio_distribution WHERE portfolio_name = ? ;");
					getDists.setString(1, symbol);
					
					ResultSet stockCheck = getDists.executeQuery();
					
					
					
					
					
					//Checks if the stocks in fund to buy has invalid closing prices
					while(stockCheck.next()){
						PreparedStatement getClosePrice = connect.prepareStatement("SELECT close FROM quote_history WHERE company_symbol = ? and date = ? ;" );
						getClosePrice.setString(1, stockCheck.getString("symbol"));
						getClosePrice.setString(2, date);
						ResultSet closePrice = getClosePrice.executeQuery();
						
						if(closePrice.next() == false){
							
							return;
						}
					}
					
					double cashToAdd = Double.parseDouble(dollar_amount);
					
					PreparedStatement getDists2 = connect.prepareStatement("SELECT symbol, percentage FROM portfolio_distribution WHERE portfolio_name = ? ;");
					getDists2.setString(1, symbol);
					ResultSet percentSet = getDists2.executeQuery();
					while(percentSet.next()){
						
						Double percent = percentSet.getDouble("percentage") / 100;
						Double amountBreak = Double.parseDouble(dollar_amount) * percent;	//...you now have the amount of money to be invested into that company
						Double sharePrice = 0.0;
						int personNumShares = 0;
						
						PreparedStatement getClosePrice = connect.prepareStatement("SELECT close FROM quote_history WHERE company_symbol = ? and date = ? ;" );
						getClosePrice.setString(1, percentSet.getString("symbol"));
						getClosePrice.setString(2, date);
						ResultSet closePrice = getClosePrice.executeQuery();
						
						/*
						if(closePrice.next() == false){	//.....if no close price is available
							System.out.println("There is no stock info for "+symbol+" on "+date);
							return;
							
							PreparedStatement getOpenPrice = connect.prepareStatement("SELECT open FROM quote_history WHERE company_symbol = ? and date = ? ;" );
							getOpenPrice.setString(1, percentSet.getString("symbol"));
							getOpenPrice.setString(2, date);
							ResultSet openPrice = getOpenPrice.executeQuery();
							
							if(openPrice.next())
								sharePrice = openPrice.getDouble("open");
							else
								sharePrice = 1.0;	//...don't know about this. Should it be equal to one or something else?
								
						} else {
								sharePrice = closePrice.getDouble("close"); 
								
						}
						*/
						closePrice.next();
						sharePrice = closePrice.getDouble("close");
						//....no can calculate the number of shares that the person gets through the portfolio
						Double temp = (amountBreak / sharePrice);
						personNumShares = temp.intValue();
						cashToAdd -= amountBreak;
						
						//...update the person's individual activity record for that specific port/company
						PreparedStatement updateActivity = connect.prepareStatement("INSERT INTO individual_activity VALUES (default, ?,?,?,?,?,?,?)");
						
						updateActivity.setString(1, date);
						updateActivity.setInt(2, personNumShares);	// num shares
						updateActivity.setDouble(3, Double.parseDouble(dollar_amount));	//AMOUNT!
						updateActivity.setString(4, symbol);
						updateActivity.setString(5, percentSet.getString("symbol"));
						updateActivity.setDouble(6, 0.0);
						
						PreparedStatement preparedStatement2 = connect.prepareStatement("SELECT individual_id FROM individual WHERE name  = ? ;");
						preparedStatement2.setString(1,name);
						ResultSet tempRS = preparedStatement2.executeQuery();
						tempRS.next();	
						int ind_id = tempRS.getInt("individual_id");
						
						updateActivity.setInt(7,ind_id);
						updateActivity.executeUpdate();	//... done updating the activity table, now gotta update the num shares for that port/comp in the portfolio activity
						
						//....update the num_shares for the portfolio
						/*
						PreparedStatement updatePort = connect.prepareStatement("SELECT num_shares FROM portfolio_activity WHERE portfolio_name = ? AND company_symbol = ? ;");
						updatePort.setString(1, symbol);
						updatePort.setString(2, percentSet.getString("symbol"));
						ResultSet portUpdate = updatePort.executeQuery();
						portUpdate.next();
						int portCompShare = portUpdate.getInt("num_shares");
						portCompShare += personNumShares;
						
						PreparedStatement insertUpdate = connect.prepareStatement("UPDATE portfolio_activity SET num_shares = ? WHERE portfolio_name = ? AND company_symbol = ? ;");
						insertUpdate.setInt(1, portCompShare);
						insertUpdate.setString(2, "symbol");
						insertUpdate.setString(3, percentShare.getString("symbol")); //.....num shares for portfolio has been updated for that specific port/company
						*/		
					}
											
						
				/*		PreparedStatement getNumShares = connect
								.prepareStatement("SELECT close FROM quote_history WHERE date = ? AND company_symbol = ? ; ");
						getNumShares.setString(1, date);
						getNumShares.setString(2, symbol);
						rs = getNumShares.executeQuery();
						Double numshares = 0.0;
		
						if(rs.next() == false){
							System.out.println("Fund Name: "+name+"Company: "+symbol+"on "+date+ "dollar ammount: "+dollar_amount);
						}
						
						else{
						numshares = Double.parseDouble(dollar_amount)/rs.getDouble("close");
						}
						int num = numshares.intValue();
						
					preparedStatement.setInt(2, num);	
					preparedStatement.setInt(6,ind_id);
					preparedStatement.executeUpdate();	*/
					
					
					
					//Update Individual Portfolio
			/*			PreparedStatement getCash = connect.prepareStatement("SELECT cash FROM portfolio WHERE name = ? ;");	
						getCash.setString(1, symbol);
						rs = getCash.executeQuery();
						rs.next();
						
						Double currentCash = rs.getDouble("cash");
					
					Double percentagePortfolio = (Double.parseDouble(dollar_amount)/currentCash);
					PreparedStatement insertInd_port = connect
							.prepareStatement("INSERT INTO individual_portfolio VALUES (default, ?,?,?,?)");
					
					insertInd_port.setDouble(1, percentagePortfolio);
					insertInd_port.setString(2, symbol);
					insertInd_port.setDouble(3, Double.parseDouble(dollar_amount)); //total cash amout invested in this portfolio
					insertInd_port.setInt(4, ind_id);
					
					insertInd_port.executeUpdate(); */
					
					
					
					//Update individual cash balance
					PreparedStatement preparedStatement2 = connect.prepareStatement("SELECT individual_id FROM individual WHERE name  = ? ;");
					preparedStatement2.setString(1,name);
					ResultSet tempRS = preparedStatement2.executeQuery();
					tempRS.next();	
					int ind_id = tempRS.getInt("individual_id");
					
					
						PreparedStatement getCash = connect.prepareStatement("SELECT cash FROM INDIVIDUAL WHERE individual_id = ? ;");
						getCash.setInt(1, ind_id);
						rs = getCash.executeQuery();
						rs.next();
						
						Double currentCash = rs.getDouble("cash");
						
						Double updatedCash = currentCash - Double.parseDouble(dollar_amount);
					
					PreparedStatement updateCash = connect
							.prepareStatement("UPDATE individual SET cash ="+updatedCash+" WHERE individual_id = ? ;");
					updateCash.setInt(1,ind_id);
					updateCash.executeUpdate();
					
					//Update fund cash 
					
					//Loop through fund distribution
						PreparedStatement getPortCash = connect.prepareStatement("SELECT cash FROM portfolio WHERE name = ? ;");
						getPortCash.setString(1, symbol);
						rs = getPortCash.executeQuery();
						rs.next();
						
						Double portCash = rs.getDouble("cash");
						
						
				/*		PreparedStatement distributions = connect
								.prepareStatement("SELECT symbol, num_shares percentage FROM portfolio_distribution WHERE portfolio_name = ? ;");
						distributions.setString(1, symbol);
						rs = distributions.executeQuery();
						
						Double accumulator = 0.0;
						Double dist_ac = 0.0;
						while(rs.next()){
							
							Double dist = rs.getDouble("percentage");
							dist_ac += dist;
							Double temp = Double.parseDouble(dollar_amount)*dist;
							
							accumulator += temp;
							
						} */
						double updatedPortCash = portCash + cashToAdd;
					
					PreparedStatement updatePortCash = connect.
							prepareStatement("UPDATE portfolio SET cash ="+updatedPortCash+"WHERE name = ?;");
					updatePortCash.setString(1, symbol);
					updatePortCash.executeUpdate();
					
					
					////.............. NOT SURE IF WE NEED THIS ANYMORE
					//Update portfolio net_worth
					PreparedStatement getNetWorth = connect
							.prepareStatement("SELECT net_worth FROM portfolio WHERE name = ? ;");
					getNetWorth.setString(1, symbol);
					rs = getNetWorth.executeQuery();
					rs.next();
					
					Double nw = rs.getDouble("net_worth");
					Double updated_nw = nw + Double.parseDouble(dollar_amount);
					
					PreparedStatement updateNetWorth = connect.prepareStatement("UPDATE portfolio SET net_worth ="+updated_nw+" WHERE name = ?;");
					updateNetWorth.setString(1,symbol);
					updateNetWorth.executeUpdate();
				
				}
				
			}else{//PORTFOLIO BUYING COMPANY 
			
				
				
				PreparedStatement getNumShares = connect
						.prepareStatement("SELECT close FROM quote_history WHERE date = ? AND company_symbol = ? ; ");
				getNumShares.setString(1, date);
				getNumShares.setString(2, symbol);
				rs = getNumShares.executeQuery();
				Double numshares = 0.0;
				//rs.next();
				if(rs.next() == false){
					return;
				}
				
				
				numshares = Double.parseDouble(dollar_amount)/rs.getDouble("close");
				
				int num = numshares.intValue();
				
				//Insert into Portfolio_Activity Table
				preparedStatement = connect
						.prepareStatement("insert into portfolio_activity values (default, ?,?,?,?,?,?)");
				
				preparedStatement.setString(1, date);
				preparedStatement.setInt(2, num);
				preparedStatement.setDouble(3, Double.parseDouble(dollar_amount));
				preparedStatement.setString(4, symbol);
				preparedStatement.setString(5, "buy");
				preparedStatement.setString(6, name);
				preparedStatement.executeUpdate();
				
				//Update Portfolio cash
				PreparedStatement getInitialFund = connect.prepareStatement("SELECT initial_fund FROM portfolio WHERE name = ? ;");	
				getInitialFund.setString(1, name);
				rs = getInitialFund.executeQuery();
				rs.next();
				Double initialFund =  rs.getDouble("initial_fund");
				
				PreparedStatement totalInvested = connect.prepareStatement("SELECT sum(cash_amount) FROM individual_portfolio WHERE portfolio_name = ? ;");
				totalInvested.setString(1, name);
				rs = totalInvested.executeQuery();
				rs.next();
				Double ti = rs.getDouble("sum(cash_amount)");
				
				
				PreparedStatement getCash = connect.prepareStatement("SELECT cash FROM portfolio WHERE name = ? ;");	
				getCash.setString(1, name);
				rs = getCash.executeQuery();
				rs.next();
				Double currentCash = rs.getDouble("cash");
				Double updatedCash = currentCash - Double.parseDouble(dollar_amount);
		
				//Update Portfolios Distribution
				Double percentagePortfolio = (Double.parseDouble(dollar_amount)/(initialFund+ti))*100;
				PreparedStatement insertInd_port = connect
						.prepareStatement("INSERT INTO portfolio_distribution VALUES (default, ?,?,?)");
				
				insertInd_port.setString(1, symbol);
				insertInd_port.setDouble(2, percentagePortfolio);
				insertInd_port.setString(3, name);
				
				insertInd_port.executeUpdate();
				
				
				PreparedStatement cashUpdate = connect
						.prepareStatement("UPDATE portfolio SET cash ="+updatedCash+" WHERE name = ? ;");
				cashUpdate.setString(1, name);
				cashUpdate.executeUpdate();
				
				//update individual_portfolio table
				//update portfolio
			}	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void execute_individual(String name , String dollar_figure, String date) {
		try {
			preparedStatement = connect
					.prepareStatement("INSERT INTO individual VALUES (default,?, ?, ?,?,?,?)");
					
					preparedStatement.setString(1, name); //name
					preparedStatement.setDouble(2, Double.parseDouble(dollar_figure)); //cash
					preparedStatement.setDouble(3, Double.parseDouble(dollar_figure)); //net_worth
					preparedStatement.setDouble(4, Double.parseDouble(dollar_figure)); //initial_fund
					preparedStatement.setDouble(5, 0.0); //total_return
					preparedStatement.setString(6, date); //date_created
						
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	//fund,name,dollar,figure,date
	@SuppressWarnings("deprecation")
	public void execute_fund(String name, String dollar_figure, String date) {
	
		try {
			
			preparedStatement = connect
			.prepareStatement("insert into Finance.portfolio values (?, ?, ?,?,?,?)");
			
			preparedStatement.setString(1, name);
			preparedStatement.setDouble(2, Double.parseDouble(dollar_figure)); //cash
			preparedStatement.setDouble(3, Double.parseDouble(dollar_figure)); //net_worth
			preparedStatement.setDouble(4, Double.parseDouble(dollar_figure)); //initial fund
			preparedStatement.setDouble(5, 0.0); //total return
			preparedStatement.setString(6, date); //date created
			
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
	}
	public void read_file(File file) throws SQLException, ClassNotFoundException, IOException {
		  Scanner inFile = new Scanner(file);  
		  BufferedReader br = new BufferedReader(new FileReader(file));
		  String commandInfo;
		  String cvsSplitBy = ",";
		  while ((commandInfo = br.readLine()) != null) {
			
			String[] company = commandInfo.split(cvsSplitBy);
			
			if(company[0].equals("fund"))
				this.execute_fund(company[1], company[2], company[3]);
			else if (company[0].equals("individual"))
				this.execute_individual(company[1], company[2], company[3]);
			else if (company[0].equals("buy"))
				this.execute_buy(company[1], company[2], company[3], company[4]);
			else if (company[0].equals("sell"))
				this.execute_sell(company[1], company[2], company[3]);
			else if (company[0].equals("sellbuy"))
				this.execute_exchange(company[1], company[2], company[3], company[4]);
			else 
				System.out.println("Unknown Command");
		  }
		  
	
		 UpdateTotalNetWorthPortfolio();
		 UpdateTotalNetWorthIndividual();
		System.out.println("Done Reading");
	}
	public void UpdateTotalNetWorth() throws SQLException{
		 PreparedStatement portfolios = connect
				  .prepareStatement("SELECT name FROM portfolio");
		  
		  ResultSet rs = portfolios.executeQuery();
		  Double appreciatedFunds = 0.0;
		  
		  while(rs.next()){
			 String currentPortfolio = rs.getString("name");
			 PreparedStatement dist = connect
					 .prepareStatement("SELECT symbol, percentage FROM portfolio_distribution WHERE portfolio_name = ? ;");
			 dist.setString(1, currentPortfolio);
			 ResultSet rs2 = dist.executeQuery();
			
			 while(rs2.next()){
				 	//Gets final num shares
					PreparedStatement numShares = connect
							.prepareStatement("SELECT num_shares FROM portfolio_activity WHERE portfolio_name = ? AND company_symbol = ? ;");
					
					
					numShares.setString(1, currentPortfolio);
					numShares.setString(2, rs2.getString("symbol"));
					
					ResultSet rs3 = numShares.executeQuery();
					if(rs3.next() == false){
						//System.out.println("Symbol: "+rs2.getString("symbol")+"portfolio_name: "+rs2.getString("portfolio_name"));
						continue;
					}
				
					int portNumShares = rs3.getInt("num_shares");
					
					PreparedStatement indiv_numShares = connect
							.prepareStatement("SELECT num_shares FROM individual_activity WHERE company_symbol = ?;");
					indiv_numShares.setString(1,currentPortfolio);
					
					ResultSet temp2 = indiv_numShares.executeQuery();
					if(temp2.next() == false){
						continue;
					}
						int indiv_shares = temp2.getInt("num_shares");
					
					
					
					//Gets close value
					PreparedStatement shareValue = connect
							.prepareStatement("SELECT close FROM quote_history WHERE company_symbol = ? AND date = ? ;");
					shareValue.setString(1, rs2.getString("symbol"));
					shareValue.setString(2, "2013-12-31");
					rs3 = shareValue.executeQuery();
					double closeVal;
					if(rs3.next()){
					 closeVal = rs3.getDouble("close");
					}
					else{
						closeVal = 0.0;
					}
					
					int total_numShares = indiv_shares+portNumShares;
			
					appreciatedFunds += (total_numShares * closeVal);
					
				}
			 PreparedStatement updatePortfolio = connect.prepareStatement("SELECT cash from portfolio WHERE name = ? ;");
				updatePortfolio.setString(1, currentPortfolio);
				rs = updatePortfolio.executeQuery();
				rs.next();
				
				double portCash = rs.getDouble("cash");
			 
				
				double portNetWorth =  (appreciatedFunds + portCash);
				
				
			
				PreparedStatement updatePortWorth = connect.prepareStatement("UPDATE portfolio SET net_worth = ? WHERE name = ? ;");
				updatePortWorth.setDouble(1, portNetWorth);
				updatePortWorth.setString(2, currentPortfolio);
				updatePortWorth.executeUpdate();
		  }
	}
	
	public void UpdateTotalNetWorthIndividual() throws SQLException {
		PreparedStatement individuals = connect
				  .prepareStatement("SELECT individual_id, cash FROM individual");
		  
		  ResultSet Individual = individuals.executeQuery();
		  while(Individual.next()){
			   int InvidiualID = Individual.getInt("individual_id");
			  	Double TotalNetWorth = 0.0;
			 	PreparedStatement IndividualAct = connect
						.prepareStatement("SELECT company_symbol, num_shares FROM individual_activity WHERE individual_id = ? AND activity_type = ? ;");
			 	IndividualAct.setInt(1, InvidiualID);
			 	IndividualAct.setString(2, "stock");
			 	Double SharesWorth = 0.0;
			 	ResultSet IndividualShares = IndividualAct.executeQuery();
			 	while(IndividualShares.next()){
			 		
			 		PreparedStatement shareValue = connect
							.prepareStatement("SELECT close FROM quote_history WHERE company_symbol = ? AND date = ? ;");
					shareValue.setString(1, IndividualShares.getString("company_symbol"));
					shareValue.setString(2, "2013-12-31");
					ResultSet ShareCloseValue = shareValue.executeQuery();
					while(ShareCloseValue.next()){
					SharesWorth += (IndividualShares.getInt("num_shares")*
								   ShareCloseValue.getDouble("close"));
					}
			 	}
			 	
			 	//Get Portfolio Investments
			 	PreparedStatement getInitialPortInvest = connect
			 			.prepareStatement("SELECT DISTINCT company_symbol, amount FROM individual_activity WHERE activity_type != ? AND individual_id = ? ;");
			 	getInitialPortInvest.setString(1,"stock");
			 	getInitialPortInvest.setInt(2,InvidiualID);
			 	ResultSet portInvest = getInitialPortInvest.executeQuery();
			 	Double portsTotalNW = 0.0;
			 	while(portInvest.next()){
			 		
			 		Double portTotalNW = 0.0;
			 		Double ind_portInvest = portInvest.getDouble("amount");
			 		String portfolio = portInvest.getString("company_symbol");
			 		
			 		PreparedStatement getPortInitAmount = connect.prepareStatement("SELECT initial_fund FROM portfolio WHERE name = ?;");
			 		getPortInitAmount.setString(1, portfolio);
			 		ResultSet rs = getPortInitAmount.executeQuery();
			 		
			 		Double initAmount = 0.0;
			 		
			 		while(rs.next()){
			 			 initAmount = rs.getDouble("initial_fund");
			 		}
			 		PreparedStatement getAllPortInvest = connect.prepareStatement("SELECT DISTINCT amount FROM individual_activity WHERE company_symbol = ? ;");
			 		getAllPortInvest.setString(1,portfolio);
			 		ResultSet rs2 = getAllPortInvest.executeQuery();
			 		
			 		Double totalPortInvestments = 0.0;
			 		
			 		while(rs2.next()){
			 			totalPortInvestments += rs2.getDouble("amount");
			 		}
			 		
			 		Double totalInvestmentsPercent = ind_portInvest/(totalPortInvestments + initAmount);
			 	
			 		PreparedStatement getPortNW = connect.prepareStatement("SELECT net_worth FROM portfolio WHERE name = ?;");
			 		getPortNW.setString(1, portfolio);
			 		ResultSet rs3 = getPortNW.executeQuery();
			 		
			 		
			 		
			 		while(rs3.next()){
			 			portTotalNW = totalInvestmentsPercent*rs3.getDouble("net_worth");
			 		}
			 		
			 		portsTotalNW += portTotalNW;
			 		
			 		
			 		
			 		
			 	}
			 	
			
			 	TotalNetWorth = Individual.getInt("cash") + SharesWorth + portsTotalNW;
			 	
			 	
			 	
			 
			 	//Final Update
				PreparedStatement updatePortWorth = connect.prepareStatement("UPDATE individual SET net_worth = ? WHERE individual_id = ? ;");
					updatePortWorth.setInt(2, InvidiualID);
					updatePortWorth.setDouble(1,TotalNetWorth);
					updatePortWorth.executeUpdate();
					
			 	
			 	
		  }
		
	}
	
	
	public void execute_rankPortfoliosTR() throws SQLException  {
		 UpdateTotalNetWorthPortfolio();
		 UpdateTotalNetWorthIndividual();
		resetTable();
		PreparedStatement getPortfoliosNames = connect
				.prepareStatement("SELECT name, net_worth, initial_fund   FROM portfolio ; ");
		ResultSet rs = getPortfoliosNames.executeQuery();
		
		while(rs.next()){
			
			String currentPort = rs.getString("name");
			Double portNW = rs.getDouble("net_worth");
			Double portInitialFund = rs.getDouble("initial_fund");
			PreparedStatement indAmount = connect
					.prepareStatement("SELECT DISTINCT amount FROM individual_activity WHERE company_symbol =  ?; ");
			indAmount.setString(1,currentPort);
			Double amountInvested = 0.0;
			ResultSet rs2 = indAmount.executeQuery();
			while(rs2.next()){
				amountInvested += rs2.getDouble("amount");
			}
			Double finalInvestments = amountInvested/(portInitialFund+amountInvested);
			
			Double TR = (portNW*(1-finalInvestments))/portInitialFund;
			PreparedStatement updateTR = connect.prepareStatement("UPDATE portfolio SET total_return = "+TR+ "WHERE name =?;");
			updateTR.setString(1, currentPort);
			updateTR.executeUpdate();
		}
		
		PreparedStatement rankByTR = 
				connect.prepareStatement("SELECT name, total_return FROM portfolio ORDER BY total_return DESC");
		ResultSet rankSet = rankByTR.executeQuery();
		int row = 0;
		while(rankSet.next()){
			
			String name = rankSet.getString("name");
			Double nw = rankSet.getDouble("total_return");
			GUIStocksInterface.table.setValueAt(name,row,0);
			GUIStocksInterface.table.setValueAt(nw,row,1);
			row++;
					
		}
		
	}
	public void execute_rankIndividualsTR() throws SQLException {
		 UpdateTotalNetWorthPortfolio();
		 UpdateTotalNetWorthIndividual();
		resetTable();
		PreparedStatement getTR = 
				connect.prepareStatement("SELECT individual_id, net_worth, initial_fund FROM individual");
		ResultSet rs = getTR.executeQuery();
		while(rs.next()){
			Double ind_NW = rs.getDouble("net_worth");
			Double ind_initial_fund = rs.getDouble("initial_fund");
			Integer individual_id = rs.getInt("individual_id");
			Double TR = (ind_NW - ind_initial_fund)/ind_initial_fund;
			PreparedStatement updateTotalReturn = 
					connect.prepareStatement("UPDATE individual SET total_return = "+TR+"WHERE individual_id = ?;");
			updateTotalReturn.setInt(1,individual_id);
					
			updateTotalReturn.executeUpdate();
		}
		
		PreparedStatement rankByTR = 
				connect.prepareStatement("SELECT name, total_return FROM individual ORDER BY total_return DESC");
		ResultSet rankSet = rankByTR.executeQuery();
		int row = 0;
		while(rankSet.next()){
			
			String name = rankSet.getString("name");
			Double nw = rankSet.getDouble("total_return");
			GUIStocksInterface.table.setValueAt(name,row,0);
			GUIStocksInterface.table.setValueAt(nw,row,1);
			row++;
					
		}
		
		
	}
	private void resetTable() {
		
		for(int i = 0; i < 1000000; i++){
			GUIStocksInterface.table.setValueAt(null,i,0);
			GUIStocksInterface.table.setValueAt(null,i,1);
			GUIStocksInterface.table.setValueAt(null,i,2);
		}
	}


	public void execute_top25stocks() throws SQLException {
		resetTable();
		PreparedStatement finalPrice = connect.prepareStatement("SELECT DISTINCT company_symbol, close FROM quote_history WHERE date = ?;");
		finalPrice.setString(1,"2013-12-31");
		ResultSet rs = finalPrice.executeQuery();
		
		while(rs.next()){
			String company = rs.getString("company_symbol");
			Double final_Price = rs.getDouble("close");
			PreparedStatement initialPrice = connect.prepareStatement("SELECT DISTINCT close FROM quote_history WHERE company_symbol =? AND date = ?;");
			initialPrice.setString(1,company);
			initialPrice.setString(2,"2005-01-03");
			ResultSet rs2 = initialPrice.executeQuery();
			
			while(rs2.next()){
				
				Double initial_Price = rs2.getDouble("close");
				
				Double stockReturn = (final_Price - initial_Price)/initial_Price;
				
				if(stockReturn < 0){
					stockReturn = stockReturn * -1.0;
					Double annualizedReturn = Math.pow(stockReturn, .125);
					annualizedReturn *= -1.0;
					
					PreparedStatement updatePortWorth = connect.prepareStatement("UPDATE company SET annualized_rate = ? WHERE symbol = ? ;");
					updatePortWorth.setDouble(1, annualizedReturn);
					updatePortWorth.setString(2, company);
					updatePortWorth.executeUpdate();
				}
				else{
					
					Double annualizedReturn = Math.pow(stockReturn, .125);
					PreparedStatement updatePortWorth = connect.prepareStatement("UPDATE company SET annualized_rate = ? WHERE symbol = ? ;");
					updatePortWorth.setDouble(1, annualizedReturn);
					updatePortWorth.setString(2, company);
					updatePortWorth.executeUpdate();
				}
				
			

			}
			
		
		}
		PreparedStatement rank = 
				connect.prepareStatement("SELECT symbol, annualized_rate FROM company ORDER BY annualized_rate DESC LIMIT 25");
		ResultSet rank25 = rank.executeQuery();
		int row = 0;
		while(rank25.next()){
			
			String name = rank25.getString("symbol");
			Double nw = rank25.getDouble("annualized_rate");
			GUIStocksInterface.table.setValueAt(name,row,0);
			GUIStocksInterface.table.setValueAt(nw,row,1);
			row++;
					
		}
		
		
	}
	public void execute_rankPortfoliosNW() throws SQLException {
	
		 UpdateTotalNetWorthPortfolio();
		 UpdateTotalNetWorthIndividual();
		resetTable();
		PreparedStatement query = connect.prepareStatement("SELECT name,net_worth FROM portfolio ORDER BY net_worth DESC");
		ResultSet rankedPortfoliosNW = query.executeQuery();
		
		int row = 0;
		while(rankedPortfoliosNW.next()){
			
			String name = rankedPortfoliosNW.getString("name");
			Double nw = rankedPortfoliosNW.getDouble("net_worth");
			GUIStocksInterface.table.setValueAt(name,row,0);
			GUIStocksInterface.table.setValueAt(nw,row,1);
			row++;
					
		}
		
	}
	public void execute_companiesStocksInc() throws SQLException {
		 UpdateTotalNetWorthPortfolio();
		 UpdateTotalNetWorthIndividual();
		resetTable();
		ArrayList<String> ValidStocks = new ArrayList<String>();
		PreparedStatement getSymbol = connect.prepareStatement("SELECT symbol FROM company");
		ResultSet rs = getSymbol.executeQuery();
		while(rs.next()){
			
		
			String company = rs.getString("symbol"); 
			
		
			Double currentPrice;
			Double max = 0.0;
			PreparedStatement getClose = connect.prepareStatement("SELECT close FROM quote_history WHERE date = ? AND company_symbol = ?;");
			getClose.setString(1,"2005-01-01");
			getClose.setString(2,company);
			ResultSet rs2 = getClose.executeQuery();
			
			while(rs2.next()){
				max = rs2.getDouble("close");
			}
			
			
			boolean increased = true;
			
			for(int i = 2006; i<2014; i++){
				String year = i+"-01-03";
				PreparedStatement getSymbols = connect.prepareStatement("SELECT close FROM quote_history WHERE date = ? AND company_symbol = ?;");
				getSymbols.setString(1,year);
				getSymbols.setString(2,company);
				ResultSet rs3 = getSymbols.executeQuery();
				while(rs3.next()){
					currentPrice = rs3.getDouble("close");
					System.out.print(company+" "+currentPrice + " ");
					if(max < currentPrice){
						max = currentPrice;
					}
					else{
						
						increased = false;
						break;
					}
					
				}
				if(increased = false){
					break;
				}	
			}
			
			if(increased == true){
			
				ValidStocks.add(company);
			}
		}
		int row = 0;
		for(String s: ValidStocks){
			GUIStocksInterface.table.setValueAt(s, row, 0);
		}
		
	}
	public void execute_ÞveLowestRiskStocks() throws SQLException {
		PreparedStatement getCompany = connect.prepareStatement("SELECT symbol FROM company");
		ResultSet companies = getCompany.executeQuery();
		while(companies.next()){
			
		}
	}
	public void execute_rankIndividualPortfolio() throws SQLException {
		 UpdateTotalNetWorthPortfolio();
		 UpdateTotalNetWorthIndividual();
		resetTable();
		PreparedStatement individualID = connect.prepareStatement("SELECT individual_id FROM individual");
		ResultSet rs = individualID.executeQuery();
		
		while(rs.next()){
			Integer ind_id = rs.getInt("individual_id");
			PreparedStatement getIndStock= connect.prepareStatement("SELECT company_symbol FROM individual_activity WHERE activity_type = ? AND individual_id = ?;");
			getIndStock.setString(1,"stock");
			getIndStock.setInt(2, ind_id);
			ResultSet rs2 = getIndStock.executeQuery();
			while(rs2.next()){
				PreparedStatement getAR = connect.prepareStatement("SELECT annualized_rate FROM company WHERE symbol = ?;");
				String stock = rs2.getString("company_symbol");
				getAR.setString(1,stock);
				ResultSet rs3 = getAR.executeQuery();
				rs3.next();
				Double compAR = rs3.getDouble("annualized_rate");
				
				PreparedStatement update = connect.prepareStatement("UPDATE individual_activity SET annualized_rate ="+compAR+"WHERE individual_id = ? AND company_symbol = ?;");
				update.setInt(1,ind_id);
				update.setString(2,stock);
				update.executeUpdate();
				
			}
		}
		
		PreparedStatement rank = connect
				.prepareStatement("SELECT name, net_worth FROM individual ORDER BY net_worth DESC");
		ResultSet rs2 = rank.executeQuery();
		int row = 0;
		while(rs2.next()){
			
			String name = rs2.getString("name");
			Double nw = rs2.getDouble("net_worth");
			GUIStocksInterface.table.setValueAt(name,row,0);
			GUIStocksInterface.table.setValueAt(nw,row,1);
			row++;
					
		}
		
	}
	public void execute_rankIndividualsNW() throws SQLException {
		
		 UpdateTotalNetWorthPortfolio();
		 UpdateTotalNetWorthIndividual();
		resetTable();
		UpdateTotalNetWorthIndividual();
		PreparedStatement rank = connect
				.prepareStatement("SELECT name, net_worth FROM individual ORDER BY net_worth DESC");
		ResultSet rs = rank.executeQuery();
		int row = 0;
		while(rs.next()){
			
			String name = rs.getString("name");
			Double nw = rs.getDouble("net_worth");
			GUIStocksInterface.table.setValueAt(name,row,0);
			GUIStocksInterface.table.setValueAt(nw,row,1);
			row++;
					
		}
		
	}
	
	public void execute_mysteryQuery() throws SQLException{
		 UpdateTotalNetWorthPortfolio();
		 UpdateTotalNetWorthIndividual();
		resetTable();
		PreparedStatement getPortfolios = connect.prepareStatement("SELECT name FROM portfolio");
		ResultSet portfolios = getPortfolios.executeQuery();
		int row = 1;
		int rowTable = 0;
		while(portfolios.next()){
			PreparedStatement getAmount = connect.prepareStatement("SELECT DISTINCT individual_id, amount  FROM individual_activity WHERE company_symbol = ?;");
			String portName = portfolios.getString("name");
			getAmount.setString(1, portName);
			
			ResultSet rs = getAmount.executeQuery();
			HashMap<Integer, Double> Owners = new HashMap<Integer, Double>();
			
			while(rs.next()){
				
				Owners.put(rs.getInt("individual_id"), rs.getDouble("amount"));
			
			java.util.Map.Entry<Integer,Double> maxEntry = null;
			for(java.util.Map.Entry<Integer, Double> entry : Owners.entrySet()) {
			    if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
			    	
			    	maxEntry = entry;
			    }
			}
			
			
	      
			PreparedStatement rank = connect
					.prepareStatement("SELECT individual_id, name FROM individual ORDER BY net_worth DESC");
			ResultSet rs5 = rank.executeQuery();
			
			while(rs5.next()){
				
				int individualID = rs5.getInt("individual_id");
				String individualName = rs5.getString("name");
				
				if(maxEntry == null){
					
				}
				if(individualID == maxEntry.getKey()){
					//RankPosition.add(row);
					GUIStocksInterface.table.setValueAt(portName,rowTable,0);
					GUIStocksInterface.table.setValueAt(individualName,rowTable,1);
					GUIStocksInterface.table.setValueAt("#"+row,rowTable,2); //ranking
					rowTable++;
					row++;
					break;
					
					
				}
				else{
				
					row++;
				}
				
			}
			}
			
		}
		
	}
	
	public void UpdateTotalNetWorthPortfolio() throws SQLException{
		
				PreparedStatement portfolios = connect
						  .prepareStatement("SELECT name, cash FROM portfolio");
				  
				  ResultSet rs = portfolios.executeQuery();
				  
				  Double appreciatedFunds = 0.0;
				  
				  //Getting num shares for a each stock in the portfolio
				  while(rs.next()){
					 String currentPortfolio = rs.getString("name");
					 PreparedStatement port_shares = connect
							 .prepareStatement("SELECT company_symbol, num_shares FROM portfolio_activity WHERE portfolio_name = ? ;");
					 port_shares.setString(1, currentPortfolio);
					 ResultSet Portfolioshares = port_shares.executeQuery();
					 Double SharesWorth = 0.0;
					 
					 
					 while(Portfolioshares.next()){
					
					 	PreparedStatement shareValue = connect
									.prepareStatement("SELECT close FROM quote_history WHERE company_symbol = ? AND date = ? ;");
							shareValue.setString(1, Portfolioshares.getString("company_symbol"));
							shareValue.setString(2, "2013-12-31");
							ResultSet ShareCloseValue = shareValue.executeQuery();
							while(ShareCloseValue.next()){
							SharesWorth += (Portfolioshares.getInt("num_shares")*
										   ShareCloseValue.getDouble("close"));
							}
							

					 		
					 }
					 PreparedStatement investors_shares = connect
							 .prepareStatement("SELECT activity_type, num_shares FROM individual_activity WHERE company_symbol = ? ;");
					 investors_shares.setString(1, currentPortfolio);
					 
					 ResultSet InvestorsShares = investors_shares.executeQuery();
				
					 
					 while(InvestorsShares.next()){
						
					 	PreparedStatement shareValueInvestor = connect
									.prepareStatement("SELECT close FROM quote_history WHERE company_symbol = ? AND date = ? ;");
					 	String current_stock = InvestorsShares.getString("activity_type");
					
					 	shareValueInvestor.setString(1, current_stock );
					 	shareValueInvestor.setString(2, "2013-12-31");
						ResultSet ShareCloseValueInvestor = shareValueInvestor.executeQuery();
							while(ShareCloseValueInvestor.next()){
								SharesWorth += (InvestorsShares.getInt("num_shares")*
										   ShareCloseValueInvestor.getDouble("close"));
							}
							

					 }
					 Double PortfolioCash = rs.getDouble("cash");
					 Double TotalNetWorth = PortfolioCash + SharesWorth;
					PreparedStatement updatePortWorth = connect.prepareStatement("UPDATE portfolio SET net_worth = ? WHERE name = ? ;");
						updatePortWorth.setDouble(1, TotalNetWorth);
						updatePortWorth.setString(2, currentPortfolio);
						updatePortWorth.executeUpdate();

				}
				
	}


	
	public void execute_exportToCSV() throws IOException {
		PrintWriter output = new PrintWriter(new FileWriter("output.csv"));
		
	
		Object headers[] = new Object[3];
		for(int i=0; i <GUIStocksInterface.table.getColumnCount(); i++ ){
		JTableHeader th = GUIStocksInterface.table.getTableHeader();
		TableColumnModel ch = th.getColumnModel();
			if(ch.getColumn(i).getHeaderValue() != null){
				output.write(""+ch.getColumn(i).getHeaderValue());
				
				if(ch.getColumn(1) != null && i == 0)
					output.write(",");
				else if(ch.getColumn(2) != null && i == 1){
					output.write(",");
				}
				
	    	
			}
		}
		output.write("\n");
    	
		
		
		
		for(int row = 0; row < GUIStocksInterface.Data.length; row++){
			
			if(GUIStocksInterface.table.getValueAt(row, 0) != null){
				output.write("" + GUIStocksInterface.table.getValueAt(row, 0));
			}
			else{
				break;
			}
			if(GUIStocksInterface.table.getValueAt(row, 1) != null){
				output.write("," + GUIStocksInterface.table.getValueAt(row, 1));
			}
			else{
				output.write("\n");
			}
			if(GUIStocksInterface.table.getValueAt(row, 2) != null){
				output.write("," + GUIStocksInterface.table.getValueAt(row, 1));
				output.write("\n");
			}
			else{
				output.write("\n");
			}
			
		}
		
		output.close();
		
		// TODO Auto-generated method stub
		
	}


	
	
	
}