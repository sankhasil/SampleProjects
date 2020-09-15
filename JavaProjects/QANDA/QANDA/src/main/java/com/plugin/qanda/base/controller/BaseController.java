package com.plugin.qanda.base.controller;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.JsonObject;
import com.plugin.qanda.base.rest.model.Response;
import com.plugin.qanda.base.service.BaseService;
import com.plugin.qanda.base.service.IService;

/**
 * 
 * @author Sankha
 * @param <T> parameterized Model Object
 */
public class BaseController<T> {

	protected Logger logger = LoggerFactory.getLogger(BaseController.class);
	protected IService<T> service;
	protected Class busClass;

	private BaseController() {
		this.service = new BaseService<>();
		setParameterisedBusinessClass();
	}

	@Autowired
	public BaseController(IService service) {
		this();
		this.service = service;
		logger = LoggerFactory.getLogger(service.getClass());
		if (service != null) {
			service.setBusinessClass(busClass);
		}
	}
	@CrossOrigin
	@GetMapping(value = { "ping", "/ping" }, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public ResponseEntity<Response> pingTest() {
		Response pingResponse = new Response(HttpStatus.OK.value(),"!!It Works!!");
		return ResponseEntity.ok(pingResponse);
	}
	@CrossOrigin
	@GetMapping(value = { "", "/" }, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public Iterable<?> findAll() {
		logger.info(">>>>>> Find All " + busClass.getSimpleName());
		return service.findAll();
	}
	@CrossOrigin
	@GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }, value = "/{id}")
	public T findById(@PathVariable("id") String id) {
		logger.info(">>>>>> Find " + busClass.getSimpleName() + ":" + id);
		T byId = service.findById(id);
			return byId;
	}

	@PostMapping(value = { "/", "" }, produces = { MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE }, consumes = { MediaType.APPLICATION_JSON_VALUE,
					MediaType.APPLICATION_XML_VALUE })
	public T create(@RequestBody T object) {
		
//		logger.info(">>>>> Creating  " + busClass.getSimpleName());
		return service.create(object);
	}

	@PutMapping(value = "/{id}")
	public T update(@PathVariable("id") String id, @RequestBody T object) {
		// todo - update should find perticular object then update the object
		logger.info(">>>>> Updating " + busClass.getSimpleName());
		return service.update(object, id);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable("id") Serializable id) {
		logger.info(">>>>> Deleting " + busClass.getSimpleName() + ":" + id);
		service.deleteById(id);
	}

	@PostMapping(value = "/search", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
	public List<T> search(@RequestBody JsonObject object) {
		return service.search(object);
	}
	@CrossOrigin
	@GetMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }, value = "/search")
	public List<T> searchByParam(@RequestParam("field") String field,@RequestParam("value") String value) {
		return service.searchByField(field,value);
	}

//	
//	@PostMapping(value= {"/uploadFile","/UploadFile","/uploadfile"})
//	public ResponseEntity<String> uploadFile(@RequestParam("file")MultipartFile file){
//		service.storeFile(file);
//		return ResponseEntity.ok().build();
//	}
//	
	
	private void setParameterisedBusinessClass() {
		Type genericSuperclass = getClass().getGenericSuperclass();
		if (genericSuperclass instanceof ParameterizedType) {
			Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
			if (actualTypeArguments != null && actualTypeArguments.length > 0) {
				busClass = ((Class) (actualTypeArguments[0]));
			}
		}
	}

}
