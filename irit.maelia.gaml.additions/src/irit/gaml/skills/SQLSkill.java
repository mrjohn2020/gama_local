package irit.gaml.skills;

import irit.gaml.SqlConnection;

import java.sql.Connection;
import java.sql.SQLException;

import msi.gama.common.util.GuiUtils;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.args;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaList;
import msi.gaml.skills.Skill;
import msi.gaml.types.IType;
/*
* @Authors: TRUONG Minh Thai 
* @Supervisors:
*     Christophe Sibertin-BLANC
*     Fredric AMBLARD
*     Benoit GAUDOU
* SQLSKILL supports the method
* - helloWorld: print "Hello world on output screen as a test of skill.
* - testConnection: make a connection to DBMS as test connection.
* - select: connect to DBMS and run executeQuery to select data from DBMS.
* - executeUpdate: connect to DBMS and run executeUpdate to update/insert/delete/drop/create data
* on DBMS.
* 
* Created date: 20-Jun-2012
* Modified:
*   10-Sep-2012: change MAELIASQL Skill to SQLSKILL
*   13-sep-2012:
*       1. Change the input name of selectDB
*       2. Add action: select  
*   19-Sep-2012: Change const MSSQL to SQLSERVER for SQL Server
*                Change paramater name url to host
*   24-Sep-2012: move method connectDB, SelectDB and executeUpdateDB
*                add method testConnection 
*   24-Sep-2012: add sqlite to SQLSKILL
* Last Modified: 20-Sep-2012
*/

@skill(name = "SQLSKILL")
public class SQLSkill extends Skill {
	static final boolean DEBUG = false; // Change DEBUG = false for release version

	/*
	 * for test only
	 */
	@action(name="helloWorld")
	@args(names = {})
	public Object helloWorld(final IScope scope) throws GamaRuntimeException {
		GuiUtils.informConsole("Hello World");
		return null;
	}

	/*
	 * Make a connection to BDMS
	 * 
	 * @syntax: do action: connectDB {
	 * arg dbtype value: vendorName; //MySQL/sqlserver/sqlite
	 * arg host value: urlvalue;
	 * arg port value: portvaluse;
	 * arg database value: dbnamevalue;
	 * arg user value: usrnamevalue;
	 * arg passwd value: password valuse;
	 * }
	 */
//	@action(name="connectDB")
//	@args(names = { "dbtype", "host", "port", "database", "user", "passwd" })
//	public  Object connectDB(final IScope scope) throws GamaRuntimeException
//	{
//		Connection conn;
//		String dbtype = (String) scope.getArg("dbtype", IType.STRING);
//		String host= (String) scope.getArg("host", IType.STRING);
//		String port = (String) scope.getArg("port", IType.STRING);
//		String database = (String) scope.getArg("database", IType.STRING);
//		String user = (String) scope.getArg("user", IType.STRING);
//		String passwd = (String) scope.getArg("passwd", IType.STRING);
//
//		SqlConnection sqlConn;
//		// create connection
//		if (dbtype.equalsIgnoreCase(SqlConnection.SQLITE)){
//			String DBRelativeLocation =
//					scope.getSimulationScope().getModel().getRelativeFilePath(database, true);
//
//			//sqlConn=new SqlConnection(dbtype,database);
//			sqlConn=new SqlConnection(dbtype,DBRelativeLocation);
//		}else{
//			sqlConn=new SqlConnection(dbtype,host,port,database,user,passwd);
//		}
//		try {	
//			conn = sqlConn.connectDB();
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new GamaRuntimeException("SQLSkill.connectDB: " + e.toString());
//		} 
//		return conn;
//	}
	
	/*
	 * Make a connection to BDMS
	 * 
	 * @syntax: do action: connectDB {
	 *			arg params value:[
	 * 					 "dbtype":"SQLSERVER", 
	 *                   "url":"host address",
	 *                   "port":"port number",
	 *                   "database":"database name",
	 *                   "user": "user name",
	 *                   "passwd": "password"
	 *                  ];
	 * }
	 */
	@action(name="testConnection")
	@args(names = { "params" })
	public  boolean testConnection(final IScope scope) throws GamaRuntimeException
	{
		Connection conn;
		java.util.Map params = (java.util.Map) scope.getArg("params", IType.MAP);
		String dbtype = (String) params.get("dbtype");
		String host = (String)params.get("host");
		String port = (String)params.get("port");
		String database = (String) params.get("database");
		String user = (String) params.get("user");
		String passwd = (String)params.get("passwd");
		SqlConnection sqlConn;

		// create connection
		if (dbtype.equalsIgnoreCase(SqlConnection.SQLITE)){
			String DBRelativeLocation =
					scope.getSimulationScope().getModel().getRelativeFilePath(database, true);

			//sqlConn=new SqlConnection(dbtype,database);
			sqlConn=new SqlConnection(dbtype,DBRelativeLocation);
		}else{
			sqlConn=new SqlConnection(dbtype,host,port,database,user,passwd);
		}
		try {	
			conn = sqlConn.connectDB();
			conn.close();
		} catch (Exception e) {
			//e.printStackTrace();
			//throw new GamaRuntimeException("SQLSkill.connectDB: " + e.toString());
			return false;
		} 
		return true;
	}

	/*
	 * Make a connection to BDMS and execute the select statement
	 * 
	 * @syntax do action: selectDB {
	 * arg dbtype value: vendorName; //MySQL/sqlserver/sqlite
	 * arg host value: urlvalue;
	 * arg port value: portvaluse;
	 * arg database value: dbnamevalue;
	 * arg user value: usrnamevalue;
	 * arg passwd value: password valuse;
	 * }
	 * 
	 * @return GamaList<GamaList<Object>>
	 */
//	@action(name="selectDB")
//	@args(names = { "dbtype", "host", "port", "database", "user", "passwd", "select" })
//	//public GamaList<GamaList<Object>> selectDB(final IScope scope) throws GamaRuntimeException
//	public GamaList<Object> selectDB(final IScope scope) throws GamaRuntimeException
//	{
//		String selectComm=(String) scope.getArg("select", IType.STRING);
//		String dbtype = (String) scope.getArg("dbtype", IType.STRING);
//		String host= (String) scope.getArg("host", IType.STRING);
//		String port = (String) scope.getArg("port", IType.STRING);
//		String database = (String) scope.getArg("database", IType.STRING);
//		String user = (String) scope.getArg("user", IType.STRING);
//		String passwd = (String) scope.getArg("passwd", IType.STRING);
//
//		SqlConnection sqlConn;
//		// create connection
//		if (dbtype.equalsIgnoreCase(SqlConnection.SQLITE)){
//			String DBRelativeLocation =
//					scope.getSimulationScope().getModel().getRelativeFilePath(database, true);
//
//			//sqlConn=new SqlConnection(dbtype,database);
//			sqlConn=new SqlConnection(dbtype,DBRelativeLocation);
//		}else{
//			sqlConn=new SqlConnection(dbtype,host,port,database,user,passwd);
//		}
//		
//		//Select data
//		//GamaList<GamaList<Object>> repRequest = new GamaList<GamaList<Object>>();
//		GamaList<Object> repRequest = new GamaList<Object>();
//		try{
//			repRequest = sqlConn.selectDB(selectComm);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			throw new GamaRuntimeException("SQLSkill.selectDB: " + e.toString());
//		}
//		if ( DEBUG ) {
//			GuiUtils.informConsole(selectComm + " was run");
//		}
//
//		return repRequest;
//
//	}
//	
	/*
	 * Make a connection to BDMS and execute the select statement
	 * 
	 * @syntax do action: 
	 * 	select {
	 * 		arg params value:[
	 * 					 "dbtype":"SQLSERVER", 
	 *                   "url":"host address",
	 *                   "port":"port number",
	 *                   "database":"database name",
	 *                   "user": "user name",
	 *                   "passwd": "password",
	 *                   "select": "select string"
	 *                  ],
	 *  	arg select value: "select string"
	 *   }
	 * 
	 * @return GamaList<GamaList<Object>>
	 */
	@action(name="select")
	@args(names = { "params", "select"})
	public GamaList<Object> select(final IScope scope) throws GamaRuntimeException
	{
		java.util.Map params = (java.util.Map) scope.getArg("params", IType.MAP);
		String selectComm = (String) scope.getArg("select", IType.STRING);
		String dbtype = (String) params.get("dbtype");
		String host = (String)params.get("host");
		String port = (String)params.get("port");
		String database = (String) params.get("database");
		String user = (String) params.get("user");
		String passwd = (String)params.get("passwd");
		SqlConnection sqlConn;
		GamaList<Object> repRequest = new GamaList<Object>();
		// create connection
		if (dbtype.equalsIgnoreCase(SqlConnection.SQLITE)){
			String DBRelativeLocation =
					scope.getSimulationScope().getModel().getRelativeFilePath(database, true);

			//sqlConn=new SqlConnection(dbtype,database);
			sqlConn=new SqlConnection(dbtype,DBRelativeLocation);
		}else{
			sqlConn=new SqlConnection(dbtype,host,port,database,user,passwd);
		}
		
		// get data
		try{
			  //repRequest= sqlcon.selectDB((String) params.get("select"));
			  repRequest = sqlConn.selectDB(selectComm);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new GamaRuntimeException("SQLSkill.select: " + e.toString());
		}
		if ( DEBUG ) {
			GuiUtils.informConsole(selectComm + " was run");
		}

		return repRequest;

	}

	/*
	 * Make a connection to BDMS and execute the update statement (update/insert/delete/create/drop)
	 * 
	 * @syntax: do action: executeUpdateDB {
	 * 							arg dbtype value: vendorvalue;
	 * 							arg url value: urlvalue;
	 * 							arg port value: portvalue;
	 * 							arg database value: dbnamevalue;
	 * 							arg user value: usrnamevalue;
	 * 							arg passwd value: pwvaluse;
	 * 							arg updateComm value: updateStatement;
	 * }
	 */
//	@action(name="executeUpdateDB")
//	@args(names = { "dbtype", "host", "port", "database", "user", "passwd", "updateComm" })
//	public int executeUpdateDB(final IScope scope) throws GamaRuntimeException {
//		String dbtype = (String) scope.getArg("dbtype", IType.STRING);
//		String host= (String) scope.getArg("host", IType.STRING);
//		String port = (String) scope.getArg("port", IType.STRING);
//		String database = (String) scope.getArg("database", IType.STRING);
//		String user = (String) scope.getArg("user", IType.STRING);
//		String passwd = (String) scope.getArg("passwd", IType.STRING);
//		String updateComm = (String) scope.getArg("updateComm", IType.STRING);
//		SqlConnection sqlConn;
//		// create connection
//		if (dbtype.equalsIgnoreCase(SqlConnection.SQLITE)){
//			String DBRelativeLocation =
//					scope.getSimulationScope().getModel().getRelativeFilePath(database, true);
//
//			//sqlConn=new SqlConnection(dbtype,database);
//			sqlConn=new SqlConnection(dbtype,DBRelativeLocation);
//		}else{
//			sqlConn=new SqlConnection(dbtype,host,port,database,user,passwd);
//		}
//		int n = 0;
//		try {
//			n = sqlConn.executeUpdateDB(updateComm);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			throw new GamaRuntimeException("SQLSkill.exexuteUpdateDB:" + e.toString());
//		}
//
//		if ( DEBUG ) {
//			GuiUtils.informConsole(updateComm + " was run");
//		}
//
//		return n;
//
//	}
	/*
	 * Make a connection to BDMS and execute the update statement (update/insert/delete/create/drop)
	 * 
	 * @syntax: do action: executeUpdate {
	 * arg params value:[
	 * 					 "dbtype":"MSSQL", 
	 *                   "url":"host address",
	 *                   "port":"port number",
	 *                   "database":"database name",
	 *                   "user": "user name",
	 *                   "passwd": "password",
	 *                  ],
	 *  	arg updateComm value: "update string"
	 *   }
	 *   
	 *   */
	@action(name="executeUpdate")
	@args(names = { "params", "updateComm"})
	public int executeUpdate(final IScope scope) throws GamaRuntimeException {
		java.util.Map params = (java.util.Map) scope.getArg("params", IType.MAP);
		String updateComm = (String) scope.getArg("updateComm", IType.STRING);
		String dbtype = (String) params.get("dbtype");
		String host = (String)params.get("host");
		String port = (String)params.get("port");
		String database = (String) params.get("database");
		String user = (String) params.get("user");
		String passwd = (String)params.get("passwd");
		SqlConnection sqlConn;
		// create connection
		if (dbtype.equalsIgnoreCase(SqlConnection.SQLITE)){
			String DBRelativeLocation =
					scope.getSimulationScope().getModel().getRelativeFilePath(database, true);
			System.out.println("database sqlite:"+DBRelativeLocation);
			//sqlConn=new SqlConnection(dbtype,database);
			sqlConn=new SqlConnection(dbtype,DBRelativeLocation);
		}else{
			sqlConn=new SqlConnection(dbtype,host,port,database,user,passwd);
		}
		int n = 0;
		try {
			n = sqlConn.executeUpdateDB(updateComm);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new GamaRuntimeException("SQLSkill.exexuteUpdateDB:" + e.toString());
		}

		if ( DEBUG ) {
			GuiUtils.informConsole(updateComm + " was run");
		}

		return n;

	}

}
