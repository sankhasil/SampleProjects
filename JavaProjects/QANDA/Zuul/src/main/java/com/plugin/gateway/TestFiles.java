package com.plugin.gateway;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TestFiles {
	private static final String AUDIT_TRAIL_FILE_PREFIX = "trailList";
	private static final String AUDIT_TRAIL_FILE_EXTENTION = ".dat";
	private static final String AUDIT_TRAIL_FOLDER = "AuditTrail";
	public static int fibonacci(int n)  {
	    if (n < 2) return n;
	    return fibonacci(n - 1) + fibonacci(n - 2);
	}
	
	public static String maskify(String creditCardNumber) {
	      int totalLength = creditCardNumber.length();
//	      if(creditCardNumber.matches("^[0-9]+(-[a-zA-Z0-9]+)+$") || creditCardNumber.matches("[0-9]+")){
	        if( totalLength > 5){
	        String firstChar = creditCardNumber.charAt(0)+"";
	        String lastFourChars = creditCardNumber.substring(totalLength - 4,totalLength);
	        char[] toMaskArray = creditCardNumber.substring(1,totalLength-4).toCharArray();
	        StringBuilder maskBuilder = new StringBuilder();
	        for(char c : toMaskArray){
	          String eachChar = c+"";
	          maskBuilder.append(eachChar.replaceAll("[0-9]+","#"));
	        }  
	        System.out.println(firstChar+"  "+maskBuilder.toString()+"  "+lastFourChars);
	        return firstChar+maskBuilder.toString()+lastFourChars;
	        }else{
	          return creditCardNumber;
	        }
//	      }else if(creditCardNumber != null){
//	        return creditCardNumber;
//	      }
	    }
	public static void main(String[] args) {
	Long now = Instant.now().getEpochSecond();
	System.out.println(maskify("A234-2345-3456-ABCD"));
	System.out.println(LocalDateTime.ofEpochSecond(now, 0, OffsetDateTime.now(ZoneId.systemDefault()).getOffset()).format(DateTimeFormatter.BASIC_ISO_DATE));
	System.out.println("\n"+fibonacci(5));
//		System.out.println(AUDIT_TRAIL_FOLDER+File.separator+AUDIT_TRAIL_FILE_PREFIX+LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)+AUDIT_TRAIL_FILE_EXTENTION);
		
//		if (Files.exists(Paths.get("AuditTrail"))) {
//			try {
//				List<AuditTrail> listOfEntries = new CopyOnWriteArrayList<>();
//				String pwd = System.getProperty("user.dir");
//				  final int maxDepth = 10;
//			        Stream<Path> matches = Files.find(Paths.get(pwd,"AuditTrail"),maxDepth,(path, basicFileAttributes) -> {
//			        	
//			       return String.valueOf(path).endsWith(".dat");
//			        
//			        });
////			        matches.map(path -> path.getFileName()).forEach(System.out::println);
//			        matches.distinct().parallel().forEachOrdered(loadFilePath ->{
//			        	try {
//							FileInputStream fileInputStream = new FileInputStream(loadFilePath.toFile());
//							ObjectInputStream objectStream = new ObjectInputStream(fileInputStream);
//							listOfEntries.addAll((List<AuditTrail>) objectStream.readObject());
//							objectStream.close();
//							fileInputStream.close();
//						} catch (Exception e) {
//						}
//			        });
//listOfEntries.forEach(System.out::println);
////				Path loadFilePath = Paths.get(pwd,"AuditTrail/trails.dat");
////				FileInputStream fileInputStream = new FileInputStream(loadFilePath.toFile());
////				ObjectInputStream objectStream = new ObjectInputStream(fileInputStream);
////				List<AuditTrail> listOfEntries = (List<AuditTrail>) objectStream.readObject();
////				objectStream.close();
////				fileInputStream.close();
//
//	}catch (Exception e) {
//		e.printStackTrace();
//	}
//	}
	}
}
