package com.gbm;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.mfp.adapter.api.AdaptersAPI;
import com.ibm.mfp.adapter.api.ConfigurationAPI;
import com.ibm.mfp.adapter.api.OAuthSecurity;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Api;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.sql.*;

@Api(value = "Devices Insert Resource GBM")
@Path("/")
public class UserInsertDBResource {

	@Context
	ConfigurationAPI configurationAPI;

	@Context
	AdaptersAPI adaptersAPI;
	private static String CANT_DISPOSITIVOS_INCORRECTA = "La cantidad de dispositivos ingresados es incorrecta.";
	private static String DISPOSITIVOS_INGRESADOS = "Los dispositivos fueron ingresados correctamente.";
	private static String BD_NAME_INCORRECT = "Debe ingresar el nombre de la BD correctamente.";
	private static String INTEGRITY_ERROR = "Error de integridad de datos.";


	public Connection getSQLConnection() throws SQLException{
		UserInsertDBApplication app = adaptersAPI.getJaxRsApplication(UserInsertDBApplication.class);
		Connection con = app.dataSource.getConnection();
		return con;
	}

	@ApiOperation(value = "Recurso sin seguridad", notes = "Este recurso se encarga de insertar los dispositivos requeridos en la BD.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Retorna un mensaje en formato JSON") })
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/insertDevices")
	@OAuthSecurity(enabled=false)
	public Response insertUser()throws SQLException {
		Connection con = getSQLConnection();
		PreparedStatement ps = null;
		JSONObject result = new JSONObject();
		int startUser = Integer.parseInt(configurationAPI.getPropertyValue("Start_device_id"));
		int count = Integer.parseInt(configurationAPI.getPropertyValue("Count_device_id"));
		String dbName = configurationAPI.getPropertyValue("DB_name");
		if (count == 0) {
			result.put("Error", CANT_DISPOSITIVOS_INCORRECTA);
			return Response.ok(result).build();
		}
		int total = startUser + count;
		try {
			do {
				ps = con.prepareStatement("INSERT INTO " +
						dbName +
						".MFP_PERSISTENT_DATA\n" +
						"  VALUES (" +
						startUser +
						", REPLACE('{\"clientId\":\"CLIENT_NUM\",\"registrationData\":{\"application\":{\"id\":\"com.sample.oauthdemoandroid\",\"clientPlatform\":\"android\",\"version\":\"1.0\"},\"device\":{\"id\":\"device_CLIENT_NUM\",\"hardware\":\"Samsung Galaxy S7 - 6.0.0 - API 23 - 1440x2560\",\"platform\":\"android 6.0\",\"deviceDisplayName\":" +
						"\"GBM_Performance_" +
						startUser +
						"\",\"deviceStatus\":\"ACTIVE\"}, \"attributes\":[\"java.util.LinkedHashMap\",{}]},\"signatureAlgorithm\":\"RS256\",\"publicCredentials\":[\"java.security.interfaces.RSAPublicKey\",{\"kty\":\"RSA\",\"n\":\"AJqHV9w5pYsoIQ_INnOFspIMAsZHxKgkEf_VsMC2CYXq5KeFdg0cuCE-AbGqu6TXEDHXaTh_YtUBaQz06HDZqOeKmmE-ESypdZtAxPg56E5QKIDupWtaMWlxw-fScKAD30RT_E_GkkfdK3IEckWZ1gM1tvVCx_3YQ4Cxtxg-I6LJ\",\"e\":\"AQAB\"}],\"privateCredentials\":null,\"registrationComplete\":true,\"enabled\":true,\"remoteDisableNotifyId\":0,\"lastActivityTime\":1462785710974,\"associatedUsers\":[\"java.util.HashMap\",{}],\"publicAttributes\":{\"attributes\":{}},\"protectedAttributes\":{\"attributes\":{}}}',\n" +
						"  'CLIENT_NUM'," +
						startUser +
						"), CONCAT('device_', " +
						startUser +
						"), CONCAT('GBM_Performance_', " +
						startUser +
						"), 0, 'com.sample.oauthdemoandroid',\n" +
						"    '1.0', 1, '[]', 0, 'hash')");
				ps.executeUpdate();
				startUser++;
			} while (startUser < total);
			result.put("Mensaje", DISPOSITIVOS_INGRESADOS);
			result.put("Cantidad", count);
			return Response.ok(result).build();
		}
		catch (SQLIntegrityConstraintViolationException sql){
			result.put("Error", INTEGRITY_ERROR);
			return Response.ok(result).build();
		}
		catch (Exception e){
			return	Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		finally{
			ps.close();
			con.close();
		}
	}

	@ApiOperation(value = "Recurso sin seguridad", notes = "Este recurso se encarga de consultar TODOS los dispositivos existentes en la BD.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Retorna un mensaje en formato JSON") })
	@GET
	@OAuthSecurity(enabled=false)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getAllDevices")
	public Response getAllDevices() throws SQLException{
		JSONArray results = new JSONArray();
		String dbName = configurationAPI.getPropertyValue("DB_name");
		Connection con = getSQLConnection();
		PreparedStatement getAllDevices = con.prepareStatement("SELECT DEVICE_ID, DEVICE_DISPLAY_NAME, DEVICE_STATUS, APPLICATION_ID, APPLICATION_VERSION, CLIENT_STATUS, ASSOCIATED_USERS FROM " + dbName + ".MFP_PERSISTENT_DATA");
		ResultSet data = getAllDevices.executeQuery();

		if (dbName == null || dbName.isEmpty()){
			JSONObject item = new JSONObject();
			item.put("Error", BD_NAME_INCORRECT);
			results.add(item);
			return Response.ok(results).build();
		}
		while(data.next()){
			JSONObject item = new JSONObject();
			item.put("DEVICE_ID", data.getString("DEVICE_ID"));
			item.put("DEVICE_DISPLAY_NAME", data.getString("DEVICE_DISPLAY_NAME"));
			item.put("DEVICE_STATUS", data.getInt("DEVICE_STATUS"));
			item.put("APPLICATION_ID", data.getString("APPLICATION_ID"));
			item.put("APPLICATION_VERSION", data.getString("APPLICATION_VERSION"));
			item.put("CLIENT_STATUS", data.getInt("CLIENT_STATUS"));
			item.put("ASSOCIATED_USERS", data.getString("ASSOCIATED_USERS"));

			results.add(item);
		}

		getAllDevices.close();
		con.close();

		return Response.ok(results).build();
	}

	@ApiOperation(value = "Recurso sin seguridad", notes = "Este recurso se encarga de consultar la cantidad de dispositivos existentes en la BD.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Retorna un mensaje en formato JSON") })
	@GET
	@OAuthSecurity(enabled=false)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/getCountDevices")
	public Response getCountDevices() throws SQLException{
		JSONArray results = new JSONArray();
		String dbName = configurationAPI.getPropertyValue("DB_name");
		Connection con = getSQLConnection();
		PreparedStatement getCountDevices = con.prepareStatement("SELECT COUNT(ID) as COUNT FROM " + dbName + ".MFP_PERSISTENT_DATA");
		ResultSet data = getCountDevices.executeQuery();

		if (dbName == null || dbName.isEmpty()){
			JSONObject item = new JSONObject();
			item.put("Error", BD_NAME_INCORRECT);
			results.add(item);
			return Response.ok(results).build();
		}
		while(data.next()){
			JSONObject item = new JSONObject();
			item.put("COUNT", data.getString("COUNT"));
			results.add(item);
		}

		getCountDevices.close();
		con.close();
		return Response.ok(results).build();
	}
}
