package com.trackingpath.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.CalibrationDetailDTO;
import com.trackingpath.dtos.DeviceSensorMappingDTO;
import com.trackingpath.dtos.LiveDataBean;
import com.trackingpath.dtos.ResultantSensorBean;

import lombok.extern.slf4j.Slf4j;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.Expression;


import com.trackingpath.dtos.SensorBean;


@Component
@Slf4j
public class AttributeHelper {
	@Autowired
	JdbcTemplate jdbcTemplate_pg;


	public List<ResultantSensorBean> getAttributeHelperData(LiveDataBean bean) {
		// get all sensor data on the basis of device id
		
		List<SensorBean> allSensorData = getAllSensorDataByDevice_id(bean.getDevice_id());
		
		List<ResultantSensorBean> resultantSensorList = new ArrayList<ResultantSensorBean>();

		if (allSensorData != null && allSensorData.size() > 0 && bean.getAttributes() != null
				&& !bean.getAttributes().isEmpty()) {
			ObjectMapper mapper = new ObjectMapper();
			String attributes = bean.getAttributes();
			try {
				Map<String, Object> attributesMap = mapper.readValue(attributes,
						new TypeReference<Map<String, Object>>() {
						});
				for (SensorBean sensorData : allSensorData) {
					ResultantSensorBean resultantSensor = new ResultantSensorBean();

					String attributeParameter = sensorData.getDeviceSensorMappingBean().getParameter();
					if (attributesMap.containsKey(attributeParameter)) {
						Object attributeValue = attributesMap.get(attributeParameter);

						// Check if the attributeValue is either Integer or Double
						if (attributeValue instanceof Integer || attributeValue instanceof Double) {
							String formula = sensorData.getDeviceSensorMappingBean().getFormula();
							List<CalibrationDetailDTO> calibrationList = sensorData.getCalibratedDetailBean();
							
							if (calibrationList != null && !calibrationList.isEmpty()) {
								List<Long> calibratedXList = new ArrayList<>();
								List<Long> calibratedYList = new ArrayList<>();
								for (CalibrationDetailDTO calibratedData : calibrationList) {
									calibratedXList.add(calibratedData.getCalibrated_param());
									calibratedYList.add(calibratedData.getCalibrated_value());
								}
								double evaluatedCalibrationData = evaluateCalibration(calibratedXList, calibratedYList,
										attributeValue);
								resultantSensor.setResultant_value(evaluatedCalibrationData);
							}else 
								if (formula != null && !formula.isEmpty()) {
									// Call evaluateFormula and pass the formula and attributeValue
									double evaluatedFormulaData = evaluateFormula(formula, attributeValue);
									resultantSensor.setResultant_value(evaluatedFormulaData);
								}
							
							resultantSensor.setSensor_name(sensorData.getDeviceSensorMappingBean().getName());
							resultantSensor.setIcon_name(sensorData.getDeviceSensorMappingBean().getIcon_name());
							resultantSensor.setUnit_of_measurement(sensorData.getDeviceSensorMappingBean().getUnit_of_measurement());
							resultantSensor.setType(sensorData.getDeviceSensorMappingBean().getSensor_type_name());

						} else if (attributeValue instanceof Boolean) { 
	
							//comparing value
							if((boolean) attributeValue) {
								resultantSensor.setResultant_value(sensorData.getDeviceSensorMappingBean().getIf_sensor_1());
							}else {
								resultantSensor.setResultant_value(sensorData.getDeviceSensorMappingBean().getIf_sensor_0());
							}
							resultantSensor.setSensor_name(sensorData.getDeviceSensorMappingBean().getName());
							resultantSensor.setIcon_name(sensorData.getDeviceSensorMappingBean().getIcon_name());
							resultantSensor.setType(sensorData.getDeviceSensorMappingBean().getSensor_type_name());

						}
						resultantSensorList.add(resultantSensor);

					} else {
						// Handle the case when the parameter is not found in attributesMap
					}
				}

			} catch (Exception e) {
				//System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
		return resultantSensorList;
	}

	private List<SensorBean> getAllSensorDataByDevice_id(long device_id) {

		String sql = "select dsm.id,dsm.name,dsm.sensor_type_id,dsm.parameter,dsm.type,dsm.unit_of_measurement,dsm.if_sensor_0,dsm.if_sensor_1,dsm.formula,"
				+ " dsm.lowest_value,dsm.highest_value,dsm.ignore_ignition_off,dsm.user_id,dsm.admin_id,st.icon_name,st.sensor_type_name from device_sensor_mapping as dsm inner join sensor_type as st on dsm.sensor_type_id=st.id where dsm.device_id=?";		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<SensorBean> sensorList = new ArrayList<>();
		try {
			con = jdbcTemplate_pg.getDataSource().getConnection();
			ps = con.prepareStatement(sql);
			ps.setLong(1, device_id);
			rs = ps.executeQuery();
              //System.out.println("sensor"+ps);
			while (rs.next()) {
				SensorBean sensor = new SensorBean();
				DeviceSensorMappingDTO bean = new DeviceSensorMappingDTO();
				bean.setId(rs.getLong("id"));
				bean.setName(rs.getString("name"));
				bean.setSensor_type_id(rs.getLong("sensor_type_id"));
				bean.setParameter(rs.getString("parameter"));
				bean.setType(rs.getString("type"));
				bean.setUnit_of_measurement(rs.getString("unit_of_measurement"));
				bean.setIf_sensor_0(rs.getString("if_sensor_0"));
				bean.setIf_sensor_1(rs.getString("if_sensor_1"));
				bean.setFormula(rs.getString("formula"));
				bean.setLowest_value(rs.getDouble("lowest_value"));
				bean.setHighest_value(rs.getDouble("highest_value"));
				bean.setIgnore_ignition_off(rs.getBoolean("ignore_ignition_off"));
				bean.setUser_id(rs.getLong("user_id"));
				bean.setAdmin_id(rs.getLong("admin_id"));
				bean.setIcon_name(rs.getString("icon_name"));
				bean.setSensor_type_name(rs.getString("sensor_type_name"));
				sensor.setDeviceSensorMappingBean(bean);
				//sensor.setCalibratedDetailBean(getCalibrationData(device_id,bean.getId()));	
				sensorList.add(sensor);
			}

		} catch (SQLException e) {
			log.error(e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					log.error(e.getMessage());
				}
			}
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					log.error(e.getMessage());
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					log.error(e.getMessage());
				}
			}
		}

		return sensorList;

	}

	

	// calculation on the basis of formula

	public double evaluateFormula(String formula, Object attributeValue) {
		// Create an Expression using the formula
	    String detectedVariable = extractVariableFromFormula(formula);
	 // Replace the detected variable with 'X'
	    formula = formula.replace(detectedVariable, "X");
	    
		Expression expression = new ExpressionBuilder(formula).variable("X").build();
		double value;
		if (attributeValue instanceof Integer) {
			value = ((Integer) attributeValue).doubleValue();
		} else if (attributeValue instanceof Double) {
			value = (Double) attributeValue;
		} else {
			// If attributeValue is neither Integer nor Double, handle the error
			// appropriately
			throw new IllegalArgumentException(
					"Unsupported attributeValue type: " + attributeValue.getClass().getSimpleName());
		}

		// Set the variable 'x' in the formula and evaluate the result
		expression.setVariable("X", value);
		double result = expression.evaluate();
		return result;

	}
	
	// Assumes there's only one variable in the formula
	private String extractVariableFromFormula(String formula) {
	    // Regular expression to find the variable (a word character sequence)
	    // Adjust based on your use case, this detects words in the formula
	    return formula.replaceAll("[^a-zA-Z]", "")
	                  .replaceAll(".*(\\b[a-zA-Z]+\\b).*", "$1");  // Find the first variable-like word
	}

	// calculation on the basis of calibration
	private double evaluateCalibration(List<Long> calibratedXList, List<Long> calibratedYList, Object attributeValue) {

		  // Convert the attribute value to Long
		Long currentX=0L;

		if (attributeValue instanceof Integer) {
		    currentX = ((Integer) attributeValue).longValue();  // Convert Integer to Long
		} else if (attributeValue instanceof Long) {
		    currentX = (Long) attributeValue;  // Directly cast if it's already a Long
		} else {
		  // System.out.println("Unsupported attributeValue type: " + attributeValue.getClass());
		}
        //Long currentX = (long) 1582;
        
        // Check for edge cases
        if (calibratedXList.isEmpty() || calibratedYList.isEmpty() || calibratedXList.size() != calibratedYList.size()) {
            throw new IllegalArgumentException("Input lists must not be empty and must be of the same size.");
        }
        // Find the interval surrounding currentX
        int i = 0;
        while (i < calibratedXList.size() - 1 && currentX > calibratedXList.get(i + 1)) {
            i++;
        }

        // Ensure we are within the bounds of the array
        if (i == 0) {
            return calibratedYList.get(0); // If currentX is less than the smallest x
        } else if (i == calibratedXList.size() - 1) {
            return calibratedYList.get(i); // If currentX is greater than the largest x
        }

        // Perform linear interpolation
        Long x1 = calibratedXList.get(i);
        Long y1 = calibratedYList.get(i);
        Long x2 = calibratedXList.get(i + 1);
        Long y2 = calibratedYList.get(i + 1);

        // Linear interpolation formula
        double interpolatedY = y1 + ((double) (currentX - x1) * (y2 - y1)) / (x2 - x1);
        
        // Round the result to 2 decimal places
        double roundedY = Math.round(interpolatedY * 100.0) / 100.0;

        return roundedY;
	}

}
