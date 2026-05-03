package com.trackingpath.util;

public class Haversine {
    //public static final double R = 6372.8; // In kilometers
    public static final double R = 6372.8; // In kilometers
	public static double getDistanceInMetere(double lat1, double lon1, double lat2, double lon2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return R * c*1000;
	}
	public static double getDistanceInKm(double lat1, double lon1, double lat2, double lon2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return R * c;
	}
	
	
	public static void main(String[] args) {
		//System.out.println(Haversine.getDistanceInMetere(25.323843333333333,83.02653,25.3239176,83.0267575));
		//System.out.println(Haversine.getDistanceInMetere(25.323843333333333,83.02653,25.3240416,83.0268968));//65 		
		//System.out.println(Haversine.getDistanceInMetere(25.323843333333333,83.02653,25.32386,83.0266496));//66
		
		
		
	}
    
    
}
