/**
 * 
 */
package no.hvl.dat152.rest.ws.controller;

import no.hvl.dat152.rest.ws.exceptions.UnauthorizedOrderActionException;
import org.springframework.security.access.prepost.PreAuthorize;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import no.hvl.dat152.rest.ws.exceptions.OrderNotFoundException;
import no.hvl.dat152.rest.ws.exceptions.UserNotFoundException;
import no.hvl.dat152.rest.ws.model.Order;
import no.hvl.dat152.rest.ws.service.OrderService;

/**
 * @author tdoy
 */
@RestController
@RequestMapping("/elibrary/api/v1")
public class OrderController {

	@Autowired
	private OrderService orderService;

	// TODO - getAllBorrowOrders (@Mappings, URI=/orders, and method) + filter by expiry and paginate
	@PreAuthorize("hasAuthority('ADMIN')")
	@GetMapping("/orders")
	public ResponseEntity<Object> getAllBorrowOrders(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiry,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size){

		Pageable pageable = PageRequest.of(page, size);

		List<Order> orders = expiry == null ?
				orderService.findAllOrders(pageable) : orderService.findByExpiryDate(expiry, pageable);

		if(orders.isEmpty())
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		else
			return new ResponseEntity<>(orders, HttpStatus.OK);
	}

	// TODO - getBorrowOrder (@Mappings, URI=/orders/{id}, and method)
	@PreAuthorize("hasAnyAuthority('ADMIN','USER')")
	@GetMapping("/orders/{id}")
	public ResponseEntity<Object> getBorrowOrder(@PathVariable Long id) throws OrderNotFoundException, UnauthorizedOrderActionException {

		Order order = orderService.findOrder(id);
		order.add(linkTo(methodOn(OrderController.class).getBorrowOrder(id)).withSelfRel());

		return new ResponseEntity<>(order, HttpStatus.OK);
	}

	// TODO - updateOrder (@Mappings, URI=/orders/{id}, and method)
	@PreAuthorize("hasAnyAuthority('ADMIN','USER')")
	@PutMapping("/orders/{id}")
	public ResponseEntity<Object> updateOrder(@PathVariable Long id, @RequestBody Order order) throws OrderNotFoundException, UnauthorizedOrderActionException {

		Order updated = orderService.updateOrder(order, id);

		return new ResponseEntity<>(updated, HttpStatus.OK);
	}

	// TODO - deleteBookOrder (@Mappings, URI=/orders/{id}, and method)
	@PreAuthorize("hasAnyAuthority('ADMIN','USER')")
	@DeleteMapping("/orders/{id}")
	public ResponseEntity<Object> deleteBookOrder(@PathVariable Long id) throws OrderNotFoundException, UnauthorizedOrderActionException {

		orderService.deleteOrder(id);

		return new ResponseEntity<>(HttpStatus.OK);
	}

}
