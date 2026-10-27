/**
 * 
 */
package no.hvl.dat152.rest.ws.controller;

import no.hvl.dat152.rest.ws.exceptions.UnauthorizedOrderActionException;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import no.hvl.dat152.rest.ws.exceptions.OrderNotFoundException;
import no.hvl.dat152.rest.ws.exceptions.UserNotFoundException;
import no.hvl.dat152.rest.ws.model.Order;
import no.hvl.dat152.rest.ws.model.User;
import no.hvl.dat152.rest.ws.service.UserService;

/**
 * @author tdoy
 */
@RestController
@RequestMapping("/elibrary/api/v1")
public class UserController {

	@Autowired
	private UserService userService;
	
	@PreAuthorize("hasAuthority('ADMIN')")
	@GetMapping("/users")
	public ResponseEntity<Object> getUsers(){
		
		List<User> users = userService.findAllUsers();
		
		if(users.isEmpty())
			
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		else
			return new ResponseEntity<>(users, HttpStatus.OK);
	}
	
	@PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('USER') and #id == authentication.details.userid)")
	@GetMapping(value = "/users/{id}")
	public ResponseEntity<Object> getUser(@PathVariable Long id) throws UserNotFoundException, OrderNotFoundException{
		
		User user = userService.findUser(id);
		
		return new ResponseEntity<>(user, HttpStatus.OK);	
		
	}
	
	// TODO - createUser (@Mappings, URI=/users, and method)
	@PreAuthorize("hasAuthority('ADMIN')")
	@PostMapping("/users")
	public ResponseEntity<Object> createUser(@RequestBody User user){

		return new ResponseEntity<>(userService.saveUser(user), HttpStatus.CREATED);
	}

	// TODO - updateUser (@Mappings, URI, and method)
	@PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('USER') and #id == authentication.details.userid)")
	@PutMapping("/users/{id}")
	public ResponseEntity<Object> updateUser(@PathVariable Long id, @RequestBody User user) throws UserNotFoundException {

		return new ResponseEntity<>(userService.updateUser(user, id), HttpStatus.OK);
	}
	
	// TODO - deleteUser (@Mappings, URI, and method)
	@PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('USER') and #id == authentication.details.userid)")
	@DeleteMapping("/users/{id}")
	public ResponseEntity<Object> deleteUser(@PathVariable Long id) throws UserNotFoundException {

		userService.deleteUser(id);

		return new ResponseEntity<>(HttpStatus.OK);
	}

	// TODO - getUserOrders (@Mappings, URI=/users/{id}/orders, and method)
	@PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('USER') and #id == authentication.details.userid)")
	@GetMapping("/users/{id}/orders")
	public ResponseEntity<Object> getUserOrders(@PathVariable Long id) throws UserNotFoundException {

		return new ResponseEntity<>(userService.getUserOrders(id), HttpStatus.OK);
	}
	
	// TODO - getUserOrder (@Mappings, URI=/users/{uid}/orders/{oid}, and method)
	@PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('USER') and #uid == authentication.details.userid)")
	@GetMapping("/users/{uid}/orders/{oid}")
	public ResponseEntity<Object> getUserOrder(@PathVariable Long uid, @PathVariable Long oid) throws UserNotFoundException, OrderNotFoundException {

		return new ResponseEntity<>(userService.getUserOrder(uid, oid), HttpStatus.OK);
	}

	// TODO - deleteUserOrder (@Mappings, URI, and method)
	@PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('USER') and #uid == authentication.details.userid)")
	@DeleteMapping("/users/{uid}/orders/{oid}")
	public ResponseEntity<Object> deleteUserOrder(@PathVariable Long uid, @PathVariable Long oid) throws UserNotFoundException, OrderNotFoundException {

		userService.deleteOrderForUser(uid, oid);

		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	// TODO - createUserOrder (@Mappings, URI, and method) + HATEOAS links
	@PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('USER') and #id == authentication.details.userid)")
	@PostMapping("/users/{id}/orders")
	public ResponseEntity<Object> createUserOrder(@PathVariable Long id, @RequestBody Order order) throws UserNotFoundException, OrderNotFoundException, UnauthorizedOrderActionException {

		User user = userService.createOrdersForUser(id, order);

		// links for every order of the user, the new order included
		for(Order o : user.getOrders()) {
			o.add(linkTo(methodOn(OrderController.class).getBorrowOrder(o.getId())).withSelfRel());
			o.add(linkTo(methodOn(UserController.class).getUserOrder(id, o.getId())).withRel("user-order"));
			o.add(linkTo(methodOn(UserController.class).getUserOrders(id)).withRel("user-orders"));
			o.add(linkTo(methodOn(UserController.class).deleteUserOrder(id, o.getId())).withRel("cancel-order"));
		}

		return new ResponseEntity<>(user.getOrders(), HttpStatus.CREATED);
	}

	
}
