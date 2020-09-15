/**
 * 
 */
package org.playground.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.playground.processor.CustomMultiFormatReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

/**
 * @author sanky
 *
 */
@Service
public class CodeDetectAndExtractService {
	public static final Logger LOGGER = LoggerFactory.getLogger(CodeDetectAndExtractService.class);
	
//TODO: make zip based processImage to test the benchMark
	public JSONObject processImageForCodes(byte[] content, String contentType) throws IOException {
		JSONArray processArraySuccessful = new JSONArray();
		JSONArray processArrayFailed = new JSONArray();
		String fileType = StringUtils.substringAfterLast(contentType, "/");
		if (fileType.equalsIgnoreCase("zip")) {
			File destDir = new File("temp_" + UUID.randomUUID().toString());
			destDir.mkdir();
			LOGGER.info("Extraction Started...");
			try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(content))) {
				ZipEntry zipEntry = zipIn.getNextEntry();
				byte[] buffer = new byte[1024];
				while (zipEntry != null) {
					File newFile = newFile(destDir, zipEntry);
					try (FileOutputStream fos = new FileOutputStream(newFile)) {
						int len;
						while ((len = zipIn.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
						fos.close();
						LOGGER.debug(newFile+" is extracted.");
					} catch (Exception e) {
						LOGGER.error(e.getMessage());
					}

					zipEntry = zipIn.getNextEntry();
				}
				zipIn.closeEntry();
			} catch (Exception e) {
				e.printStackTrace();
			}
			LOGGER.info("Extraction Finished.");
			int count = 0;
			Collection<File> listFiles = FileUtils.listFiles(destDir, null, true);
			for (File file : listFiles) {
				JSONObject processObject = new JSONObject();
				processObject.putOpt("name", file.getName());
				try {
					String fileContentType = Files.probeContentType(file.toPath()).toLowerCase();
					String ext = StringUtils.substringAfterLast(fileContentType, "/");
					processObject.putOpt("contentType", fileContentType);
					LOGGER.info("Decoding started....");
					if (StringUtils.equalsAnyIgnoreCase(ext, "png", "jpg", "jpeg")) {
						JSONArray decodeResultArray = processBarCode(FileUtils.readFileToByteArray(file));
						if(decodeResultArray != null) {
							processObject.putOpt("decodeResult", decodeResultArray);
							processArraySuccessful.put(processObject);
						}
						else
							processArrayFailed.put(processObject);
							
					}else {
						processObject.putOpt("decodeResult",ext+ " file type not supported for detect and decode");
						processArrayFailed.put(processObject);
					}
					count++;
					LOGGER.info("Decoding ended of "+count+"/"+listFiles.size());
				} catch (Exception e) {
					processObject.putOpt("failure", e.getMessage());
					e.printStackTrace();
				}
				
			}
			FileUtils.deleteDirectory(destDir);
		}
		if (StringUtils.equalsAnyIgnoreCase(fileType, "png", "jpg", "jpeg")) {
			JSONObject processObject = new JSONObject();
			processObject.putOpt("decodeResult", processBarCode(content));
			processArraySuccessful.put(processObject);
		}
		
		if (processArraySuccessful.length() == 0)
			throw new RuntimeException("Nothing found");
		
		JSONObject allProcessed = new JSONObject();
		allProcessed.putOpt("success", processArraySuccessful);
		allProcessed.putOpt("failed", processArrayFailed);
		return allProcessed;
	}

	private JSONArray processBarCode(byte[] content) {
		try {
			// TODO: Research on Custom BufferedImageLuminanceSource for more image
			// enhancements
			BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(
					new BufferedImageLuminanceSource(ImageIO.read(new ByteArrayInputStream(content)))));
			Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<>();
			decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
//			CustomMultiFormatReader custom = new CustomMultiFormatReader();
//			custom.setHints(decodeHints);
			MultiFormatReader mulReader = new MultiFormatReader();
			JSONArray decodedArray = new JSONArray();
			
//			List<Result> listOfDecodeResult = custom.decodeAllCodes(bitmap);
//			for (Result decodedResult : listOfDecodeResult) {
			Result decodedResult = mulReader.decode(bitmap, decodeHints);
				JSONObject decodedObject = new JSONObject();
				decodedObject.putOpt("type", decodedResult.getBarcodeFormat().name());
				decodedObject.putOpt("value", decodedResult.getText());
				decodedObject.putOpt("metaInfo", decodedResult.getResultMetadata());
				decodedArray.put(decodedObject);
//			}
			
			return decodedArray;
		} catch (IOException | NotFoundException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Crates new File with check of directory.
	 *
	 * @param destinationDir
	 * @param zipEntry
	 * @return {@link File}
	 * @throws IOException
	 */
	private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		File destFile = new File(destinationDir, zipEntry.getName());

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new RuntimeException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}
}
