/**
 * 
 */
package no.hvl.dat152.rest.ws.service;

import java.util.List;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import no.hvl.dat152.rest.ws.exceptions.OrderNotFoundException;
import no.hvl.dat152.rest.ws.exceptions.UnauthorizedOrderActionException;
import no.hvl.dat152.rest.ws.model.Order;
import no.hvl.dat152.rest.ws.repository.OrderRepository;
import no.hvl.dat152.rest.ws.security.UserDetailsImpl;

/**
 * @author tdoy
 */
@Service
public class OrderService {

	@Autowired
	private OrderRepository orderRepository;
	
	public Order saveOrder(Order order) {
		
		order = orderRepository.save(order);
		
		return order;
	}
	
	public Order findOrder(Long id) throws OrderNotFoundException, UnauthorizedOrderActionException {
		
		Order order = orderRepository.findById(id)
				.orElseThrow(()-> new OrderNotFoundException("Order with id: "+id+" not found in the order list!"));
		
		checkOrderAccess(id);
		
		return order;
	}
	
	// admin can access any order, a user only their own
	private void checkOrderAccess(Long id) throws UnauthorizedOrderActionException {
		
		JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		
		boolean isAdmin = auth.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ADMIN"));
		
		if(isAdmin)
			return;
		
		UserDetailsImpl user = (UserDetailsImpl) auth.getDetails();
		String ownerEmail = orderRepository.findEmailByOrderId(id);
		
		if(ownerEmail == null || !ownerEmail.equals(user.getEmail()))
			throw new UnauthorizedOrderActionException("Order with id: "+id+" does not belong to the user");
	}
	
	// TODO public void deleteOrder(Long id)
	public void deleteOrder(Long id) throws OrderNotFoundException, UnauthorizedOrderActionException {
		
		Order order = findOrder(id);
		
		orderRepository.delete(order);
	}
	
	// TODO public List<Order> findAllOrders()
	public List<Order> findAllOrders(Pageable page) {
		
		return orderRepository.findAll(page).getContent();
	}
	
	// TODO public List<Order> findByExpiryDate(LocalDate expiry, Pageable page)
	public List<Order> findByExpiryDate(LocalDate expiry, Pageable page) {
		
		// "expires by" is inclusive, findByExpiryBefore is exclusive
		return orderRepository.findByExpiryBefore(expiry.plusDays(1), page).getContent();
	}
	
	// TODO public Order updateOrder(Order order, Long id)
	public Order updateOrder(Order order, Long id) throws OrderNotFoundException, UnauthorizedOrderActionException {
		
		Order existing = findOrder(id);
		existing.setIsbn(order.getIsbn());
		existing.setExpiry(order.getExpiry());
		
		return orderRepository.save(existing);
	}

}
