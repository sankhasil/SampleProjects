/**
 * 
 */
package com.sdgt.gateway.repository;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.MongoClient;
import com.sdgt.gateway.enums.AuditStorageEngine;
import com.sdgt.gateway.trail.model.AuditTrail;

/**
 * @author Sankha
 *
 */
public class AuditTrailRepository {
	private static final String AUDIT_TRAIL_FILE_PREFIX = "trailList";
	private static final String AUDIT_TRAIL_FILE_EXTENTION = ".dat";
	private static final String AUDIT_TRAIL_FOLDER = "AuditTrail";
	protected List<AuditTrail> listOfEntries;
	private Logger repositoryLogger = LoggerFactory.getLogger(AuditTrailRepository.class);
	private MongoTemplate mongoTemplate;

	private AuditStorageEngine storageEngine;

	public AuditTrailRepository(AuditStorageEngine storageEngine) {
		switch (storageEngine) {
		case FILE:
		case FILESYSTEM:
			if (Files.notExists(Paths.get(AUDIT_TRAIL_FOLDER), LinkOption.NOFOLLOW_LINKS)) {
				try {
					Files.createDirectory(Paths.get(AUDIT_TRAIL_FOLDER));
				} catch (IOException e) {
					repositoryLogger.error(ExceptionUtils.getFullStackTrace(e));
				}
			}
			if (listOfEntries == null || listOfEntries.isEmpty())
				loadAllTrailDataByDate(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
			this.storageEngine = AuditStorageEngine.FILESYSTEM;
			break;
		case MONGO:
		case MONGODB:
			MongoClient mongoClient = new MongoClient();
			mongoTemplate = new MongoTemplate(mongoClient, AUDIT_TRAIL_FOLDER);
			this.storageEngine = AuditStorageEngine.MONGODB;
			break;
		default:
			break;
		}

	}

	private void loadAllTrailDataByDate(String format) {
		listOfEntries = new CopyOnWriteArrayList<AuditTrail>();
		if (Files.exists(Paths.get(AUDIT_TRAIL_FOLDER))) {
			String pwd = System.getProperty("user.dir");
			Path loadFilePath = Paths.get(pwd, AUDIT_TRAIL_FOLDER + File.separator + AUDIT_TRAIL_FILE_PREFIX + "_"
					+ format + AUDIT_TRAIL_FILE_EXTENTION);
			if (Files.exists(loadFilePath)) {
				try {
					FileInputStream fileInputStream = new FileInputStream(loadFilePath.toFile());
					BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
					ObjectInputStream objectStream = new ObjectInputStream(bufferedInputStream);
					Object fileData = null;
					if (bufferedInputStream.available() > 0) {
						fileData = objectStream.readObject();
					}
					if (fileData != null)
						listOfEntries.addAll((List<AuditTrail>) fileData);
					objectStream.close();
					fileInputStream.close();
				} catch (Exception e) {
					repositoryLogger.error(ExceptionUtils.getFullStackTrace(e));
				}
			}
		}
	}

	public List<AuditTrail> findAll() {
		switch (storageEngine) {
		case FILESYSTEM:
			loadAllTrailData();
			break;
		case MONGODB:
			listOfEntries = mongoTemplate.findAll(AuditTrail.class);
			break;
		default:
			break;
		}
		return listOfEntries;
	}

	public AuditTrail findById(String id) {
		switch (storageEngine) {
		case FILESYSTEM:
			if (listOfEntries.isEmpty()) {
				loadAllTrailDataByDate(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
			}
			if (listOfEntries.stream().anyMatch(resource -> resource.getId().equals(UUID.fromString(id)))) {
				return listOfEntries.stream().filter(resource -> resource.getId().equals(UUID.fromString(id)))
						.findFirst().orElse(null);
			} else {
				loadAllTrailData();
				return listOfEntries.stream().filter(resource -> resource.getId().equals(UUID.fromString(id)))
						.findFirst().orElse(null);
			}
		case MONGODB:
			return mongoTemplate.findById(id, AuditTrail.class);
		default:
			break;
		}
		return null;
	}

	public AuditTrail save(AuditTrail object) {
		switch (storageEngine) {
		case FILESYSTEM:
			saveToDataFile(object);
			break;
		case MONGODB:
			mongoTemplate.save(object);
			break;
		default:
			break;
		}

		return object;
	}

	public List<AuditTrail> search(JsonObject object) {
		switch (storageEngine) {
		case FILESYSTEM:
			return searchResultInFileSystem(object);
		case MONGODB:
			return searchResultInMongoDB(object);
		default:
			break;
		}
		return Collections.emptyList();

	}

	/**
	 * 
	 * @param object
	 * @return
	 */
	private List<AuditTrail> searchResultInMongoDB(JsonObject object) {
		Query searchQuery = new Query();
		if (checkSearchKey(object, "requestTimestamp"))
			searchQuery.addCriteria(Criteria.where("requestTimestamp").is(object.get("requestTimestamp").getAsLong()));

		if (checkSearchKey(object, "serviceName")) {
			String searchData = object.get("serviceName").getAsString();
			if (checkIfHasSpecialCharacter(searchData)) {
				searchData = escapeMetaCharacters(searchData);
			}
			searchQuery.addCriteria(
					Criteria.where("serviceName").regex(Pattern.compile(searchData, Pattern.CASE_INSENSITIVE)));
		}
		if (checkSearchKey(object, "action")) {
			String searchData = object.get("action").getAsString();
			if (checkIfHasSpecialCharacter(searchData)) {
				searchData = escapeMetaCharacters(searchData);
			}
			searchQuery
					.addCriteria(Criteria.where("action").regex(Pattern.compile(searchData, Pattern.CASE_INSENSITIVE)));
		}
		if (checkSearchKey(object, "status")) {
			String searchData = object.get("status").getAsString();
			if (checkIfHasSpecialCharacter(searchData)) {
				searchData = escapeMetaCharacters(searchData);
			}
			searchQuery
					.addCriteria(Criteria.where("status").regex(Pattern.compile(searchData, Pattern.CASE_INSENSITIVE)));
		}
		return mongoTemplate.find(searchQuery, AuditTrail.class);
	}

	/**
	 * @param object
	 * @param entries
	 * @return
	 */
	private List<AuditTrail> searchResultInFileSystem(JsonObject object) {
		Set<Map.Entry<String, JsonElement>> entries = object.entrySet();
		JsonElement targetDate = object.get("targetDate");
		if (object.has("targetDate") && targetDate != null && StringUtils.isNotBlank(targetDate.getAsString())) {

			if (targetDate.getAsString().equalsIgnoreCase("today") || targetDate.getAsString()
					.equalsIgnoreCase(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)))
				loadAllTrailDataByDate(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));

			else if (targetDate.getAsString().equalsIgnoreCase("yesterday") || targetDate.getAsString()
					.equalsIgnoreCase(LocalDate.now().minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE)))
				loadAllTrailDataByDate(LocalDate.now().minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE));

			else if (targetDate.getAsString().equalsIgnoreCase("daybeforeyesterday") || targetDate.getAsString()
					.equalsIgnoreCase(LocalDate.now().minusDays(2).format(DateTimeFormatter.BASIC_ISO_DATE)))
				loadAllTrailDataByDate(LocalDate.now().minusDays(2).format(DateTimeFormatter.BASIC_ISO_DATE));
			else
				loadAllTrailDataByDate(targetDate.getAsString());
		} else {
			loadAllTrailData();
		}
		List<AuditTrail> results = listOfEntries.stream().filter(resource -> {
			if (resource != null) {
				boolean predicate = true;
				for (Map.Entry<String, JsonElement> entry : entries) {
					if (entry.getValue() != null) {
						switch (entry.getKey().toLowerCase()) {
						case "id":
							predicate = predicate && resource.getId() != null
									&& resource.getId().equals(UUID.fromString(entry.getValue().getAsString()));
							break;
						case "serviceName":
							predicate = predicate && resource.getServiceName() != null
									&& resource.getServiceName().equalsIgnoreCase(entry.getValue().getAsString());
							break;
						case "action":
							predicate = predicate && resource.getAction() != null
									&& resource.getAction().equalsIgnoreCase(entry.getValue().getAsString());
							break;
						case "status":
							predicate = predicate && resource.getStatus() != null
									&& resource.getStatus().equalsIgnoreCase(entry.getValue().getAsString());
							break;
						case "requestMethod":
							predicate = predicate && resource.getRequestMethod() != null
									&& resource.getRequestMethod().equalsIgnoreCase(entry.getValue().getAsString());
						case "performedBy":
							predicate = predicate && resource.getPerformedBy() != null
									&& resource.getPerformedBy().equalsIgnoreCase(entry.getValue().getAsString());
							break;
						default:
							break;
						}
					}
				}
				if (object.has("fromDate") && object.get("fromDate") != null && object.has("toDate")
						&& object.get("toDate") != null) {
					predicate = predicate
							&& resource.getRequestTimestamp().longValue() >= object.get("fromDate").getAsLong()
							&& resource.getRequestTimestamp().longValue() <= object.get("toDate").getAsLong();
				}
				return predicate;
			}
			return false;
		}).collect(Collectors.toList());
		return results;
	}

	public boolean update(AuditTrail object, AuditTrail patchObject) {
		if (listOfEntries.isEmpty()) {
			loadAllTrailDataByDate(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
		}

		if (!listOfEntries.stream().anyMatch(resource -> resource.getId().equals(object.getId()))) {
			loadAllTrailData();
		}
		AuditTrail auditTrailToBeUpdated = listOfEntries.stream()
				.filter(resource -> resource.getId().equals(object.getId())).findFirst().orElse(null);
		if (auditTrailToBeUpdated != null) {
			auditTrailToBeUpdated.setStatus(patchObject.getStatus());
			auditTrailToBeUpdated.setUpdatedBy(patchObject.getUpdatedBy());
			auditTrailToBeUpdated.setUpdateTimestamp(Instant.now().getEpochSecond());

			try {
				String pwd = System.getProperty("user.dir");
				String dateFormatForFile = LocalDateTime
						.ofEpochSecond(auditTrailToBeUpdated.getRequestTimestamp(), 0,
								OffsetDateTime.now(ZoneId.systemDefault()).getOffset())
						.format(DateTimeFormatter.BASIC_ISO_DATE);
				Path updateFilePath = Paths.get(pwd, AUDIT_TRAIL_FOLDER + File.separator + AUDIT_TRAIL_FILE_PREFIX + "_"
						+ dateFormatForFile + AUDIT_TRAIL_FILE_EXTENTION);
				FileOutputStream fileOutputStream = new FileOutputStream(updateFilePath.toFile());
				synchronized (fileOutputStream) {
					ObjectOutputStream objectStream = new ObjectOutputStream(fileOutputStream);
					objectStream.writeObject(listOfEntries);
					objectStream.close();
					fileOutputStream.close();
				}
				return true;
			} catch (Exception e) {
				repositoryLogger.error(ExceptionUtils.getFullStackTrace(e));
			}
		}
		return false;
	}

	public boolean updateById(String id, JsonObject object) {
		AuditTrail patchObject = new AuditTrail();
		patchObject.setStatus(object.get("status").getAsString());
		patchObject.setUpdatedBy(object.get("updatedBy").getAsString());
		patchObject.setId(UUID.fromString(id));
		return update(findById(id), patchObject);
	}
	// Change method
//	public void delete(AuditTrail obj) {
//		if (obj != null) {
//			if (Files.exists(Paths.get(AUDIT_TRAIL_FOLDER))) {
//				try {
//					String pwd = System.getProperty("user.dir");
//					Path saveFilePath = Paths.get(pwd, AUDIT_TRAIL_FILE_LOCATION);
//					FileOutputStream fileOutputStream = new FileOutputStream(saveFilePath.toFile());
//					ObjectOutputStream objectStream = new ObjectOutputStream(fileOutputStream);
//					listOfEntries.remove(obj);
//					objectStream.writeObject(listOfEntries);
//					objectStream.close();
//					fileOutputStream.close();
//				} catch (Exception e) {
//					throw new RuntimeException(e.getMessage(), e);
//				}
//			}
//		} else {
//			throw new RuntimeException("Object can't be null");
//		}
//	}
//
//	public void deleteById(String id) {
//		delete(findById(id));
//	}

	private void loadAllTrailData() {
		listOfEntries = new CopyOnWriteArrayList<AuditTrail>();
		if (Files.exists(Paths.get(AUDIT_TRAIL_FOLDER))) {
			try {
				String pwd = System.getProperty("user.dir");
				final int maxDepth = 10;
				Stream<Path> matches = Files.find(Paths.get(pwd, AUDIT_TRAIL_FOLDER), maxDepth,
						(path, basicFileAttributes) -> String.valueOf(path).endsWith(AUDIT_TRAIL_FILE_EXTENTION));
				matches.distinct().parallel().forEachOrdered(loadFilePath -> {
					try {
						FileInputStream fileInputStream = new FileInputStream(loadFilePath.toFile());
						ObjectInputStream objectStream = new ObjectInputStream(fileInputStream);
						listOfEntries.addAll((List<AuditTrail>) objectStream.readObject());
						objectStream.close();
						fileInputStream.close();
					} catch (Exception e) {
						repositoryLogger.error(ExceptionUtils.getStackTrace(e));
					}
				});
				matches.close();
			} catch (Exception e) {
				repositoryLogger.error(ExceptionUtils.getStackTrace(e));
			} finally {
			}
		}
	}

	private boolean checkSearchKey(JsonObject jObject, String key) {
		if (jObject.has(key) && !jObject.get(key).isJsonNull() && jObject.get(key) != null) {
			String content = StringUtils.removeStart(jObject.get(key).toString(), "\"");
			content = StringUtils.removeEnd(content, "\"");
			return StringUtils.isNotBlank(content);

		}
		return false;
	}

	private String escapeMetaCharacters(String inputString) {
		final String[] metaCharacters = { "\\", "^", "$", "{", "}", "[", "]", "(", ")", ".", "*", "+", "?", "|", "<",
				">", "-", "&", "%" };

		for (int i = 0; i < metaCharacters.length; i++) {
			if (inputString.contains(metaCharacters[i])) {
				inputString = inputString.replace(metaCharacters[i], "\\" + metaCharacters[i]);
			}
		}
		return inputString;
	}

	private boolean checkIfHasSpecialCharacter(String searchData) {
		Pattern special = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]");
		return special.matcher(searchData).find();
	}

	private void saveToDataFile(AuditTrail object) {
		if (Files.exists(Paths.get(AUDIT_TRAIL_FOLDER))) {
			try {
				// based on list size write in multiple files.
				if (listOfEntries == null || listOfEntries.isEmpty())
					listOfEntries = new CopyOnWriteArrayList<AuditTrail>();
				if (listOfEntries.contains(object))
					listOfEntries.remove(object);
				listOfEntries.add(object);
				String pwd = System.getProperty("user.dir");
				Path saveFilePath = Paths.get(pwd, AUDIT_TRAIL_FOLDER + File.separator + AUDIT_TRAIL_FILE_PREFIX + "_"
						+ LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + AUDIT_TRAIL_FILE_EXTENTION);
				FileOutputStream fileOutputStream = new FileOutputStream(saveFilePath.toFile());
				synchronized (fileOutputStream) {
					ObjectOutputStream objectStream = new ObjectOutputStream(fileOutputStream);
					objectStream.writeObject(listOfEntries);
					objectStream.close();
					fileOutputStream.close();
				}
			} catch (Exception e) {
				throw new RuntimeException("Data File save problem " + e.getMessage(), e);
			}
		}
	}
}
